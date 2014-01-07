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
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Locator implements AutoCloseable {

    private static final SocketAddress localhost = new InetSocketAddress("localhost", 10053);
    private static final Logger logger = Logger.getLogger(Locator.class);

    private final SocketAddress endpoint;
    private final EventLoopGroup eventLoop;
    private final MessagePack pack;
    private final Bootstrap bootstrap;
    private final Service service;

    private Locator(SocketAddress endpoint) {
        this.endpoint = endpoint;
        this.eventLoop = new NioEventLoopGroup();
        this.pack = new MessagePack();
        this.pack.register(Message.class, MessageTemplate.getInstance());
        this.bootstrap = new Bootstrap()
                .group(eventLoop)

                .channel(NioSocketChannel.class)

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

        ServiceApi locator = ServiceApi.of("locator", "resolve", 0);
        this.service = Service.create("locator", bootstrap, Suppliers.ofInstance(endpoint), locator);
    }

    public static Locator create() {
        return create(localhost);
    }

    public static Locator create(SocketAddress endpoint) {
        logger.info("Creating locator " + endpoint);
        return new Locator(endpoint);
    }

    public Service service(final String name) {
        logger.info("Creating service " + name);
        return Service.create(name, bootstrap, new Supplier<SocketAddress>() {
            @Override
            public SocketAddress get() {
                return resolve(name).getEndpoint();
            }
        }, resolve(name).getApi());
    }

    @Override
    public void close() {
        logger.info("Shutting down locator");
        eventLoop.shutdownGracefully();
    }

    private ServiceInfo resolve(String name) {
        logger.info("Resolving service info for " + name);
        try {
            byte[] result = service.invoke("resolve", name).toBlockingObservable().single();
            return pack.read(result, ServiceInfoTemplate.create(name));
        } catch (Exception e) {
            throw new LocatorResolveException(name, endpoint, e);
        }
    }

}
