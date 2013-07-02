package cocaine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public interface ServiceResponse<T> {

    long getSession();

    String getServiceName();

    T next() throws ExecutionException, InterruptedException;

    T next(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    ListenableFuture<T> poll();

    ListenableFuture<List<T>> gather();

    ListenableFuture<List<T>> gather(Executor executor);

    ServiceResponse<T> take(int count);

    <V> ServiceResponse<V> map(Function<T, V> mapper);

    <V> ListenableFuture<V> then(Callback<T, V> callback);

    <V> ListenableFuture<V> then(Callback<T, V> callback, Executor executor);

    <V> ListenableFuture<V> then(AsyncCallback<T, V> callback);

    <V> ListenableFuture<V> then(AsyncCallback<T, V> callback, Executor executor);

    <V> ListenableFuture<V> reduce(V initial, ReduceFunction<V, T> function);

    <V> ListenableFuture<V> reduce(V initial, ReduceFunction<V, T> function, Executor executor);

}
