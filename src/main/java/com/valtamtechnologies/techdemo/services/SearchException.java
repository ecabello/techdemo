package com.valtamtechnologies.techdemo.services;

public class SearchException extends RuntimeException {
	private static final long serialVersionUID = 8456161924597458449L;

	public SearchException(String msg, Throwable t) {
		super(msg, t);
	}

	public SearchException(String msg) {
		super(msg);
	}

	public SearchException(Throwable t) {
		super(t);
	}
}
