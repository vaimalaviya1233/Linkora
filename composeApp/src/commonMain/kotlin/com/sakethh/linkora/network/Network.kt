package com.sakethh.linkora.network

import com.sakethh.linkora.Localization
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.linkora.utils.getLocalizedString
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object Network {

    private fun HttpClientConfig<CIOEngineConfig>.installLogger() {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    linkoraLog("HTTP CLIENT:\n$message")
                }
            }
            level = LogLevel.ALL
        }
    }

    private fun HttpClientConfig<CIOEngineConfig>.installContentNegotiation() {
        val jsonConfig = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    val standardClient = HttpClient(CIO) {
        installContentNegotiation()
        installLogger()
    }

    private var syncServerClient: HttpClient? = null

    fun getSyncServerClient(): HttpClient {
        return syncServerClient
            ?: error(Localization.Key.SyncServerConfigurationError.getLocalizedString())
    }

    fun closeSyncServerClient() {
        syncServerClient?.close()
        syncServerClient = null
    }

    fun configureSyncServerClient(signedCertificate: X509Certificate?, bypassCertCheck: Boolean) {
        if (syncServerClient != null) return

        if (signedCertificate == null && !bypassCertCheck) {
            error(Localization.Key.SyncServerConfigurationError.getLocalizedString())
        }

        syncServerClient = HttpClient(CIO) {
            install(HttpTimeout) {
                this.socketTimeoutMillis = 240_000
                this.connectTimeoutMillis = 240_000
                this.requestTimeoutMillis = 240_000
            }
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
                            if (bypassCertCheck) {
                                linkoraLog("Bypassing checkServerTrusted")
                                return
                            }

                            if (chain?.isEmpty() == true) {
                                throw CertificateException("Certificate chain is empty") as Throwable
                            }

                            val serverCert = chain?.get(0)
                            signedCertificate?.let {
                                serverCert?.verify(it.publicKey)
                            }
                            serverCert?.checkValidity()
                        }

                        override fun getAcceptedIssuers(): Array<out X509Certificate?>? {
                            return if (bypassCertCheck) arrayOf() else arrayOf(signedCertificate)
                        }

                    }
                }
            }

            installContentNegotiation()
            installLogger()

            install(WebSockets) {
                pingIntervalMillis = 20_000
            }
        }
    }
}