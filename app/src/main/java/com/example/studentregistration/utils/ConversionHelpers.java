package com.example.studentregistration.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class ConversionHelpers {
    private String charsetName;

    /**
     * Initializes the ConversionHelpers object and sets the default charset to
     * utf-8
     */
    public ConversionHelpers() {
        this.charsetName = "utf-8";
    }

    /**
     * Initializes the ConversionHelpers object and sets the charset to the
     * parameter `charsetName`.
     *
     * @param charsetName name of the character set to use e.g: utf-8
     */
    public ConversionHelpers(String charsetName) {
        this.charsetName = charsetName;
    }

    /**
     * Converts a string to its hexadecimal representation.
     *
     * @param targetString string to convert to hexadecimal.
     * @return the converted hexadecimal representation of the string.
     * @throws UnsupportedEncodingException if the `charsetName` passed to the
     *                                      constructor is not supported.
     */
    public String toHex(String targetString) throws UnsupportedEncodingException {
        BigInteger bigInteger = new BigInteger(1, targetString.getBytes(charsetName));

        return String.format("%x", bigInteger);
    }

    /**
     * Converts a hexadecimal string back to the usual string representation.
     *
     * @param hexString hexadecimal string to convert back to the original string.
     * @return the original string.
     * @throws UnsupportedEncodingException if the `charsetName` passed to the
     *                                      constructor is not supported.
     */

    public String fromHex(String hexString) throws UnsupportedEncodingException {
        BigInteger bigInteger = new BigInteger(hexString, 16);

        return new String(bigInteger.toByteArray());
    }

    /**
     *
     * @return the charset used for converting to hexadecimal representation and
     *         converting from hexadecimal representation.
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     *
     * @param charsetName the charset to be used for converting to hexadecimal
     *                    representation and converting from hexadecimal
     *                    representation.
     */
    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public static String regToHex(String regNumber) {
        regNumber = regNumber.trim();

        // Convert the registration number to its hexadecimal representation to use as
        // the document id using the default utf-8 charset
        ConversionHelpers cHelpers = new ConversionHelpers();
        try {
            return cHelpers.toHex(regNumber);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}