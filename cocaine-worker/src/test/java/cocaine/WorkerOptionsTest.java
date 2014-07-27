package cocaine;

import java.util.UUID;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class WorkerOptionsTest {

    @Test
    public void parse() {
        WorkerOptions options = new WorkerOptions();
        new JCommander(options, "--app", "example", "--endpoint", "/tmp/cocaine.sock",
                "--uuid", "26e82218-161f-11e4-be84-1867b0bd9c14", "-d", "120000", "-h", "12000");

        Assert.assertEquals("example", options.getApplication());
        Assert.assertEquals("/tmp/cocaine.sock", options.getEndpoint());
        Assert.assertEquals(UUID.fromString("26e82218-161f-11e4-be84-1867b0bd9c14"), options.getUuid());
        Assert.assertEquals(120000, options.getDisownTimeout());
        Assert.assertEquals(12000, options.getHeartbeatTimeout());
    }

}
