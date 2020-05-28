package com.lambdalogic.test.booking.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

import com.lambdalogic.test.booking.model.CurrencyAmount;


/**
 * Helper class for type conversions.
 */
public class TypeHelper {

	/**
	 * Convert a given value into a {@link BigDecimal}.
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public static BigDecimal toBigDecimal(Object value) throws ParseException {
    	BigDecimal bdValue = null;
        if (value != null) {
            if (value instanceof BigDecimal) {
                bdValue = (BigDecimal) value;
            }
            else if (value instanceof BigInteger) {
            	bdValue = new BigDecimal((BigInteger) value);
            }
            else if (value instanceof Long) {
            	bdValue = new BigDecimal(((Long) value).longValue());
            }
            else if (value instanceof Integer) {
            	bdValue = new BigDecimal(((Integer) value).intValue());
            }
            else if (value instanceof Short) {
            	bdValue = new BigDecimal(((Short) value).intValue());
            }
            else if (value instanceof Double) {
            	Double d = (Double) value;
            	String s = d.toString();
            	
            	// try to convert directly
            	try {
            		/* Don't use
            		 * bdValue = new BigDecimal(d);
            		 * because it may cause to wrong values.
            		 * Example: 3.14 --> 3.140000000000000124344978758017532527446746826171875
            		 */
            		bdValue = new BigDecimal(s);
            	}
            	catch (NumberFormatException e) {
            		// in the case of d is infinite or NaN it is rounded with a precision of 8 
            		BigDecimal precisionFactor = BigDecimal.valueOf(100000000L);
            		long l = Math.round(d * precisionFactor.doubleValue());
            		bdValue = BigDecimal.valueOf(l);
            		bdValue = bdValue.divide(precisionFactor);
            	}
            }
            else if (value instanceof Float) {
            	Float f = (Float) value;
            	String s = f.toString();
            	
            	// try to convert directly
            	try {
            		bdValue = new BigDecimal(s);
            	}
            	catch (NumberFormatException e) {
            		// in the case of d is infinite or NaN it is rounded with a precision of 8
            		Double d = ((Float) value).doubleValue();
            		BigDecimal precisionFactor = BigDecimal.valueOf(100000000L);
            		long l = Math.round(d * precisionFactor.doubleValue());
            		bdValue = BigDecimal.valueOf(l);
            		bdValue = bdValue.divide(precisionFactor);
            	}
            }
            else if (value instanceof Boolean) {
                Boolean b = (Boolean) value;
                if (b.booleanValue()) {
                	bdValue = BigDecimal.ONE;
                }
                else {
                	bdValue = BigDecimal.ZERO;
                }
            }
            else if (value instanceof CurrencyAmount) {
                CurrencyAmount currencyAmount = (CurrencyAmount) value;
                bdValue = currencyAmount.getAmount();
            }
            else {
                String strValue = value.toString();
                if (strValue != null && strValue.length() > 0) {
                    try {
                        bdValue = new BigDecimal(strValue);
                    }
                    catch (Exception e) {
                    }
                    
                    if (bdValue == null) {
                    	strValue = removeWhiteSpaceForDoubleAndFloat(strValue);
                        try {
                            bdValue = new BigDecimal(strValue);
                        }
                        catch (Exception e) {
                        }
                    }
                    
                    if (bdValue == null) {
                        throw new ParseException("Value '" + value + "' could not be interpreted as BigDecimal.", 0);
                    }
                }
            }
        }
        return bdValue;
    }
    
    
    /**
     * Remove whitespace (space, line break, tabs and replace comma (",") by point (".").
     * @param value
     * @return the given value without whitespace
     */
    private static String removeWhiteSpaceForDoubleAndFloat(String value) {
        if (value == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(value.length());
        char[] charArray = value.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == ',') {
                sb.append('.');
            }
            else if ( ! Character.isWhitespace(charArray[i])) {
                sb.append(charArray[i]);
            }
        }
        return sb.toString();
    }

}
