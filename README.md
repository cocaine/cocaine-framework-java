Cocaine framework java
===============

Usage example
====================

```java
package example;

import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cocaine.AsyncCallback;
import cocaine.Callback;
import cocaine.Locator;
import cocaine.ReduceFunctions;
import cocaine.Service;
import cocaine.ServiceResponse;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Example {

    private static final Logger logger = Logger.getLogger(Example.class);

    public static void main(String[] args) throws Exception {
        final Executor executor = Executors.newFixedThreadPool(2);

        try (Locator locator = Locator.create()) {

            Service node = locator.service("echo");
            ServiceResponse<byte[]> response = node.invoke("invoke", 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

            ListenableFuture<Long> result = response.map(new Function<byte[], String>() {

                @Override
                public String apply(byte[] bytes) {
                    return new String(bytes, Charsets.UTF_8);
                }

            }).then(new AsyncCallback<String, Long>() {

                @Override
                public ListenableFuture<Long> onSuccess(String value, ServiceResponse<String> response)
                        throws Exception
                {
                    logger.info(value);

                    response.then(new Callback<String, Void>() {
                        @Override
                        public Void onSuccess(String value, ServiceResponse<String> response) throws Exception {
                            logger.info(value);
                            return null;
                        }

                        @Override
                        public Void onFailure(Throwable throwable, ServiceResponse<String> response) throws Throwable {
                            logger.error(throwable.getMessage());
                            return null;
                        }
                    });

                    return response.map(new Function<String, Long>() {
                        @Override
                        public Long apply(String value) {
                            return Long.valueOf(value);
                        }
                    }).reduce(0L, ReduceFunctions.adder(), executor);
                }

                @Override
                public ListenableFuture<Long> onFailure(Throwable throwable, ServiceResponse<String> response)
                        throws Throwable
                {
                    if (throwable instanceof NoSuchElementException) {
                        return Futures.immediateFuture(0L);
                    }
                    throw throwable;
                }

            }, executor);

            logger.info(result.get());

        }
    }

}
```