package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.model.Price;

import java.math.BigDecimal;

public class MonkeyPatchingPrice extends Price {

    protected MonkeyPatchingPrice(Price price) {
        super(price.getAmount(), price.getCurrency(), price.getTaxRate(), price.isGross());
    }

    /**
     * Return the amount gross (which is a rounded value if gross == false).
     *
     * <p>
     * Programmatically comment line no 264 on Price.java
     *
     * @return
     */
    public BigDecimal getAmountGross() {
        BigDecimal amountGross = null;

        if (amount != null) {
            amountGross = amount;
            if (!gross && taxRateDiv100Add1 != null) {
                amountGross = amountGross.multiply(taxRateDiv100Add1);
            }
        }

        return amountGross;
    }
}
