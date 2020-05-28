package com.lambdalogic.test.booking.exception;

public class InconsistentCurrenciesException extends Exception {

	private static final long serialVersionUID = 1L;

	
	public InconsistentCurrenciesException(String currency0, String currency1) {
		super("Input data contains more than one currency: " + currency0 + " and " + currency1 + ".");
	}
	
}
