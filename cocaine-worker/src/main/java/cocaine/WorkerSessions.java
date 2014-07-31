package cocaine;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cocaine.message.ErrorMessage;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
final class WorkerSessions {

    private static final Logger logger = Logger.getLogger(WorkerSessions.class);

    private final Worker worker;
    private final Map<Long, Session> sessions;

    public WorkerSessions(Worker worker) {
        this.worker = worker;
        this.sessions = new ConcurrentHashMap<>();
    }

    public Session create(long id) {
        Subject<byte[], byte[]> input = ReplaySubject.create();
        Subject<byte[], byte[]> output = PublishSubject.create();
        output.subscribe(new Writer(id, worker));

        logger.debug("Creating new session: " + id);
        Session session = new Session(id, input, output);
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

    public void onError(long id, ClientException exception) {
        Session session = sessions.remove(id);
        if (session != null) {
            logger.debug("Setting error " + exception.getMessage() + " for session " + id);
            session.getOutput().onError(exception);
        } else {
            logger.warn("Session " + id + " does not exist");
        }
    }

    public void onCompleted() {
        logger.debug("Closing all sessions");
        for (long session : sessions.keySet()) {
            onCompleted(session);
        }
    }

    public void onError(ClientException exception) {
        logger.debug("Setting errors for all sessions");
        for (long session : sessions.keySet()) {
            onError(session, exception);
        }
    }

    public static final class Session {

        private final long id;
        private final Subject<byte[], byte[]> input;
        private final Subject<byte[], byte[]> output;

        private Session(long id, Subject<byte[], byte[]> input, Subject<byte[], byte[]> output) {
            this.id = id;
            this.input = input;
            this.output = output;
        }

        public long getId() {
            return id;
        }

        public Observable<byte[]> getInput() {
            return input;
        }

        public Observer<byte[]> getOutput() {
            return output;
        }
    }

    private static final class Writer implements Observer<byte[]> {

        private final long session;
        private final Worker worker;

        public Writer(long session, Worker worker) {
            this.session = session;
            this.worker = worker;
        }

        @Override
        public void onCompleted() {
            this.worker.sendChoke(session);
        }

        @Override
        public void onError(Throwable error) {
            this.worker.sendError(session, ErrorMessage.Code.EINVFAILED, error.getMessage());
        }

        @Override
        public void onNext(byte[] bytes) {
            this.worker.sendChunk(session, bytes);
        }
    }
}
