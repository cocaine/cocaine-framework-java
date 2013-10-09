package cocaine;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class JsonDeserializer implements CocaineDeserializer {

    private final Gson gson;

    @Inject
    public JsonDeserializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Type type) throws IOException {
        return gson.fromJson(new String(bytes, Charsets.UTF_8), type);
    }

}
