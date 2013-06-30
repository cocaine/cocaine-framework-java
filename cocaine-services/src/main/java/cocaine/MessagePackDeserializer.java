package cocaine;

import javax.inject.Inject;

import java.io.IOException;

import com.google.common.reflect.TypeToken;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class MessagePackDeserializer implements CocaineDeserializer {

    private final MessagePack messagePack;

    @Inject
    public MessagePackDeserializer(MessagePack messagePack) {
        this.messagePack = messagePack;
    }

    @Override
    public Object deserialize(byte[] bytes, TypeToken<?> type) throws IOException {
        Template<?> template = messagePack.lookup(type.getType());
        return messagePack.read(bytes, template);
    }

}
