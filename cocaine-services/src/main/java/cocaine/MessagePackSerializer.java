package cocaine;

import javax.inject.Inject;

import java.io.IOException;

import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class MessagePackSerializer extends BaseSerializer {

    private final MessagePack messagePack;

    @Inject
    public MessagePackSerializer(MessagePack messagePack) {
        this.messagePack = messagePack;
    }

    @Override
    public byte[] serialize(Object data) throws IOException {
        return messagePack.write(data);
    }

}
