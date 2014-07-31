package cocaine;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.msgpack.MessagePack;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class Services {

    private static final Method close;

    static {
        try {
            close = AutoCloseable.class.getMethod("close");
        } catch (NoSuchMethodException e) {
            throw Throwables.propagate(e);
        }
    }

    private final List<CocaineSerializer> serializers;
    private final List<CocaineDeserializer> deserializers;
    private final Locator locator;

    @Inject
    public Services(List<CocaineSerializer> serializers, List<CocaineDeserializer> deserializers, Locator locator) {
        this.serializers = serializers;
        this.deserializers = deserializers;
        this.locator = locator;
    }

    public Services(Locator locator) {
        MessagePack pack = new MessagePack();
        this.serializers = ImmutableList.<CocaineSerializer>of(new MessagePackSerializer(pack));
        this.deserializers = ImmutableList.<CocaineDeserializer>of(new MessagePackDeserializer(pack));
        this.locator = locator;
    }

    public <T extends AutoCloseable> T service(Class<T> type) {
        Service service = locator.service(getServiceName(type));
        return create(type, new ServiceMethodHandler(service));
    }

    public <T extends AutoCloseable> T app(Class<T> type) {
        Service service = locator.service(getServiceName(type));
        return create(type, new AppServiceMethodHandler(service));
    }

    private static <T> String getServiceName(Class<T> type) {
        CocaineService service = Preconditions.checkNotNull(type.getAnnotation(CocaineService.class),
                "Service interface must be annotated with @CocaineService annotation");
        return service.value();
    }

    @SuppressWarnings("unchecked")
    private static <T extends AutoCloseable> T create(Class<T> type, MethodHandler handler) {
        Preconditions.checkArgument(type.isInterface(), "Service must be described with interface");
        try {
            ProxyObject instance = createType(type).newInstance();
            instance.setHandler(handler);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServiceInstantiationException(type, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AutoCloseable> Class<? extends ProxyObject> createType(Class<T> type) {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[] { type });
        return factory.createClass();
    }

    private abstract class CocaineMethodHandler implements MethodHandler {

        private final TypeToken<Observable<byte[]>> raw = new TypeToken<Observable<byte[]>>(){};
        private final Service service;

        protected CocaineMethodHandler(Service service) {
            this.service = service;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (isToString(thisMethod)) {
                return service.toString();
            } else if (thisMethod.equals(close)) {
                service.close();
                return null;
            }

            CocaineMethod methodDescriptor = Preconditions.checkNotNull(thisMethod.getAnnotation(CocaineMethod.class),
                    "Service method must be annotated with @CocaineMethod annotation");
            CocaineDeserializer deserializer =
                    Iterables.find(deserializers, Predicates.instanceOf(methodDescriptor.deserializer()));

            Invokable<?, Object> invokable = Invokable.from(thisMethod);
            Parameter[] parameters = Iterables.toArray(invokable.getParameters(), Parameter.class);

            String method = getMethod(thisMethod);
            List<Object> arguments = getArgs(thisMethod, parameters, args);

            Observable<byte[]> invocationResult = service.invoke(method, arguments);
            if (methodDescriptor.raw()) {
                Preconditions.checkState(raw.equals(TypeToken.of(invokable.getReturnType().getType())),
                        "Raw methods must return Observable<byte[]>");
                return invocationResult;
            }

            ResultInfo result = ResultInfo.fromType(invokable.getReturnType().getType());

            return result.isSingle()
                    ? deserializer.deserialize(invocationResult.toBlocking().single(), result.getValueType())
                    : invocationResult.map(new Transformer(deserializer, result.getValueType()));
        }

        protected abstract String getMethod(Method method);

        protected abstract List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) throws IOException;

    }

    private class ServiceMethodHandler extends CocaineMethodHandler {

        public ServiceMethodHandler(Service service) {
            super(service);
        }

        @Override
        protected String getMethod(Method method) {
            CocaineMethod methodDescriptor = Preconditions.checkNotNull(method.getAnnotation(CocaineMethod.class),
                    "Service method must be annotated with @CocaineMethod annotation");
            return methodDescriptor.value().isEmpty() ? method.getName() : methodDescriptor.value();
        }

        @Override
        protected List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) {
            return Arrays.asList(args);
        }
    }

    private class AppServiceMethodHandler extends CocaineMethodHandler {

        public AppServiceMethodHandler(Service service) {
            super(service);
        }

        @Override
        protected String getMethod(Method method) {
            return "enqueue";
        }

        @Override
        protected List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) throws IOException {
            CocaineMethod methodDescriptor = Preconditions.checkNotNull(method.getAnnotation(CocaineMethod.class),
                    "AppService method must be annotated with @CocaineMethod annotation");
            String name = methodDescriptor.value().isEmpty() ? method.getName() : methodDescriptor.value();
            CocaineSerializer serializer =
                    Iterables.find(serializers, Predicates.instanceOf(methodDescriptor.serializer()));
            return Arrays.<Object>asList(name, serializer.serialize(parameters, args));
        }
    }

    private static class ResultInfo {

        private final boolean single;
        private final Type valueType;

        private ResultInfo(boolean single, Type valueType) {
            this.single = single;
            this.valueType = valueType;
        }

        private boolean isSingle() {
            return single;
        }

        private Type getValueType() {
            return valueType;
        }

        public static ResultInfo fromType(Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) type;
                if (Observable.class.isAssignableFrom((Class<?>) parameterized.getRawType())) {
                    return new ResultInfo(false, parameterized.getActualTypeArguments()[0]);
                }
            }
            return new ResultInfo(true, type);
        }

    }

    private static class Transformer implements Func1<byte[], Object> {

        private final CocaineDeserializer deserializer;
        private final Type type;

        private Transformer(CocaineDeserializer deserializer, Type type) {
            this.deserializer = deserializer;
            this.type = type;
        }

        @Override
        public Object call(byte[] bytes) {
            try {
                return deserializer.deserialize(bytes, type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isToString(Method method) {
        return method.getName().equals("toString") && method.getParameterTypes().length == 0;
    }

}
