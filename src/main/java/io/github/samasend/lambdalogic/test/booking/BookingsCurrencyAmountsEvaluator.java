package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.IBookingsCurrencyAmountsEvaluator;
import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.CurrencyAmount;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementations of this interface are intended for adding up the total amount, the paid amount and open amount of a
 * list of {@link Booking}s taking care that rounding errors are not summed up.
 * <p>
 * First, {@link #calculate(List, Long)} should be called. Afterwards the methods {@link #getTotalAmount()},
 * {@link #getTotalOpenAmount()} and {@link #getTotalPaidAmount()} ()} can be called to get the results.
 * <p>
 * Implementations does not mix-up gross and net amounts and minimize rounding errors (do not add them).
 * <p>
 * The implementation does not have to be thread-safe, but multiple calls must lead to correct results.
 * <p>
 * Care is taken not to add amounts of different currencies.
 * An {@link InconsistentCurrenciesException} is thrown, if relevant bookings have different currencies.
 */
public class BookingsCurrencyAmountsEvaluator implements IBookingsCurrencyAmountsEvaluator {

    /**
     * a non-thread safe storage of {@link #calculate} results
     * for subsequent access through {@link #getTotalAmount()}
     */
    private CurrencyAmount totalAmount = null;

    /**
     * a non-thread safe storage of {@link #calculate} results
     * for subsequent access through {@link #getTotalPaidAmount()} ()}
     */
    private CurrencyAmount totalPaidAmount = null;

    /**
     * a non-thread safe storage of {@link #calculate} results
     * for subsequent access through {@link #getTotalOpenAmount()} ()}
     */
    private CurrencyAmount totalOpenAmount = null;

    /**
     * Add up the total amount, the paid amount and open amount of a list of {@link Booking}s.
     * <p>
     * The results of this method can be retrieved by the methods {@link #getTotalAmount()},
     * {@link #getTotalOpenAmount()} and {@link #getTotalPaidAmount()} ()}.
     * <p>
     * Only such {@link Booking}s are mentioned, where the given <code>invoiceRecipientID</code> matches the bookings's
     * invoice recipient ( {@link Booking#getInvoiceRecipientPK()} ), others are ignored.
     * <p>
     * The method does not mix-up gross and net amounts and minimizes rounding errors (does not add them).
     * <p>
     * The method is not thread-safe, but multiple calls lead to correct results.
     *
     * @param bookingList        - a list of {@link Booking}s
     * @param invoiceRecipientID - the PK of the person who is the booking's invoice recipient
     * @throws InconsistentCurrenciesException Care is taken not to add amounts of different currencies.
     *                                         An {@link InconsistentCurrenciesException} is thrown, if any two relevant bookings have different currencies.
     *                                         Bookings that doesn't belong to the given invoice recipient or whose amount and paid amount are both 0
     *                                         are not relevant are therefore ignored.
     */
    @Override
    public void calculate(List<Booking> bookingList, Long invoiceRecipientID) throws InconsistentCurrenciesException {
        resetAllFields();

        final List<Booking> filteredBookings = filterBookings(bookingList, invoiceRecipientID);

        if (filteredBookings.isEmpty()) {
            return;
        }

        final List<String> currencies = getDistinctCurrencies(filteredBookings);

        if (containsMultipleCurrencies(currencies)) {
            throw new InconsistentCurrenciesException(currencies.get(0), currencies.get(1));
        }

        final String theCurrency = currencies.get(0);

        pluckFieldSumItThenSetItToThis(filteredBookings, Booking::getTotalAmountGross, this::setTotalAmount, theCurrency);

        pluckFieldSumItThenSetItToThis(filteredBookings, Booking::getPaidAmount, this::setTotalPaidAmount, theCurrency);

        pluckFieldSumItThenSetItToThis(filteredBookings, Booking::getOpenAmount, this::setTotalOpenAmount, theCurrency);
    }

    private void resetAllFields() {
        setTotalAmount(null);
        setTotalPaidAmount(null);
        setTotalOpenAmount(null);
    }

    private void setTotalAmount(CurrencyAmount totalAmount) {
        this.totalAmount = totalAmount;
    }

    private void setTotalPaidAmount(CurrencyAmount totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    private void setTotalOpenAmount(CurrencyAmount totalOpenAmount) {
        this.totalOpenAmount = totalOpenAmount;
    }

    private List<Booking> filterBookings(List<Booking> bookingList, long invoiceRecipientID) {
        return bookingList.stream()
                .filter(booking -> (long) booking.getInvoiceRecipientPK() == invoiceRecipientID)
                .filter(booking -> !booking.isZero())
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> getDistinctCurrencies(final List<Booking> bookings) {
        return bookings.stream()
                .map(Booking::getCurrency)
                .distinct()
                .limit(2)
                .collect(Collectors.toList());
    }

    private boolean containsMultipleCurrencies(List<String> currencies) {
        return currencies.size() > 1;
    }

    private void pluckFieldSumItThenSetItToThis(List<Booking> subject,
                                                Function<Booking, BigDecimal> plucker,
                                                Consumer<CurrencyAmount> setter,
                                                String currency) {
        subject.stream()
                .map(plucker)
                .reduce(BigDecimal::add)
                .ifPresent(bigDecimal -> setter.accept(new CurrencyAmount(bigDecimal, currency)));
    }

    /**
     * After successful calling of {@link #calculate(List, Long)} this method returns the total amount gross of all
     * {@link Booking}s. In case of an {@link InconsistentCurrenciesException} or any other error the result is null.
     *
     * @return
     */
    @Override
    public CurrencyAmount getTotalAmount() {
        return totalAmount;
    }

    /**
     * After successful calling {@link #calculate(List, Long)} this method returns the total paid amount of all
     * {@link Booking}s. In case of an {@link InconsistentCurrenciesException} or any other error the result is null.
     *
     * @return
     */
    @Override
    public CurrencyAmount getTotalPaidAmount() {
        return totalPaidAmount;
    }

    /**
     * After successful calling {@link #calculate(List, Long)} this method returns the total open amount of all
     * {@link Booking}s. In case of an {@link InconsistentCurrenciesException} or any other error the result is null.
     *
     * @return
     */
    @Override
    public CurrencyAmount getTotalOpenAmount() {
        return totalOpenAmount;
    }
}
