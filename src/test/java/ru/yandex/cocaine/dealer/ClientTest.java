package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import ru.yandex.misc.test.Assert;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class ClientTest {

    private static final long TIMEOUT = 1000;
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final MessagePolicy policy = MessagePolicy.builder()
            .timeout(TIMEOUT / 3, TIME_UNIT).build();

    @Test
    public void testGood() throws TimeoutException {
        Client client = null;
        String testString = "hello_world";
        try {
            client = new Client("./src/test/resources/dealer_config.json");
            Response response = null;
            try {

                response = client.sendMessage("app1/test_handle",
                        new TextMessage(testString), policy);
                String responseStr = response.get(TIMEOUT, TIME_UNIT);
                Assert.assertContains(responseStr, testString);
                response.close();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws TimeoutException {
        Client client = null;
        String testString = "hello_world";
        try {
            client = new Client("./src/test/resources/dealer_config.json");
            Response response = null;
            try {
                response = client.sendMessage("app1/test_handle_timeout",
                        new TextMessage(testString), policy);
                response.get(TIMEOUT, TIME_UNIT);
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}
