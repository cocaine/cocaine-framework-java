package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Response {
	private long cResponsePtr;

	public Response(long cResponsePtr){
		this.cResponsePtr = cResponsePtr;
	}
	
	public String get(long timeout, TimeUnit timeUnit) throws TimeoutException{
		if (cResponsePtr==0) {
			throw new IllegalStateException("Response is closed");
		}
		
		long milliseconds = timeUnit.toMillis(timeout);
		// see response_impl.cpp: response_impl_t::get for cocaineTimeout
		// definition cocaineTimeout==1 is 1000 seconds
		double cocaineTimeout = milliseconds / 1000000.0; 
		return get(cResponsePtr, cocaineTimeout);
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
	
	private native String get(long cResponsePtr, double timeout) throws TimeoutException;
	private native void close(long cResponsePtr);

	{
		System.loadLibrary("cocaine-framework-java");
	}
}
