package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ErrorMessage extends Message {

    private final int code;
    private final String message;

    public ErrorMessage(long session, int code, String message) {
        super(Type.ERROR, session);
        this.code = code;
        this.message = message;
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
}
