package cocaine;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cocaine.message.Message;
import cocaine.message.Messages;
import cocaine.msgpack.MessageTemplate;
import cocaine.netty.MessageDecoder;
import cocaine.netty.MessageEncoder;
import cocaine.netty.WorkerMessageHandler;
import com.beust.jcommander.JCommander;
import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Worker {

    private static final Logger logger = Logger.getLogger(Worker.class);
    private static final MessagePack pack = new MessagePack();

    static {
        pack.register(Message.class, MessageTemplate.getInstance());
    }

    private final WorkerOptions options;

    private final Timer heartbeat;
    private final Timer disown;

    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    private final UUID id;
    private final String app;

    private Worker(WorkerOptions options, final CocaineServlet servlet) throws Exception {
        logger.debug("Initializing worker: " + options);

        this.options = options;

        this.id = Preconditions.checkNotNull(options.id);
        this.app = Preconditions.checkNotNull(options.app);
        Preconditions.checkNotNull(options.endpoint);

        this.heartbeat = new Timer("Heartbeat Timer", true);
        this.disown = new Timer("Disown Timer", true);

        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)

                .option(ChannelOption.SO_TIMEOUT, (int) TimeUnit.SECONDS.toMillis(options.socketTimeout))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)

                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast("Message Encoder", new MessageEncoder(pack));
                        channel.pipeline().addLast("Message Decoder", new MessageDecoder(pack));
                        channel.pipeline().addLast("Message Handler", new WorkerMessageHandler(Worker.this, servlet));
                    }
                });
    }

    public void stopDisownTimer() {
        logger.info("Stopping disown timer");
        disown.cancel();
    }

    public static Worker create(String[] args, CocaineServlet servlet) throws Exception {
        WorkerOptions options = new WorkerOptions();
        JCommander commander = new JCommander(options);
        commander.parse(args);

        if (options.help) {
            commander.usage();
        }

        return new Worker(options, servlet);
    }

    public static void main(String[] args) throws Exception {
        System.err.println(Arrays.toString(args));
        logger.debug(args);
        Worker.create(args, new CocaineServlet()).start();
    }

    public void start() throws Exception {
        logger.info("Starting");

        logger.info("Connecting to  " + options.endpoint);
        ChannelFuture future = bootstrap.connect(options.endpoint).sync();
        logger.info("Sending handshake message");
        future.channel().write(Messages.handshake(id));
        logger.info("Sending heartbeat message");
        future.channel().write(Messages.heartbeat());

        heartbeat.scheduleAtFixedRate(new HeartbeatTask(future), 0L, TimeUnit.SECONDS.toMillis(options.heartbeatTimeout));
        disown.schedule(new DisownTask(), TimeUnit.SECONDS.toMillis(options.disownTimeout));
    }

    public void stop() {
        logger.info("Stopping");
        heartbeat.cancel();
        workerGroup.shutdownGracefully();
    }

    @Override
    public String toString() {
        return "Worker [" + app + "]: " + id + "; " + options;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id.equals(Worker.class.cast(o).id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private class HeartbeatTask extends TimerTask {

        private final ChannelFuture future;

        private HeartbeatTask(ChannelFuture future) {
            this.future = future;
        }

        @Override
        public void run() {
            logger.info("Sending heartbeat message");
            future.channel().write(Messages.heartbeat());
        }
    }

    private class DisownTask extends TimerTask {
        @Override
        public void run() {
            logger.error("Disowned");
            Worker.this.stop();
        }
    }
}
