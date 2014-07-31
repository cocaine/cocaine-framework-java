package cocaine.netty;

import cocaine.ServiceErrorException;
import cocaine.Sessions;
import cocaine.UnexpectedServiceMessageException;
import cocaine.message.ChunkMessage;
import cocaine.message.ErrorMessage;
import cocaine.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(ServiceMessageHandler.class);

    private final String service;
    private final Sessions sessions;

    public ServiceMessageHandler(String service, Sessions sessions) {
        this.service = service;
        this.sessions = sessions;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Handling message: " + msg);

        Message message = (Message) msg;
        long session = message.getSession();

        switch (message.getType()) {
            case CHUNK: {
                ChunkMessage chunk = (ChunkMessage) msg;
                sessions.onChunk(session, chunk.getData());
                break;
            }
            case CHOKE: {
                sessions.onCompleted(session);
                break;
            }
            case ERROR: {
                ErrorMessage error = (ErrorMessage) msg;
                sessions.onError(session, new ServiceErrorException(service, error.getMessage(), error.getCode()));
                break;
            }
            default: {
                sessions.onError(session, new UnexpectedServiceMessageException(service, message));
                break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessions.onCompleted();
    }
}
