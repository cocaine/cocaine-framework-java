package cocaine.netty;

import java.io.EOFException;
import java.nio.ByteBuffer;

import cocaine.message.Message;
import cocaine.msgpack.MessageTemplate;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = Logger.getLogger(MessageDecoder.class);

    private final MessagePack pack;

    public MessageDecoder(MessagePack pack) {
        this.pack = pack;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageBuf<Object> out) throws Exception {
        logger.debug("Decoding message");
        in.markReaderIndex();

        int available = in.readableBytes();
        ByteBuffer buffer = ByteBuffer.allocate(available);
        logger.debug("Reading " + available + " bytes");
        in.readBytes(buffer);
        buffer.position(0);

        try {
            Message message = pack.read(buffer, MessageTemplate.getInstance());
            logger.debug("Message was successfully decoded: " + message);
            in.readerIndex(in.readerIndex() - buffer.remaining());
            out.add(message);
        } catch (EOFException e) {
            logger.debug("Not enough bytes. Resetting reader index");
            in.resetReaderIndex();
        }
    }

}
