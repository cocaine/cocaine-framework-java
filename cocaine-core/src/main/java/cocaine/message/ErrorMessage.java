package cocaine.message;

import com.google.common.base.Preconditions;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class ErrorMessage extends Message {

    private final int code;
    private final String message;

    public ErrorMessage(long session, int code, String message) {
        super(MessageType.ERROR, session);
        this.code = code;
        this.message = Preconditions.checkNotNull(message, "Error message can not be null");;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorMessage/" + getSession() + ": " + code + " - " + message;
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

        ErrorMessage that = (ErrorMessage) o;
        return code == that.code && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + code;
        result = 31 * result + message.hashCode();
        return result;
    }

}
