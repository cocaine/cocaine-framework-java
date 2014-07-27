package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownMethodException extends ClientException {

    public UnknownMethodException(String application, String method) {
        super(application, "Unknown application method: " + method);
    }

}
