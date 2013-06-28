package cocaine.msgpack;

import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UUIDTemplateTest {

    @Test
    public void write() throws IOException {
        UUID uuid = UUID.randomUUID();

        MessagePack pack = new MessagePack();
        byte[] bytes = pack.write(uuid.toString());

        byte[] result = pack.write(uuid, UUIDTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void read() throws IOException {
        UUID uuid = UUID.randomUUID();

        MessagePack pack = new MessagePack();
        byte[] bytes = pack.write(uuid.toString());

        UUID result = pack.read(bytes, UUIDTemplate.getInstance());

        Assert.assertEquals(uuid, result);
    }
}
