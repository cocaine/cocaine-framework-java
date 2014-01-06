package cocaine.message;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

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

    private static final Map<Integer, MessageType> mapping =
            Maps.uniqueIndex(Arrays.asList(MessageType.values()), MessageType.valueF());

    private final int value;

    private MessageType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static MessageType fromValue(int value) {
        MessageType result = mapping.get(value);
        if (result == null) {
            throw new IllegalArgumentException("MessageType " + value + " does not exist");
        }
        return result;
    }

    public static Function<MessageType, Integer> valueF() {
        return new Function<MessageType, Integer>() {
            @Override
            public Integer apply(MessageType type) {
                return type.value();
            }
        };
    }
}
