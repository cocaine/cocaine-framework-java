package cocaine;

import javax.inject.Inject;

import java.util.List;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class Services {

    private final List<CocaineSerializer> serializers;
    private final List<CocaineDeserializer> deserializers;
    private final Locator locator;

    @Inject
    public Services(List<CocaineSerializer> serializers, List<CocaineDeserializer> deserializers, Locator locator) {
        this.serializers = serializers;
        this.deserializers = deserializers;
        this.locator = locator;
    }
/*
    public <T> T service(Class<T> type) {
        Service service = locator.service(getServiceName(type));
    }

    private static <T> String getServiceName(Class<T> type) {
        CocaineService service = Preconditions.checkNotNull(type.getAnnotation(CocaineService.class),
                "Service interface must be annotated with @CocaineService annotation");
        return service.value();
    }

    private class CocaineMethodHandler implements MethodHandler {

        private final Service service;

        private CocaineMethodHandler(Service service) {
            this.service = service;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            CocaineMethod methodDescriptor = Preconditions.checkNotNull(thisMethod.getAnnotation(CocaineMethod.class),
                    "Service method must be annotated with @CocaineMethod annotation");
            String method = methodDescriptor.value();
            CocaineSerializer serializer =
                    Iterables.find(serializers, Predicates.instanceOf(methodDescriptor.serializer()));
            CocaineDeserializer<?> deserializer =
                    Iterables.find(deserializers, Predicates.instanceOf(methodDescriptor.deserializer()));

            Invokable<?, Object> invokable = Invokable.from(thisMethod);
            Parameter[] parameters = Iterables.toArray(invokable.getParameters(), Parameter.class);
            Object bytes = serializer.serialize(parameters, args);
            SyncServiceResponse invocationResult = service.invoke(method, bytes);

            TypeToken<?> returnType = invokable.getReturnType();

            deserializer.deserialize(, returnType);

            return invocationResult;
        }
    }
    */

}
