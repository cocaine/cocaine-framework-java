package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class HandshakeMessage extends Message {

    private final String uniqueId;

    public HandshakeMessage(long session, String uniqueId) {
        super(Type.HANDSHAKE, session);
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return "HandshakeMessage/" + getSession() + ": " + uniqueId;
    }
}
