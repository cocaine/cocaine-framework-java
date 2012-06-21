package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Dealer {
    private long cDealerPtr = 0;

    public Dealer(String configPath) {
        cDealerPtr = init(configPath);
    }

    public Response sendMessage(String path, Message message,
            MessagePolicy messagePolicy) {
        if (cDealerPtr == 0) {
            throw new IllegalStateException("client is closed");
        }
        String[] parts = path.split("/");
        String service = parts[0];
        String handle = parts[1];
        double cocaineTimeout = toCocainTimestamp(
                messagePolicy.timeoutDuration, messagePolicy.timeoutTimeUnit);
        double cocaineDeadline = toCocainTimestamp(
                messagePolicy.timeoutDuration, messagePolicy.timeoutTimeUnit);
        long responsePtr = sendMessage(cDealerPtr, service, handle,
                message.toString(), messagePolicy.sendToAllHosts,
                messagePolicy.urgent, cocaineTimeout, cocaineDeadline,
                messagePolicy.maxRetries);
        return new Response(responsePtr);
    }

    public void close() {
        if (cDealerPtr != 0) {
            delete(cDealerPtr);
        }
        cDealerPtr = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public static double toCocainTimestamp(long timeout, TimeUnit timeUnit) {
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
