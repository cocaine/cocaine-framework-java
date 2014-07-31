package cocaine.combinatorics.worker;

import java.util.Arrays;

import cocaine.EventHandler;
import cocaine.combinatorics.utils.MessagePackUtils;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Sequences implements EventHandler {

    private static final Logger logger = Logger.getLogger(Sequences.class);

    private final MessagePack messagePack;

    public Sequences(MessagePack messagePack) {
        this.messagePack = messagePack;
    }

    @Override
    public void handle(Observable<byte[]> input, final Observer<byte[]> output) throws Exception {
        input.map(new Func1<byte[], Value[]>() {
            @Override
            public Value[] call(byte[] bytes) {
                return MessagePackUtils.read(messagePack, bytes);
            }
        }).finallyDo(new Action0() {
            @Override
            public void call() {
                output.onCompleted();
            }
        }).subscribe(new Observer<Value[]>() {
            @Override
            public void onCompleted() {
                logger.info("done");
            }

            @Override
            public void onError(Throwable e) {
                logger.error(e.getMessage(), e);
            }

            @Override
            public void onNext(Value[] value) {
                permutations(value, output);
            }
        });
    }

    private void permutations(Value[] array, Observer<byte[]> output) {
        int[] indexes = new int[array.length];
        Arrays.fill(indexes, 0);

        while (true) {
            output.onNext(MessagePackUtils.write(messagePack, create(array, indexes)));

            int k = indexes.length - 1;
            while (k >= 0 && indexes[k] == indexes.length - 1) {
                k--;
            }

            if (k < 0) {
                break;
            }

            indexes[k]++;
            for (int i = k + 1; i < indexes.length; i++) {
                indexes[i] = 0;
            }
        }
    }

    private Value[] create(Value[] array, int[] indexes) {
        Value[] result = new Value[array.length];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = array[indexes[i]];
        }
        return result;
    }

}
