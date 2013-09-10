package cocaine;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceSessionTest {

    @Ignore
    @Test
    public void gather() throws Exception {
        Executor executor = Executors.newFixedThreadPool(2);
        ServiceSession session = ServiceSession.create(0, "service");

        executor.execute(new Pusher(session));

        ListenableFuture<List<String>> result = session.map(new ToString()).gather(executor);

        System.out.println(Iterables.toString(result.get()));
    }

    @Ignore
    @Test
    public void reduceLeft() throws Exception {
        Executor executor = Executors.newFixedThreadPool(2);
        ServiceSession session = ServiceSession.create(0, "service");

        executor.execute(new Pusher(session));

        ListenableFuture<String> result = session.map(new ToString())
                .reduce("", ReduceFunctions.<String>stringAppender(), executor);

        System.out.println(result.get());
    }

    @Ignore
    @Test
    public void then() throws Exception {
        final Executor executor = Executors.newFixedThreadPool(2);
        ServiceSession session = ServiceSession.create(0, "service");

        executor.execute(new Pusher(session));

        ListenableFuture<Integer> result = session.map(new ToString()).then(new AsyncCallback<String, Integer>() {

            @Override
            public ListenableFuture<Integer> onSuccess(String value, ServiceResponse<String> response)
                    throws Exception {
                System.out.println(value);

                response.then(new Callback<String, Void>() {
                    @Override
                    public Void onSuccess(String value, ServiceResponse<String> response) throws Exception {
                        System.out.println(value);
                        return null;
                    }

                    @Override
                    public Void onFailure(Throwable throwable, ServiceResponse<String> response) throws Throwable {
                        System.err.println(throwable.getMessage());
                        return null;
                    }
                });

                return response.map(new Function<String, Integer>() {
                    @Override
                    public Integer apply(String value) {
                        return Integer.valueOf(value);
                    }
                }).reduce(0, ReduceFunctions.adder(), executor);
            }

            @Override
            public ListenableFuture<Integer> onFailure(Throwable throwable, ServiceResponse<String> response)
                    throws Throwable {
                if (throwable instanceof NoSuchElementException) {
                    return Futures.immediateFuture(0);
                }
                throw throwable;
            }

        }, executor);

        System.out.println(result.get());
    }

    @Ignore
    @Test
    public void take() throws Exception {
        final Executor executor = Executors.newFixedThreadPool(2);
        ServiceSession session = ServiceSession.create(0, "service");

        executor.execute(new Pusher(session));

        ServiceResponse<Integer> ints = session.map(new ToString()).map(new ParseInt());

        ListenableFuture<Integer> one = ints.take(10).reduce(0, ReduceFunctions.adder());
        ListenableFuture<Integer> two = ints.take(10).reduce(0, ReduceFunctions.adder());
        ListenableFuture<Integer> three = ints.take(10).reduce(0, ReduceFunctions.adder());
        ListenableFuture<Integer> four = ints.reduce(0, ReduceFunctions.adder());

        System.out.println(one.get());
        System.out.println(two.get());
        System.out.println(three.get());
        System.out.println(four.get());
    }

    private static class ParseInt implements Function<String, Integer> {
        @Override
        public Integer apply(String value) {
            return Integer.valueOf(value);
        }
    }

    private static class ToString implements Function<byte[], String> {
        @Override
        public String apply(byte[] bytes) {
            return new String(bytes, Charsets.UTF_8);
        }
    }

    private static class Pusher implements Runnable {

        private final ServiceResponseHolder holder;

        private Pusher(ServiceResponseHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            for (int i = 0; i < 15000; i++) {
                holder.push(Integer.toString(i).getBytes(Charsets.UTF_8));
            }
            holder.complete();
        }

    }
}
