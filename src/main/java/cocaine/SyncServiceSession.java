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
public class SyncServiceSession extends AbstractIterator<byte[]> implements SyncServiceResponse, ServiceResponseHolder {

    private static final byte[] POISON_PILL = new byte[0];

    private final AtomicInteger count = new AtomicInteger(0);
    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();
    private final ReentrantLock putLock = new ReentrantLock();
    private final AtomicReference<Exception> exception = new AtomicReference<>();

    private final String name;
    private final long session;

    private Chunk head;
    private Chunk last;

    private SyncServiceSession(long session, String name) {
        Preconditions.checkNotNull(name, "Service name can not be null");

        this.name = name;
        this.session = session;
        this.head = new Chunk();
        this.last = this.head;
    }

    public static SyncServiceSession create(long id, String name) {
        return new SyncServiceSession(id, name);
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
    public void error(Exception e) {
        Preconditions.checkNotNull(exception, "Exception can not be null");

        if (exception.compareAndSet(null, e)) {
            signalNotEmpty();
        }
    }

    @Override
    public void complete() {
        push(POISON_PILL);
    }

    @Override
    public void complete(Exception e) {
        complete();
    }

    @Override
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
        return "Session " + name + "/" + session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SyncServiceSession that = (SyncServiceSession) o;
        return session == that.session && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (session ^ (session >>> 32));
        return result;
    }

    private static boolean isPoisonPill(byte[] chunk) {
        return POISON_PILL == chunk;
    }

    private void tryThrowException() {
        Throwable e = exception.get();
        if (e != null) {
            throw new RuntimeException(e);
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
        last.next = node;
        last = last.next;
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
