package py.com.sodep.mobileforms.impl.license;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import py.com.sodep.mobileforms.api.exceptions.LicenseException;
import py.com.sodep.mobileforms.license.Constants;
import py.com.sodep.mobileforms.license.crypto.CryptoUtils;
import py.com.sodep.mobileforms.license.json.keys.KeyEnvelope;
import py.com.sodep.mobileforms.utils.SSLUtils;

@Component
public class KeyStore {
	
	private static final Logger logger = LoggerFactory.getLogger(KeyStore.class);

	@Value("${MOBILEFORMS_HOME}")
	private String mobileFormsHomePath;

	private volatile KeyPair keyPair;

	private volatile PublicKey lsPublicKey;

	private Object keyPairLock = new Object();

	private Object lsPublicKeyLock = new Object();

	@Autowired
	private RestOperations rest;

	/*
	 * Double-check idiom
	 * 
	 * Effective Java, p. 283 - 284
	 */
	public KeyPair getServerKeyPair() {
		// p. 284 explains why we use result
		// It's not strictly necessary but may improves performance
		// significantly
		KeyPair result = keyPair;
		if (result == null) {
			synchronized (keyPairLock) {
				result = keyPair;
				if (result == null) {
					try {
						File keysDir = new File(mobileFormsHomePath + "/license/.keys/fs");
						File publicKeyFile = new File(keysDir, "public.key");
						File privateKeyFile = new File(keysDir, "private.key");
						if (!publicKeyFile.exists() || !privateKeyFile.exists()) {
							throw new LicenseException("Server Key Pair files not found");
						}

						PublicKey lsPublicKey = getLicenseServerPublicKey();
						PublicKey serverPublicKey = getPublicKey(publicKeyFile, lsPublicKey);
						PrivateKey serverPrivateKey = getPrivateKey(privateKeyFile, lsPublicKey);

						keyPair = result = new KeyPair(serverPublicKey, serverPrivateKey);
					} catch (Exception e) {
						throw new LicenseException("Invalid Server KeyPair", e);
					}
				}
			}
		}
		return result;
	}

	private PrivateKey getPrivateKey(File privateKeyFile, PublicKey lsPublicKey) throws FileNotFoundException,
			IOException, InvalidKeySpecException {
		byte[] encryptedPrivateKeyBytes = CryptoUtils.getBytes(privateKeyFile);
		byte[] privateKeyBytes = CryptoUtils.decrypt(encryptedPrivateKeyBytes, lsPublicKey);
		PrivateKey serverPrivateKey = CryptoUtils.getPrivateKey(privateKeyBytes);
		return serverPrivateKey;
	}

	private PublicKey getPublicKey(File publicKeyFile, PublicKey lsPublicKey) throws FileNotFoundException,
			IOException, InvalidKeySpecException {
		byte[] encryptedPublicKeyBytes = CryptoUtils.getBytes(publicKeyFile);
		byte[] publicKeyBytes = CryptoUtils.decrypt(encryptedPublicKeyBytes, lsPublicKey);
		PublicKey serverPublicKey = CryptoUtils.getPublicKey(publicKeyBytes);
		return serverPublicKey;
	}

	private String getPublicKeyURL() {
		return Constants.LS_PUBLIC_KEY_URL;
	}

	public PublicKey getLicenseServerPublicKey() {
		PublicKey result = lsPublicKey;
		if (result == null) {
			synchronized (lsPublicKeyLock) {
				result = lsPublicKey;
				if (result == null) {
					try {
						String publicKeyURL = getPublicKeyURL();
						logger.info("GET License Server's public key : " + publicKeyURL);
						// Confiamos todos los certificados al comunicarnos por HTTPS
				        // http://stackoverflow.com/questions/23504819/how-to-disable-ssl-certificate-checking-with-spring-resttemplate
						if (publicKeyURL.startsWith("https://") && SSLUtils.turnOffSslChecking()) {
							((RestTemplate) rest).setRequestFactory(getRequestFactory());
						}
						KeyEnvelope envelope = rest.getForObject(publicKeyURL, KeyEnvelope.class);
						byte[] bytes = CryptoUtils.fromHexString(envelope.getKey());
						lsPublicKey = result = CryptoUtils.getPublicKey(bytes);
					} catch (Exception e) {
						throw new LicenseException("Error while getting the License Server Public Key", e);
					}
				}
			}
		}
		return result;
	}

	private ClientHttpRequestFactory getRequestFactory() {
		return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if(connection instanceof HttpsURLConnection ){
                    ((HttpsURLConnection) connection).setHostnameVerifier(SSLUtils.DO_NOT_VERIFY);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
	}

}
