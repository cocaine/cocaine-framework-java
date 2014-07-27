# Cocaine Client

## Usage Example

```java
package example;

import java.io.IOException;
import java.util.Comparator;

import cocaine.Locator;
import cocaine.Service;
import com.google.common.base.Throwables;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Example {

    private static final Logger logger = Logger.getLogger(Example.class);

    public static void main(String[] args) throws Exception {
        try (Locator locator = Locator.create()) {

            Service echo = locator.service("echo");
            Observable<byte[]> response = echo.invoke("enqueue", "invoke", pack(10L));

            Observable<String> strings = response.map(new Func1<byte[], String>() {
                @Override
                public String call(byte[] bytes) {
                    return unpack(bytes);
                }
            }).doOnEach(new Action1<String>() {
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
            }).max(new Comparator<Long>() {
                @Override
                public int compare(Long first, Long second) {
                    return first.compareTo(second);
                }
            }).toBlockingObservable().single();
            logger.info("Max: " + max);

            String aggregated = strings.skip(5).filter(new Func1<String, Boolean>() {
                @Override
                public Boolean call(String value) {
                    return value.length() < 2;
                }
            }).aggregate("", new Func2<String, String, String>() {
                @Override
                public String call(String result, String current) {
                    return result + " " + current;
                }
            }).toBlockingObservable().single().trim();
            logger.info("Aggregated: " + aggregated);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static byte[] pack(long value) {
        try {
            return MessagePack.pack(value);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    public static String unpack(byte[] bytes) {
        try {
            return MessagePack.unpack(bytes, String.class);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

}

```
