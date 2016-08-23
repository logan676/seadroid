package com.seafile.seadroid2.ssl;

import android.util.Base64;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.seafile.seadroid2.account.Account;

import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class SSLTrustManager {
    public enum SslFailureReason {
        CERT_NOT_TRUSTED,
        CERT_CHANGED,
    }

    private static final String DEBUG_TAG = "SSLTrustManager";

    private X509TrustManager defaultTrustManager;

    private Map<Account, SecureX509TrustManager> keyManagers =
            Maps.newHashMap();

    private Map<Account, SecureX509TrustManager> managers =
            Maps.newHashMap();

    private Map<Account, SSLSocketFactory> cachedFactories =
            Maps.newHashMap();

    private static SSLTrustManager instance;

    private SSLTrustManager() {
    }

    public static synchronized SSLTrustManager instance() {
        if (instance == null) {
            instance = new SSLTrustManager();
            instance.init();
        }

        return instance;
    }

    private void init() {
        try {
            javax.net.ssl.TrustManagerFactory tmf;
            TrustManager[] tms;
            tmf = javax.net.ssl.TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);
            tms = tmf.getTrustManagers();
            if (tms != null) {
                for (TrustManager tm : tms) {
                    if (tm instanceof X509TrustManager) {
                        defaultTrustManager = (X509TrustManager) tm;
                        break;
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(DEBUG_TAG, "Unable to get X509 Trust Manager ", e);
        } catch (KeyStoreException e) {
            Log.e(DEBUG_TAG, "Key Store exception while initializing TrustManagerFactory ", e);
        }
    }

    public synchronized TrustManager[] getTrustManagers(Account account) {
        SecureX509TrustManager mgr = managers.get(account);
        if (mgr == null) {
            mgr = new SecureX509TrustManager(account);
            managers.put(account, mgr);
        }

        return new TrustManager[] {mgr};
    }

    public synchronized KeyManager[] getKeyManagers(File clientCertFile, String clientCertPassword) throws Exception {
        final KeyStore keyStore = loadPKCS12KeyStore(clientCertFile, clientCertPassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(keyStore, clientCertPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Produces a KeyStore from a PKCS12 (.p12) certificate file, typically the client certificate
     * @param certificateFile A file containing the client certificate
     * @param clientCertPassword Password for the certificate
     * @return A KeyStore containing the certificate from the certificateFile
     * @throws Exception
     */
    private KeyStore loadPKCS12KeyStore(File certificateFile, String clientCertPassword) throws Exception {
        KeyStore keyStore = null;
        FileInputStream fis = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            fis = new FileInputStream(certificateFile);
            keyStore.load(fis, clientCertPassword.toCharArray());
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch(IOException ex) {
                // ignore
            }
        }
        return keyStore;
    }

    /**
     * Produces a KeyStore from a String containing a PEM certificate (typically, the server's CA certificate)
     * @param certificateString A String containing the PEM-encoded certificate
     * @return a KeyStore (to be used as a trust store) that contains the certificate
     * @throws Exception
     */
    private KeyStore loadPEMTrustStore(String certificateString) throws Exception {

        byte[] der = loadPemCertificate(new ByteArrayInputStream(certificateString.getBytes()));
        ByteArrayInputStream derInputStream = new ByteArrayInputStream(der);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);

        return trustStore;
    }


    /**
     * Reads and decodes a base-64 encoded DER certificate (a .pem certificate), typically the server's CA cert.
     * @param certificateStream an InputStream from which to read the cert
     * @return a byte[] containing the decoded certificate
     * @throws IOException
     */
    byte[] loadPemCertificate(InputStream certificateStream) throws IOException {

        byte[] der = null;
        BufferedReader br = null;

        try {
            StringBuilder buf = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(certificateStream));

            String line = br.readLine();
            while(line != null) {
                if(!line.startsWith("--")){
                    buf.append(line);
                }
                line = br.readLine();
            }

            String pem = buf.toString();
            der = Base64.decode(pem, Base64.DEFAULT);

        } finally {
            if(br != null) {
                br.close();
            }
        }

        return der;
    }

    public synchronized SSLSocketFactory getSSLSocketFactory(Account account, AuthenticationParameters authParams) {
        SSLSocketFactory factory = cachedFactories.get(account);

        if (factory != null) {
            return factory;
        }

        if (authParams != null) {
            final File clientCertFile = authParams.getClientCertificate();
            final String clientCertPassword = authParams.getClientCertificatePassword();

            try {
                KeyManager[] keyManagers = getKeyManagers(clientCertFile, clientCertPassword);
                Log.d(DEBUG_TAG, "a keyManagers is created");

                /*TrustManager[] mgrs = getTrustManagers(account);*/
                final KeyStore trustStore = loadPEMTrustStore("master-cacert.pem");
                TrustManager[] mgrs = {new CustomTrustManager(trustStore)};

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagers, mgrs, null);
                factory = sslContext.getSocketFactory();
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "error when create SSLSocketFactory", e);
            }
        }

        if (factory != null) {
            cachedFactories.put(account, factory);
        }

        return factory;
    }

    public synchronized SSLSocketFactory getSSLSocketFactory(Account account) {
        SSLSocketFactory factory = cachedFactories.get(account);

        if (factory != null) {
            return factory;
        }

        try {
            TrustManager[] mgrs = getTrustManagers(account);
            factory = new SSLSeafileSocketFactory(null, mgrs, new SecureRandom());
            Log.d(DEBUG_TAG, "a SSLSocketFactory is created:" + factory);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "error when create SSLSocketFactory", e);
        }

        if (factory != null) {
            cachedFactories.put(account, factory);
        }

        return factory;
    }

    public List<X509Certificate> getCertsChainForAccount(Account account) {
        SecureX509TrustManager mgr = managers.get(account);
        if (mgr == null) {
            return null;
        }

        return mgr.getServerCertsChain();
    }
    
    public X509Certificate getCertificateInfo(Account account) throws CertificateParsingException {
        List<X509Certificate> certs = getCertsChainForAccount(account);
        if (certs == null || certs.size() == 0) {
            return null;
        }
        final X509Certificate cert = certs.get(0);
        return cert;
    }

    public SslFailureReason getFailureReason(Account account) {
        SecureX509TrustManager mgr = managers.get(account);
        SslFailureReason reason = null;
        if (mgr != null) {
            reason = mgr.getReason();
        }

        return reason != null ? reason : SslFailureReason.CERT_NOT_TRUSTED;
    }

    /**
     * Reorder the certificates chain, since it may not be in the right order when passed to us
     * @see http://stackoverflow.com/questions/7822381/need-help-understanding-certificate-chains
     */
    public List<X509Certificate> orderCerts(X509Certificate[] certificates) {
        if (certificates == null || certificates.length == 0) {
            return ImmutableList.of();
        }

        Set<X509Certificate> all = Sets.newHashSet(certificates);

        List<X509Certificate> certs = Lists.newArrayList(all);
        // certs.addAll(Arrays.asList(certificates));
        X509Certificate certChain = certs.get(0);
        certs.remove(certChain);
        LinkedList<X509Certificate> chainList= Lists.newLinkedList();
        chainList.add(certChain);
        Principal certIssuer = certChain.getIssuerDN();
        Principal certSubject = certChain.getSubjectDN();
        while(!certs.isEmpty()){
            List<X509Certificate> tempcerts = ImmutableList.copyOf(certs);
            for (X509Certificate cert : tempcerts) {
                if(cert.getIssuerDN().equals(certSubject)){
                    chainList.addFirst(cert);
                    certSubject = cert.getSubjectDN();
                    certs.remove(cert);
                    continue;
                }

                if(cert.getSubjectDN().equals(certIssuer)){
                    chainList.addLast(cert);
                    certIssuer = cert.getIssuerDN();
                    certs.remove(cert);
                    continue;
                }
            }
        }

        return chainList;
    }

    private class SecureX509TrustManager implements X509TrustManager {
        private Account account;
        private SslFailureReason reason;

        private volatile List<X509Certificate> certsChain = ImmutableList.of();

        public SecureX509TrustManager(Account account) {
            this.account = account;
            Log.d(DEBUG_TAG, "a SecureX509TrustManager is created:" + hashCode());
        }

        public List<X509Certificate> getServerCertsChain() {
            return certsChain;
        }

        public SslFailureReason getReason() {
            return reason;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            defaultTrustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            if (chain == null || chain.length == 0) {
                defaultTrustManager.checkServerTrusted(chain, authType);
                return;
            }

            List<X509Certificate> orderedChain = orderCerts(chain);
            try {
                // First try to do default check
                defaultTrustManager.checkServerTrusted(chain, authType);
                // Second check if hostname is valid
                validateHostName(orderedChain);
            } catch (CertificateException e) {
                customCheck(orderedChain, authType);
            }
        }

        public String getCeritificateInfo() throws CertificateParsingException {
            X509Certificate cert = CertsManager.instance().getCertificate(account);
            return "sigalgName:" + cert.getSigAlgName() + " Type: "
                    + cert.getType() + " Version: " + cert.getVersion()
                    + " IssuerAlternative: " + cert.getIssuerAlternativeNames()
                    + " NotAfter: " + cert.getNotAfter();
        }

        /**
         * Interface for checking if a hostname matches the names stored inside the server's X.509 certificate
         */
        private void validateHostName(List<X509Certificate> chain) throws CertificateException {
            X509Certificate cert = chain.get(0);
            // BrowserCompatHostnameVerifier can verify hostnames in the form of IP addresses (like a browser)
            // where as the DefaultHostnameVerifier will always try to lookup IP addresses via the DNS.
            X509HostnameVerifier mHostnameVerifier = new BrowserCompatHostnameVerifier();
            try {
                mHostnameVerifier.verify(account.getServerDomainName(), cert);
            } catch (SSLException e) {
                throw new CertificateException();
            }
        }

        private void customCheck(List<X509Certificate> chain, String authType)
            throws CertificateException {

            certsChain = ImmutableList.copyOf(chain);

            X509Certificate cert = chain.get(0);

            X509Certificate savedCert = CertsManager.instance().getCertificate(account);
            if (savedCert == null) {
                Log.d(DEBUG_TAG, "no saved cert for " + account.server);
                reason = SslFailureReason.CERT_NOT_TRUSTED;
                throw new CertificateException();
            } else if (savedCert.equals(cert)) {
                // The user has confirmed to trust this certificate
                Log.d(DEBUG_TAG, "the cert of " + account.server + " is trusted");
                return;
            } else {
                // The certificate is different from the one user confirmed to trust,
                // This may be either:
                // 1. The server admin has changed its cert
                // 2. The user is under security attack
                Log.d(DEBUG_TAG, "the cert of " + account.server + " has changed");
                reason = SslFailureReason.CERT_CHANGED;
                throw new CertificateException();
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return defaultTrustManager.getAcceptedIssuers();
        }

        @Override
        protected void finalize() {
            Log.d(DEBUG_TAG, "a SecureX509TrustManager is finalized:" + hashCode());
        }
    }
    
    public Map<Account, SSLSocketFactory> getCachedFactories() {
        return cachedFactories;
    }

    /*public void setCachedFactories(Map<Account, SSLSocketFactory> cachedFactories) {
        this.cachedFactories = cachedFactories;
    }*/
}
