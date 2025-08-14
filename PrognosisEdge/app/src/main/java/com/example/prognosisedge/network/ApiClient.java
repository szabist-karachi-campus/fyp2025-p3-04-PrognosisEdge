package com.example.prognosisedge.network;

import com.example.prognosisedge.AppConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    // Configure logging interceptor
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // Build OkHttpClient with SSL handling
                    OkHttpClient client = getUnsafeOkHttpClient()
                            .addInterceptor(loggingInterceptor) // Add logging interceptor
                            .build();

                    // Build Retrofit instance
                    retrofit = new Retrofit.Builder()
                            .baseUrl(AppConfig.BASE_URL) // Use base URL from AppConfig
                            .addConverterFactory(GsonConverterFactory.create()) // JSON deserializer
                            .client(client) // Attach OkHttpClient
                            .build();
                }
            }
        }
        return retrofit;
    }

    /**
     * Create an OkHttpClient that bypasses SSL verification for development only.
     */
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Build the OkHttpClient
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true); // Trust all hostnames
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}