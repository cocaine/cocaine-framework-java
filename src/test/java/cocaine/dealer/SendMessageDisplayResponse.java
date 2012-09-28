package cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cocaine.dealer.util.Helper;

public class SendMessageDisplayResponse {
    public static void main(String[] args) throws TimeoutException {
        if (args.length!=2) {
            System.out.println("Usage: SendMessageDisplayResponse <path> <messageData>");
            System.exit(0);
        }
        MessagePolicy policy = MessagePolicy.builder()
               .persistent()
               .maxRetries(10)
               .build();
        String path = args[0];
        String messageData = args[1];
        Dealer dealer = null;
        try {
            dealer = Helper.createDealer();
            System.out.println("dealer created successfully");
            Response response = null;
            try { 
                response = dealer.sendMessage(path, messageData.getBytes(), policy);
                System.out.println("sent successfully ");
                String responseStr = new String(response.getAllChunks(1000, TimeUnit.HOURS));
                System.out.println("response: "+responseStr);
            } finally {
                if (response!=null) {
                    response.close();
                }
            }
        } finally {
            if (dealer!=null) {
                dealer.close();
            }
        }
    }
}
