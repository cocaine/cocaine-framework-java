package cocaine.dealer;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Once {
    private final static String CONFIG_PATH = "./src/test/resources/dealer_config.json";
    private final static String PATH = "python1/test_handle";
    
    public static void main(String[] args) throws TimeoutException {
        Dealer dealer = null;
        Response response = null;
        MessagePolicy policy = MessagePolicy.builder().build();
        TextMessage msg = new TextMessage("hi");
        ByteBuffer buffer = ByteBuffer.allocateDirect(1000);
        buffer.asCharBuffer().append("hi");
        try{
            dealer = new Dealer(CONFIG_PATH);
            response = dealer.sendMessage(PATH,msg, policy);
            String resp= response.getString(1000, TimeUnit.MILLISECONDS);
            System.out.println(resp);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (response!=null) {
                response.close();
            }
            if (dealer!=null)
                dealer.close();
        }
        
    }
    
}
