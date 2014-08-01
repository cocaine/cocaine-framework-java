package cocaine.combinatorics.worker;

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
public class Permutations implements EventHandler {

    private static final Logger logger = Logger.getLogger(Permutations.class);

    private final MessagePack messagePack;

    public Permutations(MessagePack messagePack) {
        this.messagePack = messagePack;
    }

    @Override
    public void handle(Observable<byte[]> request, final Observer<byte[]> response) throws Exception {
        request.map(new Func1<byte[], Value[]>() {
            @Override
            public Value[] call(byte[] bytes) {
                return MessagePackUtils.read(messagePack, bytes);
            }
        }).finallyDo(new Action0() {
            @Override
            public void call() {
                response.onCompleted();
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
                permutations(value, response);
            }
        });
    }

    private void permutations(Value[] array, Observer<byte[]> output) {
        int[] indexes = new int[array.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        while (true) {
            output.onNext(MessagePackUtils.write(messagePack, permute(array, indexes)));

            int k = indexes.length - 2;
            while (k >= 0 && indexes[k] > indexes[k + 1]) {
                k--;
            }

            if (k < 0) {
                break;
            }

            int t = k + 1;
            while (t < indexes.length - 1 && indexes[t + 1] > indexes[k]) {
                t++;
            }
            swap(indexes, k, t);
            for (int i = k + 1, j = indexes.length - 1; i < j; i++, j--) {
                swap(indexes, i, j);
            }
        }
    }

    private Value[] permute(Value[] array, int[] indexes) {
        Value[] result = new Value[array.length];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = array[indexes[i]];
        }
        return result;
    }

    private void swap(int[] indexes, int a, int b) {
        indexes[a] ^= indexes[b];
        indexes[b] ^= indexes[a];
        indexes[a] ^= indexes[b];
    }
}
