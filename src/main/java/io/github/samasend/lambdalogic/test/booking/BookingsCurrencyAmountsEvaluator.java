package io.github.samasend.lambdalogic.test.booking;

import com.lambdalogic.test.booking.IBookingsCurrencyAmountsEvaluator;
import com.lambdalogic.test.booking.exception.InconsistentCurrenciesException;
import com.lambdalogic.test.booking.model.Booking;
import com.lambdalogic.test.booking.model.CurrencyAmount;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
        // Might hold calculation to a previous calculation
        resetAllFields();

        // Filter the booking by price and currency, cache it (might be expensive)
        final List<Booking> filteredBookings = filterBookings(bookingList, invoiceRecipientID);

        // is the filteredBooking is empty, nothing to do here, just bail
        if (filteredBookings.isEmpty()) {
            return;
        }

        // Get a distinct currencies sorted set for a given booking
        final SortedSet<String> currencies = getDistinctCurrencies(filteredBookings);

        // if currencies contains multiple values, throw an exception
        if (containsMultipleCurrencies(currencies)) {
            throw new InconsistentCurrenciesException(currencies.first(), currencies.last());
        }

        // get the booking list currency
        final String currency = currencies.first();

        // sum the total gross amount
        sum(filteredBookings, Booking::getTotalAmountGross, this::setTotalAmount, currency);

        // sum the total paid amount
        sum(filteredBookings, Booking::getPaidAmount, this::setTotalPaidAmount, currency);

        // sum the total open amount
        sum(filteredBookings, Booking::getOpenAmount, this::setTotalOpenAmount, currency);
    }

    /**
     * Set {@link #totalAmount}, {@link #totalPaidAmount} and {@link #totalOpenAmount} instance fields to {@code null}
     */
    private void resetAllFields() {
        setTotalAmount(null);
        setTotalPaidAmount(null);
        setTotalOpenAmount(null);
    }

    /**
     * Set private setter for {@link #totalAmount} instance field
     *
     * @param totalAmount total amount paid of all bookings
     */
    private void setTotalAmount(final CurrencyAmount totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * Set private setter for {@link #totalPaidAmount} instance field
     *
     * @param totalPaidAmount total open amount of all bookings
     */
    private void setTotalPaidAmount(final CurrencyAmount totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    /**
     * Set private setter for {@link #totalOpenAmount} instance field
     *
     * @param totalOpenAmount total amount gross of all bookings
     */
    private void setTotalOpenAmount(final CurrencyAmount totalOpenAmount) {
        this.totalOpenAmount = totalOpenAmount;
    }

    /**
     * Filter the bookings by invoice recipient ID and where the zero price booking
     *
     * <p>
     * Bookings that doesn't belong to the given invoice recipient or whose
     * amount and paid amount are both 0 are not relevant are therefore ignored.
     * </p>
     *
     * @param bookings           the unfiltered booking list given at {@link #calculate(List, Long)}
     * @param invoiceRecipientID the id we would like to calculate the amount
     * @return a filtered booking unmodifiable list for the current calculation
     */
    private List<Booking> filterBookings(final List<Booking> bookings, long invoiceRecipientID) {
        return bookings.stream()

                // filter bookings matching invoice recipient ID
                .filter(booking -> (long) booking.getInvoiceRecipientPK() == invoiceRecipientID)

                // filter non zero amounts
                // could also user a method reference {Booking::isZero} with a Predicate.negate()
                .filter(booking -> !booking.isZero())

                // collect it to unmodifiable list
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Get the distinct set of currencies on a given booking list
     *
     * <p>
     * Care is taken not to add amounts of different currencies.
     * An exception is thrown, if any two relevant bookings have different currencies.
     * </p>
     *
     * @param bookings the booking list
     * @return set of distinct currencies (max of 2)
     */
    private SortedSet<String> getDistinctCurrencies(final List<Booking> bookings) {
        return bookings.stream()

                // map to the booking currency
                .map(Booking::getCurrency)

                // get distinct values
                .distinct()

                // limit the result to a maximum of 2, that's enough to throw an exception from calculate
                .limit(2)

                // collect it to unmodifiable sorted set
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Check if the set contains multiple currencies
     *
     * @param currencies the distinct set of currencies
     * @return {@code true} if it contains multiple values
     */
    private boolean containsMultipleCurrencies(final SortedSet<String> currencies) {
        return currencies.size() > 1;
    }

    /**
     * A map-reduce style total (gross, paid or open) amount calculator
     * <p>
     * It will pluck the given amount for each {@link Booking} using the {@code plucker}
     * Then sum it up using a {@link BigDecimal#add(BigDecimal)} as a reducer
     * Then create a CurrencyAmount instance using the sum and {@code currency}
     * And Finally set it to {@code this} it using the given {@code setter}
     *
     * @param bookings a list of booking we want to calculate on
     * @param plucker  a mapper to pluck the appropriate amount from a {@link Booking}.
     *                 <p>
     *                 Appropriate mappers might be {@link Booking#getTotalAmount()},
     *                 {@link Booking#getPaidAmount()} or {@link Booking#getOpenAmount()}
     * @param setter   a setter to store the calculated sum.
     *                 <p>
     *                 Appropriate setters might be {@link #setTotalAmount(CurrencyAmount)},
     *                 {@link #setTotalPaidAmount(CurrencyAmount)} or {@link #setTotalOpenAmount(CurrencyAmount)}
     * @param currency the single currency use for a given booking
     */
    private void sum(final List<Booking> bookings,
                     final Function<Booking, BigDecimal> plucker,
                     final Consumer<CurrencyAmount> setter,
                     final String currency) {

        bookings.stream()

                // pluck the appropriate getter (from the Booking class) for a given amount
                .map(plucker)

                // Sum it up
                .reduce(BigDecimal::add)

                // If any value is present
                // create a new CurrencyAmount using the sum and currency
                // pass it to the setter
                .ifPresent(sum -> setter.accept(new CurrencyAmount(sum, currency)));
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
