package cocaine;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public interface AsyncServiceResponse extends ServiceResponse<ListenableFuture<byte[]>> {
}
