package cocaine;

import java.io.IOException;

import com.google.common.reflect.TypeToken;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public interface CocaineDeserializer {

    Object deserialize(byte[] bytes, TypeToken<?> type) throws IOException;

}
