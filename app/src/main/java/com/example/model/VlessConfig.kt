package com.example.model

/**
 * Parsed VLESS configuration parameters.
 */
data class VlessConfig(
    val uuid: String,
    val server: String,
    val port: Int,
    val encryption: String = "none",
    val security: String? = null,
    val network: String = "tcp",
    val flow: String? = null,
    val sni: String? = null,
    val fingerprint: String? = null,
    val publicKey: String? = null,
    val shortId: String? = null,
    val spiderX: String? = null,
    val host: String? = null,
    val path: String? = null,
    val alpn: String? = null,
    val serviceName: String? = null,
    val authority: String? = null,
    val headerType: String? = null,
    val remarks: String = "",
    val rawUrl: String = ""
) {
    /**
     * Safe summary for logging that never leaks UUID or secret keys.
     */
    fun toSafeSummary(): String {
        val maskedServer = if (server.length > 6) {
            "${server.take(4)}...${server.takeLast(2)}"
        } else {
            "***"
        }
        val sec = security ?: "none"
        return "VLESS[$remarks] host=$maskedServer:$port net=$network sec=$sec"
    }

    val displayServer: String
        get() = server.ifBlank { "Unknown" }

    val displayPort: String
        get() = port.toString()

    val displayProtocol: String
        get() = "VLESS"

    val displayTransport: String
        get() = network.uppercase()

    val displaySecurity: String
        get() = (security ?: "NONE").uppercase()
}
