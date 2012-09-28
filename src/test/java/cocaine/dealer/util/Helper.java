package cocaine.dealer.util;

import cocaine.dealer.Dealer;

public class Helper {
    private final static String CONFIG_PATH = "./src/test/resources/dealer_config.json";
    public static Dealer createDealer() {
        return new Dealer(CONFIG_PATH);
    }
}
