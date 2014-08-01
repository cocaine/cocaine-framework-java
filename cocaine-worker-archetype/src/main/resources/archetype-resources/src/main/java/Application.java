package ${package};

import java.util.Arrays;

import cocaine.Handler;
import cocaine.Runner;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;

public class Application {

    private static final Logger logger = Logger.getLogger(Application.class);

    @Handler("echo")
    public void echo(Observable<byte[]> request, final Observer<byte[]> response) {
        request.subscribe(new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                logger.info("onCompleted");
                response.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                logger.info("onError: " + e);
                response.onError(e);
            }

            @Override
            public void onNext(byte[] bytes) {
                logger.info("onNext: " + Arrays.toString(bytes));
                response.onNext(bytes);
            }
        });
    }

    public static void main(String[] args) {
        Runner.run(new Application(), args);
    }

}
