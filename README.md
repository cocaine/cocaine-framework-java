Cocaine framework java
===============

Building
====================

Prerequisites
====================
To build cocaine-framework-java you'll need

* ant

generating artifacts
====================


cd to cocaine-framework-java directory
and run `ant`

Output
====================
in cocaine-framework-java

* ./target/jars/cocaine-framework-java.jar
* ./target/jars/cocaine-framework-java-sources.jar

Usage example
====================

```java
package example;

import java.util.Iterator;

import cocaine.AsyncServiceResponse;
import cocaine.Locator;
import cocaine.Service;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Example {

    private static final Logger logger = Logger.getLogger(Example.class);

    public static class MessagePackUnpacker<T> implements AsyncFunction<byte[], T> {

        private final MessagePack messagePack;
        private final Template<T> template;

        public MessagePackUnpacker(Template<T> template) {
            this.messagePack = new MessagePack();
            this.template = template;
        }

        @Override
        public ListenableFuture<T> apply(byte[] input) throws Exception {
            T result = input == null ? null : messagePack.read(input, template);
            return Futures.immediateFuture(result);
        }
    }

    public static class LoggerCallback<T> implements FutureCallback<T> {

        @Override
        public void onSuccess(T result) {
            logger.info(result);
        }

        @Override
        public void onFailure(Throwable throwable) {
            logger.error(throwable.getMessage(), throwable);
        }

    }

    public static class Unpack<F, T>
            implements Function<ListenableFuture<F>, ListenableFuture<T>>
    {

        private final AsyncFunction<F, T> transformer;

        public Unpack(AsyncFunction<F, T> transformer) {
            this.transformer = transformer;
        }

        @Override
        public ListenableFuture<T> apply(ListenableFuture<F> input) {
            return Futures.transform(input, transformer);
        }
    }

    public static class WithFallback<T>
            implements Function<ListenableFuture<T>, ListenableFuture<T>>
    {

        private final FutureFallback<T> fallback;

        public WithFallback(FutureFallback<T> fallback) {
            this.fallback = fallback;
        }

        @Override
        public ListenableFuture<T> apply(ListenableFuture<T> input) {
            return Futures.withFallback(input, fallback);
        }
    }

    public static class NullFallback<T> implements FutureFallback<T> {

        @Override
        public ListenableFuture<T> create(Throwable throwable) throws Exception {
            logger.error(throwable.getMessage(), throwable);
            return Futures.immediateFuture(null);
        }
    }

    public static void main(String[] args) {
        try (Locator locator = Locator.create()) {

            Service node = locator.service("echo");
            AsyncServiceResponse response = node.invokeAsync("echo");

            final Iterator<ListenableFuture<byte[]>> responseWithFallback = Iterators.transform(
                    response,
                    new WithFallback<>(new NullFallback<byte[]>())
            );

            final Iterator<ListenableFuture<String>> responseAsStrings = Iterators.transform(
                    responseWithFallback,
                    new Unpack<>(new MessagePackUnpacker<>(Templates.TString))
            );

            Futures.addCallback(responseAsStrings.next(), new FutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Futures.addCallback(responseAsStrings.next(), new LoggerCallback<String>());

                    Preconditions.checkNotNull(result, "First chunk is null");
                    logger.info(result);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.error(throwable.getMessage(), throwable);
                }
            });

        }
    }
}
```