package cocaine.message;

import java.util.Arrays;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ChunkMessage extends Message {

    private final byte[] data;

    public ChunkMessage(long session, byte[] data) {
        super(Type.CHUNK, session);
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ChunkMessage/" + getSession() + ": " + Arrays.toString(data);
    }
}
