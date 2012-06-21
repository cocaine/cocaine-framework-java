package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import ru.yandex.misc.test.Assert;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class DealerTest {

    private static final long TIMEOUT = 1000;
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final MessagePolicy policy = MessagePolicy.builder()
            .timeout(TIMEOUT / 3, TIME_UNIT).build();

    @Test
    public void testGood() throws TimeoutException {
        Dealer dealer = null;
        String testString = "hello_world";
        try {
            dealer = new Dealer("./src/test/resources/dealer_config.json");
            Response response = null;
            try {

                response = dealer.sendMessage("app1/test_handle",
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
            if (dealer != null) {
                dealer.close();
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void testBadConfig() {
        new Dealer("aaa");
    }

    @Test(expected = RuntimeException.class)
    public void testBadSendMessage() {
        Dealer c = new Dealer("./src/test/resources/dealer_config.json");
        c.sendMessage("a/b", new TextMessage("text"), MessagePolicy.builder()
                .build());
    }

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws TimeoutException {
        Dealer dealer = null;
        String testString = "hello_world";
        try {
            dealer = new Dealer("./src/test/resources/dealer_config.json");
            Response response = null;
            try {
                response = dealer.sendMessage("app1/test_handle_timeout",
                        new TextMessage(testString), policy);
                response.get(TIMEOUT, TIME_UNIT);
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        } finally {
            if (dealer != null) {
                dealer.close();
            }
        }
    }

}
