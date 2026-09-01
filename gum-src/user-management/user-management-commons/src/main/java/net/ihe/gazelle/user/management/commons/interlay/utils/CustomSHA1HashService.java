package net.ihe.gazelle.user.management.commons.interlay.utils;

import net.ihe.gazelle.user.management.commons.interlay.exceptions.CustomSHA1HashException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CustomSHA1HashService {

    public String encode(String value) {
        if (value==null)
            throw new IllegalArgumentException("Cannot hash null value");
        byte[] rawValueToHashBytes;
        try {
            rawValueToHashBytes = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new CustomSHA1HashException(e.getMessage());
        }

        //split the array in two parts
        byte[] hashedBytesFirstHalf= Arrays.copyOfRange(rawValueToHashBytes, 0, rawValueToHashBytes.length/2);
        byte[] hashedBytesSecondHalf= Arrays.copyOfRange(rawValueToHashBytes, rawValueToHashBytes.length/2, rawValueToHashBytes.length);

        //XOR between the two arrays and we store the result in hashedBytesFirstHalf
        for (int i = 0; i < hashedBytesFirstHalf.length; i++) {
            hashedBytesFirstHalf[i] ^= hashedBytesSecondHalf[i];
        }

        //Add padding if the first byte start with 0, ex 05 .. .. .. etc
        String padding = "%0" + (hashedBytesFirstHalf.length << 1) + "x";

        //Return the hex value
        return String.format(padding, new BigInteger(1, hashedBytesFirstHalf));
    }


    public boolean verify(String value, String hashedValue) {
        if (hashedValue==null)
            throw new IllegalArgumentException("Cannot verify null value");

        return encode(value).equals(hashedValue);
    }
}
