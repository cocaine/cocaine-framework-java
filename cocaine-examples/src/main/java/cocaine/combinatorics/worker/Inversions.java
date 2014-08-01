package cocaine.combinatorics.worker;

import java.util.Arrays;

import cocaine.EventHandler;
import cocaine.combinatorics.utils.MessagePackUtils;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Inversions implements EventHandler {

    private static final Logger logger = Logger.getLogger(Inversions.class);

    private final MessagePack messagePack;

    public Inversions(MessagePack messagePack) {
        this.messagePack = messagePack;
    }

    @Override
    public void handle(Observable<byte[]> request, final Observer<byte[]> response) throws Exception {
        request.map(new Func1<byte[], int[]>() {
            @Override
            public int[] call(byte[] bytes) {
                return MessagePackUtils.read(messagePack, bytes, int[].class);
            }
        }).finallyDo(new Action0() {
            @Override
            public void call() {
                response.onCompleted();
            }
        }).subscribe(new Observer<int[]>() {
            @Override
            public void onCompleted() {
                logger.info("done");
            }

            @Override
            public void onError(Throwable e) {
                logger.error(e.getMessage(), e);
            }

            @Override
            public void onNext(int[] value) {
                response.onNext(MessagePackUtils.write(messagePack, inversions(value)));
            }
        });
    }

    private int inversions(int[] array) {
        if (array.length < 2) {
            return 0;
        }

        int m = (array.length + 1) / 2;
        int left[] = Arrays.copyOfRange(array, 0, m);
        int right[] = Arrays.copyOfRange(array, m, array.length);

        return inversions(left) + inversions(right) + merge(array, left, right);
    }

    private int merge(int[] array, int[] left, int[] right) {
        int i = 0;
        int j = 0;
        int count = 0;

        while (i < left.length || j < right.length) {
            if (i == left.length) {
                array[i + j] = right[j];
                j++;
            } else if (j == right.length) {
                array[i + j] = left[i];
                i++;
            } else if (left[i] <= right[j]) {
                array[i + j] = left[i];
                i++;
            } else {
                array[i + j] = right[j];
                count += left.length - i;
                j++;
            }
        }
        return count;
    }

}
