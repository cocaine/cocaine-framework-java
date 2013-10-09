package cocaine;

import java.util.List;

import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
@CocaineService("node")
public interface Node {

    @CocaineMethod("info")
    NodeInfo info();

    @CocaineMethod("info")
    ServiceResponse<NodeInfo> asyncInfo();

    @CocaineMethod("start_app")
    void startApps(Runlist runlist);

    @CocaineMethod("pause_app")
    void pauseApps(List<String> apps);

}
