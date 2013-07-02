package cocaine;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cocaine.annotations.CocaineApp;
import cocaine.annotations.CocaineConverter;
import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineParameter;
import cocaine.annotations.ValueConverter;
import cocaine.services.NodeInfo;
import cocaine.services.NodeInfoTemplate;
import org.msgpack.MessagePack;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        SocketAddress endpoint = new InetSocketAddress("startrekdev-cocaine03.tools.public.infranimbula.yandex.net", 10053);
        Services services = new Services();

        OAuthService service = services.createApp(OAuthService.class, endpoint);
        String token = service.getToken("07270ebaf797471fa2e6bf1be5925e8a", 1120000000012322L, "77.88.18.248", "fd9f477e76e74e6ab82a7acaa4de7b32");
/*
        Map<String, Object> params = Maps.newHashMap();
        params.put("oauth_token", "07270ebaf797471fa2e6bf1be5925e8a");
        params.put("uid", 1120000000012322L);
        params.put("local_ip", "77.88.18.248");
        params.put("application_id", "fd9f477e76e74e6ab82a7acaa4de7b32");
        String json = service.getToken(params);

        JavaApp java_app = Services.createApp(JavaApp.class, endpoint);
        java_app.echoString("foo");

        EchoApp echo = services.createApp(EchoApp.class, endpoint);
        String value = echo.echoString("TEXT");
        */
    }

    @CocaineApp("yandex-oauth-token-generator-app")
    public static interface OAuthService {

        @CocaineMethod("generate")
        @CocaineConverter(TokenConverter.class)
        String getToken(
                @CocaineParameter("oauth_token") String token,
                @CocaineParameter("uid") long uid,
                @CocaineParameter("local_ip") String ip,
                @CocaineParameter("application_id") String appId
        );
    }

    public static class TokenConverter implements ValueConverter<String> {

        @Override
        public String convert(Value value) throws Exception {
            Map<String, Value> map = new Converter(value).read(Templates.tMap(Templates.TString, Templates.TValue));
            if (map.containsKey("error")) {
                throw new RuntimeException(map.get("error").asRawValue().getString());
            }
            return map.get("token").asRawValue().getString();
        }

    }

    @CocaineApp("java_app")
    public static interface JavaApp {

        @CocaineMethod("invokeMethod")
        String echoString(String value);
    }

    @CocaineApp("echo")
    public static interface EchoApp {

        @CocaineMethod("echo")
        String echoString(String value);
    }

    public static void main1(String[] args) throws Exception {
        final MessagePack pack = new MessagePack();

        SocketAddress endpoint = new InetSocketAddress("startrekdev-cocaine03.tools.public.infranimbula.yandex.net", 10053);

        try (Locator locator = new Locator()) {
            /*Service echo = locator.getService("echo", endpoint);
            ServiceSession response = echo.invoke("invoke", "echo", "Hello World!");

            while (!response.hasMoreChunks());

            */
            final Service node = locator.getService("node", endpoint);

            class Task implements Callable<Void> {

                @Override
                public Void call() throws Exception {
                    NodeInfo info;
                    for (int i = 0; i < 3; i++) {
                        info = pack.read(node.invoke("info", Arrays.asList()).getNextChunk(), NodeInfoTemplate.getInstance());
                        System.out.println(info);
                    }
                    return null;
                }
            }

            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                executor.submit(new Task());
            }

            TimeUnit.SECONDS.sleep(5);
        }
    }

}