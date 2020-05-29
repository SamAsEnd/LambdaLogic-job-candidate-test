# LambdaLogic-job-candidate-test

[![Build Status](https://travis-ci.org/SamAsEnd/LambdaLogic-job-candidate-test.svg?branch=master)](https://travis-ci.org/SamAsEnd/LambdaLogic-job-candidate-test)

> LambdaLogic - "Job Candidate Test"

## Description

[The description provide](description.txt)

## Challenge

1. Implement [IBookingsCurrencyAmountsEvaluator](src/main/java/com/lambdalogic/test/booking/IBookingsCurrencyAmountsEvaluator.java) interface
2. Unit test for the above implementation

## Solution

I felt like it's a very easy challenge, or I have misunderstood the question.
I will try to explain the solution I implemented, and the thought process behind it.

### Implementation of the interface

The whole concept is like, there is a database of some sort which the customer is booking online. An implementation of
[IBookingsCurrencyAmountsEvaluator](src/main/java/com/lambdalogic/test/booking/IBookingsCurrencyAmountsEvaluator.java)
class will be triggered on the checkout page or on some kind of billing page to calculate the *total amount*, 
the *total open amount*, and *total paid amount* to show to the user or generate an invoice receipt.

Most of the heavy lifting is implemented on the `com.lambdalogic.test.booking` package which includes a class to 
facilitate `CurrencyAmount`, `Price`, `Booking`, and `TypeHelper` classes.

The interface `IBookingsCurrencyAmountsEvaluator` has a `calculate` method to receive a `List<Booking>` of `bookingList`
and `Long` `invoiceRecipientID`. The method is expected to calculate the *total amount*, *total paid amount*, and
*total open amount* and return them in subsequent calls to `getTotalAmount`, `getTotalPaidAmount`, 
and `getTotalOpenAmount`.

The instance of the implementation class is expected to be used multiple times and is not expected to be thread-safe.
So I have created multiple instances fields initialize with null to store the results.

The algorithm for the `calculate` method is listed below

 - Reset the fields to `null`, might hold a previous value
 - Filter the booking list, only allow bookings with a matching `invoiceRecipientID`
 - Filter the booking, only allow with non-zero price (all the prices)
 - If the filtered bookings list is empty, bail
 - If the filtered bookings list has multiple currencies, throw an exception
 - Calculate the `totalAmount`
    - Collect the *total amount* of each booking
    - Sum them up
    - Create a `CurrencyAmount` with the currency and set it on `totalAmount`
 - Calculate the `totalAmount`
    - collect the *total paid amount* of each booking
    - Create a `CurrencyAmount` with the currency and set it on `totalPaidAmount`
 - Calculate the `totalAmount`
    - collect the *total open amount* of each booking
    - Create a `CurrencyAmount` with the currency and set it on `totalOpenAmount`

The rest of the method are a simple getters just return the instance fields value.

### Testing the Implementation

I have created a unit test with 100% code coverage for the class i implemented. The tests assert the following features

 - `throwExceptionWhenGivenInconsistentCurrencies`
   - Which tests the method will be thrown when inconsistent pricing is provided.
 - `ignoreDifferentInvoiceRecipientID`
   - Ignore bookings with different invoiceRecipientID
 - `ignoreZeroAmountAndZeroPaidBooking`
   - Ignore booking with zero paid and zero amount
 - `nullForEmptyCalculations`
   - Test it returns null when calculate is provide with an empty booking list
 - `doCalculate`
   - Properly calculate the amounts required
 - `noRoundingProblems`
   - Don't have a decimal rounding problem (Adding up of roundings) when calculating the total amounts
 - `wontMixGrossAndNetValues`
   - Don't mix gross and net values when adding the values
 - `deductPaidAmount`
   - Deduct the *paid amount* when calculating the *total open amount*
