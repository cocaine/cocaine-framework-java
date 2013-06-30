package cocaine;

import javax.inject.Inject;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
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
    public Object deserialize(byte[] bytes, TypeToken<?> type) throws IOException {
        return gson.fromJson(new String(bytes, Charsets.UTF_8), type.getType());
    }

}
