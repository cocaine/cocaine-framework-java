package cocaine;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class AsyncServiceSession implements AsyncServiceResponse, ServiceResponseHolder {

    private static enum State {

        RUNNING(new EmptyFutureSupplier()),
        COMPLETED(new CompletedFutureSupplier());

        private final Supplier<SettableFuture<byte[]>> futureSupplier;

        private State(Supplier<SettableFuture<byte[]>> futureSupplier) {
            this.futureSupplier = futureSupplier;
        }

        public SettableFuture<byte[]> newFuture() {
            return futureSupplier.get();
        }
    }

    private static final NoSuchElementException completedException =
            new NoSuchElementException("All response chunks have already been read");

    private final String name;
    private final long session;
    private final Queue<SettableFuture<byte[]>> outgoing;
    private final Queue<SettableFuture<byte[]>> incoming;
    private final ReentrantLock lock;
    private final AtomicReference<State> state;

    private AsyncServiceSession(String name, long session) {
        this.name = name;
        this.session = session;
        this.state = new AtomicReference<>(State.RUNNING);
        this.lock = new ReentrantLock();
        this.incoming = new LinkedList<>();
        this.outgoing = new LinkedList<>();
    }

    public static AsyncServiceSession create(long session, String service) {
        return new AsyncServiceSession(service, session);
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
    public void complete(RuntimeException exception) {
        if (state.compareAndSet(State.RUNNING, State.COMPLETED)) {
            lock.lock();
            try {
                for (SettableFuture<byte[]> future = outgoing.poll(); !outgoing.isEmpty(); future = outgoing.poll()) {
                    future.setException(exception);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void error(RuntimeException exception) {
        lock.lock();
        try {
            SettableFuture<byte[]> future = outgoing.poll();
            if (future != null) {
                future.setException(exception);
            } else {
                future = SettableFuture.create();
                future.setException(exception);
                incoming.add(future);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void push(byte[] data) {
        Preconditions.checkState(state.get() == State.RUNNING, "Pushing chunk to the closed session");

        lock.lock();
        try {
            SettableFuture<byte[]> future = outgoing.poll();
            if (future != null) {
                future.set(data);
            } else {
                future = state.get().newFuture();
                future.set(data);
                incoming.add(future);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<byte[]> next() {
        lock.lock();
        try {
            SettableFuture<byte[]> future = incoming.poll();
            if (future != null) {
                return future;
            }
            future = state.get().newFuture();
            if (state.get() == State.RUNNING) {
                outgoing.add(future);
            }
            return future;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private static class EmptyFutureSupplier implements Supplier<SettableFuture<byte[]>> {
        @Override
        public SettableFuture<byte[]> get() {
            return SettableFuture.create();
        }
    }

    private static class CompletedFutureSupplier implements Supplier<SettableFuture<byte[]>> {
        @Override
        public SettableFuture<byte[]> get() {
            SettableFuture<byte[]> result = SettableFuture.create();
            result.setException(completedException);
            return result;
        }

    }


}
