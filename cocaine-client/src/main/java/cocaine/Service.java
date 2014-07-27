package cocaine;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cocaine.netty.ServiceMessageHandler;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import rx.Observable;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Service {

    private static final Logger logger = Logger.getLogger(Service.class);

    private final String name;
    private final ServiceApi api;
    private final Sessions sessions;

    private Channel channel;

    private Service(String name, ServiceApi api, Bootstrap bootstrap, Supplier<SocketAddress> endpoint) {
        this.name = name;
        this.sessions = new Sessions(name);
        this.api = api;
        connect(bootstrap, endpoint, new ServiceMessageHandler(name, sessions));
    }

    public static Service create(String name, Bootstrap bootstrap, Supplier<SocketAddress> endpoint, ServiceApi api) {
        return new Service(name, api, bootstrap, endpoint);
    }

    public Observable<byte[]> invoke(String method, Object... args) {
        return invoke(method, Arrays.asList(args));
    }

    public Observable<byte[]> invoke(String method, List<Object> args) {
        logger.debug("Invoking " + method + "(" + Joiner.on(", ").join(args) + ") asynchronously");

        Sessions.Session session = sessions.create();
        int requestedMethod = api.getMethod(method);
        channel.write(new InvocationRequest(requestedMethod, session.getId(), args));

        return session.getInput();
    }

    @Override
    public String toString() {
        return name + "/" + channel.remoteAddress();
    }

    private void connect(final Bootstrap bootstrap, final Supplier<SocketAddress> endpoint,
            final ServiceMessageHandler handler)
    {
        try {
            channel = bootstrap.connect(endpoint.get()).sync().channel();
            channel.pipeline().addLast(handler);
            channel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    future.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (!bootstrap.group().isShuttingDown()) {
                                connect(bootstrap, endpoint, handler);
                            }
                        }
                    }, 2, TimeUnit.SECONDS);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CocaineException(e);
        }
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
