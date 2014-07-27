package cocaine.msgpack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import cocaine.ServiceInfo;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceInfoTemplateTest {

    @Test
    public void read() throws IOException {
        String service = "locator";
        SocketAddress endpoint = new InetSocketAddress("localhost", 3456);
        long session = 0L;
        Map<Integer, String> api = Collections.singletonMap(0, "invoke");

        MessagePack pack = new MessagePack();
        pack.register(SocketAddress.class, SocketAddressTemplate.getInstance());
        byte[] bytes = pack.write(Arrays.asList(endpoint, session, api));

        ServiceInfo result = pack.read(bytes, ServiceInfoTemplate.create(service));

        Assert.assertEquals(endpoint, result.getEndpoint());
        Assert.assertEquals(0, result.getApi().getMethod("invoke"));
    }
}
