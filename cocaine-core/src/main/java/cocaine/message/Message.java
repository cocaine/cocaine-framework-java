package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public abstract class Message {

    private static final long SYSTEM_SESSION = 0L;

    private final MessageType type;
    private final long session;

    protected Message(MessageType type, long session) {
        this.type = type;
        this.session = session;
    }

    protected Message(MessageType type) {
        this(type, SYSTEM_SESSION);
    }

    public MessageType getType() {
        return type;
    }

    public long getSession() {
        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;
        return session == message.session && type == message.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (session ^ (session >>> 32));
        return result;
    }

}
