package cocaine;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public interface CocaineDeserializer {

    <T> T deserialize(byte[] bytes, Type type) throws IOException;

}
