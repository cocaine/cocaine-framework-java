package cocaine;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Sessions {

    private static final Logger logger = Logger.getLogger(Sessions.class);

    private final AtomicLong counter;
    private final Map<Long, Session> sessions;
    private final String service;

    public Sessions(String service) {
        this.service = service;
        this.counter = new AtomicLong(0);
        this.sessions = new ConcurrentHashMap<>();
    }

    public Session create() {
        long id = counter.getAndIncrement();
        Subject<byte[], byte[]> subject = ReplaySubject.create();

        logger.debug("Creating new session: " + id);
        Session session = new Session(id, subject);
        sessions.put(id, session);
        return session;
    }

    public void onChunk(long id, byte[] chunk) {
        Session session = sessions.get(id);
        if (session != null) {
            logger.debug("Pushing new chunk " + Arrays.toString(chunk) + " to session " + id);
            session.input.onNext(chunk);
        } else {
            logger.warn("Session " + id + " does not exist");
        }
    }

    public void onCompleted(long id) {
        Session session = sessions.remove(id);
        if (session != null) {
            logger.debug("Closing session " + id);
            session.input.onCompleted();
        } else {
            logger.warn("Session " + id + " does not exist");
        }
    }

    public void onError(long id, ServiceException exception) {
        Session session = sessions.remove(id);
        if (session != null) {
            logger.debug("Setting error " + exception.getMessage() + " for session " + id);
            session.input.onError(exception);
        } else {
            logger.warn("Session " + id + " does not exist");
        }
    }

    public void onCompleted() {
        logger.debug("Closing all sessions of " + service);
        for (long session : sessions.keySet()) {
            onCompleted(session);
        }
    }

    public void onError(ServiceException exception) {
        logger.debug("Setting errors for all sessions of " + service);
        for (long session : sessions.keySet()) {
            onError(session, exception);
        }
    }

    public static final class Session {

        private final long id;
        private final Subject<byte[], byte[]> input;

        private Session(long id, Subject<byte[], byte[]> input) {
            this.id = id;
            this.input = input;
        }

        public long getId() {
            return id;
        }

        public Observable<byte[]> getInput() {
            return input;
        }
    }
}
