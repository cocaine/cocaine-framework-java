package cocaine;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import cocaine.message.Message;
import cocaine.msgpack.MessageTemplate;
import cocaine.msgpack.ServiceInfoTemplate;
import cocaine.netty.LocatorMessageHandler;
import cocaine.netty.MessageDecoder;
import cocaine.netty.MessageEncoder;
import cocaine.netty.MessagePackableEncoder;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.msgpack.MessagePack;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Locator implements AutoCloseable {

    private static final int SOCKET_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(4);
    private static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(4);

    private static final MessagePack pack = new MessagePack();

    static {
        pack.register(Message.class, MessageTemplate.getInstance());
    }

    private final NioEventLoopGroup eventLoop;
    private final Bootstrap bootstrap;

    public Locator() {
        this.eventLoop = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap()
                .group(eventLoop)

                .channel(NioSocketChannel.class)

                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_TIMEOUT, SOCKET_TIMEOUT)

                .handler(new ChannelInitializer<Channel>() {
                    public void initChannel(Channel channel) {
                        channel.pipeline()
                                .addLast("Message Decoder", new MessageDecoder(pack))
                                .addLast("Message Encoder", new MessageEncoder(pack))
                                .addLast("Message Packable Encoder", new MessagePackableEncoder(pack));
                    }
                });
    }

    @Override
    public void close() {
        eventLoop.shutdownGracefully();
    }

    public Service getService(String name, SocketAddress endpoint) {
        try {
            ServiceInfo info = resolve(name, endpoint);
            return new Service(info, bootstrap.connect(info.getEndpoint()).sync());
        } catch (InterruptedException e) {
            throw new LocatorResolveException(name, endpoint, e);
        }
    }

    public ServiceInfo resolve(String name, SocketAddress endpoint) {
        try {
            SettableFuture<byte[]> promise = SettableFuture.create();

            ChannelFuture connection = bootstrap.connect(endpoint).sync();
            connection.channel().pipeline().addLast("Message Handler", new LocatorMessageHandler(name, promise));
            connection.channel().write(new LocatorRequest(name)).sync();

            byte[] data = promise.get(10, TimeUnit.SECONDS);
            return pack.read(data, ServiceInfoTemplate.create(name));
        } catch (Exception e) {
            throw new LocatorResolveException(name, endpoint, e);
        }
    }

    private static class LocatorRequest implements MessagePackable {

        private final String service;

        private LocatorRequest(String service) {
            this.service = service;
        }

        /**
         * Message Format: [ 0, 1, [ "service" ]]
         */
        @Override
        public void writeTo(Packer packer) throws IOException {
            packer.writeArrayBegin(3);
            packer.write(0);
            packer.write(1);
            packer.writeArrayBegin(1);
            packer.write(service);
            packer.writeArrayEnd();
            packer.writeArrayEnd();
        }

        @Override
        public void readFrom(Unpacker unpacker) throws IOException {
            throw new UnsupportedOperationException("Reading LocatorRequest is not supported");
        }
    }

}
