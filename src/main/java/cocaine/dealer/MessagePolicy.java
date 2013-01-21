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

/** Defines cocaine message handling policy
 * 
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public class MessagePolicy {
    protected final boolean urgent;
    protected final boolean persistent;
    protected final long timeoutDuration;
    protected final TimeUnit timeoutTimeUnit;
    protected final long ackTimeoutDuration;
    protected final TimeUnit ackTimeoutTimeUnit;
    protected final long deadlineDuration;
    protected final TimeUnit deadlineTimeUnit;
    protected final int maxRetries;

    /**
     * @param urgent
     * @param persistent
     * @param timeoutDuration
     * @param timeoutTimeUnit
     * @param deadlineDuration
     * @param deadlineTimeUnit 
     * @param maxRetries - number of retries dealer would try until it receives ACK from cocaine node in time
     */
    public MessagePolicy(boolean urgent, boolean persistent,
            long timeoutDuration, TimeUnit timeoutTimeUnit,
            long ackTimeoutDuration, TimeUnit ackTimeoutTimeUnit,
            long deadlineDuration, TimeUnit deadlineTimeUnit, int maxRetries)
    {
        this.urgent = urgent;
        this.persistent = persistent;
        this.timeoutDuration = timeoutDuration;
        this.timeoutTimeUnit = timeoutTimeUnit;
        this.ackTimeoutDuration = ackTimeoutDuration;
        this.ackTimeoutTimeUnit = ackTimeoutTimeUnit;
        this.deadlineDuration = deadlineDuration;
        this.deadlineTimeUnit = deadlineTimeUnit;
        this.maxRetries = maxRetries;
    }

    public MessagePolicy(boolean urgent, boolean persistent, long timeoutMillis, long ackTimeoutMillis,
            long deadlineMillis, int maxRetries)
    {
        this(urgent, persistent, timeoutMillis, TimeUnit.MILLISECONDS,
                ackTimeoutMillis, TimeUnit.MILLISECONDS,
                deadlineMillis, TimeUnit.MILLISECONDS, maxRetries);
    }

    public double getTimeoutSeconds() {
        return timeoutTimeUnit.toMicros(timeoutDuration) / 1000000.0;
    }

    public double getAckTimeoutSeconds() {
        return ackTimeoutTimeUnit.toMicros(ackTimeoutDuration) / 1000000.0;
    }

    public double getDeadlineSeconds() {
        return deadlineTimeUnit.toMicros(deadlineDuration) / 1000000.0;
    }

    public boolean getUrgent() {
        return urgent;
    }

    public boolean getPersistent() {
        return persistent;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.urgent) {
            builder.append("urgent ");
        }
        if (this.persistent) {
            builder.append("persistent");
        }
        builder.append("timeout " + timeoutTimeUnit.toMillis(timeoutDuration) + " millis ");
        builder.append("ack timeout " + ackTimeoutTimeUnit.toMillis(ackTimeoutDuration) +" millis ");
        builder.append("deadline " + deadlineTimeUnit.toMillis(deadlineDuration) + " millis ");
        builder.append("max retries " + maxRetries + " ");
        return builder.toString();
    }

    public static MessagePolicyBuilder builder() {
        return new MessagePolicyBuilder();
    }

    public static class MessagePolicyBuilder {

        private boolean urgent = false;
        private boolean persistent = false;
        private long timeoutDuration = 0;
        private TimeUnit timeoutTimeUnit = TimeUnit.MILLISECONDS;
        private long ackTimeoutDuration = 0;
        private TimeUnit ackTimeoutTimeUnit = TimeUnit.MILLISECONDS;
        private long deadlineDuration = 0;
        private TimeUnit deadlineTimeUnit = TimeUnit.MILLISECONDS;
        private int maxRetries = 0;

        private MessagePolicyBuilder() {
        }

        public MessagePolicyBuilder urgent() {
            this.urgent = true;
            return this;
        }

        public MessagePolicyBuilder persistent() {
            this.persistent = true;
            return this;
        }

        public MessagePolicyBuilder timeout(long timeout, TimeUnit timeUnit) {
            this.timeoutDuration = timeout;
            this.timeoutTimeUnit = timeUnit;
            return this;
        }

        public MessagePolicyBuilder deadline(long timeout, TimeUnit timeUnit) {
            this.deadlineDuration = timeout;
            this.deadlineTimeUnit = timeUnit;
            return this;
        }

        public MessagePolicyBuilder maxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }

        public MessagePolicy build() {
            return new MessagePolicy(urgent, persistent, timeoutDuration,
                    timeoutTimeUnit, ackTimeoutDuration, ackTimeoutTimeUnit, deadlineDuration, deadlineTimeUnit,
                    maxRetries);
        }

        public static MessagePolicyBuilder from(MessagePolicy policy) {
            MessagePolicyBuilder builder = new MessagePolicyBuilder();
            builder.ackTimeoutDuration = policy.ackTimeoutDuration;
            builder.ackTimeoutTimeUnit = policy.ackTimeoutTimeUnit;
            builder.deadlineDuration = policy.deadlineDuration;
            builder.deadlineTimeUnit = policy.deadlineTimeUnit;
            builder.maxRetries = policy.maxRetries;
            builder.persistent = policy.persistent;
            builder.timeoutDuration = policy.timeoutDuration;
            builder.timeoutTimeUnit = policy.timeoutTimeUnit;
            builder.urgent = policy.urgent;
            return builder;
        }
    }
}
