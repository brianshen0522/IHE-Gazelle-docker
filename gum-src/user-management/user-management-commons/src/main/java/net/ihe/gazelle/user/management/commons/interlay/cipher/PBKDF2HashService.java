package net.ihe.gazelle.user.management.commons.interlay.cipher;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;

/**
 * Implementation of HashPasswordService using PBKDF2 algorithm for hashing passwords.
 */
public class PBKDF2HashService implements HashPasswordService {

    private static final int KEY_LENGTH = 512;
    public static final String SALT_KEY_NAME = "salt";
    public static final String PBKDF2_HASH_METHOD = "PBKDF2";
    private final Random rand = new SecureRandom();

    // Default constructor
    public PBKDF2HashService() {
        // No initialization needed for now
    }

    @Override
    public Credentials hash(String password) {
        // Generate a random salt
        String salt = generateRandomSalt();
        // For iteration generate a random number between 10.000 and 60.000
        int iterations = (rand.nextInt(60000 - 10000) + 10000);

        return hashWithParameters(password, salt,iterations);
    }

    @Override
    public boolean verify(Credentials credentials, String password) {
        if (password ==null)
            throw new IllegalArgumentException("Password to verify is null");

        if (credentials.getSalt() == null)
            throw new IllegalArgumentException("Salt is null");
        if (credentials.getIterations() == null)
            throw new IllegalArgumentException("Number of iterations is null");

        return hashWithParameters(password,credentials.getSalt(), credentials.getIterations())
                .getPassword().equals(credentials.getPassword());
    }

    @Override
    public String getHashMethodName() {
        return PBKDF2_HASH_METHOD;
    }

    private String generateRandomSalt() {
        byte[] array = new byte[16];
        rand.nextBytes(array);
        return Base64.getEncoder().encodeToString(array);
    }

    private Credentials hashWithParameters(String password, String salt, int iterations) {
        if (password == null)
            throw new IllegalArgumentException("Password is null");
        try {
            // Hash the password
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),salt.getBytes(), iterations, KEY_LENGTH);
            SecretKey key = secretKeyFactory.generateSecret(spec);
            String hashedPassword = String.format("%x", new BigInteger(1, key.getEncoded()));

            // Create the credentials object
            Credentials credentials = new Credentials(hashedPassword);
            credentials.setSalt(salt);
            credentials.setIterations(iterations);
            credentials.setHashMethod(PBKDF2_HASH_METHOD);
            return credentials;
        } catch (NumberFormatException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PBKDF2HashException(e.getMessage());
        }
    }
}
