package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.IBookingsCurrencyAmountsEvaluator;
import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.Price;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertNull;

public class BookingsCurrencyAmountsEvaluatorTest {

    protected IBookingsCurrencyAmountsEvaluator evaluator;

    @Before
    public void setUp() throws Exception {
        evaluator = new BookingsCurrencyAmountsEvaluator();
    }

    @Test(expected = InconsistentCurrenciesException.class)
    public void throwExceptionWhenGivenInconsistentCurrencies() throws InconsistentCurrenciesException {
        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price(new BigDecimal("100"), "ETB", ZERO, true)),
                getBooking(10001L, new Price(new BigDecimal("100"), "USD", ZERO, true))
        ), 10001L);
    }

    @Test(expected = Test.None.class)
    public void ignoreDifferentInvoiceRecipientID() throws InconsistentCurrenciesException {
        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price("USD")),
                getBooking(10002L, new Price("ETB"))
        ), 10001L);
    }

    @Test(expected = Test.None.class)
    public void ignoreZeroAmountAndZeroPaidBooking() throws InconsistentCurrenciesException {
        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price(new BigDecimal("100"), "ETB", ZERO, true)),
                getBooking(10001L, new Price(ZERO, "USD", ZERO, false), ZERO)
        ), 10001L);
    }

    @Test(expected = Test.None.class)
    public void emptyCalculationGivesNull() throws InconsistentCurrenciesException {
        evaluator.calculate(new ArrayList<>(), 10001L);

        assertNull(evaluator.getTotalAmount());
        assertNull(evaluator.getTotalPaidAmount());
        assertNull(evaluator.getTotalOpenAmount());
    }

    protected Booking getBooking(Long invoiceRecipientID, Price mainPrice) {
        return getBooking(invoiceRecipientID, mainPrice, null);
    }

    protected Booking getBooking(Long invoiceRecipientID, Price mainPrice, BigDecimal paidAmount) {
        return new Booking(
                new Random().nextLong(),
                mainPrice, null, null, null, paidAmount,
                new Date(), null,
                Arrays.asList(100L, 101L, 102L),
                invoiceRecipientID,
                new Random().nextLong());
    }
}
