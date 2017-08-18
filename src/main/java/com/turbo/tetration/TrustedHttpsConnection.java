package com.turbo.tetration;

import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustedHttpsConnection {

    public static HttpsURLConnection genConnection(String urlstr) {
        HttpsURLConnection conn = null;

        try {
            URL url = new URL(urlstr);
            conn = (HttpsURLConnection) url.openConnection();

            if (sslSocketFactory == null) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, ALL_TRUSTING_TRUST_MANAGER, new SecureRandom());
                sslSocketFactory = sc.getSocketFactory();
            }

            conn.setSSLSocketFactory(sslSocketFactory);
            conn.setHostnameVerifier(ALL_TRUSTING_HOSTNAME_VERIFIER);

        } catch (Exception e) {
            System.out.println("failed to create a HttpsConnection.");
            e.printStackTrace();
            conn = null;
        }

        return conn;
    }

    private  static SSLSocketFactory sslSocketFactory = null;

    private static final TrustManager[] ALL_TRUSTING_TRUST_MANAGER = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    private static final HostnameVerifier ALL_TRUSTING_HOSTNAME_VERIFIER  = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}