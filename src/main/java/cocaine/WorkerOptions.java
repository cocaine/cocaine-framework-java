package cocaine;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.BaseConverter;
import com.beust.jcommander.converters.IntegerConverter;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
@Parameters(separators = " =")
public class WorkerOptions {

    @Parameter(
            names = { "-u", "--uuid" },
            description = "Universally unique identifier of the application",
            required = true,
            converter = UUIDConverter.class
    )
    public UUID id;

    @Parameter(
            names = { "-a", "--app" },
            description = "Application name",
            required = true
    )
    public String app;

    @Parameter(
            names = { "-e", "--endpoint" },
            description = "Cocaine runtime endpoint",
            required = true,
            converter = SocketAddressConverter.class
    )
    public SocketAddress endpoint;

    @Parameter(
            names = { "-h", "--heartbeat" },
            description = "Heartbeat timeout in seconds",
            required = false,
            converter = IntegerConverter.class
    )
    public int heartbeatTimeout = 5;

    @Parameter(
            names = { "-d", "--disown" },
            description = "Disown timeout in seconds",
            required = false,
            converter = IntegerConverter.class
    )
    public int disownTimeout = 10;

    @Parameter(
            names = { "-s", "--socket" },
            description = "Socket timeout in seconds",
            required = false,
            converter = IntegerConverter.class
    )
    public int socketTimeout = 5;

    @Parameter(
            names = { "--help" },
            help = true
    )
    public boolean help;

    @Override
    public String toString() {
        return "WorkerOptions {" +
                " id: " + id +
                ", app: '" + app + "'" +
                ", endpoint: " + endpoint +
                ", heartbeatTimeout: " + heartbeatTimeout +
                ", disownTimeout: " + disownTimeout +
                ", socketTimeout: " + socketTimeout +
                " }";
    }

    public static class UUIDConverter extends BaseConverter<UUID> {

        public UUIDConverter(String optionName) {
            super(optionName);
        }

        @Override
        public UUID convert(String value) {
            try {
                return UUID.fromString(value);
            } catch (Exception e) {
                throw new ParameterException(String.format("'%s': couldn't convert '%s' to UUID",
                        getOptionName(), value));
            }
        }

    }

    public static class SocketAddressConverter extends BaseConverter<SocketAddress> {

        public SocketAddressConverter(String optionName) {
            super(optionName);
        }

        @Override
        public SocketAddress convert(String value) {
            try {
                String[] address = value.split(":", 2);
                return new InetSocketAddress(InetAddress.getByName(address[0]), Integer.parseInt(address[1]));
            } catch (Exception e) {
                throw new ParameterException(String.format("'%s': couldn't convert '%s' to SocketAddress",
                        getOptionName(), value));
            }
        }

    }

}
