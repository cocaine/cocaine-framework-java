package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public enum MessageType {

    HANDSHAKE(0),
    HEARTBEAT(1),
    TERMINATE(2),
    INVOKE(3),
    CHUNK(4),
    ERROR(5),
    CHOKE(6),
    ;

    private final int value;

    private MessageType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static MessageType fromValue(int value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType: " + value);
    }
}
