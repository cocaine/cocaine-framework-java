package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Response {
    private final Ptr cResponsePtr;
    private long timeout;
    private TimeUnit timeUnit;
    private final Lock lock = new ReentrantLock();

    Response(long cResponsePtr) {
        this.cResponsePtr = new Ptr(cResponsePtr);
    }

    public String get(long timeout, TimeUnit timeUnit) throws TimeoutException {
        lock.lock();
        long milliseconds = timeUnit.toMillis(timeout);
        // see response_impl.cpp: response_impl_t::get for cocaineTimeout
        // cocaineTimeout==1 equals to 1000 seconds
        double cocaineTimeout = milliseconds / 1000000.0;
        try {
            if (!cResponsePtr.isReferring()) {
                throw new IllegalStateException("Response is closed");
            }
            return get(cResponsePtr.get(), cocaineTimeout * 2);
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

    private native String get(long cResponsePtr, double timeout)
            throws TimeoutException;

    private native void close(long cResponsePtr);

    {
        System.loadLibrary("cocaine-framework-java");
    }
}
