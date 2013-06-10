package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class InvokeMessage extends Message {

    private final String event;

    public InvokeMessage(long session, String event) {
        super(Type.INVOKE, session);
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "InvokeMessage/" + getSession() + ": " + event;
    }
}
