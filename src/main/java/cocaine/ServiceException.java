package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceException extends CocaineException {

    private final String serviceName;

    public ServiceException(String serviceName, String message) {
        super(serviceName + " - " + message);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

}
