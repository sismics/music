package com.sismics.music.resource

import android.content.Context
import android.os.Build
import com.androidquery.callback.AbstractAjaxCallback
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.PersistentCookieStore
import com.sismics.music.util.ApplicationUtil
import com.sismics.music.util.PreferenceUtil
import org.apache.http.conn.ssl.SSLSocketFactory
import java.io.IOException
import java.net.Socket
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Base class for API access.
 *
 * @author bgamard
 */
open class BaseResource {

    /**
     * Socket factory to allow self-signed certificates.
     *
     * @author bgamard
     */
    class MySSLSocketFactory @Throws(NoSuchAlgorithmException::class, KeyManagementException::class, KeyStoreException::class, UnrecoverableKeyException::class)
    constructor(truststore: KeyStore) : SSLSocketFactory(truststore) {
        internal var sslContext = SSLContext.getInstance("TLS")

        init {

            val tm = object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }
            }

            sslContext.init(null, arrayOf<TrustManager>(tm), null)
        }

        @Throws(IOException::class)
        override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket {
            return sslContext.socketFactory.createSocket(socket, host, port, autoClose)
        }

        @Throws(IOException::class)
        override fun createSocket(): Socket {
            return sslContext.socketFactory.createSocket()
        }
    }

    companion object {

        /**
         * User-Agent to use.
         */
        private var USER_AGENT: String? = null

        /**
         * Accept-Language header.
         */
        private var ACCEPT_LANGUAGE: String? = null

        /**
         * HTTP client.
         */
        var client = AsyncHttpClient()

        init {
            // 20sec default timeout
            client.timeout = 60000
            try {
                val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
                trustStore.load(null, null)
                val sf = MySSLSocketFactory(trustStore)
                sf.hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
                client.setSSLSocketFactory(sf)
                AbstractAjaxCallback.setSSF(sf)
            } catch (e: Exception) {
                // NOP
            }

        }

        /**
         * Resource initialization.
         *
         * @param context Context
         */
        fun init(context: Context) {
            val cookieStore = PersistentCookieStore(context)
            client.setCookieStore(cookieStore)

            if (USER_AGENT == null) {
                USER_AGENT = "Sismics Reader Android " + ApplicationUtil.getVersionName(context) + "/Android " + Build.VERSION.RELEASE + "/" + Build.MODEL
                client.setUserAgent(USER_AGENT)
            }

            if (ACCEPT_LANGUAGE == null) {
                val locale = Locale.getDefault()
                ACCEPT_LANGUAGE = locale.language + "_" + locale.country
                client.addHeader("Accept-Language", ACCEPT_LANGUAGE)
            }
        }

        /**
         * Returns cleaned API URL.
         *
         * @param context Context
         * @return Cleaned API URL
         */
        fun getApiUrl(context: Context): String? {
            val serverUrl = PreferenceUtil.getServerUrl(context) ?: return null

            return serverUrl + "/api"
        }
    }
}
