package ru.yandex.cocaine.dealer;


/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Dealer {
    private final Ptr cDealerPtr;
    
    public Dealer(String configPath) {
        cDealerPtr = new Ptr(init(configPath));
    }

    public Response sendMessage(String path, Message message,
            MessagePolicy messagePolicy) {
        if (!cDealerPtr.isReferring()){
            throw new IllegalStateException("Dealer is closed");
        }
        String[] parts = path.split("/");
        String service = parts[0];
        String handle = parts[1];
        double cocaineTimeout = messagePolicy.cocaineTimeout();
        double cocaineDeadline = messagePolicy.cocaineDeadline();
        long responsePtr = sendMessage(cDealerPtr.get(), service, handle,
                message.getBytes(), messagePolicy.sendToAllHosts,
                messagePolicy.urgent, cocaineTimeout, cocaineDeadline,
                messagePolicy.maxRetries);
        return new Response(responsePtr);
    }

    public void close() {
        if (cDealerPtr.isReferring()) {
            delete(cDealerPtr.get());
        }
        cDealerPtr.close();
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
