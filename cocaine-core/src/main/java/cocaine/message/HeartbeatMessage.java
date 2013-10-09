package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class HeartbeatMessage extends Message {

    public HeartbeatMessage() {
        super(MessageType.HEARTBEAT);
    }

    @Override
    public String toString() {
        return "HeartbeatMessage/" + getSession();
    }

}
