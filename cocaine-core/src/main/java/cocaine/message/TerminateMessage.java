package cocaine.message;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class TerminateMessage extends Message {

    public static enum Reason {

        NORMAL(1),
        ABNORMAL(2),
        ;

        private static final Map<Integer, Reason> mapping =
                Maps.uniqueIndex(Arrays.asList(Reason.values()), Reason.valueF());

        private final int value;

        private Reason(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Reason fromValue(int value) {
            Reason result = mapping.get(value);
            if (result == null) {
                throw new IllegalArgumentException("Reason " + value + " does not exist");
            }
            return result;
        }

        public static Function<Reason, Integer> valueF() {
            return new Function<Reason, Integer>() {
                @Override
                public Integer apply(Reason reason) {
                    return reason.value();
                }
            };
        }
    }

    private final Reason reason;
    private final String message;

    public TerminateMessage(Reason reason, String message) {
        super(MessageType.TERMINATE);
        this.reason = Preconditions.checkNotNull(reason, "Termination reason can not be null");
        this.message = Preconditions.checkNotNull(message, "Message can not be null");
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
