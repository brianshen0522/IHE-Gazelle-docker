package net.ihe.gazelle.user.management.commons.interlay.cipher;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of HashPasswordService using MD5 hashing algorithm.
 */
public class MD5HashService implements HashPasswordService {

    public static final String MD5_HASH_METHOD = "MD5";

    /** Instantiates a new MD5 hash service. */
    public MD5HashService() {
        // No initialization required for MD5 hashing
    }

    @Override
    public boolean verify(Credentials credentials, String password) {
        if (password == null)
            throw new IllegalArgumentException("Password is null");
        if (credentials == null || credentials.getPassword() == null)
            throw new IllegalArgumentException("Hashed password is null");

        return hash(password).getPassword().equals(credentials.getPassword());
    }

    @Override
    public Credentials hash(String password) {
        if (password == null)
            throw new IllegalArgumentException("Password is null");

        byte[] rawValueToHashBytes;
        try {
            rawValueToHashBytes = MessageDigest.getInstance(MD5_HASH_METHOD).digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new MD5HashException(e.getMessage());
        }

        //Add padding if the first byte start with 0, ex 05 .. .. .. etc
        String padding = "%0" + (rawValueToHashBytes.length << 1) + "x";

        String hashedPassword = String.format(padding, new BigInteger(1, rawValueToHashBytes));
        Credentials credentials = new Credentials(hashedPassword);
        credentials.setHashMethod(MD5_HASH_METHOD);
        return credentials;
    }

    @Override
    public String getHashMethodName() {
        return MD5_HASH_METHOD;
    }
}
