package cocaine.dealer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Dealer {
    private final Ptr cDealerPtr;
    private final Lock lock = new ReentrantLock();

    public Dealer(String configPath) {
        cDealerPtr = new Ptr(init(configPath));
    }

    public Response sendMessage(String path, Message message,
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
                    message.getBytes(), messagePolicy.sendToAllHosts,
                    messagePolicy.urgent, cocaineTimeout, cocaineDeadline,
                    messagePolicy.maxRetries);
            return new Response(responsePtr);
        } finally {
            lock.unlock();
        }
    }

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

    {
        System.loadLibrary("cocaine-framework-java");
    }
}
