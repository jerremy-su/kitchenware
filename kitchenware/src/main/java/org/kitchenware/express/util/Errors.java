package org.kitchenware.express.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;


public class Errors {
	
	public static final String lineSeparator = System.getProperty("line.separator");
	public static String tokenString(Throwable exception){
		if(exception == null)return "";
		StringBuffer result = new StringBuffer();
		StringBuilder buff;
		for(;exception != null;){
			buff = new StringBuilder(
					String.format("%s:%s", exception.getClass().getName(), exception.getMessage())
						).append(lineSeparator);
			for (StackTraceElement e : exception.getStackTrace()) {
				buff.append(e).append(lineSeparator);
			}
			result.append(buff);
			exception = exception.getCause();
		}
		return result.toString();
	}
	
	public static RuntimeException throwRuntimeable(Throwable e){
		Deque<Throwable> throwableDeque = new LinkedList<>();
		Throwable src;
		for(src = e; src != null; src = src.getCause()){
			throwableDeque.addFirst(src);
		}
		
		RuntimeException ex = null;
		RuntimeException cause = null;
		while((src = throwableDeque.pollFirst()) != null){
			if (ex == null) {
				cause = new RuntimeException(src.getMessage());
				cause.setStackTrace(src.getStackTrace());
				ex = cause;
			}else{
				ex = new RuntimeException(src.getMessage(), cause);
				ex.setStackTrace(src.getStackTrace());
				cause = ex;
			}
		}
		return ex;
	}
	
	public static IOException throwIOException(Throwable e){
		Deque<Throwable> throwableDeque = new LinkedBlockingDeque<Throwable>();
		Throwable src;
		for(src = e; src != null; src = src.getCause()){
			throwableDeque.addFirst(src);
		}
		
		IOException ex = null;
		IOException cause = null;
		while((src = throwableDeque.pollFirst()) != null){
			if (ex == null) {
				cause = new IOException(src.getMessage());
				cause.setStackTrace(src.getStackTrace());
				ex = cause;
			}else{
				ex = new IOException(src.getMessage(), cause);
				ex.setStackTrace(src.getStackTrace());
				cause = ex;
			}
		}
		return ex;
	}
	
	public static SQLException throwSQLException(Throwable e) {
		Deque<Throwable> throwableDeque = new LinkedBlockingDeque<Throwable>();
		Throwable src;
		for(src = e; src != null; src = src.getCause()){
			throwableDeque.addFirst(src);
		}
		
		SQLException ex = null;
		SQLException cause = null;
		while((src = throwableDeque.pollFirst()) != null){
			Integer errorCode = null;
			String state = null;
			if(SQLException.class.isInstance(src)) {
				SQLException parent = (SQLException) src;
				errorCode = parent.getErrorCode();
				state = parent.getSQLState();
			}
			if (ex == null) {
				if(errorCode != null) {
					cause = new SQLException(src.getMessage(), state, errorCode);
				}else {
					cause = new SQLException(src.getMessage());
				}
				cause.setStackTrace(src.getStackTrace());
				ex = cause;
			}else{
				if(errorCode != null) {
					ex = new SQLException(src.getMessage(), state, errorCode, cause);
				}else {
					ex = new SQLException(src.getMessage(), cause);
				}
				ex.setStackTrace(src.getStackTrace());
				cause = ex;
			}
		}
		return ex;
	}
	
	public static Throwable unshellThrowable(Throwable src) {
		Throwable result = src;
		for (;result.getCause() != null;) {
			result = result.getCause();
		}
		return result;
	}
}
