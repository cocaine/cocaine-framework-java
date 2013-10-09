package cocaine.netty;

import java.io.IOException;

import cocaine.ConnectionHolder;
import cocaine.ServiceErrorException;
import cocaine.Sessions;
import cocaine.UnexpectedMessageException;
import cocaine.message.ChunkMessage;
import cocaine.message.ErrorMessage;
import cocaine.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceMessageHandler extends ChannelInboundMessageHandlerAdapter<Message> {

    private static final Logger logger = Logger.getLogger(ServiceMessageHandler.class);

    private final String service;
    private final ConnectionHolder connection;
    private final Sessions sessions;

    public ServiceMessageHandler(String service, ConnectionHolder connection, Sessions sessions) {
        super(Message.class);
        this.service = service;
        this.connection = connection;
        this.sessions = sessions;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Message msg) throws IOException {
        logger.info("Handling message: " + msg);
        long session = msg.getSession();

        switch (msg.getType()) {
            case CHUNK: {
                ChunkMessage chunk = (ChunkMessage) msg;
                sessions.pushChunk(session, chunk.getData());
                break;
            }
            case CHOKE: {
                sessions.close(session);
                break;
            }
            case ERROR: {
                ErrorMessage error = (ErrorMessage) msg;
                sessions.error(session, new ServiceErrorException(service, error.getMessage(), error.getCode()));
                break;
            }
            default: {
                sessions.error(session, new UnexpectedMessageException(service, msg));
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("Service channel is inactive");
        sessions.close();
        connection.reconnect();
    }
}
