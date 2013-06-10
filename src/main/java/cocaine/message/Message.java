package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public abstract class Message {

    public static enum Type {
        HANDSHAKE(0),
        HEARTBEAT(1),
        TERMINATE(2),
        INVOKE(3),
        CHUNK(4),
        ERROR(5),
        CHOKE(6);

        private final int value;

        private Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Type fromValue(int value) {
            for (Type type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid MessageType: " + value);
        }
    }

    private final Type type;
    private final long session;

    protected Message(Type type, long session) {
        this.type = type;
        this.session = session;
    }

    public Type getType() {
        return type;
    }

    public long getSession() {
        return session;
    }

}
