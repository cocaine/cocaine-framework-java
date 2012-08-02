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
        cDealerPtr = new Ptr(init(configPath));
    }

    /**
     * send a message(byte[]) to the specified cocaine app/path [='app/handle']
     * with a specified MessagePolicy
     * and returns a Response object for retrieving results
     */
    public Response sendMessage(String path, byte[] message,
            MessagePolicy messagePolicy) {
        String[] parts = path.split("/");
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
            responsePtr = sendMessage(cDealerPtr.get(), service, handle,
                    message, messagePolicy.sendToAllHosts,
                    messagePolicy.urgent, cocaineTimeout, cocaineDeadline,
                    messagePolicy.maxRetries);
            return new Response(responsePtr);
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
                delete(cDealerPtr.get());
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
    private native long init(String configPath);

    // deletes client
    private native void delete(long cClientPtr);

    // returns pointer to response
    private native long sendMessage(long cClientPtr, String service,
            String handle, byte[] message, boolean sendToAllHosts,
            boolean urgent, double cocaineTimeOut, double cocaineDeadline,
            int maxRetries);

    static {
        System.loadLibrary("cocaine-framework-java");
    }
}
