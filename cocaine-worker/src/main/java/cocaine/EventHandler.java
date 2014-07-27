package cocaine;

import rx.Observable;
import rx.Observer;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public interface EventHandler {

    void handle(Observable<byte[]> in, Observer<byte[]> out) throws Exception;

}
