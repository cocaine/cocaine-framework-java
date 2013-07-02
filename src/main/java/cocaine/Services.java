package cocaine;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cocaine.annotations.CocaineApp;
import cocaine.annotations.CocaineConverter;
import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineParameter;
import cocaine.annotations.CocaineService;
import cocaine.annotations.ValueConverter;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Services {

    private static final Logger logger = Logger.getLogger(Services.class);
    private static final SocketAddress localhost = new InetSocketAddress("localhost", 10053);
    private static final MessagePack pack = new MessagePack();

    private final Locator locator;

    public Services() {
        this.locator = new Locator();
    }

    public <T> T createApp(Class<T> type, SocketAddress endpoint, MessagePack pack) throws IOException {
        return createApp(type, getAppName(type), endpoint, pack);
    }

    public <T> T createApp(Class<T> type, MessagePack pack) throws IOException {
        return createApp(type, getAppName(type), pack);
    }

    public <T> T createApp(Class<T> type, String name, SocketAddress endpoint, MessagePack pack) throws IOException {
        Service service = locator.getService(name, endpoint);
        return create(type, new AppMethodHandler(service, pack));
    }

    public <T> T createApp(Class<T> type, String name, MessagePack pack) throws IOException {
        Service service = locator.getService(name, localhost);
        return create(type, new AppMethodHandler(service, pack));
    }

    public <T> T createApp(Class<T> type, SocketAddress endpoint) throws IOException {
        return createApp(type, getAppName(type), endpoint);
    }

    public <T> T createApp(Class<T> type) throws IOException {
        return createApp(type, getAppName(type));
    }

    public <T> T createApp(Class<T> type, String name, SocketAddress endpoint) throws IOException {
        Service service = locator.getService(name, endpoint);
        return create(type, new AppMethodHandler(service, pack));
    }

    public <T> T createApp(Class<T> type, String name) throws IOException {
        Service service = locator.getService(name, localhost);
        return create(type, new AppMethodHandler(service, pack));
    }

    public <T> T createService(Class<T> type, SocketAddress endpoint, MessagePack pack) throws IOException {
        return createService(type, getServiceName(type), endpoint, pack);
    }

    public <T> T createService(Class<T> type, MessagePack pack) throws IOException {
        return createService(type, getServiceName(type), pack);
    }

    public <T> T createService(Class<T> type, String name, SocketAddress endpoint, MessagePack pack) throws IOException {
        Service service = locator.getService(name, endpoint);
        return create(type, new ServiceMethodHandler(service, pack));
    }

    public <T> T createService(Class<T> type, String name, MessagePack pack) throws IOException {
        Service service = locator.getService(name, localhost);
        return create(type, new ServiceMethodHandler(service, pack));
    }

    public <T> T createService(Class<T> type, SocketAddress endpoint) throws IOException {
        return createService(type, getServiceName(type), endpoint);
    }

    public <T> T createService(Class<T> type) throws IOException {
        return createService(type, getServiceName(type));
    }

    public <T> T createService(Class<T> type, String name, SocketAddress endpoint) throws IOException {
        Service service = locator.getService(name, endpoint);
        return create(type, new ServiceMethodHandler(service, pack));
    }

    public <T> T createService(Class<T> type, String name) throws IOException {
        Service service = locator.getService(name, localhost);
        return create(type, new ServiceMethodHandler(service, pack));
    }

    @SuppressWarnings("unchecked")
    private <T> T create(Class<T> type, MethodHandler handler) {
        Preconditions.checkArgument(type.isInterface(), "Type must be interface");
        try {
            ProxyObject instance = createType(type).newInstance();
            instance.setHandler(handler);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServiceInstantiationException(type, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends ProxyObject> createType(Class<T> type) {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[] { type });
        return factory.createClass();
    }

    private static <T> String getServiceName(Class<T> type) {
        CocaineService cocaineService = type.getAnnotation(CocaineService.class);
        if (cocaineService == null) {
            throw new IllegalArgumentException("Type must be annotated with @cocaine.annotations.CocaineService");
        }
        return cocaineService.value();
    }

    private static <T> String getAppName(Class<T> type) {
        CocaineApp cocaineApp = type.getAnnotation(CocaineApp.class);
        if (cocaineApp == null) {
            throw new IllegalArgumentException("Type must be annotated with @cocaine.annotations.CocaineApp");
        }
        return cocaineApp.value();
    }

    private static String getMethodName(Method method) {
        CocaineMethod cocaineMethod = method.getAnnotation(CocaineMethod.class);
        return cocaineMethod != null ? cocaineMethod.value() : method.getName();
    }

    private static abstract class CocaineMethodHandler implements MethodHandler {

        private static final Map<Class<?>, ValueConverter<?>> converters = new HashMap<>();

        private final Service service;
        private final MessagePack pack;

        public CocaineMethodHandler(Service service, MessagePack pack) {
            this.service = service;
            this.pack = pack;
        }

        @Override
        public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Exception {
            if (isToString(overridden)) {
                return service.toString();
            }
            String name = getMethod(overridden);
            ServiceSession session = service.invoke(name, getArgs(overridden, args));
            Class<?> returnType = overridden.getReturnType();
            byte[] chunk = session.getNextChunk();

            if (returnType.equals(void.class) || returnType.equals(Void.class)) {
                if (!ServiceSession.isPoisonPill(chunk)) {
                    logger.warn("Data returned by service [" + session + "] is ignored");
                }
                return null;
            }
            if (ServiceSession.isPoisonPill(chunk) || !ServiceSession.isPoisonPill(session.getNextChunk())) {
                throw new IllegalStateException("Service [" + session + "] has returned more than one data chunks");
            }

            ValueConverter<?> converter = getConverter(overridden);
            if (converter == null) {
                return pack.read(chunk, returnType);
            } else {
                return converter.convert(pack.read(chunk));
            }
        }

        public MessagePack getPack() {
            return pack;
        }

        protected abstract String getMethod(Method method);
        protected abstract List<Object> getArgs(Method method, Object[] args) throws IOException;

        private static boolean isToString(Method method) {
            return method.getName().equals("toString") && method.getParameterTypes().length == 0;
        }

        private ValueConverter<?> getConverter(Method method) throws IllegalAccessException, InstantiationException {
            CocaineConverter converter = method.getAnnotation(CocaineConverter.class);
            if (converter == null) {
                return null;
            }
            if (!converters.containsKey(converter.value())) {
                ValueConverter<?> valueConverter = converter.value().newInstance();
                converters.put(converter.value(), valueConverter);
            }
            return converters.get(converter.value());
        }

    }

    private static class ServiceMethodHandler extends CocaineMethodHandler {

        public ServiceMethodHandler(Service service, MessagePack pack) {
            super(service, pack);
        }

        @Override
        protected String getMethod(Method method) {
            return getMethodName(method);
        }

        @Override
        protected List<Object> getArgs(Method method, Object[] args) {
            return Arrays.asList(args);
        }

    }

    private static class AppMethodHandler extends CocaineMethodHandler {

        private static final Map<Method, List<CocaineParameter>> methodParameters = new HashMap<>();

        public AppMethodHandler(Service service, MessagePack pack) {
            super(service, pack);
        }

        @Override
        protected String getMethod(Method method) {
            return "invoke";
        }

        @Override
        protected List<Object> getArgs(Method method, Object[] args) throws IOException {
            return packArgs(method, args);
        }

        private List<Object> packArgs(Method method, Object[] args) throws IOException {
            List<Object> result = new ArrayList<>(2);
            result.add(getMethodName(method));
            if (args.length == 0) {
                return result;
            }

            List<CocaineParameter> params = getCocaineParameters(method);

            if (args.length == 1) {
                CocaineParameter param = params.get(0);
                if (param == null) {
                    result.add(getPack().write(args[0]));
                } else {
                    result.add(getPack().write(Collections.singletonMap(param.value(), args[0])));
                }
            } else {
                HashMap<String, Object> argsMap = Maps.newHashMap();
                for (int i = 0; i < params.size(); i++) {
                    CocaineParameter param = params.get(i);
                    if (param == null) {
                        throw new IllegalStateException("All method parameters must be annotated with @CocaineParameter");
                    }
                    if (argsMap.containsKey(param.value())) {
                        throw new IllegalArgumentException("Duplicate parameter name: " + param.value());
                    }
                    argsMap.put(param.value(), args[i]);
                }
                result.add(getPack().write(argsMap));
            }
            return result;
        }

        private List<CocaineParameter> getCocaineParameters(Method method) {
            if (!methodParameters.containsKey(method)) {
                Invokable<?, Object> invokable = Invokable.from(method);
                List<CocaineParameter> params = Lists.transform(invokable.getParameters(), getCocaineParameterF());
                methodParameters.put(method, params);
            }

            return methodParameters.get(method);
        }

        private Function<Parameter, CocaineParameter> getCocaineParameterF() {
            return new Function<Parameter, CocaineParameter>() {
                public CocaineParameter apply(Parameter param) {
                    return param.getAnnotation(CocaineParameter.class);
                }
            };
        }

    }

}
