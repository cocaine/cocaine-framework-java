package cocaine.message;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class TerminateMessageReasonTest {

    @Test
    public void fromValue() {
        TerminateMessage.Reason result;

        result = TerminateMessage.Reason.fromValue(1);
        Assert.assertEquals(TerminateMessage.Reason.NORMAL, result);

        result = TerminateMessage.Reason.fromValue(2);
        Assert.assertEquals(TerminateMessage.Reason.ABNORMAL, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromValueOutOfRange() {
        TerminateMessage.Reason.fromValue(0);
    }
}
