package ${package};

import java.net.InetSocketAddress;

import cocaine.Locator;
import cocaine.Service;
import org.apache.log4j.Logger;
import rx.functions.Func1;

public class Application {

    private static final Logger logger = Logger.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        try (Locator locator = Locator.create(new InetSocketAddress("localhost", 10053))) {

            Service echo = locator.service("echo");
            String result = echo.invoke("enqueue", "echo", "Hello!".getBytes())
                    .map(new Func1<byte[], String>() {
                        @Override
                        public String call(byte[] bytes) {
                            return new String(bytes);
                        }
                    }).toBlocking().single();
            logger.info(result);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
