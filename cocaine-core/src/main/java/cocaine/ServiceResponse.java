package cocaine;

import java.util.Iterator;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public interface ServiceResponse<T> extends Iterator<T> {

    long getSession();

    String getServiceName();

}
