package cocaine.msgpack;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import cocaine.message.Message;
import cocaine.message.Messages;
import cocaine.message.TerminateMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MessageTemplateTest {

    private MessagePack pack;

    @Before
    public void setUp() {
        this.pack = new MessagePack();
        this.pack.register(SocketAddress.class, SocketAddressTemplate.getInstance());
        this.pack.register(UUID.class, UUIDTemplate.getInstance());
    }

    @Test
    public void writeHandshakeMessage() throws IOException {
        UUID uuid = UUID.randomUUID();
        Message msg = Messages.handshake(uuid);
        byte[] bytes = pack.write(Arrays.asList(0, 0, Arrays.asList(uuid)));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeHeartbeatMessage() throws IOException {
        Message msg = Messages.heartbeat();
        byte[] bytes = pack.write(Arrays.asList(1, 0, Arrays.asList()));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeTerminateMessage() throws IOException {
        String message = "PANIC!";
        Message msg = Messages.terminate(TerminateMessage.Reason.NORMAL, message);
        byte[] bytes = pack.write(Arrays.asList(2, 0, Arrays.asList(1, message)));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeInvokeMessage() throws IOException {
        long session = 1L;
        String method = "invoke";
        Message msg = Messages.invoke(session, method);
        byte[] bytes = pack.write(Arrays.asList(3, session, Arrays.asList(method)));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeChunkMessage() throws IOException {
        long session = 1L;
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        Message msg = Messages.chunk(session, data);
        byte[] bytes = pack.write(Arrays.asList(4, session, Collections.singletonList(data)));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeErrorMessage() throws IOException {
        long session = 1L;
        int code = -200;
        String message = "Failed!";
        Message msg = Messages.error(session, code, message);
        byte[] bytes = pack.write(Arrays.asList(5, session, Arrays.asList(code, message)));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeChokeMessage() throws IOException {
        long session = 1L;
        Message msg = Messages.choke(session);
        byte[] bytes = pack.write(Arrays.asList(6, session, Arrays.asList()));

        byte[] result = pack.write(msg, MessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void readHandshakeMessage() throws IOException {
        UUID uuid = UUID.randomUUID();
        Message msg = Messages.handshake(uuid);
        byte[] bytes = pack.write(Arrays.asList(0, 0, Arrays.asList(uuid)));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }

    @Test
    public void readHeartbeatMessage() throws IOException {
        Message msg = Messages.heartbeat();
        byte[] bytes = pack.write(Arrays.asList(1, 0, Arrays.asList()));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }

    @Test
    public void readTerminateMessage() throws IOException {
        String message = "PANIC!";
        Message msg = Messages.terminate(TerminateMessage.Reason.NORMAL, message);
        byte[] bytes = pack.write(Arrays.asList(2, 0, Arrays.asList(1, message)));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }

    @Test
    public void readInvokeMessage() throws IOException {
        long session = 1L;
        String method = "invoke";
        Message msg = Messages.invoke(session, method);
        byte[] bytes = pack.write(Arrays.asList(3, session, Arrays.asList(method)));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }

    @Test
    public void readChunkMessage() throws IOException {
        long session = 1L;
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        Message msg = Messages.chunk(session, data);
        byte[] bytes = pack.write(Arrays.asList(4, session, Collections.singletonList(data)));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }

    @Test
    public void readErrorMessage() throws IOException {
        long session = 1L;
        int code = -200;
        String message = "Failed!";
        Message msg = Messages.error(session, code, message);
        byte[] bytes = pack.write(Arrays.asList(5, session, Arrays.asList(code, message)));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }

    @Test
    public void readChokeMessage() throws IOException {
        long session = 1L;
        Message msg = Messages.choke(session);
        byte[] bytes = pack.write(Arrays.asList(6, session, Arrays.asList()));

        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(msg, result);
    }
}
