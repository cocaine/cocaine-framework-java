package cocaine.message;

import java.util.UUID;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class HandshakeMessage extends Message {

    private final UUID id;

    public HandshakeMessage(long session, UUID id) {
        super(Type.HANDSHAKE, session);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "HandshakeMessage/" + getSession() + ": " + id;
    }
}
