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
    protected final boolean sendToAllHosts;
    protected final boolean urgent;
    protected final long timeoutDuration;
    protected final TimeUnit timeoutTimeUnit;
    protected final long deadlineDuration;
    protected final TimeUnit deadlineTimeUnit;
    protected final int maxRetries;
    /**
     * @param sendToAllHosts 
     * @param urgent
     * @param timeoutDuration
     * @param timeoutTimeUnit
     * @param deadlineDuration
     * @param deadlineTimeUnit 
     * @param maxRetries - number of retries dealer would try untill it receives ACK from cocaine node in time
     */
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
