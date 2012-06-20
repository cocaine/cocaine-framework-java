package ru.yandex.cocaine.dealer;

import org.junit.Test;

import ru.yandex.misc.test.Assert;

public class ClientTest {
	@Test
	public void testOnRealCocaine(){
		Client client = null;
		String testString = "hello_world";
		try{
			 client = new Client("./src/test/resources/config_example.json");
			 Response response = client.sendMessage("app1/test_handle", new TextMessage(testString));
			 String responseStr = response.get(1);
			 Assert.assertContains(responseStr, testString);
			 response.close();
		} finally {
			if (client!=null){
				client.close();
			}
		}
	}
}
