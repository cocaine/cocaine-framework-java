package cocaine;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import cocaine.annotations.CocaineParameter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Parameter;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public abstract class BaseSerializer implements CocaineSerializer {

    @Override
    public byte[] serialize(Parameter[] parameters, Object[] values) throws IOException {
        Object result;
        if (parameters.length == 0) {
            result = Lists.newArrayList();
        } else if (parameters.length == 1) {
            CocaineParameter parameterDescriptor = parameters[0].getAnnotation(CocaineParameter.class);
            if (parameterDescriptor == null) {
                result = values[0];
            } else {
                result = Collections.singletonMap(parameterDescriptor.value(), parameters[0]);
            }
        } else {
            Map<String, Object> map = Maps.newHashMap();
            for (int i = 0; i < parameters.length; i++) {
                CocaineParameter parameterDescriptor = parameters[i].getAnnotation(CocaineParameter.class);
                Preconditions.checkState(parameterDescriptor != null,
                        "All parameters must be annotated with @CocaineParameter annotation");
                Preconditions.checkState(!map.containsKey(parameterDescriptor.value()),
                        "All parameters names must be unique within method");
                map.put(parameterDescriptor.value(), values[i]);
            }
            result = map;
        }
        return serialize(result);
    }

    protected abstract byte[] serialize(Object data) throws IOException;
}
