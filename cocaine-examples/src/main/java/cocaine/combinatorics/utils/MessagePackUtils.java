package cocaine.combinatorics.utils;

import java.io.IOException;

import com.google.common.base.Throwables;
import org.msgpack.MessagePack;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class MessagePackUtils {

    public static <T> T read(MessagePack messagePack, byte[] bytes, Class<T> type) {
        try {
            return messagePack.read(bytes, type);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Value[] read(MessagePack messagePack, byte[] bytes) {
        try {
            return ArrayValue.class.cast(messagePack.read(bytes)).getElementArray();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> byte[] write(MessagePack messagePack, T value) {
        try {
            return messagePack.write(value);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private MessagePackUtils() { }
}
