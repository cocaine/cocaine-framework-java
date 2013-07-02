package cocaine.services;

import java.io.IOException;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class LevelTemplate extends AbstractTemplate<Logging.Level> {

    private static final Template<Logging.Level> instance = new LevelTemplate();

    private LevelTemplate() { }

    public static Template<Logging.Level> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, Logging.Level value, boolean required) throws IOException {
        packer.write(value.value());
    }

    @Override
    public Logging.Level read(Unpacker unpacker, Logging.Level value, boolean required) throws IOException {
        return Logging.Level.fromValue(unpacker.readInt());
    }

}
