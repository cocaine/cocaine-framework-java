package cocaine;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class ServiceSessions {

    private static final Logger logger = Logger.getLogger(ServiceSessions.class);

    private final Map<Long, ServiceSession> sessions;

    public ServiceSessions() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public ServiceSession create(String service) {
        ServiceSession session = ServiceSession.create(service);
        logger.debug("Creating new session: " + session);
        sessions.put(session.getId(), session);
        return session;
    }

    public void pushChunk(long id, byte[] chunk) {
        ServiceSession session = sessions.get(id);
        if (session != null) {
            logger.debug("Pushing new chunk " + Arrays.toString(chunk) + " to " + session);
            session.push(chunk);
        } else {
            logger.warn("Attempt to push chunk to not opened session: " + id);
        }
    }

    public void close(long id) {
        ServiceSession session = sessions.get(id);
        if (session != null) {
            logger.debug("Closing " + session);
            session.close();
        } else {
            logger.warn("Attempt to close not opened session: " + id);
        }
    }

    public void setException(long id, Exception exception) {
        ServiceSession session = sessions.get(id);
        if (session != null) {
            logger.debug("Setting exception " + exception.getMessage() + " for " + session);
            session.setException(exception);
        } else {
            logger.warn("Attempt to set exception for not opened session: " + id, exception);
        }
    }
}
