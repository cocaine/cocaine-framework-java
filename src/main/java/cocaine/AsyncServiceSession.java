package cocaine;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class AsyncServiceSession implements AsyncServiceResponse, ServiceResponseHolder {

    private static final NoSuchElementException completedException =
            new NoSuchElementException("All response chunks have already been read");

    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final Queue<SettableFuture<byte[]>> outgoing = new ConcurrentLinkedQueue<>();
    private final Queue<SettableFuture<byte[]>> incoming = new ConcurrentLinkedQueue<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    private final String name;
    private final long session;

    private AsyncServiceSession(long session, String name) {
        Preconditions.checkNotNull(name, "Service name can not be null");

        this.name = name;
        this.session = session;
    }

    public static AsyncServiceSession create(long session, String service) {
        return new AsyncServiceSession(session, service);
    }

    @Override
    public String getServiceName() {
        return name;
    }

    @Override
    public long getSession() {
        return session;
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
                Thread.yield();
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
                Thread.yield();
                future = outgoing.poll();
            }
        } else {
            future = SettableFuture.create();
            incoming.offer(future);
        }
        future.set(data);
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<byte[]> next() {
        SettableFuture<byte[]> future;
        if (counter.getAndDecrement() > 0) {
            future = incoming.poll();
            while (future == null) {
                Thread.yield();
                future = incoming.poll();
            }
        } else {
            future = SettableFuture.create();
            Throwable throwable = exception.get();
            if (throwable == null) {
                outgoing.offer(future);
            } else {
                future.setException(throwable);
            }
        }
        return future;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
