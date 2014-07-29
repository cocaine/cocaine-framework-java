package cocaine;

import java.util.UUID;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class WorkerOptions {

    @Parameter(names = {"-d", "--disown-timeout"}, description = "Disown timeout in millis")
    private int disownTimeout = 5_000;

    @Parameter(names = {"-h", "--heartbeat-timeout"}, description = "Heartbeat timeout in millis")
    private int heartbeatTimeout = 20_000;

    @Parameter(names = "--app")
    private String application;

    @Parameter(names = "--uuid", converter = UUIDConverter.class)
    private UUID uuid;

    @Parameter(names = "--endpoint", description = "Unix Domain Socket")
    private String endpoint;

    @Parameter(names = "--locator")
    private String locator;

    @Parameter(names = "--help", help = true)
    private boolean help;

    public int getDisownTimeout() {
        return disownTimeout;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public String getApplication() {
        return application;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getLocator() {
        return locator;
    }

    @Override
    public String toString() {
        return "WorkerOptions{endpoint: " + endpoint + ", disownTimeout: " + disownTimeout
                + ", heartbeatTimeout: " + heartbeatTimeout + ", application: " + application + ", uuid: " + uuid + "}";
    }

    public static final class UUIDConverter implements IStringConverter<UUID> {
        @Override
        public UUID convert(String value) {
            return UUID.fromString(value);
        }
    }
}
