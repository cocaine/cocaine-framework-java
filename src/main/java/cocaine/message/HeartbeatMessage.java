package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class HeartbeatMessage extends Message {

    public HeartbeatMessage() {
        super(Type.HEARTBEAT, 0L);
    }

    @Override
    public String toString() {
        return "HeartbeatMessage/" + getSession();
    }

}
