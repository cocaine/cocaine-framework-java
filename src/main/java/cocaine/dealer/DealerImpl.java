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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** 
 * see src/test/java/cocaine/dealer/UsageExample.java for usage example
 *
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public class DealerImpl implements Dealer {
    private final Ptr cDealerPtr;
    private final Lock lock = new ReentrantLock();

    /** Constructs the dealer with the specified filesystem path
     *  to a dealer config json. For an example of dealer config see
     *  {@link https://github.com/cocaine/cocaine-dealer/blob/master/README.md}
     */
    public DealerImpl(String configPath) {
        cDealerPtr = new Ptr(nativeInit(configPath));
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#sendMessages(java.lang.String, byte[], cocaine.dealer.MessagePolicy)
     */
    @Override
    public List<Response> sendMessages(String path, byte[] message, MessagePolicy policy) {
        String[] parts = path.split("/");
        if (parts.length!=2) {
            throw new IllegalArgumentException("path should be in a form 'app/handle'");
        }
        String service = parts[0];
        String handle = parts[1];
        double cocaineTimeout = policy.getTimeoutSeconds();
        double cocaineAckTimeout = policy.getAckTimeoutSeconds();
        double cocaineDeadline = policy.getDeadlineSeconds();
        lock.lock();
        List<Response> responseList = new ArrayList<Response>();
        try {
            List<Long> responsePtrs = nativeSendMessages(
                    cDealerPtr.get(), service, handle, message, policy.urgent, 
                    policy.persistent, cocaineTimeout, cocaineAckTimeout, cocaineDeadline, policy.maxRetries);
            for (Long responsePtr : responsePtrs) {
                responseList.add(new ResponseImpl(responsePtr));
            }
            return responseList;
        } catch(Exception e) {
            for(Response response : responseList) {
                response.close();
            }
            throw new RuntimeException("sending failed",e);
        } finally {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#sendMessages(java.lang.String, byte[])
     */
    @Override
    public List<Response> sendMessages(String path, byte[] message) {
        String[] parts = path.split("/");
        if (parts.length!=2) {
            throw new IllegalArgumentException("path should be in a form 'app/handle'");
        }
        String service = parts[0];
        String handle = parts[1];
        lock.lock();
        List<Response> responseList = new ArrayList<Response>();
        try {
            List<Long> responsePtrs = nativeSendMessages(
                    cDealerPtr.get(), service, handle, message);
            for (Long responsePtr : responsePtrs) {
                responseList.add(new ResponseImpl(responsePtr));
            }
            return responseList;
        } catch(Exception e) {
            for(Response response : responseList) {
                response.close();
            }
            throw new RuntimeException("sending failed",e);
        } finally {
            lock.unlock();
        }
    }
    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#sendMessage(java.lang.String, byte[], cocaine.dealer.MessagePolicy)
     */
    @Override
    public Response sendMessage(String path, byte[] message,
            MessagePolicy messagePolicy) {
        String[] parts = path.split("/");
        if (parts.length!=2) {
            throw new IllegalArgumentException("path should be in a form 'app/handle'");
        }
        String service = parts[0];
        String handle = parts[1];
        double cocaineTimeout = messagePolicy.getTimeoutSeconds();
        double cocaineAckTimeout = messagePolicy.getAckTimeoutSeconds();
        double cocaineDeadline = messagePolicy.getDeadlineSeconds();
        lock.lock();
        try {
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("DealerImpl is closed");
            }
            long responsePtr;
            responsePtr = nativeSendMessage(cDealerPtr.get(), service, handle,
                    message, messagePolicy.urgent, messagePolicy.persistent, cocaineTimeout, cocaineAckTimeout, cocaineDeadline,
                    messagePolicy.maxRetries);
            return new ResponseImpl(responsePtr);
        } finally {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#sendMessage(java.lang.String, byte[])
     */
    @Override
    public Response sendMessage(String path, byte[] message) {
        String[] parts = path.split("/");
        if (parts.length!=2) {
            throw new IllegalArgumentException("path should be in a form 'app/handle'");
        }
        String service = parts[0];
        String handle = parts[1];
        lock.lock();
        try {
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("DealerImpl is closed");
            }
            long responsePtr;
            responsePtr = nativeSendMessage(cDealerPtr.get(), service, handle,message);
            return new ResponseImpl(responsePtr);
        } finally {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#getStoredMessages(java.lang.String)
     */
    @Override
    public List<Message> getStoredMessages(String app) {
        lock.lock();
        try{
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("DealerImpl is closed");
            }
            return nativeGetStoredMessages(cDealerPtr.get(), app);
        } finally {
            lock.unlock();
        }
    }

    
    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#removeStoredMessageFor(cocaine.dealer.Response)
     */
    @Override
    public void removeStoredMessageFor(Response response) {
        lock.lock();
        try{
            ((ResponseImpl)response).removedStoredMessageFor(cDealerPtr.get());
        } finally {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#removeStoredMessage(cocaine.dealer.Message)
     */
    @Override
    public void removeStoredMessage(Message message) {
        lock.lock();
        try{
            nativeRemoveStoredMessage(cDealerPtr.get(), message);
        } finally {
            lock.unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#getStoredMessagesCount(java.lang.String)
     */
    @Override
    public int getStoredMessagesCount(String app) {
        lock.lock();
        try {
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("DealerImpl is closed");
            }
            return nativeGetStoredMessagesCount(cDealerPtr.get(), app);
        } finally {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#policyForService(java.lang.String)
     */
    @Override
    public MessagePolicy policyForService(String serviceAlias) {
        lock.lock();
        try {
            if (!cDealerPtr.isReferring()) {
                throw new IllegalStateException("DealerImpl is closed");
            }
            return nativePolicyForService(cDealerPtr.get(), serviceAlias);
        } finally {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see cocaine.dealer.Dealer#close()
     */
    @Override
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

    private native void nativeRemoveStoredMessage(long cDealerPtr, Message message);
    
    // returns pointer to a client
    private native long nativeInit(String configPath);

    // deletes client
    private native void nativeDelete(long cDealerPtr);

    // retrieves the number of unsent messages
    private native int nativeGetStoredMessagesCount(long cDealerPtr, String serviceAlias);

    // retrieves the unsent messages
    private native List<Message> nativeGetStoredMessages(long cDealerPtr, String service);

    // returns pointer to response
    private native long nativeSendMessage(long cDealerPtr, String service,
            String handle, byte[] message, 
            boolean urgent, boolean persistent, 
            double cocaineTimeOut, double cocaineAckTimeout, double cocaineDeadline,
            int maxRetries);

    // returns pointer to response
    private native long nativeSendMessage(long cDealerPtr, String service,
            String handle, byte[] message);

    // returns list of pointers to responses
    private native List<Long> nativeSendMessages(long cDealerPtr, String service,
            String handle, byte[] message);

    // returns list of pointers to responses
    private native List<Long> nativeSendMessages(long cDealerPtr, String service,
            String handle, byte[] message,
            boolean urgent, boolean persistent,
            double cocaineTimeOut, double cocaineAckTimeout, double cocaineDeadline,
            int maxRetries);

    private native MessagePolicy nativePolicyForService(long cDealerPtr, String serviceAlias);

    static {
        Library.loadLib();
    }
}
