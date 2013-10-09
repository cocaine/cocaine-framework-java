package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceInstantiationException extends CocaineException {

    private final Class<?> type;

    public ServiceInstantiationException(Class<?> type, String message) {
        super("Service [" + type.getName() + "] instantiation failed: " + message);
        this.type = type;
    }

    public ServiceInstantiationException(Class<?> type, Throwable throwable) {
        super("Service [" + type.getName() + "] instantiation failed: " + throwable.getMessage(), throwable);
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
