package cocaine.msgpack;

import java.io.IOException;
import java.util.UUID;

import cocaine.message.ChunkMessage;
import cocaine.message.ErrorMessage;
import cocaine.message.HandshakeMessage;
import cocaine.message.InvokeMessage;
import cocaine.message.Message;
import cocaine.message.MessageType;
import cocaine.message.Messages;
import cocaine.message.TerminateMessage;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class MessageTemplate extends AbstractTemplate<Message> {

    private static final Template<Message> instance = new MessageTemplate();

    private MessageTemplate() { }

    public static Template<Message> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, Message message, boolean required) throws IOException {
        packer.writeArrayBegin(3);
        packer.write(message.getType().value());
        packer.write(message.getSession());

        switch (message.getType()) {
            case HANDSHAKE: {
                HandshakeMessage handshakeMessage = (HandshakeMessage) message;
                packer.writeArrayBegin(1);
                UUIDTemplate.getInstance().write(packer, handshakeMessage.getId());
                packer.writeArrayEnd();
                break;
            }
            case TERMINATE: {
                TerminateMessage terminateMessage = (TerminateMessage) message;
                packer.writeArrayBegin(2);
                packer.write(terminateMessage.getReason().value());
                packer.write(terminateMessage.getMessage());
                packer.writeArrayEnd();
                break;
            }
            case INVOKE: {
                InvokeMessage invokeMessage = (InvokeMessage) message;
                packer.writeArrayBegin(1);
                packer.write(invokeMessage.getEvent());
                packer.writeArrayEnd();
                break;
            }
            case CHUNK: {
                ChunkMessage chunkMessage = (ChunkMessage) message;
                packer.writeArrayBegin(1);
                packer.write(chunkMessage.getData());
                packer.writeArrayEnd();
                break;
            }
            case ERROR: {
                ErrorMessage errorMessage = (ErrorMessage) message;
                packer.writeArrayBegin(2);
                packer.write(errorMessage.getCode());
                packer.write(errorMessage.getMessage());
                packer.writeArrayEnd();
                break;
            }
            case HEARTBEAT:
            case CHOKE: {
                packer.writeArrayBegin(0);
                packer.writeArrayEnd();
                break;
            }
        }

        packer.writeArrayEnd();
    }

    @Override
    public Message read(Unpacker unpacker, Message message, boolean required) throws IOException {
        Message result;

        unpacker.readArrayBegin();
        MessageType type = MessageType.fromValue(unpacker.readInt());
        long session = unpacker.readLong();

        unpacker.readArrayBegin();
        switch (type) {
            case HANDSHAKE: {
                UUID id = unpacker.read(UUIDTemplate.getInstance());
                result = Messages.handshake(id);
                break;
            }
            case HEARTBEAT: {
                result = Messages.heartbeat();
                break;
            }
            case TERMINATE: {
                TerminateMessage.Reason reason = TerminateMessage.Reason.fromValue(unpacker.readInt());
                String msg = unpacker.readString();
                result = Messages.terminate(reason, msg);
                break;
            }
            case INVOKE: {
                String event = unpacker.readString();
                result = Messages.invoke(session, event);
                break;
            }
            case CHUNK: {
                byte[] data = unpacker.readByteArray();
                result = Messages.chunk(session, data);
                break;
            }
            case ERROR: {
                int code = unpacker.readInt();
                String msg = unpacker.readString();
                result = Messages.error(session, code, msg);
                break;
            }
            case CHOKE: {
                result = Messages.choke(session);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown message type: " + type.name());
            }
        }
        unpacker.readArrayEnd();
        unpacker.readArrayEnd();

        return result;
    }

}
