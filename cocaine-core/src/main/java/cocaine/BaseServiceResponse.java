package cocaine;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public abstract class BaseServiceResponse<T> implements ServiceResponse<T> {

    private final String name;
    private final long session;

    protected BaseServiceResponse(String name, long session) {
        Preconditions.checkNotNull(name, "Service name can not be null");
        this.name = name;
        this.session = session;
    }

    @Override
    public long getSession() {
        return session;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public T next() throws ExecutionException, InterruptedException {
        return poll().get();
    }

    @Override
    public T next(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return poll().get(timeout, unit);
    }

    @Override
    public ListenableFuture<List<T>> gather() {
        return reduce(Lists.<T>newArrayList(), ReduceFunctions.<T>listAccumulator());
    }

    @Override
    public ListenableFuture<List<T>> gather(Executor executor) {
        return reduce(Lists.<T>newArrayList(), ReduceFunctions.<T>listAccumulator(), executor);
    }

    @Override
    public <V> ListenableFuture<V> then(Callback<T, V> callback) {
        return then(callback, MoreExecutors.sameThreadExecutor());
    }

    @Override
    public <V> ListenableFuture<V> then(AsyncCallback<T, V> callback) {
        return Futures.dereference(then((Callback<T, ListenableFuture<V>>) callback));
    }

    @Override
    public <V> ListenableFuture<V> then(Callback<T, V> callback, Executor executor) {
        SettableFuture<V> result = SettableFuture.create();
        Futures.addCallback(poll(), new ThenCallback<>(result, callback, this), executor);
        return result;
    }

    @Override
    public <V> ListenableFuture<V> then(AsyncCallback<T, V> callback, Executor executor) {
        return Futures.dereference(then((Callback<T, ListenableFuture<V>>) callback, executor));
    }

    @Override
    public <V> ServiceResponse<V> map(Function<T, V> mapper) {
        return new MappingServiceResponse<>(mapper, this);
    }

    @Override
    public ServiceResponse<T> take(int count) {
        return new TakingServiceResponse<>(count, this);
    }

    @Override
    public <V> ListenableFuture<V> reduce(V initial, ReduceFunction<V, T> function) {
        return reduce(initial, function, MoreExecutors.sameThreadExecutor());
    }

    @Override
    public <V> ListenableFuture<V> reduce(V initial, ReduceFunction<V, T> function, Executor executor) {
        SettableFuture<V> result = SettableFuture.create();
        Futures.addCallback(poll(), new ReduceCallback<>(result, initial, function, this, executor), executor);
        return result;
    }

    public abstract ListenableFuture<T> poll();

    private static class ReduceCallback<T, V> implements FutureCallback<T> {

        private final ReduceFunction<V, T> function;
        private final BaseServiceResponse<T> response;
        private final Executor executor;
        private final SettableFuture<V> result;

        private V accumulator;

        public ReduceCallback(SettableFuture<V> result, V initial, ReduceFunction<V ,T> function,
                BaseServiceResponse<T> response, Executor executor)
        {
            this.result = result;
            this.accumulator = initial;
            this.function = function;
            this.response = response;
            this.executor = executor;
        }

        @Override
        public void onSuccess(T value) {
            accumulator = function.apply(accumulator, value);
            Futures.addCallback(response.poll(), this, executor);
        }

        @Override
        public void onFailure(Throwable throwable) {
            if (throwable instanceof NoSuchElementException) {
                result.set(accumulator);
            } else {
                result.setException(throwable);
            }
        }
    }

    private static class ThenCallback<T, V> implements FutureCallback<T> {

        private final SettableFuture<V> future;
        private final Callback<T, V> callback;
        private final ServiceResponse<T> response;

        public ThenCallback(SettableFuture<V> future, Callback<T, V> callback,
                ServiceResponse<T> response)
        {
            this.future = future;
            this.callback = callback;
            this.response = response;
        }

        @Override
        public void onSuccess(T value) {
            try {
                future.set(callback.onSuccess(value, response));
            } catch (Throwable throwable) {
                future.setException(throwable);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            try {
                future.set(callback.onFailure(throwable, response));
            } catch (Throwable t) {
                future.setException(t);
            }
        }
    }
}
