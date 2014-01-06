package cocaine.message;

import java.util.UUID;

import com.google.common.base.Preconditions;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class HandshakeMessage extends Message {

    private final UUID id;

    public HandshakeMessage(UUID id) {
        super(MessageType.HANDSHAKE);
        this.id = Preconditions.checkNotNull(id, "ID can not be null");
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return "HandshakeMessage/" + getSession() + ": " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        HandshakeMessage that = (HandshakeMessage) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

}
