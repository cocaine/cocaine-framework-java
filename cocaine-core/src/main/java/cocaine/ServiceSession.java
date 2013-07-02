package cocaine;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceSession extends BaseServiceResponse<byte[]> implements ServiceResponseHolder {

    private static final NoSuchElementException completedException =
            new NoSuchElementException("All chunks have already been read");

    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final Queue<SettableFuture<byte[]>> outgoing = new ConcurrentLinkedQueue<>();
    private final Queue<SettableFuture<byte[]>> incoming = new ConcurrentLinkedQueue<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    private ServiceSession(long session, String name) {
        super(name, session);
    }

    public static ServiceSession create(long session, String service) {
        return new ServiceSession(session, service);
    }

    @Override
    public void complete() {
        complete(completedException);
    }

    @Override
    public void complete(Exception e) {
        if (exception.compareAndSet(null, e)) {
            while (!outgoing.isEmpty()) {
                outgoing.poll().setException(e);
            }
        }
    }

    @Override
    public void error(Exception e) {
        SettableFuture<byte[]> future;
        if (counter.getAndIncrement() < 0) {
            future = outgoing.poll();
            while (future == null) {
                future = outgoing.poll();
            }
        } else {
            future = SettableFuture.create();
            incoming.offer(future);
        }
        future.setException(e);
    }

    @Override
    public void push(byte[] data) {
        SettableFuture<byte[]> future;
        if (counter.getAndIncrement() < 0) {
            future = outgoing.poll();
            while (future == null) {
                future = outgoing.poll();
            }
        } else {
            future = SettableFuture.create();
            incoming.offer(future);
        }
        future.set(data);
    }

    @Override
    public ListenableFuture<byte[]> poll() {
        SettableFuture<byte[]> future;
        if (counter.getAndDecrement() > 0) {
            future = incoming.poll();
            while (future == null) {
                future = incoming.poll();
            }
        } else {
            future = SettableFuture.create();
            Throwable throwable = exception.get();
            if (throwable != null) {
                future.setException(throwable);
            }
            outgoing.offer(future);
        }
        return future;
    }

}
