package cocaine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MappingServiceResponse<T, V> implements ServiceResponse<V> {

    private final Function<T, V> mapper;
    private final ServiceResponse<T> delegate;

    public MappingServiceResponse(Function<T, V> mapper, ServiceResponse<T> delegate) {
        this.mapper = mapper;
        this.delegate = delegate;
    }

    @Override
    public long getSession() {
        return delegate.getSession();
    }

    @Override
    public String getServiceName() {
        return delegate.getServiceName();
    }

    @Override
    public V next() throws ExecutionException, InterruptedException {
        return mapper.apply(delegate.next());
    }

    @Override
    public V next(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return mapper.apply(delegate.next(timeout, unit));
    }

    @Override
    public ListenableFuture<List<V>> gather() {
        return Futures.transform(delegate.gather(), new Function<List<T>, List<V>>() {
            @Override
            public List<V> apply(List<T> input) {
                return Lists.transform(input, mapper);
            }
        });
    }

    @Override
    public ListenableFuture<List<V>> gather(Executor executor) {
        return Futures.transform(delegate.gather(executor), new Function<List<T>, List<V>>() {
            @Override
            public List<V> apply(List<T> input) {
                return Lists.transform(input, mapper);
            }
        });
    }

    @Override
    public <K> ListenableFuture<K> then(final Callback<V, K> callback) {
        return delegate.then(new Callback<T, K>() {
            @Override
            public K onSuccess(T value, ServiceResponse<T> response) throws Exception {
                return callback.onSuccess(mapper.apply(value), response.map(mapper));
            }

            @Override
            public K onFailure(Throwable throwable, ServiceResponse<T> response) throws Throwable {
                return callback.onFailure(throwable, response.map(mapper));
            }
        });
    }

    @Override
    public <K> ListenableFuture<K> then(final AsyncCallback<V, K> callback) {
        return delegate.then(new AsyncCallback<T, K>() {
            @Override
            public ListenableFuture<K> onSuccess(T value, ServiceResponse<T> response) throws Exception {
                return callback.onSuccess(mapper.apply(value), response.map(mapper));
            }

            @Override
            public ListenableFuture<K> onFailure(Throwable throwable, ServiceResponse<T> response) throws Throwable {
                return callback.onFailure(throwable, response.map(mapper));
            }
        });
    }

    @Override
    public <K> ListenableFuture<K> then(final Callback<V, K> callback, Executor executor) {
        return delegate.then(new Callback<T, K>() {
            @Override
            public K onSuccess(T value, ServiceResponse<T> response) throws Exception {
                return callback.onSuccess(mapper.apply(value), response.map(mapper));
            }

            @Override
            public K onFailure(Throwable throwable, ServiceResponse<T> response) throws Throwable {
                return callback.onFailure(throwable, response.map(mapper));
            }
        }, executor);
    }

    @Override
    public <K> ListenableFuture<K> then(final AsyncCallback<V, K> callback, Executor executor) {
        return delegate.then(new AsyncCallback<T, K>() {
            @Override
            public ListenableFuture<K> onSuccess(T value, ServiceResponse<T> response) throws Exception {
                return callback.onSuccess(mapper.apply(value), response.map(mapper));
            }

            @Override
            public ListenableFuture<K> onFailure(Throwable throwable, ServiceResponse<T> response) throws Throwable {
                return callback.onFailure(throwable, response.map(mapper));
            }
        }, executor);
    }

    @Override
    public <K> ServiceResponse<K> map(Function<V, K> mapper) {
        return new MappingServiceResponse<>(mapper, this);
    }

    @Override
    public ServiceResponse<V> take(int count) {
        return new TakingServiceResponse<>(count, this);
    }

    @Override
    public <L> ListenableFuture<L> reduce(L initial, final ReduceFunction<L, V> function) {
        return delegate.reduce(initial, new ReduceFunction<L, T>() {
            @Override
            public L apply(L accumulator, T value) {
                return function.apply(accumulator, mapper.apply(value));
            }
        });
    }

    @Override
    public <L> ListenableFuture<L> reduce(L initial, final ReduceFunction<L, V> function, Executor executor) {
        return delegate.reduce(initial, new ReduceFunction<L, T>() {
            @Override
            public L apply(L accumulator, T value) {
                return function.apply(accumulator, mapper.apply(value));
            }
        }, executor);
    }

    @Override
    public ListenableFuture<V> poll() {
        return Futures.transform(delegate.poll(), mapper);
    }

}
