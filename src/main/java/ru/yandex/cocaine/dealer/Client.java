package ru.yandex.cocaine.dealer;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Client {
	private long cClientPtr=0;

	public Client(String configPath) {
		cClientPtr = init(configPath);
	}

	public Response sendMessage(String path, Message message) {
		if (cClientPtr==0){
			throw new IllegalStateException("client is closed");
		}
		String[] parts = path.split("/");
		String service = parts[0];
		String handle = parts[1];
		long responsePtr = sendMessage(cClientPtr, service, handle, message.toString());
		return new Response(responsePtr);
	}

	public void close(){
		if (cClientPtr!=0) {
			delete(cClientPtr);
		}
		cClientPtr = 0;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	// returns pointer to a client
	private native long init(String configPath);
	// deletes client
	private native void delete(long cClientPtr);

	// returns pointer to response
	private native long sendMessage(long cClientPtr, String service, String handle,
			String message);

	{
		System.loadLibrary("cocaine-framework-java");
	}
}
