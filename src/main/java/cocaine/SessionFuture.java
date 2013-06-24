package cocaine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class SessionFuture extends AbstractIterator<byte[]> {

    private static final byte[] POISON_PILL = new byte[0];

    private final AtomicInteger count = new AtomicInteger(0);
    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();
    private final ReentrantLock putLock = new ReentrantLock();
    private final AtomicReference<ServiceException> exception = new AtomicReference<>();

    private final String name;
    private final long id;

    private Chunk head;
    private Chunk last;

    private SessionFuture(long id, String name) {
        this.name = name;
        this.id = id;
        this.last = this.head = new Chunk();
    }

    public static SessionFuture create(long id, String name) {
        return new SessionFuture(id, name);
    }

    public long getId() {
        return id;
    }

    public void error(ServiceException t) {
        Preconditions.checkNotNull(exception, "Exception can not be null");

        if (exception.compareAndSet(null, t)) {
            signalNotEmpty();
        }
    }

    public void complete() {
        push(POISON_PILL);
    }

    public void push(byte[] data) {
        Preconditions.checkNotNull(data, "Chunk can not be null");

        int newSize;
        Chunk chunk = new Chunk(data);
        putLock.lock();
        try {
            enqueue(chunk);
            newSize = count.incrementAndGet();
        } finally {
            putLock.unlock();
        }
        if (newSize == 1) {
            signalNotEmpty();
        }
    }

    @Override
    protected byte[] computeNext() {
        try {
            byte[] data;
            takeLock.lockInterruptibly();
            try {
                while (count.get() == 0) {
                    notEmpty.await();
                }
                tryThrowException();

                data = dequeue();
                int newSize = count.decrementAndGet();
                if (newSize > 0) {
                    notEmpty.signal();
                }
            } finally {
                takeLock.unlock();
            }
            return isPoisonPill(data) ? endOfData() : data;
        } catch (InterruptedException e) {
            throw new ServiceException(name, e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Session " + name + "/" + id;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id == SessionFuture.class.cast(o).id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    private static boolean isPoisonPill(byte[] chunk) {
        return POISON_PILL == chunk;
    }

    private void tryThrowException() {
        ServiceException exception = this.exception.get();
        if (exception != null) {
            throw exception;
        }
    }

    private void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    private void enqueue(Chunk node) {
        last = last.next = node;
    }

    private byte[] dequeue() {
        Chunk h = head;
        Chunk first = h.next;
        h.next = h;
        head = first;
        byte[] data = first.data;
        first.data = null;
        return data;
    }

    private static class Chunk {

        private byte[] data;
        private Chunk next;

        public Chunk(byte[] data) {
            this.data = data;
        }

        public Chunk() {
            this(null);
        }

    }

}
