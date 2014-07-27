package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownMethodException extends ServiceException {

    public UnknownMethodException(String service, String method) {
        super(service, "Unknown service method: " + method);
    }

}
