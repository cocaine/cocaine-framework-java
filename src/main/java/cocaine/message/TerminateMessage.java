package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class TerminateMessage extends Message {

    public static enum Reason {
        NORMAL(1),
        ABNORMAL(2)
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
        super(Type.TERMINATE, 0L);
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
}
