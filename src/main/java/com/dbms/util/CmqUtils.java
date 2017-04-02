package com.dbms.util;

public class CmqUtils {
	public static String getExceptionMessageChain(Throwable throwable) {
	    StringBuilder sb = new StringBuilder();
	    boolean firstException = true;
	    while (throwable != null) {
	    	if(!firstException) {
	    		sb.append(", Caused By:").append(throwable.getMessage());
	    	} else {
	    		sb.append(throwable.getMessage());
	    	}
	    	throwable = throwable.getCause();
	    }
	    return sb.toString();
	}
}
