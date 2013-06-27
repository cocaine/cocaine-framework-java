package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public interface ServiceResponseHolder {

    void push(byte[] data);

    void complete();

    void complete(RuntimeException throwable);

    void error(RuntimeException throwable);

}
