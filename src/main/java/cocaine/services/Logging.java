package cocaine.services;

import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
@CocaineService("logging")
public interface Logging {

    enum Level {
        FATAL(0),
        ERROR(1),
        WARN(2),
        INFO(3),
        DEBUG(4),
        ;

        private final int value;

        private Level(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Level fromValue(int value) {
            for (Level type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid Level: " + value);
        }
    }

    @CocaineMethod("verbosity")
    Level getVerbosity();

    @CocaineMethod("emit")
    void log(Level level, String name, String message);
}
