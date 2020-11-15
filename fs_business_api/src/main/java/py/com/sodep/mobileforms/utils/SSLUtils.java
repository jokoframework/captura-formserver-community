package py.com.sodep.mobileforms.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SSLUtils {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(SSLUtils.class);
    
    public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
 
    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
 
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // Pass
        }
 
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // Pass
        }
    } };
 
    private SSLUtils(){
        throw new UnsupportedOperationException( "Do not instantiate libraries.");
    }
 
    public static boolean turnOffSslChecking() {
        try {
            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init( null, UNQUESTIONING_TRUST_MANAGER, null );
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            return true;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.warn("Could not turn off SSL checking");
            return false;
        }
    }
}
