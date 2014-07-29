# Cocaine Client

## Usage Example

```java
package example;

import cocaine.Locator;
import cocaine.Service;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Example {

    private static final Logger logger = Logger.getLogger(Example.class);

    public static void main(String[] args) throws Exception {
        try (Locator locator = Locator.create()) {

            Service echo = locator.service("echo");
            Observable<byte[]> response = echo.invoke("enqueue", "invoke", "10".getBytes());

            Observable<String> strings = response.map(new Func1<byte[], String>() {
                @Override
                public String call(byte[] bytes) {
                    return new String(bytes);
                }
            }).doOnNext(new Action1<String>() {
                @Override
                public void call(String value) {
                    logger.info("Received: " + value);
                }
            });

            long max = strings.take(5).map(new Func1<String, Long>() {
                @Override
                public Long call(String value) {
                    return Long.parseLong(value);
                }
            }).reduce(new Func2<Long, Long, Long>() {
                @Override
                public Long call(Long max, Long current) {
                    return Math.max(max, current);
                }
            }).toBlocking().single();
            logger.info("Max: " + max);

            String aggregated = strings.skip(5).filter(new Func1<String, Boolean>() {
                @Override
                public Boolean call(String value) {
                    return value.length() < 2;
                }
            }).reduce("", new Func2<String, String, String>() {
                @Override
                public String call(String result, String current) {
                    return result + " " + current;
                }
            }).toBlocking().single().trim();
            logger.info("Aggregated: " + aggregated);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}

```
