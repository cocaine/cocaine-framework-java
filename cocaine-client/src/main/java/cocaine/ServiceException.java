package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceException extends CocaineException {

    private final String service;

    public ServiceException(String service, String message) {
        super(service + " - " + message);
        this.service = service;
    }

    public String getService() {
        return service;
    }

}
