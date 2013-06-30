package cocaine.msgpack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class SocketAddressTemplateTest {

    @Test
    public void write() throws IOException {
        String hostname = "localhost";
        int port = 3456;

        MessagePack pack = new MessagePack();
        byte[] bytes = pack.write(Arrays.asList(hostname, port));

        byte[] result = pack.write(new InetSocketAddress(hostname, port), SocketAddressTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeUnsupportedSocketAddress() throws IOException {
        MessagePack pack = new MessagePack();
        pack.write(new SocketAddress() { }, SocketAddressTemplate.getInstance());
    }

    @Test
    public void read() throws IOException {
        String host = "localhost";
        int port = 3456;

        MessagePack pack = new MessagePack();
        byte[] bytes = pack.write(Arrays.asList(host, port));

        SocketAddress result = pack.read(bytes, SocketAddressTemplate.getInstance());

        Assert.assertThat(result, new IsInstanceOf(InetSocketAddress.class));
        Assert.assertEquals(host, ((InetSocketAddress) result).getHostName());
        Assert.assertEquals(port, ((InetSocketAddress) result).getPort());
    }
}
