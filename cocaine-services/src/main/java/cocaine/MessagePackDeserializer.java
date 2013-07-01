package cocaine;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.inject.Inject;

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
    public Object deserialize(byte[] bytes, Type type) throws IOException {
        Template<?> template = messagePack.lookup(type);
        return messagePack.read(bytes, template);
    }

}
