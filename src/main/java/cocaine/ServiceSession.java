package cocaine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class ServiceSession {

    private static final byte[] POISON_PILL = new byte[0];
    private static final AtomicLong counter = new AtomicLong(1);

    private final String name;
    private final long id;
    private final BlockingQueue<byte[]> chunks;
    private final AtomicReference<Exception> exception;

    private ServiceSession(String name) {
        this.name = name;
        this.id = counter.getAndIncrement();
        this.chunks = new LinkedBlockingQueue<>();
        this.exception = new AtomicReference<>();
    }

    public static ServiceSession create(String name) {
        return new ServiceSession(name);
    }

    public static boolean isPoisonPill(byte[] chunk) {
        return chunk.equals(POISON_PILL);
    }

    public long getId() {
        return id;
    }

    public void push(byte[] chunk) {
        this.chunks.add(chunk);
    }

    public byte[] getNextChunk() throws InterruptedException {
        return chunks.take();
    }

    public byte[] getNextChunk(long timeout, TimeUnit unit) throws InterruptedException {
        return chunks.poll(timeout, unit);
    }

    public boolean hasFailed() {
        return exception.get() != null;
    }

    public void close() {
        push(POISON_PILL);
    }

    public void setException(Exception exception) {
        this.exception.compareAndSet(null, exception);
    }

    public Exception getException() {
        return exception.get();
    }

    @Override
    public String toString() {
        return "Session " + name + "/" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceSession that = (ServiceSession) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
