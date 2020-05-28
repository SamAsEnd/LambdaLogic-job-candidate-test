package com.lambdalogic.test.booking.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A booking describes the fact that a participant booked an offering that has a certain price.
 * The participant and the offering are just presented by their IDs (participantID and offeringID).
 * But the price values are copied from the offering into the booking when the booking is created, just like a snapshot. 
 * 
 * A booking has one invoice recipient and one or more benefit recipients, which are all participants.
 * The invoice and benefit recipients are presented by their ID.
 * 
 * A booking has a main amount and two additional amounts. The additional amounts are optional.
 * In addition there might be a cancellation fee in the case of cancelled bookings.
 * 
 * All prices of a booking are either gross or net.
 * However, the values of paid amount and the open amount are always gross.
 * When doing the calculation take care that you don't mix-up gross and net amounts and be aware of rounding errors.
 * Though the booking class offers all values gross and net only one of them is exact, the other one is rounded.
 */
public class Booking {
    
    /**
     * Primary key of this booking.
     */
    protected Long id;

    /**
     * The main price that is always set (never null).
     */
    protected Price mainPrice;
    
    /**
     * Additional price 1, might be null.
     */
    protected Price add1Price;
    
    /**
     * Additional price 2, might be null.
     */
    protected Price add2Price;
    
    /**
     * Price to pay in the case that the booking has been cancelled.
     */
    protected Price cancelFeePrice;
    
    /**
     * Amount that already has been paid for this booking.
     * The paid amount is always gross.
     */
    protected BigDecimal paidAmount;

    /**
     * Date when this booking has been created.
     */
    protected Date bookingDate;
    
    /**
     * Date when this booking has been cancelled.
     */
    protected Date cancelationDate;

    /**
     * List of the PKs of the benefit recipients, which are the persons who get the benefit (service) of this booking.
     */
    protected List<Long> benefitRecipientPKs;
    
    /**
     * PK of the invoice recipient, which is the person who has to pay for this booking.
     */
    protected Long invoiceRecipientPK;

    /**
     * PK of the offering that has been booked.
     */
    protected Long offeringPK;
        
    
    public Booking() {
    }
    
    
    public Booking(
		Long id, 
		Price mainPrice,
		Price add1Price,
		Price add2Price,
		Price cancelFeePrice,
		BigDecimal paidAmount,
        Date bookingDate, 
        Date cancelationDate,
        List<Long> benefitRecipientPKs,
        Long invoiceRecipientPK,
        Long offeringPK
    ){
        this.id = id;
        this.mainPrice = mainPrice;
        this.add1Price = add1Price;
        this.add2Price = add2Price;
        this.cancelFeePrice = cancelFeePrice;
        this.paidAmount = paidAmount;
        this.bookingDate = bookingDate;
        this.cancelationDate = cancelationDate;
        this.benefitRecipientPKs = benefitRecipientPKs;
        this.invoiceRecipientPK = invoiceRecipientPK;
        this.offeringPK = offeringPK;
    }

    
    /**
     * Return the total gross amount of all prices.
     * @return
     */
    public BigDecimal getTotalAmountGross() {
		BigDecimal result = getMainPrice().getAmountGross();
		result = result.add(getAdd1Price().getAmountGross());
		result = result.add(getAdd2Price().getAmountGross());
		result = result.add(getCancelFeePrice().getAmountGross());
		return result;
    }
    
    /**
     * Return the total net amount of all prices.
     * @return
     */
    public BigDecimal getTotalAmountNet() {
		BigDecimal result = getMainPrice().getAmountNet();
		result = result.add(getAdd1Price().getAmountNet());
		result = result.add(getAdd2Price().getAmountNet());
		result = result.add(getCancelFeePrice().getAmountNet());
		return result;
    }
    
    /**
     * Return the total tax amount of all prices.
     * @return
     */
    public BigDecimal getTotalAmountTax() {
		BigDecimal result = getMainPrice().getAmountTax();
		result = result.add(getAdd1Price().getAmountTax());
		result = result.add(getAdd2Price().getAmountTax());
		result = result.add(getCancelFeePrice().getAmountTax());
		return result;
    }
    
    /**
     * Return the total amount of all prices gross or net, according to the value of isGross().
     * @return
     */
    public BigDecimal getTotalAmount() {
		BigDecimal result = getMainPrice().getAmount();
		result = result.add(getAdd1Price().getAmount());
		result = result.add(getAdd2Price().getAmount());
		result = result.add(getCancelFeePrice().getAmount());
		return result;
    }
    
    
    /**
     * Return the total gross amount of all prices.
     * @return
     */
	public CurrencyAmount getCurrencyAmountGross() {
		return new CurrencyAmount(getTotalAmountGross(), getCurrency());
	}

	
    /**
     * Return the total net amount of all prices.
     * @return
     */
	public CurrencyAmount getCurrencyAmountNet() {
		return new CurrencyAmount(getTotalAmountNet(), getCurrency());
	}
    
	
    /**
     * Return the total tax amount of all prices.
     * @return
     */
	public CurrencyAmount getCurrencyAmountTax() {
		return new CurrencyAmount(getTotalAmountTax(), getCurrency());
	}
	
    
    /**
     * Return the total amount of all prices gross or net, according to the value of isGross().
     * @return
     */
	public CurrencyAmount getCurrencyAmount() {
		return new CurrencyAmount(getTotalAmount(), getCurrency());
	}

	
    /**
     * Return true, if the amount of every price is 0.
     * If only the sum is 0 but not every single amount, the result is false.
     * @return
     */
    public boolean isZero() {
		return 
			getMainPrice().isZero() &&
			getAdd1Price().isZero() &&
			getAdd2Price().isZero() &&
			getCancelFeePrice().isZero();
    }

    
    // *************************************************************************
    // * Getter / Setter
    // *
    
    public Long getID() {
        return id;
    }
    

    public void setID(Long value) {
        id = value;
    }    

    
    public Price getMainPrice() {
		if (mainPrice == null) {
			mainPrice = new Price();
		}
		return mainPrice;
    }
    
    
    public void setMainPrice(Price price) {
    	this.mainPrice = price;
    }

    
    public Price getAdd1Price() {
		if (add1Price == null) {
			add1Price = new Price();
		}
		return add1Price;
    }
    
    
    public void setAdd1Price(Price price) {
    	this.add1Price = price;
    }

    
    public Price getAdd2Price() {
		if (add2Price == null) {
			add2Price = new Price();
		}
		return add2Price;
    }
    
    
    public void setAdd2Price(Price price) {
    	this.add2Price = price;
    }

    
    public Price getCancelFeePrice() {
		if (cancelFeePrice == null) {
			cancelFeePrice = new Price();
		}
		return cancelFeePrice;
    }
    
    
    public void setCancelFeePrice(Price price) {
    	this.cancelFeePrice = price;
    }
    
    
    public BigDecimal getPaidAmount() {
    	if (paidAmount == null) {
    		paidAmount = BigDecimal.ZERO;
    	}
    	return paidAmount;
    }
    
    
    public void setPaidAmount(BigDecimal paidAmount) {
    	this.paidAmount = paidAmount;
    }
    
    
    public Date getBookingDate() {
        return bookingDate;
    }
    
    
    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    
    public Date getCancelationDate() {
        return cancelationDate;
    }
    
    
    public void setCancelationDate(Date cancelationDate) {
    	this.cancelationDate = cancelationDate;
    }
    

    public boolean isCanceled() {
        return (cancelationDate != null);
    }
    
    
    public List<Long> getBenefitRecipientPKs() {
        if (benefitRecipientPKs == null) {
            benefitRecipientPKs = new ArrayList<>();
        }
        return benefitRecipientPKs;
    }
    
    
    public void setBenefitRecipientPK(Long benefitRecipientPK) {
    	benefitRecipientPKs = new ArrayList<>(1);
    	benefitRecipientPKs.add(benefitRecipientPK);
    }    
    
    
    public void setBenefitRecipientPKs(List<Long> benefitRecipientPKs) {
    	this.benefitRecipientPKs = benefitRecipientPKs;
    }    
    

    /**
     * Returns the PK of the current invoice recipient.
     *  
     * @return
     */
    public Long getInvoiceRecipientPK() {
        return invoiceRecipientPK;
    }
    
    
    public void setInvoiceRecipientPK(Long newInvoiceRecipientPK) {
    	this.invoiceRecipientPK = newInvoiceRecipientPK;
    }
    
    
    public Long getOfferingPK() {
        return offeringPK;
    }
    
    
    public void setOfferingPK(Long offeringPK) {
    	this.offeringPK = offeringPK;
    }

    // * 
    // * Getter / Setter
    // *************************************************************************
    
    
    // **************************************************************************
	// * Convenience Methods and Delegate Methods
	// *

    /**
     * Return the currency of this booking.
     * All prices of a booking have the same currency.
     * @return
     */
    public String getCurrency() {
    	return getMainPrice().getCurrency();
    }
    
    
    public void setCurrency(String currency) {
    	getMainPrice().setCurrency(currency);
    	
    	if (add1Price != null) {
    		add1Price.setCurrency(currency);
    	}
    	
    	if (add2Price != null) {
    		add2Price.setCurrency(currency);
    	}
    	
    	if (cancelFeePrice != null) {
    		cancelFeePrice.setCurrency(currency);
    	}
    }
    
    
    /**
     * Return the whether the prices of this booking are gross (true) or net (false).
     * All prices of a booking are either all gross or all net.
     * @return
     */
    public boolean isGross() {
    	return getMainPrice().isGross();
    }
    
    
    public void setGross(boolean gross) {
    	getMainPrice().setGross(gross);
    	
    	if (add1Price != null) {
    		add1Price.setGross(gross);
    	}
    	
    	if (add2Price != null) {
    		add2Price.setGross(gross);
    	}
    	
    	if (cancelFeePrice != null) {
    		cancelFeePrice.setGross(gross);
    	}
    }

    
    /**
     * The amount that has to be paid yet.
     * The open amount is the difference between the total amount gross and the paid amount.
     * Therefore it is always gross but rounded if the prices of this booking are actually net (gross == false). 
     * @return
     */
    public BigDecimal getOpenAmount() {
    	BigDecimal openAmount = getTotalAmountGross();
    	
    	if (paidAmount != null) {
    		openAmount = openAmount.subtract(paidAmount);
    	}
    	
    	return openAmount;
    }

    // *
	// * Convenience Methods and Delegate Methods
	// **************************************************************************
    
}
