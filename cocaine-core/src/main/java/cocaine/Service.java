package cocaine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import org.apache.log4j.Logger;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Service {

    private static final Logger logger = Logger.getLogger(Service.class);

    private final String name;
    private final Sessions sessions;
    private final ConnectionHolder connection;

    private Service(String name, Sessions sessions, ConnectionHolder connection) {
        this.name = name;
        this.sessions = sessions;
        this.connection = connection;
        this.connection.connect();
    }

    public static Service create(String name, Bootstrap bootstrap, Supplier<ServiceInfo> infoSupplier) {
        Sessions sessions = new Sessions(name);
        return new Service(name, sessions, ConnectionHolder.create(name, bootstrap, sessions, infoSupplier));
    }

    public ServiceResponse<byte[]> invoke(String method, Object... args) {
        return invoke(method, Arrays.asList(args));
    }

    public ServiceResponse<byte[]> invoke(String method, List<Object> args) {
        logger.debug("Invoking " + method + "(" + Joiner.on(", ").join(args) + ") asynchronously");

        ServiceSession session = sessions.create();
        int requestedMethod = connection.getServiceInfo().getMethod(method);
        connection.write(new InvocationRequest(requestedMethod, session.getSession(), args));

        return session;
    }

    @Override
    public String toString() {
        return name + " [" + connection.getServiceInfo().getEndpoint() + "]";
    }

    private static class InvocationRequest implements MessagePackable {

        private final int method;
        private final long session;
        private final List<Object> args;

        public InvocationRequest(int method, long session, List<Object> args) {
            this.method = method;
            this.session = session;
            this.args = args;
        }

        @Override
        public void writeTo(Packer packer) throws IOException {
            packer.writeArrayBegin(3);
            packer.write(method);
            packer.write(session);
            packer.write(args);
            packer.writeArrayEnd();
        }

        @Override
        public void readFrom(Unpacker unpacker) {
            throw new UnsupportedOperationException("Reading InvocationRequest is not supported");
        }

        @Override
        public String toString() {
            return "InvocationRequest/" + session + ": " + method + " [" + Joiner.on(", ").join(args) + "]";
        }
    }

}
