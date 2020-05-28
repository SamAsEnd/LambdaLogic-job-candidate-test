package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.IBookingsCurrencyAmountsEvaluator;
import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.Price;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class BookingsCurrencyAmountsEvaluatorTest {

    protected IBookingsCurrencyAmountsEvaluator evaluator;

    @Before
    public void setUp() throws Exception {
        evaluator = new BookingsCurrencyAmountsEvaluator();
    }

    @Test(expected = InconsistentCurrenciesException.class)
    public void throwExceptionWhenGivenInconsistentCurrencies() throws InconsistentCurrenciesException {
        evaluator.calculate(Arrays.asList(
                getBooking(10001L, new Price("USD")),
                getBooking(10001L, new Price("ETB"))
        ), 10001L);
    }

    protected Booking getBooking(Long invoiceRecipientID, Price mainPrice) {
        return new Booking(
                new Random().nextLong(),
                mainPrice, null, null, null, null,
                new Date(), null,
                Arrays.asList(100L, 101L, 102L),
                invoiceRecipientID,
                new Random().nextLong());
    }
}
