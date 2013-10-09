package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public interface Callback<K, L> {

    L onSuccess(K value, ServiceResponse<K> response) throws Exception;

    L onFailure(Throwable throwable, ServiceResponse<K> response) throws Throwable;

}
