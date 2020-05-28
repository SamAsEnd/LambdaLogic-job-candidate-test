package com.lambdalogic.test.booking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;


/**
 * Aggregation of a currency and an amount of money.
 * The latter is stored as a BigDecimal with at most two decimal points.
 * In other words: The maximum value of scale is 2.
 * If other values are set an Exception will be thrown.
 */
public class CurrencyAmount {
	
    protected static NumberFormat numberFormat;
    
    static {
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
    }


	private BigDecimal amount;
	private String isoCode;

    
    /**
     * @param amount The amount of Money. 
     *  The scale must not exceed 2.
     *  Otherwise an ArithmeticException will be thrown.
     *  Null is handled as 0.
     *  
     * @param currency The currency.
     *  Null will be accepted.
     */
    public CurrencyAmount(BigDecimal amount, String currency) {
    	setAmount(amount);
        this.isoCode = currency;
    }


	public BigDecimal getAmount() {
		return amount;
	}


	public String getCurrency() {
		return isoCode;
	}

    
    /**
     * Sets the currency.
     * Null will be accepted.
     *
     * @param currency
     */
    public CurrencyAmount setCurrency(String currency) {
        this.isoCode = currency;
        return this;
    }

 
    /**
     * Sets the amount of money.
     *  
     * @param amount The amount of Money. 
     *  The scale must not exceed 2.
     *  Otherwise an ArithmeticException will be thrown.
     *  Null is handled as 0.
     */
    public CurrencyAmount setAmount(BigDecimal amount) {
        if (amount != null) {
            this.amount = amount.setScale(2);
        } 
        else {
            this.amount = BigDecimal.ZERO;
        }
        return this;
    }

    
    public boolean equals(Object other) {
        if (other instanceof CurrencyAmount) {
            CurrencyAmount otherCurrencyAmount = (CurrencyAmount) other;
            return 
                amount.compareTo(otherCurrencyAmount.getAmount()) == 0
                &&
                (
                	isoCode == null
                	? 
                	otherCurrencyAmount.getCurrency() == null
                	: 
                	isoCode.equals(otherCurrencyAmount.getCurrency())
                );
        }
        return false;
    }
    
    
    public String toString() {
    	return numberFormat.format(amount) + " " + isoCode;
    }

	
	// **************************************************************************
    // * Math
    // *

    public CurrencyAmount absolute() {
    	return new CurrencyAmount(amount.abs(), isoCode);
    }

    
    public CurrencyAmount negate() {
    	BigDecimal negativeAmount = BigDecimal.ZERO;
    	if (amount != null) {
    		negativeAmount = amount.negate();
    	}
    	return new CurrencyAmount(negativeAmount, isoCode);
    }

    
    public CurrencyAmount add(BigDecimal summand) {
    	if (summand == null) {
    		throw new IllegalArgumentException("Parameter 'summand' must not be null.");
    	}

    	BigDecimal newAmount = amount.add(summand);
    	return new CurrencyAmount(newAmount, isoCode);
    }
    

    public CurrencyAmount add(double summand) {
    	BigDecimal bdSummand = BigDecimal.valueOf(summand);
    	BigDecimal newAmount = amount.add(bdSummand);
    	return new CurrencyAmount(newAmount, isoCode);
    }

    
    public CurrencyAmount add(CurrencyAmount summand) {
    	if (summand == null) {
    		throw new IllegalArgumentException("Parameter 'summand' must not be null.");
    	}
    	
    	if (!isoCode.equals(summand.isoCode)) {
    		throw new IllegalArgumentException(
    			"Parameter 'summand' must have the same currency (this.currency: " 
    			+ isoCode + ", summand.currency: " + summand.isoCode + ")."
    		);
    	}
    	
    	BigDecimal newAmount = amount.add(summand.getAmount());
    	return new CurrencyAmount(newAmount, isoCode);
    }
    

    public CurrencyAmount multiply(int times) {
    	if (times == 0) {
    		return new CurrencyAmount(BigDecimal.ZERO, isoCode);
    	} 
    	else if (times == 1) {
    		return this;
    	}
    	else {
    		BigDecimal timesBD = BigDecimal.valueOf(times);
        	BigDecimal newAmount = amount.multiply(timesBD);
        	
        	return new CurrencyAmount(newAmount, isoCode);
    	}
    }

    
    public CurrencyAmount multiply(BigDecimal times) {
    	if (times.signum() == 0) {
    		return new CurrencyAmount(BigDecimal.ZERO, isoCode);
    	} 
    	else if (times.compareTo(BigDecimal.ONE) == 0) {
    		return this;
    	}
    	else {
        	BigDecimal newAmount = amount.multiply(times);
        	newAmount = newAmount.setScale(2, RoundingMode.HALF_UP);
        	return new CurrencyAmount(newAmount, isoCode);
    	}
    }

    
    public CurrencyAmount convert(BigDecimal rate, String currency) {
    	BigDecimal newAmount = null;
    	if (rate.signum() == 0) {
    		newAmount = BigDecimal.ZERO;
    	} 
    	else if (rate.compareTo(BigDecimal.ONE) == 0) {
    		newAmount = amount;
    	}
    	else {
        	newAmount = amount.multiply(rate);
        	newAmount = newAmount.setScale(2, RoundingMode.HALF_UP);
    	}
    	
    	return new CurrencyAmount(newAmount, currency);
    }

    // *
    // * Math
	// **************************************************************************

}
