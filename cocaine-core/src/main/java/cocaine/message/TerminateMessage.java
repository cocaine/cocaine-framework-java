package cocaine.message;

import com.google.common.base.Preconditions;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class TerminateMessage extends Message {

    public static enum Reason {
        NORMAL(1),
        ABNORMAL(2),
        ;

        private final int value;

        private Reason(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Reason fromValue(int value) {
            for (Reason reason : values()) {
                if (reason.value == value) {
                    return reason;
                }
            }
            throw new IllegalArgumentException("Invalid Reason: " + value);
        }
    }

    private final Reason reason;
    private final String message;

    public TerminateMessage(Reason reason, String message) {
        super(MessageType.TERMINATE);
        Preconditions.checkNotNull(reason, "Termination reason can not be null");
        Preconditions.checkNotNull(message, "Message can not be null");

        this.reason = reason;
        this.message = message;
    }

    public Reason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "TerminateMessage/" + getSession() + ": " + reason.name() + " - " + message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        TerminateMessage that = (TerminateMessage) o;
        return message.equals(that.message) && reason == that.reason;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + reason.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

}
