package ru.yandex.cocaine.dealer;

import java.util.concurrent.Callable;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Response implements Callable<String>{
	private long cResponsePtr;

	public Response(long cResponsePtr){
		this.cResponsePtr = cResponsePtr;
	}
	
	public String get(double timeout) {
		if (cResponsePtr==0) {
			throw new IllegalStateException("Response is closed");
		}
		return get(cResponsePtr, timeout);
	}

	@Override
	public String call() throws Exception {
		return get(-1);
	}

	public void close(){
		if (cResponsePtr!=0) {
			close(cResponsePtr);
		}
		cResponsePtr=0;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close(cResponsePtr);
	}
	
	private native String get(long cResponsePtr, double timeout);
	private native void close(long cResponsePtr);

	{
		System.loadLibrary("cocaine-framework-java");
	}
}
