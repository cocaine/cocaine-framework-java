package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Services {

    /*
    private static final Logger logger = Logger.getLogger(Services.class);
    private static final MessagePack pack = new MessagePack();

    public static <T> T createApp(Class<T> type, Locator locator, MessagePack pack) throws IOException {
        return createApp(type, getAppName(type), locator, pack);
    }

    public static <T> T createApp(Class<T> type, String name, Locator locator, MessagePack pack) throws IOException {
        Service service = locator.service(name);
        return create(type, new AppMethodHandler(service, pack));
    }

    public static <T> T createApp(Class<T> type, Locator locator) throws IOException {
        return createApp(type, getAppName(type), locator);
    }

    public static <T> T createApp(Class<T> type, String name, Locator locator) throws IOException {
        Service service = locator.service(name);
        return create(type, new AppMethodHandler(service, pack));
    }

    public static <T> T createService(Class<T> type, Locator locator, MessagePack pack) throws IOException {
        return createService(type, getServiceName(type), locator, pack);
    }

    public static <T> T createService(Class<T> type, String name, Locator locator, MessagePack pack) throws IOException {
        Service service = locator.service(name);
        return create(type, new ServiceMethodHandler(service, pack));
    }

    public static <T> T createService(Class<T> type, Locator locator) throws IOException {
        return createService(type, getServiceName(type), locator);
    }

    public static <T> T createService(Class<T> type, String name, Locator locator) throws IOException {
        Service service = locator.service(name);
        return create(type, new ServiceMethodHandler(service, pack));
    }

    @SuppressWarnings("unchecked")
    private static <T> T create(Class<T> type, MethodHandler handler) {
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

        private static final Map<Class<?>, Function<byte[], ?>> converters = Maps.newHashMap();

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
            SyncServiceSession session = service.invoke(name, getArgs(overridden, args));
            final Class<?> returnType = overridden.getReturnType();

            if (returnType.equals(void.class) || returnType.equals(Void.class)) {
                if (session.hasNext()) {
                    logger.warn("Data returned by service [" + session + "] is ignored");
                }
                return null;
            }

            Function<byte[], ?> unpacker = getConverter(overridden);
            if (Iterator.class.isAssignableFrom(returnType)) {
                return unpacker == null
                        ? Iterators.transform(session, new MsgPackUnpacker<>(pack, returnType))
                        : Iterators.transform(session, unpacker);
            } else {
                byte[] chunk = session.next();
                if (session.hasNext()) {
                    throw new IllegalStateException("Service [" + session + "] has returned more than one data chunks");
                }
                if (unpacker == null) {
                    return pack.read(chunk, returnType);
                } else {
                    return unpacker.apply(chunk);
                }
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

        private Function<byte[], ?> getConverter(Method method) throws IllegalAccessException, InstantiationException {
            CocaineUnpacker converter = method.getAnnotation(CocaineUnpacker.class);
            if (converter == null) {
                return null;
            }
            if (!converters.containsKey(converter.value())) {
                Function<byte[], ?> valueConverter = converter.value().newInstance();
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

        private final MsgPackPacker defaultPacker;

        public AppMethodHandler(Service service, MessagePack pack) {
            super(service, pack);
            defaultPacker = new MsgPackPacker(pack);
        }

        @Override
        protected String getMethod(Method method) {
            return "invoke";
        }

        @Override
        protected List<Object> getArgs(Method method, Object[] args) {
            CocainePacker packer = method.getAnnotation(CocainePacker.class);
            if (packer == null) {
                Invokable.from(method).getParameters()
                return defaultPacker.apply()
            }
            return packArgs(method, args);
        }

    }

    private static class MsgPackPacker implements Function<Map<Parameter, Object>, byte[]> {

        private final MessagePack pack;

        private MsgPackPacker(MessagePack pack) {
            this.pack = pack;
        }

        @Override
        public byte[] apply(Map<Parameter, Object> parameters) {
            try {
                if (parameters.size() == 1) {
                    Map.Entry<Parameter, Object> parameter = parameters.entrySet().iterator().next();
                    CocaineParameter param = parameter.getKey().getAnnotation(CocaineParameter.class);
                    if (param == null) {
                        return pack.write(parameter.getValue());
                    } else {
                        return pack.write(Collections.singletonMap(param.value(), parameter.getValue()));
                    }
                } else {
                    Map<String, Object> argsMap = Maps.newHashMap();
                    for (Map.Entry<Parameter, Object> parameter : parameters.entrySet()) {
                        CocaineParameter param = parameter.getKey().getAnnotation(CocaineParameter.class);
                        if (param == null) {
                            throw new IllegalStateException("All method parameters must be annotated with @CocaineParameter");
                        }
                        if (argsMap.containsKey(param.value())) {
                            throw new IllegalArgumentException("Duplicate parameter name: " + param.value());
                        }
                        argsMap.put(param.value(), parameter.getValue());
                    }
                    return pack.write(argsMap);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class MsgPackUnpacker<T> implements Function<byte[], T> {

        private final MessagePack pack;
        private final Class<T> type;

        public MsgPackUnpacker(MessagePack pack, Class<T> type) {
            this.pack = pack;
            this.type = type;
        }

        @Override
        public T apply(byte[] data) {
            try {
                return pack.read(data, type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    */

}
