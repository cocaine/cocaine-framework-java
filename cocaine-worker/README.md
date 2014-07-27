# Cocaine Worker

## Usage Example

```java
package example;

import cocaine.Handler;
import cocaine.Runner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class Example {

    private static final Logger logger = Logger.getLogger(Example.class);

    @Handler
    public void generate(final Observable<byte[]> in, final Observer<byte[]> out) throws Exception {
        in.map(new Func1<byte[], String>() {
            @Override
            public String call(byte[] bytes) {
                return new String(bytes);
            }
        }).finallyDo(new Action0() {
            @Override
            public void call() {
                out.onCompleted();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                logger.info("done");
            }

            @Override
            public void onError(Throwable e) {
                logger.error(e.getMessage(), e);
            }

            @Override
            public void onNext(String value) {
                for (int i = 0; i < value.length(); i++) {
                    out.onNext(value.substring(i).getBytes());
                }
            }
        });
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        Runner.run(new Example(), args);
    }

}

```
