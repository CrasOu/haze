package org.haze.catalina.container;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.haze.base.util.Debug;
import org.haze.base.util.SSLUtil;
import org.haze.base.util.UtilValidate;

/**
 * SSLImpl
 */
public class SSLImpl extends JSSEImplementation {

    public static final String module = SSLImpl.class.getName();
    protected ServerSocketFactory ssFactory = null;
    protected TrustManager[] allow;

    public SSLImpl() throws ClassNotFoundException {
        super();
        this.allow =  new TrustManager[] { new AllowTrustManager() };
        Debug.logInfo("SSLImpl loaded; using custom ServerSocketFactory", module);
    }

    @Override
    public ServerSocketFactory getServerSocketFactory(AbstractEndpoint endpoint) {
        if (UtilValidate.isEmpty(this.ssFactory)) {
            this.ssFactory = (new JSSEImplementation()).getServerSocketFactory(endpoint);
        }
        return ssFactory;
    }

    class AllowTrustManager implements X509TrustManager {

        private TrustManager[] tm;

        public AllowTrustManager() throws ClassNotFoundException {
            try {
                tm = SSLUtil.getTrustManagers();
            } catch (Exception e) {
                Debug.logError(e, module);
                throw new ClassNotFoundException(e.getMessage());
            }
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String string) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String string) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return ((X509TrustManager) tm[0]).getAcceptedIssuers();
        }
    }
}
