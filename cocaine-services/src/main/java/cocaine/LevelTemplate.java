package cocaine;

import java.io.IOException;

import org.apache.log4j.Level;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class LevelTemplate extends AbstractTemplate<Level> {

    private static final Template<Level> instance = new LevelTemplate();

    private LevelTemplate() { }

    public static Template<Level> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, Level level, boolean required) throws IOException {
        if (level == Level.OFF) {
            packer.write(0);
        } else if (level == Level.ERROR) {
            packer.write(1);
        } else if (level == Level.WARN) {
            packer.write(2);
        } else if (level == Level.INFO) {
            packer.write(3);
        } else if (level == Level.DEBUG) {
            packer.write(4);
        } else {
            throw new IllegalArgumentException("Unsupported level: " + level);
        }
    }

    @Override
    public Level read(Unpacker unpacker, Level level, boolean required) throws IOException {
        int value = unpacker.readInt();
        switch (value) {
            case 0:
                return Level.OFF;
            case 1:
                return Level.ERROR;
            case 2:
                return Level.WARN;
            case 3:
                return Level.INFO;
            case 4:
                return Level.DEBUG;
            default:
                throw new IllegalArgumentException("Invalid level: " + value);
        }
    }

}
