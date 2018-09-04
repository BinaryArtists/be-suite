package com.open.exception;

import org.springframework.core.NestedRuntimeException;

public class OnRuntimeException extends NestedRuntimeException {

	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 4170221303304983122L; 
	private String reason;
	public OnRuntimeException(String msg) {
		this(msg, "");
		// TODO Auto-generated constructor stub
	}
	public OnRuntimeException(String msg, Throwable cause) {
		this(msg, "",cause);
		// TODO Auto-generated constructor stub
	}
	public OnRuntimeException(String msg, String reason) {
		super(msg);
		this.reason = reason;
	}
	public OnRuntimeException(String msg, String reason, Throwable cause) {
		super(msg, cause);
		this.reason = reason;
	}
	public String getReason() {
		return reason == null ? "" : reason;
	}
}
