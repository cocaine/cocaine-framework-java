package cocaine;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceSession extends BaseServiceResponse<byte[]> implements ServiceResponseHolder {

    private static final Logger logger = Logger.getLogger(ServiceSession.class);

    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final Queue<SettableFuture<byte[]>> outgoing = new ConcurrentLinkedQueue<>();
    private final Queue<ListenableFuture<byte[]>> incoming = new ConcurrentLinkedQueue<>();
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
                Throwable throwable = exception.get();
                if (throwable != null) {
                    logger.warn("Setting error for closed or failed session: " + throwable.getLocalizedMessage());
                    return;
                }
                future = outgoing.poll();
            }
            future.setException(e);
        } else {
            incoming.offer(Futures.<byte[]>immediateFailedFuture(e));
        }
    }

    @Override
    public void push(byte[] data) {
        SettableFuture<byte[]> future;
        if (counter.getAndIncrement() < 0) {
            future = outgoing.poll();
            while (future == null) {
                Throwable throwable = exception.get();
                if (throwable != null) {
                    logger.warn("Pushing chunk to closed or failed session: " + throwable.getLocalizedMessage());
                    return;
                }
                future = outgoing.poll();
            }
            future.set(data);
        } else {
            incoming.offer(Futures.immediateFuture(data));
        }
    }

    @Override
    public ListenableFuture<byte[]> poll() {
        ListenableFuture<byte[]> future;
        if (counter.getAndDecrement() > 0) {
            future = incoming.poll();
            while (future == null) {
                future = incoming.poll();
            }
            return future;
        } else {
            Throwable throwable = exception.get();
            if (throwable != null) {
                return Futures.immediateFailedFuture(throwable);
            } else {
                SettableFuture<byte[]> settable = SettableFuture.create();
                outgoing.offer(settable);
                return settable;
            }
        }
    }

}
