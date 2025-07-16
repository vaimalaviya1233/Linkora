package com.sakethh.linkora.common.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object Network {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val standardClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    private var syncServerClient: HttpClient? = null

    fun getSyncServerClient(): HttpClient {
        return syncServerClient ?: error("syncServerClient isn't configured; make sure a valid certificate is imported.")
    }

    fun closeSyncServerClient(){
        syncServerClient?.close()
        syncServerClient = null
    }

    fun configureSyncServerClient(signedCertificate: X509Certificate) {
        if (syncServerClient != null) return

        syncServerClient = HttpClient(CIO) {
            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<out X509Certificate?>?, authType: String?
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<out X509Certificate?>?, authType: String?
                        ) {
                            if (chain?.isEmpty() == true) {
                                throw CertificateException("Certificate chain is empty") as Throwable
                            }

                            val serverCert = chain?.get(0)
                            serverCert?.verify(signedCertificate.publicKey)
                            serverCert?.checkValidity()
                        }

                        override fun getAcceptedIssuers(): Array<out X509Certificate?>? {
                            return arrayOf(signedCertificate)
                        }

                    }
                }
            }

            install(ContentNegotiation) {
                json(jsonConfig)
            }

            install(WebSockets) {
                pingIntervalMillis = 20_000
            }
        }
    }
}