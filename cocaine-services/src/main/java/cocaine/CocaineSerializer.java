package cocaine;

import java.io.IOException;

import com.google.common.reflect.Parameter;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public interface CocaineSerializer {

    byte[] serialize(Parameter[] parameters, Object[] values) throws IOException;

}
