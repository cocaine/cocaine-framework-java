package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownClientMethodException extends ClientException {

    public UnknownClientMethodException(String application, String method) {
        super(application, "Unknown application method: " + method);
    }

}
