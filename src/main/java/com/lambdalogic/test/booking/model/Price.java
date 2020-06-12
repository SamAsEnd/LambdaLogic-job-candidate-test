package com.lambdalogic.test.booking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Combination of an amount, a currency and a tax rate.
 * The amount can be gross or net.
 */
public class Price {
	
	public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
	public static final BigDecimal BD_100 = new BigDecimal("100.00");
	
	
    /**
     * The amount.
     * It is gross if the attribute gross is true, otherwise net.
     */
    protected BigDecimal amount;

    /**
     * The currency of the amount.
     */
    protected String currency;
    
    /**
     * The tax rate.
     * A value of 19 means 19%, so actually the tax rate is multiplied with 100.
     * Valid values are between 0 and 99.99.
     */
    protected BigDecimal taxRate;
    
    /**
     * Tax rate divided by 100.
     * If taxRate is 19, taxRateDiv100 is 0.19.
     */
    protected BigDecimal taxRateDiv100;

    /**
     * Tax rate divided by 100 plus 1.
     * If taxRate is 19, taxRateDiv100 is 1.19.
     */
    protected BigDecimal taxRateDiv100Add1;
    
    /**
     * Defines if the amount is gross (true) or net (false).
     */
    protected boolean gross = true;
    
    
    public Price() {
    	setAmount(ZERO);
    	setTaxRate(ZERO);
    }
    

    public Price (
    	BigDecimal amount, 
        String currency, 
        BigDecimal taxRate, 
        boolean gross
    ) {
        setAmount(amount);
        
        this.currency = currency;

        setTaxRate(taxRate);
        
        this.gross = gross;
    }
    
    
    public Price(String currency, boolean gross) {
        this(
        	ZERO,		// amount
            currency,	// currency
            null,		// taxRate
            gross 
        );
    }
    
    
    public Price(String currency) {
        this(
        	ZERO,		// amount
            currency,	// currency
            null,		// taxRate
            true		// gross 
        );
    }

    
    /**
     * Initialize all values but currency and gross. 
     */
    public void init() {
    	setAmount(ZERO);
    	setTaxRate(ZERO);
    }
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		result = prime * result + (gross ? 1231 : 1237);
		result = prime * result + ((taxRate == null) ? 0 : taxRate.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Price other = (Price) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		}
		else if (!amount.equals(other.amount))
			return false;
		if (currency == null) {
			if (other.currency != null)
				return false;
		}
		else if (!currency.equals(other.currency))
			return false;
		if (gross != other.gross)
			return false;
		if (taxRate == null) {
			if (other.taxRate != null)
				return false;
		}
		else if (!taxRate.equals(other.taxRate))
			return false;
		return true;
	}


	/**
     * Invert the signum of the amount.
     */
    public void negate() {
    		amount = amount.negate();
    }
    

    // *************************************************************************
    // * Getter / Setter
    // *

	public BigDecimal getAmount() {
		return amount;
	}

    
	public void setAmount(BigDecimal amount) {
		if (amount == null) {
			this.amount = ZERO;
		}
		else {
			this.amount = amount.setScale(2, RoundingMode.HALF_UP);
		}
	}
    
    
	public void setAmount(BigDecimal amount, boolean brutto) {
		setAmount(amount);
		setGross(brutto);
	}
    
    
    public String getCurrency() {
        return this.currency;
    }

    
	public void setCurrency(String currency) {
		this.currency = currency;
	}
    
    
    /**
     * Return the tax rate.
     * A return value of 19 means 19%, so actually the returned tax rate is multiplied with 100. 
     * Valid values are between 0 and 99.99.
     * The default value is 0.
     * The result is never null.
     * @return
     */
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    
	public void setTaxRate(BigDecimal taxRate) {
		if (taxRate == null) {
			this.taxRate = ZERO;
			taxRateDiv100 = ZERO;
			taxRateDiv100Add1 = BigDecimal.ONE;
		}
		else {
			this.taxRate = taxRate;
			// a division through 100 never results in a periodical fraction and must not be rounded
			taxRateDiv100 = taxRate.divide(BD_100);
			taxRateDiv100Add1 = taxRateDiv100.add(BigDecimal.ONE);
		}
	}

    
    public boolean isGross() {
        return gross;
    }
    

    public boolean isNet() {
        return ! gross;
    }
    
    
	public void setGross(boolean brutto) {
		this.gross = brutto;
	}

    
    /**
     * Copy values of another PriceVO.
     * @param price
     */
	public void copyFrom(Price price) {
		setAmount(price.amount);
		currency = price.currency;
		setTaxRate(price.taxRate);
		gross = price.gross;
	}

    // *
    // * Getter / Setter
    // *************************************************************************

    // *************************************************************************
    // * Additional Getter
    // *
    
	/**
	 * Return the amount gross (which is a rounded value if gross == false).
	 * @return
	 */
	public BigDecimal getAmountGross() {
		BigDecimal amountGross = null;
		if (amount != null) {
			amountGross = amount;
			if (!gross && taxRateDiv100Add1 != null) {
				amountGross = amountGross.multiply(taxRateDiv100Add1);
				amountGross = amountGross.setScale(2, RoundingMode.HALF_UP);
			}
		}
		return amountGross;
	}

    
	/**
	 * Return the amount net (which is a rounded value if gross == true).
	 * @return
	 */
	public BigDecimal getAmountNet() {
		BigDecimal amountNet = null;
		if (amount != null) {
			amountNet = amount;
			if (gross && taxRateDiv100Add1 != null) {
				amountNet = amountNet.divide(taxRateDiv100Add1, 2, RoundingMode.HALF_UP);
			}
		}
		return amountNet;
	}


	/**
	 * Return the tax amount based of the amount, the tax rate and the circumstance if amount is net or gross.
	 * @return
	 */
	public BigDecimal getAmountTax() {
		BigDecimal amountTax = null;
		if (amount != null) {
			if (taxRate.signum() == 0) {
				amountTax = ZERO;
			}
			else if (gross) {
				amountTax = amount.subtract(getAmountNet());
			}
			else {
				amountTax = amount.multiply(taxRateDiv100);
				amountTax = amountTax.setScale(2, RoundingMode.HALF_UP);
			}
		}
		return amountTax;
	}
	
	
	public CurrencyAmount getCurrencyAmountGross() {
		return new CurrencyAmount(getAmountGross(), currency);
	}
    

    public CurrencyAmount getCurrencyAmountNet() { 
        return new CurrencyAmount(getAmountNet(), currency);
    }
    
    
    public CurrencyAmount getCurrencyAmountTax() {
        return new CurrencyAmount(getAmountTax(), currency);
    }

    
    public CurrencyAmount getCurrencyAmount() {
        return new CurrencyAmount(this.amount, this.currency);
    }
    
    
	public boolean isZero() {
		return amount.signum() == 0;
	}
    
    // *
    // * Additional Getter
    // *************************************************************************
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("amount: ");
        sb.append(getAmount().toString());
        sb.append((gross ? "(gross)" : "(net)"));

        sb.append("\ncurrency: ");
        sb.append(currency);

        sb.append("\ntaxRate: ");
        sb.append(getTaxRate().toString());
                        
        return sb.toString();
    }
    
}
