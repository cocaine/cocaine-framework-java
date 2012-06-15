package ru.yandex.cocaine.dealer;

public class Client {

	private long cClientPtr=0;

	public Client(String configPath) {
		cClientPtr = init(configPath);
	}

	public Response sendMessage(String path, Message message) {
		String[] parts = path.split("/");
		String service = parts[0];
		String handle = parts[1];
		return new Response(sendMessage(cClientPtr, service, handle, message.toString()));
	}

	public void close(){
		if (cClientPtr!=0)
			delete(cClientPtr);
		cClientPtr = 0;
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}
	
	
	// returns pointer to client
	private native long init(String configPath);
	private native void delete(long cClientPtr);

	// returns pointer to response
	private native long sendMessage(long cClientPtr, String service, String handle,
			String message);

	public static void main(String[] args) {
		String configPath = "/home/vshakhov/src/cocaine-example/config_example.json";
		new Client(configPath).sendMessage("app1/test_handle", new TextMessage(
				"hello word"));
	}
}
