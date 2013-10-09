package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownMethodException extends ServiceException {

    public UnknownMethodException(String serviceName, String method) {
        super(serviceName, "Unknown service method: " + method);
    }

}
