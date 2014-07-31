package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownServiceMethodException extends ServiceException {

    public UnknownServiceMethodException(String service, String method) {
        super(service, "Unknown service method: " + method);
    }

}
