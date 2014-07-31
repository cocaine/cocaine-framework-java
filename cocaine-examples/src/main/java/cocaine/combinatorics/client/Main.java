package cocaine.combinatorics.client;

import java.net.InetSocketAddress;
import java.util.Arrays;

import cocaine.Locator;
import cocaine.Services;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import rx.functions.Action1;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        try (Locator locator = Locator.create(new InetSocketAddress("localhost", 10053))) {

            MessagePack messagePack = new MessagePack();
            Services services = new Services(locator, messagePack);
            Combinatorics combinatorics = new Combinatorics(services, messagePack);

            logger.info("Inversions: " + combinatorics.inversions(new int[]{4, 3, 2, 1}));
            combinatorics.permutations(new String[]{"a", "b", "c", "d"}).subscribe(new Action1<String[]>() {
                @Override
                public void call(String[] value) {
                    logger.info("Permutation: " + Arrays.toString(value));
                }
            });
            combinatorics.sequences(new Long[]{1L, 2L, 3L, 4L}).subscribe(new Action1<Long[]>() {
                @Override
                public void call(Long[] value) {
                    logger.info("Sequence: " + Arrays.toString(value));
                }
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
