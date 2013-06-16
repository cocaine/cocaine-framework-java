package cocaine;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cocaine.netty.ServiceMessageHandler;
import com.google.common.base.Joiner;
import io.netty.channel.ChannelFuture;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Service {

    private static final ServiceSessions sessions = new ServiceSessions();

    private final ServiceInfo info;
    private final ChannelFuture connection;

    public Service(ServiceInfo info, ChannelFuture connection) {
        this.info = info;
        this.connection = connection;
        this.connection.channel().pipeline().addLast(new ServiceMessageHandler(info.getName(), sessions));
    }

    public ServiceSession invoke(String method, Object... args) {
        return invoke(method, Arrays.asList(args));
    }

    public ServiceSession invoke(String method, List<Object> args) {
        ServiceSession session = sessions.create(info.getName());
        int requestedMethod = info.getMethod(method);
        connection.channel().write(new InvocationRequest(requestedMethod, session.getId(), args));

        return session;
    }

    public ServiceInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return info.toString();
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
