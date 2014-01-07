package cocaine;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceSessionTest {

    @Ignore
    @Test
    public void gather() throws Exception {
        Executor executor = Executors.newFixedThreadPool(2);
        Subject<byte[], byte[]> subject = PublishSubject.create();

        executor.execute(new Pusher(subject));

        Observable<List<String>> result = subject.map(new ToString()).toList();

        System.out.println(Iterables.toString(result.toBlockingObservable().single()));
    }


    @Ignore
    @Test
    public void reduceLeft() throws Exception {
        Executor executor = Executors.newFixedThreadPool(2);
        Subject<byte[], byte[]> subject = ReplaySubject.create();

        executor.execute(new Pusher(subject));

        final StringBuilder builder = new StringBuilder();
        subject.map(new ToString()).take(10)
                .toBlockingObservable().forEach(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        builder.append(value).append("\n");
                    }
                });

        System.out.println(builder.toString());
    }

    @Ignore
    @Test
    public void then() throws Exception {
        final Executor executor = Executors.newFixedThreadPool(2);
        Subject<byte[], byte[]> subject = PublishSubject.create();

        executor.execute(new Pusher(subject));

        Observable<String> strings = subject.map(new ToString());

        strings.take(1).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                System.out.println("First completed");
            }

            @Override
            public void onError(Throwable throwable) {
                throw Throwables.propagate(throwable);
            }

            @Override
            public void onNext(String value) {
                System.out.println("Head: " + value);
            }
        });

        strings.skip(1).take(1).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                System.out.println("Second completed");
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println(throwable.getMessage());
            }

            @Override
            public void onNext(String value) {
                System.out.println(value);
            }
        });

        Observable<Long> result = strings.skip(2).map(new ParseLong()).reduce(0L, new Func2<Long, Long, Long>() {
            @Override
            public Long call(Long current, Long result) {
                return current + result;
            }
        });

        System.out.println(result.toBlockingObservable().single());
    }

    private static class ParseLong implements Func1<String, Long> {

        @Override
        public Long call(String value) {
            return Long.valueOf(value);
        }
    }

    private static class ToString implements Func1<byte[], String> {
        @Override
        public String call(byte[] bytes) {
            return new String(bytes, Charsets.UTF_8);
        }
    }

    private static class Pusher implements Runnable {

        private final Observer<byte[]> holder;

        private Pusher(Observer<byte[]> holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            for (int i = 0; i < 150000; i++) {
                holder.onNext(Integer.toString(i).getBytes(Charsets.UTF_8));
            }
            holder.onCompleted();
        }

    }
}
