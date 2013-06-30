package cocaine.message;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MessageTypeTest {

    @Test
    public void fromValue() {
        MessageType result;

        result = MessageType.fromValue(0);
        Assert.assertEquals(MessageType.HANDSHAKE, result);

        result = MessageType.fromValue(1);
        Assert.assertEquals(MessageType.HEARTBEAT, result);

        result = MessageType.fromValue(2);
        Assert.assertEquals(MessageType.TERMINATE, result);

        result = MessageType.fromValue(3);
        Assert.assertEquals(MessageType.INVOKE, result);

        result = MessageType.fromValue(4);
        Assert.assertEquals(MessageType.CHUNK, result);

        result = MessageType.fromValue(5);
        Assert.assertEquals(MessageType.ERROR, result);

        result = MessageType.fromValue(6);
        Assert.assertEquals(MessageType.CHOKE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromValueOutOfRange() {
        MessageType.fromValue(7);
    }
}
