/*
    Copyright (c) 2012 Vladimir Shakhov <bogdad@gmail.com>
    Copyright (c) 2012 Other contributors as noted in the AUTHORS file.

    This file is part of Cocaine.

    Cocaine is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Cocaine is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>. 
*/
package cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public class Response {
    private final Ptr cResponsePtr;
    private final Lock lock = new ReentrantLock();

    Response(long cResponsePtr) {
        this.cResponsePtr = new Ptr(cResponsePtr);
    }

    public String getString(long timeout, TimeUnit timeUnit) throws TimeoutException {
        lock.lock();
        long milliseconds = timeUnit.toMillis(timeout);
        // cocaineTimeout==1 equals to 1000 seconds
        double cocaineTimeout = milliseconds / 1000000.0;
        try {
            if (!cResponsePtr.isReferring()) {
                throw new IllegalStateException("Response is closed");
            }
            return getString(cResponsePtr.get(), cocaineTimeout * 2);
        } finally {
            lock.unlock();
        }
    }

    public boolean get(ArrayHolder data, long timeout, TimeUnit timeUnit) throws TimeoutException {
        lock.lock();
        long milliseconds = timeUnit.toMillis(timeout);
        // cocaineTimeout==1 equals to 1000 seconds
        double cocaineTimeout = milliseconds / 1000000.0;
        try {
            if (!cResponsePtr.isReferring()) {
                throw new IllegalStateException("Response is closed");
            }
            data.array = null;
            return get(data, cResponsePtr.get(), cocaineTimeout * 2);
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            if (cResponsePtr.isReferring()) {
                close(cResponsePtr.get());
                cResponsePtr.close();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private native String getString(long cResponsePtr, double timeout)
            throws TimeoutException;
    private native boolean get(ArrayHolder data, long cResponsePtr, double timeout)
            throws TimeoutException;

    private native void close(long cResponsePtr);

    static {
        System.loadLibrary("cocaine-framework-java");
    }
}
