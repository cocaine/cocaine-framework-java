package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Dealer {
    private Ptr cDealerPtr = new Ptr();
    
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
        double cocaineTimeout = toNanoSeconds(
                messagePolicy.timeoutDuration, messagePolicy.timeoutTimeUnit);
        double cocaineDeadline = toNanoSeconds(
                messagePolicy.timeoutDuration, messagePolicy.timeoutTimeUnit);
        long responsePtr = sendMessage(cDealerPtr.get(), service, handle,
                message.toString(), messagePolicy.sendToAllHosts,
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

    public static double toNanoSeconds(long timeout, TimeUnit timeUnit) {
        return timeUnit.toMillis(timeout) / 1000000.0;
    }

    // returns pointer to a client
    private native long init(String configPath);

    // deletes client
    private native void delete(long cClientPtr);

    // returns pointer to response
    private native long sendMessage(long cClientPtr, String service,
            String handle, String message, boolean sendToAllHosts,
            boolean urgent, double cocaineTimeOut, double cocaineDeadline,
            int maxRetries);

    {
        System.loadLibrary("cocaine-framework-java");
    }
}
