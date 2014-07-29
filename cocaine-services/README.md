# Cocaine Services

## Usage Example

```java
package example;

import cocaine.Locator;
import cocaine.Services;
import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
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

    @CocaineService("echo")
    public static interface Echo {

        @CocaineMethod("invoke")
        Observable<String> invoke(long value);

    }

    public static void main(String[] args) throws Exception {
        try (Locator locator = Locator.create()) {

            Services services = new Services(locator);
            Echo echo = services.app(Echo.class);

            Observable<Long> response = echo.invoke(10L).map(new Func1<String, Long>() {
                @Override
                public Long call(String value) {
                    return Long.valueOf(value);
                }
            });

            long sum = response.doOnNext(new Action1<Long>() {
                @Override
                public void call(Long value) {
                    logger.info("Received: " + value);
                }
            }).reduce(0L, new Func2<Long, Long, Long>() {
                @Override
                public Long call(Long current, Long result) {
                    return result + current;
                }
            }).toBlocking().single();
            logger.info("Sum: " + sum);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}

```