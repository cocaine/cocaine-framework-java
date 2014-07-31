package cocaine.combinatorics.client;

import cocaine.Services;
import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
import cocaine.combinatorics.utils.MessagePackUtils;
import org.msgpack.MessagePack;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Combinatorics {

    private final MessagePack messagePack;
    private final Application application;

    public Combinatorics(Services services, MessagePack messagePack) {
        this.messagePack = messagePack;
        this.application = services.app(Application.class);
    }

    public int inversions(int[] array) {
        return application.inversions(array);
    }

    public <T> Observable<T[]> permutations(T[] array) {
        return application.permutations(array)
                .map(mapperFor(array));
    }

    public <T> Observable<T[]> sequences(T[] array) {
        return application.sequences(array)
                .map(mapperFor(array));
    }

    private <T> Func1<byte[], T[]> mapperFor(final T[] array) {
        return new Func1<byte[], T[]>() {
            @SuppressWarnings("unchecked")
            @Override
            public T[] call(byte[] bytes) {
                return (T[]) MessagePackUtils.read(messagePack, bytes, array.getClass());
            }
        };
    }

    @CocaineService("combinatorics")
    private interface Application extends AutoCloseable {

        @CocaineMethod(value = "permutations", raw = true)
        <T> Observable<byte[]> permutations(T[] array);

        @CocaineMethod(value = "sequences", raw = true)
        <T> Observable<byte[]> sequences(T[] array);

        @CocaineMethod("inversions")
        int inversions(int[] array);

        @Override
        void close();

    }

}
