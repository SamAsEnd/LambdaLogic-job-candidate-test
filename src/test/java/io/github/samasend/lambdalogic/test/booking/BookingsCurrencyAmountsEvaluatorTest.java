package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.IBookingsCurrencyAmountsEvaluator;
import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.CurrencyAmount;
import com.lambdalogic.test.booking.model.Price;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.*;

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

        fail("Should NOT reach here");
    }

    @Test(expected = Test.None.class)
    public void ignoreDifferentInvoiceRecipientID() throws InconsistentCurrenciesException {
        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price("USD")),
                getBooking(10002L, new Price("ETB"))
        ), 10001L);

        assertTrue(true);
    }

    @Test(expected = Test.None.class)
    public void ignoreZeroAmountAndZeroPaidBooking() throws InconsistentCurrenciesException {
        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price(new BigDecimal("100"), "ETB", ZERO, true)),
                getBooking(10001L, new Price(ZERO, "USD", ZERO, false), ZERO)
        ), 10001L);

        assertTrue(true);
    }

    @Test(expected = Test.None.class)
    public void nullForEmptyCalculations() throws InconsistentCurrenciesException {
        evaluator.calculate(new ArrayList<>(), 10001L);

        assertNull(evaluator.getTotalAmount());
        assertNull(evaluator.getTotalPaidAmount());
        assertNull(evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void doCalculate() throws InconsistentCurrenciesException {
        CurrencyAmount expected0Point12 = new CurrencyAmount(new BigDecimal("0.12"), "€");
        CurrencyAmount expected0 = new CurrencyAmount(ZERO, "€");

        evaluator.calculate(Collections.singletonList(
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false))
        ), 10001L);

        assertEquals(expected0Point12, evaluator.getTotalAmount());

        assertEquals(expected0, evaluator.getTotalPaidAmount());

        assertEquals(expected0Point12, evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void noRoundingProblems() throws InconsistentCurrenciesException {
        CurrencyAmount expected1Point19 = new CurrencyAmount(new BigDecimal("1.19"), "€");
        CurrencyAmount expected0 = new CurrencyAmount(ZERO, "€");

        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),

                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false))
        ), 10001L);

        assertEquals(expected1Point19, evaluator.getTotalAmount());

        assertEquals(expected0, evaluator.getTotalPaidAmount());

        assertEquals(expected1Point19, evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void wontMixGrossAndNetValues() throws InconsistentCurrenciesException {
        CurrencyAmount expected0Point22 = new CurrencyAmount(new BigDecimal("0.22"), "€");
        CurrencyAmount expected0 = new CurrencyAmount(ZERO, "€");

        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), false)),
                getBooking(10001L, new Price(new BigDecimal("0.10"), "€", new BigDecimal(19), true))
        ), 10001L);

        assertEquals(expected0Point22, evaluator.getTotalAmount());

        assertEquals(expected0, evaluator.getTotalPaidAmount());

        assertEquals(expected0Point22, evaluator.getTotalOpenAmount());
    }

    @Test(expected = Test.None.class)
    public void deductPaidAmount() throws InconsistentCurrenciesException {
        CurrencyAmount expectedGross = new CurrencyAmount(new BigDecimal("127.65"), "ብር");
        CurrencyAmount expectedPaid = new CurrencyAmount(new BigDecimal("30"), "ብር");
        CurrencyAmount expectedOpen = new CurrencyAmount(new BigDecimal("97.65"), "ብር");

        BigDecimal taxRate = new BigDecimal(15);

        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price(new BigDecimal("100"), "ብር", taxRate, false), TEN),
                getBooking(10001L, new Price(new BigDecimal("10"), "ብር", taxRate, false), TEN),
                getBooking(10001L, new Price(new BigDecimal("1"), "ብር", taxRate, false), TEN)
        ), 10001L);

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
