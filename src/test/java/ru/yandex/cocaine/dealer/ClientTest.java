package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import ru.yandex.misc.test.Assert;

public class ClientTest {
	
	
	
	@Test
	public void testGood() throws TimeoutException{
		Client client = null;
		String testString = "hello_world";
		try{
			 client = new Client("./src/test/resources/config_example.json");
			 Response response = null;
			 try {
				 response = client.sendMessage("app1/test_handle", new TextMessage(testString));
				 String responseStr = response.get(1000, TimeUnit.MILLISECONDS);
				 Assert.assertContains(responseStr, testString);
				 response.close();
			 } finally{
				 if (response!=null) {
					 response.close();
				 }
			 }
			 
		} finally {
			if (client!=null){
				client.close();
			}
		}
	}


	@Test (expected = TimeoutException.class)
	public void testTimeout() throws TimeoutException{
		Client client = null;
		String testString = "hello_world";
		try{
			 client = new Client("./src/test/resources/config_example.json");
			 Response response = null;
			 try {
				 response = client.sendMessage("app1/test_handle_timeout", new TextMessage(testString));
				 String responseStr = response.get(10000, TimeUnit.MILLISECONDS);
			 } finally{
				 if (response!=null) {
					 response.close();
				 }
			 }
			 
		} finally {
			if (client!=null){
				client.close();
			}
		}
	}

}
