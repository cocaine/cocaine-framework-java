package cocaine.netty;

import java.io.IOException;

import cocaine.ServiceErrorException;
import cocaine.UnexpectedMessageException;
import cocaine.message.ChunkMessage;
import cocaine.message.ErrorMessage;
import cocaine.message.Message;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class LocatorMessageHandler extends ChannelInboundMessageHandlerAdapter<Message> {

    private static final Logger logger = Logger.getLogger(LocatorMessageHandler.class);

    private final String service;
    private final SettableFuture<byte[]> promise;

    public LocatorMessageHandler(String service, SettableFuture<byte[]> promise) {
        super(Message.class);
        this.service = service;
        this.promise = promise;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Message msg) throws IOException {
        logger.info("Handling message: " + msg);
        switch (msg.getType()) {
            case CHUNK:
                ChunkMessage chunk = ChunkMessage.class.cast(msg);
                promise.set(chunk.getData());
                break;
            case CHOKE:
                if (!promise.isDone()) {
                    promise.cancel(false);
                }
                break;
            case ERROR:
                ErrorMessage error = ErrorMessage.class.cast(msg);
                promise.setException(new ServiceErrorException(service, error.getMessage(), error.getCode()));
                break;
            default:
                promise.setException(new UnexpectedMessageException(service, msg));
                break;
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        promise.setException(cause);
        ctx.close();
    }
}
