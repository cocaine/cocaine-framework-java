package cocaine;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import cocaine.message.Message;
import cocaine.msgpack.MessageTemplate;
import cocaine.msgpack.ServiceInfoTemplate;
import cocaine.netty.MessageDecoder;
import cocaine.netty.MessageEncoder;
import cocaine.netty.MessagePackableEncoder;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.aio.AioEventLoopGroup;
import io.netty.channel.socket.aio.AioSocketChannel;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Locator implements AutoCloseable {

    private static final SocketAddress localhost = new InetSocketAddress("localhost", 10053);
    private static final Logger logger = Logger.getLogger(Locator.class);
    private static final MessagePack pack = new MessagePack();

    static {
        pack.register(Message.class, MessageTemplate.getInstance());
    }

    private final EventLoopGroup eventLoop;
    private final Bootstrap bootstrap;
    private final Service service;
    private final SocketAddress endpoint;

    private Locator(SocketAddress endpoint) {
        this.endpoint = endpoint;
        this.eventLoop = new AioEventLoopGroup();
        this.bootstrap = new Bootstrap()
                .group(eventLoop)

                .channel(AioSocketChannel.class)

                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(4))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)

                .handler(new ChannelInitializer<Channel>() {
                    public void initChannel(Channel channel) {
                        channel.pipeline()
                                .addLast("Message Decoder", new MessageDecoder(pack))
                                .addLast("Message Encoder", new MessageEncoder(pack))
                                .addLast("Message Packable Encoder", new MessagePackableEncoder(pack));
                    }
                });

        ServiceInfo locator = new ServiceInfo("locator", endpoint, ImmutableMap.of("resolve", 0));
        this.service = Service.create("locator", bootstrap, Suppliers.ofInstance(locator));
    }

    public static Locator create() {
        return create(localhost);
    }

    public static Locator create(SocketAddress endpoint) {
        logger.info("Creating locator " + endpoint);
        return new Locator(endpoint);
    }

    public Service service(String name) {
        logger.info("Creating service " + name);
        return Service.create(name, bootstrap, new ServiceInfoSupplier(name));
    }

    @Override
    public void close() {
        logger.info("Shutting down locator");
        eventLoop.shutdownGracefully();
    }

    private ServiceInfo resolve(String name) {
        logger.info("Resolving service info for " + name);
        try {
            SessionFuture result = service.invoke("resolve", name);
            return pack.read(result.next(), ServiceInfoTemplate.create(name));
        } catch (Exception e) {
            throw new LocatorResolveException(name, endpoint, e);
        }
    }

    private class ServiceInfoSupplier implements Supplier<ServiceInfo> {

        private final String name;

        private ServiceInfoSupplier(String name) {
            this.name = name;
        }

        @Override
        public ServiceInfo get() {
            return resolve(name);
        }
    }

}
