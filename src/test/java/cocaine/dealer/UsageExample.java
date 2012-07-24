package cocaine.dealer;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class UsageExample {
    public static void main(String[] args) {

        String dealerConfig = "./src/test/resoueces/dealer_config.json";
        String appHandlePath = "myapp/myhandle";
        Dealer dealer = null;
        byte[] message = "helloworld".getBytes();
        MessagePolicy messagePolicy = MessagePolicy.builder()
                .maxRetries(10)
                .sendToAllHosts()
                .timeout(10, TimeUnit.SECONDS)
                .deadline(10, TimeUnit.DAYS)
                .build();
        try {
            dealer = new Dealer(dealerConfig);
            Response response = null;
            try {
                response = dealer.sendMessage(appHandlePath, message,
                        messagePolicy);
                for (byte[] chunk : response.asIterable(10, TimeUnit.SECONDS)) {
                    System.out.println(new String(chunk));
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } finally {
            if (dealer != null)
                dealer.close();
        }
    }
}
