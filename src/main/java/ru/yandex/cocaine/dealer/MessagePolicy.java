package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class MessagePolicy {
    protected final boolean sendToAllHosts;
    protected final boolean urgent;
    protected final long timeoutDuration;
    protected final TimeUnit timeoutTimeUnit;
    protected final long deadlineDuration;
    protected final TimeUnit deadlineTimeUnit;
    protected final int maxRetries;
    public MessagePolicy(boolean sendToAllHosts, boolean urgent,
            long timeoutDuration, TimeUnit timeoutTimeUnit,
            long deadlineDuration, TimeUnit deadlineTimeUnit, int maxRetries)
    {
        this.sendToAllHosts = sendToAllHosts;
        this.urgent = urgent;
        this.timeoutDuration = timeoutDuration;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.deadlineDuration = deadlineDuration;
        this.deadlineTimeUnit = deadlineTimeUnit;
        this.maxRetries = maxRetries;
    }

    public double cocaineTimeout() {
        return timeoutTimeUnit.toMicros(timeoutDuration) / 1000000.0;
    }
    
    public double cocaineDeadline() {
        return deadlineTimeUnit.toMicros(deadlineDuration) / 1000000.0;
    }

    public static MessagePolicyBuilder builder() {
        return new MessagePolicyBuilder();
    }

    public static class MessagePolicyBuilder {

        private boolean sendToAllHosts = false;
        private boolean urgent = false;
        private long timeoutDuration = 0;
        private TimeUnit timeoutTimeUnit = TimeUnit.MILLISECONDS;
        private long deadlineDuration = 0;
        private TimeUnit deadlineTimeUnit = TimeUnit.MILLISECONDS;
        private int maxRetries = 0;

        private MessagePolicyBuilder() {
        }

        public MessagePolicyBuilder sendToAllHosts() {
            this.sendToAllHosts = true;
            return this;
        }

        public MessagePolicyBuilder urgent() {
            this.urgent = true;
            return this;
        }

        public MessagePolicyBuilder timeout(long timeout, TimeUnit timeUnit) {
            this.timeoutDuration = timeout;
            this.timeoutTimeUnit = timeUnit;
            return this;
        }

        public MessagePolicyBuilder duration(long timeout, TimeUnit timeUnit) {
            this.deadlineDuration = timeout;
            this.deadlineTimeUnit = timeUnit;
            return this;
        }

        public MessagePolicyBuilder maxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }

        public MessagePolicy build() {
            return new MessagePolicy(sendToAllHosts, urgent, timeoutDuration,
                    timeoutTimeUnit, deadlineDuration, deadlineTimeUnit,
                    maxRetries);
        }
    }
}
