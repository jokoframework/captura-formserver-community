package py.com.sodep.mobileforms.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * Created by afeltes on 2/10/15.
 */
public class SecurityUtils {

    private final static int ITERATION_NUMBER = 1000;

    private static final String alphaNumerico = "0123456789ABCDEFGJKLMNOPQRSTUVWXYZabcdefgjklmnopqrstuvwxyz";

    protected static Logger logger = LoggerFactory
            .getLogger(SecurityUtils.class);
    private static final int ITERATIONS = 1000;
    private static final int KEY_LENGTH = 192; // bits


    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(
                passwordChars,
                saltBytes,
                ITERATIONS,
                KEY_LENGTH
        );
        logger.info("Salt: " + salt);
        SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hashedPassword = key.generateSecret(spec).getEncoded();
        return String.format("%x", new BigInteger(hashedPassword));
    }

    public static void main(String[] args) throws Exception{
        String salt= getRandomSalt();
        System.out.println(salt);
        System.out.println(hashPassword("password", salt));
    }
    /**
     * From a base 64 representation, returns the corresponding byte[]
     *
     * @param data String The base64 representation
     * @return byte[]
     * @throws IOException
     */
    public static byte[] base64ToByte(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(data);
    }

    /**
     * From a byte[] returns a base 64 representation
     *
     * @param data byte[]
     * @return String
     * @throws IOException
     */
    public static String byteToBase64(byte[] data) {
        BASE64Encoder endecoder = new BASE64Encoder();
        return endecoder.encode(data);
    }

    public static String getRandomSalt() {
        int maxChars = 64;
        return getRandomString(maxChars);
    }

    private static String getRandomString(int p_maxChars) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i=0; i< p_maxChars; i++) {
            int randomIndex = random.nextInt(alphaNumerico.length());
            sb.append(alphaNumerico.charAt(randomIndex));
        }
        return sb.toString();
    }

    public static String getRandomPassword() {
       return getRandomString(12);
    }
}
