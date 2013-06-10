package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class HeartbeatMessage extends Message {

    public HeartbeatMessage(long session) {
        super(Type.HEARTBEAT, session);
    }

    @Override
    public String toString() {
        return "HeartbeatMessage/" + getSession();
    }

}
