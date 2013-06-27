package cocaine;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Sessions {

    private static final Logger logger = Logger.getLogger(Sessions.class);

    private final AtomicLong counter;
    private final Map<Long, ServiceResponseHolder> sessions;
    private final String service;

    public Sessions(String service) {
        this.service = service;
        this.counter = new AtomicLong(1);
        this.sessions = new ConcurrentHashMap<>();
    }

    public AsyncServiceSession createAsync() {
        AsyncServiceSession session = AsyncServiceSession.create(counter.getAndIncrement(), service);
        logger.debug("Creating new asynchronous session: " + session);
        sessions.put(session.getSession(), session);
        return session;
    }

    public SyncServiceSession createSync() {
        SyncServiceSession session = SyncServiceSession.create(counter.getAndIncrement(), service);
        logger.debug("Creating new synchronous session: " + session);
        sessions.put(session.getSession(), session);
        return session;
    }

    public void pushChunk(long id, byte[] chunk) {
        ServiceResponseHolder session = sessions.get(id);
        if (session != null) {
            logger.debug("Pushing new chunk " + Arrays.toString(chunk) + " to " + session);
            session.push(chunk);
        } else {
            logger.warn("Attempt to push chunk to not opened session: " + id);
        }
    }

    public void close(long id) {
        ServiceResponseHolder session = sessions.get(id);
        if (session != null) {
            logger.debug("Closing " + session);
            session.complete();
        } else {
            logger.warn("Attempt to close not opened session: " + id);
        }
    }

    public void error(long id, ServiceException exception) {
        ServiceResponseHolder session = sessions.get(id);
        if (session != null) {
            logger.debug("Setting exception " + exception.getMessage() + " for " + session);
            session.error(exception);
        } else {
            logger.warn("Attempt to set exception for not opened session: " + id, exception);
        }
    }

    public void close() {
        logger.debug("Closing all sessions of " + service);
        for (ServiceResponseHolder session : sessions.values()) {
            session.complete();
        }
    }

    public void error(ServiceException exception) {
        logger.debug("Setting exception for all session of " + service);
        for (ServiceResponseHolder session : sessions.values()) {
            session.complete(exception);
        }
    }
}
