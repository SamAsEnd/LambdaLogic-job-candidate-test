package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.IBookingsCurrencyAmountsEvaluator;
import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.CurrencyAmount;
import com.lambdalogic.test.booking.model.Price;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.*;

public class BookingsCurrencyAmountsEvaluatorTest {

    public static final long MY_INVOICE_RECIPIENT_ID = 10001L;
    public static final long OTHER_INVOICE_RECIPIENT_ID = 10002L;

    @Test(expected = InconsistentCurrenciesException.class)
    public void throwExceptionWhenGivenInconsistentCurrencies() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        evaluator.calculate(Arrays.asList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("100"), "ብር", ZERO, true)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("100"), "€", ZERO, true))
        ), MY_INVOICE_RECIPIENT_ID);

        fail("Should NOT reach here");
    }

    @Test(expected = Test.None.class)
    public void ignoreDifferentInvoiceRecipientID() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        evaluator.calculate(Arrays.asList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price("ብር")),
                getBooking(OTHER_INVOICE_RECIPIENT_ID, new Price("€"))
        ), MY_INVOICE_RECIPIENT_ID);
    }

    @Test(expected = Test.None.class)
    public void ignoreZeroAmountAndZeroPaidBooking() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        evaluator.calculate(Arrays.asList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("100"), "€", ZERO, true)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(ZERO, "ብር", ZERO, false), ZERO)
        ), MY_INVOICE_RECIPIENT_ID);
    }

    @Test(expected = Test.None.class)
    public void nullForEmptyCalculations() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        evaluator.calculate(new ArrayList<>(), MY_INVOICE_RECIPIENT_ID);

        assertNull(evaluator.getTotalAmount());
        assertNull(evaluator.getTotalPaidAmount());
        assertNull(evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void doCalculate() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        CurrencyAmount expectedTotalAndOpenAmount = new CurrencyAmount(new BigDecimal("0.12"), "ብር");
        CurrencyAmount expectedPaidAmount = new CurrencyAmount(ZERO, "ብር");

        evaluator.calculate(Collections.singletonList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "ብር", new BigDecimal(19), false))
        ), MY_INVOICE_RECIPIENT_ID);

        assertEquals(expectedTotalAndOpenAmount, evaluator.getTotalAmount());

        assertEquals(expectedPaidAmount, evaluator.getTotalPaidAmount());

        assertEquals(expectedTotalAndOpenAmount, evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void noRoundingProblems() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        CurrencyAmount expectedTotalAndOpenAmount = new CurrencyAmount(new BigDecimal("1.19"), "€");
        CurrencyAmount expectedPaidAmount = new CurrencyAmount(ZERO, "€");

        evaluator.calculate(Arrays.asList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false))
        ), MY_INVOICE_RECIPIENT_ID);

        assertEquals(expectedTotalAndOpenAmount, evaluator.getTotalAmount());

        assertEquals(expectedPaidAmount, evaluator.getTotalPaidAmount());

        assertEquals(expectedTotalAndOpenAmount, evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void wontMixGrossAndNetValues() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        CurrencyAmount expectedTotalAndOpenAmount = new CurrencyAmount(new BigDecimal("0.22"), "ብር");
        CurrencyAmount expectedPaidAmount = new CurrencyAmount(ZERO, "ብር");

        evaluator.calculate(Arrays.asList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "ብር", new BigDecimal(19), false)),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("0.10"), "ብር", new BigDecimal(19), true))
        ), MY_INVOICE_RECIPIENT_ID);

        assertEquals(expectedTotalAndOpenAmount, evaluator.getTotalAmount());

        assertEquals(expectedPaidAmount, evaluator.getTotalPaidAmount());

        assertEquals(expectedTotalAndOpenAmount, evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void deductPaidAmount() throws InconsistentCurrenciesException {
        IBookingsCurrencyAmountsEvaluator evaluator = new BookingsCurrencyAmountsEvaluator();

        CurrencyAmount expectedGross = new CurrencyAmount(new BigDecimal("127.65"), "ብር");
        CurrencyAmount expectedPaid = new CurrencyAmount(new BigDecimal("30"), "ብር");
        CurrencyAmount expectedOpen = new CurrencyAmount(new BigDecimal("97.65"), "ብር");

        BigDecimal taxRate = new BigDecimal(15);

        evaluator.calculate(Arrays.asList(
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("100"), "ብር", taxRate, false), TEN),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("10"), "ብር", taxRate, false), TEN),
                getBooking(MY_INVOICE_RECIPIENT_ID, new Price(new BigDecimal("1"), "ብር", taxRate, false), TEN)
        ), MY_INVOICE_RECIPIENT_ID);

        assertEquals(expectedGross, evaluator.getTotalAmount());

        assertEquals(expectedPaid, evaluator.getTotalPaidAmount());

        assertEquals(expectedOpen, evaluator.getTotalOpenAmount());
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
