package cocaine;

import rx.Observable;
import rx.Observer;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public interface EventHandler {

    void handle(Observable<byte[]> request, Observer<byte[]> response) throws Exception;

}
