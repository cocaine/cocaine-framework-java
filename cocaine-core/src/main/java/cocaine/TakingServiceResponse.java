package cocaine;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class TakingServiceResponse<T> extends BaseServiceResponse<T> {

    private static final NoSuchElementException completedException =
            new NoSuchElementException("All chunks have already been read");

    private final ListenableFuture<T> stopElement;
    private final Queue<ListenableFuture<T>> elements;

    public TakingServiceResponse(int count, ServiceResponse<T> response) {
        super(response.getServiceName(), response.getSession());
        this.elements = new ArrayDeque<>(count);
        for (int i = 0; i < count; i++) {
            elements.add(response.poll());
        }
        SettableFuture<T> future = SettableFuture.create();
        future.setException(completedException);
        this.stopElement = future;
    }

    @Override
    public ListenableFuture<T> poll() {
        ListenableFuture<T> result = elements.poll();
        return result == null ? stopElement : result;
    }
}
