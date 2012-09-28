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

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** 
 * cocaine-dealer dealer_t
 * see src/test/java/cocaine/dealer/UsageExample.java for usage example
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public class Dealer {
    private final Ptr cDealerPtr;
    private final Lock lock = new ReentrantLock();

    /** Constructs the dealer with the specified filesystem path
     *  to a dealer config json. For an example of dealer config see
     *  {@link https://github.com/cocaine/cocaine-dealer/blob/master/README.md}
     */
    public Dealer(String configPath) {
        cDealerPtr = new Ptr(nativeInit(configPath));
    }

    /**
     * send a message(byte[]) to the specified cocaine app/path [='app/handle']
     * with a specified MessagePolicy
     * and returns a Response object for retrieving results
     */
    public Response sendMessage(String path, byte[] message,
            MessagePolicy messagePolicy) {
        String[] parts = path.split("/");
        if (parts.length!=2) {
            throw new IllegalArgumentException("path should be in a form 'app/handle'");
        }
        String service = parts[0];
        String handle = parts[1];
        double cocaineTimeout = messagePolicy.cocaineTimeout();
        double cocaineDeadline = messagePolicy.cocaineDeadline();
        lock.lock();
        try {
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("Dealer is closed");
            }
            long responsePtr;
            responsePtr = nativeSendMessage(cDealerPtr.get(), service, handle,
                    message, messagePolicy.urgent, messagePolicy.persistent, cocaineTimeout, cocaineDeadline,
                    messagePolicy.maxRetries);
            return new Response(responsePtr);
        } finally {
            lock.unlock();
        }
    }

    public void removeStoredMessageFor(Response response) {
        lock.lock();
        try{
            response.removedStoredMessageFor(cDealerPtr.get());
        } finally {
            lock.unlock();
        }
    }

    public int getStoredMessagesCount(String serviceAlias) {
        lock.lock();
        try {
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("Dealer is closed");
            }
            return nativeGetStoredMessagesCount(cDealerPtr.get(), serviceAlias);
        } finally {
            lock.unlock();
        }
    }

    public List<Message> getStoredMessages(String serviceAlias) {
        lock.lock();
        try{
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("Dealer is closed");
            }
            return nativeGetStoredMessages(cDealerPtr.get(), serviceAlias);
        } finally {
            lock.unlock();
        }
    }

    /**
     * user should call close()
     */
    public void close() {
        lock.lock();
        try {
            if (cDealerPtr.isReferring()) {
                nativeDelete(cDealerPtr.get());
                cDealerPtr.close();
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

    // returns pointer to a client
    private native long nativeInit(String configPath);

    // deletes client
    private native void nativeDelete(long cDealerPtr);

    // retrieves the number of unsent messages
    private native int nativeGetStoredMessagesCount(long cDealerPtr, String serviceAlias);

    // retrieves the unsent messages
    private native List<Message> nativeGetStoredMessages(long cDealerPtr, String service);

    // returns pointer to response
    private native long nativeSendMessage(long cClientPtr, String service,
            String handle, byte[] message, 
            boolean urgent, boolean persistent, 
            double cocaineTimeOut, double cocaineDeadline,
            int maxRetries);

    static {
        Library.loadLib();
    }
}
