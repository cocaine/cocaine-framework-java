package cocaine;

import javax.inject.Inject;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.reflect.Parameter;
import com.google.gson.Gson;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class JsonSerializer extends BaseSerializer {

    private final Gson gson;

    @Inject
    public JsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected byte[] serialize(Object data) throws IOException {
        return gson.toJson(data).getBytes(Charsets.UTF_8);
    }
}
