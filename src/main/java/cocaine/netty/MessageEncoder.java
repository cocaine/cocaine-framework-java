package cocaine.netty;

import cocaine.message.Message;
import cocaine.msgpack.MessageTemplate;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final Logger logger = Logger.getLogger(MessageEncoder.class);

    private final MessagePack pack;

    public MessageEncoder(MessagePack pack) {
        super(Message.class);
        this.pack = pack;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        logger.debug("Encoding message: " + msg);
        out.writeBytes(pack.write(msg, MessageTemplate.getInstance()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.fireExceptionCaught(cause);
    }

}
