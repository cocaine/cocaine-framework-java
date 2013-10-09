package cocaine.msgpack;

import java.io.IOException;
import java.util.UUID;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UUIDTemplate extends AbstractTemplate<UUID> {

    private static final UUIDTemplate instance = new UUIDTemplate();

    private UUIDTemplate() { }

    public static Template<UUID> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, UUID uuid, boolean required) throws IOException {
        packer.write(uuid.toString());
    }

    @Override
    public UUID read(Unpacker unpacker, UUID uuid, boolean required) throws IOException {
        return UUID.fromString(unpacker.readString());
    }

}

