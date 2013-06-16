package cocaine.annotations;

import org.msgpack.type.Value;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public interface ValueConverter<T> {
    String convert(Value value) throws Exception;
}
