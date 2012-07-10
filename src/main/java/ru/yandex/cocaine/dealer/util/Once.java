package ru.yandex.cocaine.dealer.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ru.yandex.cocaine.dealer.Dealer;
import ru.yandex.cocaine.dealer.MessagePolicy;
import ru.yandex.cocaine.dealer.Response;
import ru.yandex.cocaine.dealer.TextMessage;

public class Once {
    private final static String CONFIG_PATH = "./src/test/resources/dealer_config.json";
    private final static String PATH = "python1/test_handle";
    
    public static void main(String[] args) throws TimeoutException {
        Dealer dealer = null;
        Response response = null;
        MessagePolicy policy = MessagePolicy.builder().build();
        try{
            System.out.println("creating dealer");
            System.out.flush();
            dealer = new Dealer(CONFIG_PATH);
            System.out.println("created dealer");
            System.out.flush();
            response = dealer.sendMessage(PATH, new TextMessage("hi"), policy);
            System.out.println("send message");
            System.out.flush();
            String resp= response.get(1000, TimeUnit.MILLISECONDS);
            System.out.println(resp);
            System.out.flush();
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
