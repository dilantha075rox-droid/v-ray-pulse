package com.example.parser

import com.example.model.VlessConfig
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object VlessParser {

    /**
     * Parses a standard VLESS URL into a [VlessConfig].
     * Example: vless://uuid@host:port?security=reality&encryption=none&pbk=...&type=tcp#ServerName
     *
     * @throws IllegalArgumentException if the URL format is invalid.
     */
    fun parse(rawInput: String): VlessConfig {
        val trimmed = rawInput.trim()
        if (!trimmed.startsWith("vless://", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid URL: must start with 'vless://'")
        }

        // Clean any spaces or carriage returns
        val sanitized = trimmed.replace("\\s+".toRegex(), "")

        // Parse URI
        val uri = try {
            URI(sanitized)
        } catch (e: Exception) {
            // Attempt fallback parsing if URI fails on special characters in query/fragment
            parseManually(sanitized)
        }

        val uuid: String
        val host: String
        val port: Int
        val queryMap: Map<String, String>
        val remarks: String

        if (uri is URI) {
            uuid = uri.userInfo.orEmpty()
            host = uri.host.orEmpty()
            port = if (uri.port > 0) uri.port else 443
            queryMap = parseQuery(uri.rawQuery)
            remarks = decode(uri.fragment.orEmpty())
        } else {
            val manual = uri as ManualParsedUri
            uuid = manual.uuid
            host = manual.host
            port = manual.port
            queryMap = manual.query
            remarks = manual.remarks
        }

        if (uuid.isBlank()) {
            throw IllegalArgumentException("Invalid VLESS configuration: UUID is missing")
        }
        if (host.isBlank()) {
            throw IllegalArgumentException("Invalid VLESS configuration: Server address is missing")
        }
        if (port !in 1..65535) {
            throw IllegalArgumentException("Invalid VLESS configuration: Port must be between 1 and 65535")
        }

        val encryption = queryMap["encryption"]?.ifBlank { "none" } ?: "none"
        val rawSecurity = queryMap["security"]?.lowercase()?.trim()
        val security = when (rawSecurity) {
            "reality" -> "reality"
            "tls" -> "tls"
            else -> if (rawSecurity.isNullOrBlank() || rawSecurity == "none") null else rawSecurity
        }

        val rawType = queryMap["type"] ?: queryMap["network"] ?: "tcp"
        val network = rawType.lowercase().trim()

        val sni = queryMap["sni"] ?: queryMap["serverName"]
        val fp = queryMap["fp"] ?: queryMap["fingerprint"]
        val pbk = queryMap["pbk"] ?: queryMap["publicKey"]
        val sid = queryMap["sid"] ?: queryMap["shortId"]
        val spx = queryMap["spx"] ?: queryMap["spiderX"]

        return VlessConfig(
            uuid = uuid,
            server = host,
            port = port,
            encryption = encryption,
            security = security,
            network = network,
            flow = queryMap["flow"]?.ifBlank { null },
            sni = sni?.ifBlank { null },
            fingerprint = fp?.ifBlank { null },
            publicKey = pbk?.ifBlank { null },
            shortId = sid?.ifBlank { null },
            spiderX = spx?.ifBlank { null },
            host = queryMap["host"]?.ifBlank { null },
            path = queryMap["path"]?.ifBlank { null },
            alpn = queryMap["alpn"]?.ifBlank { null },
            serviceName = queryMap["serviceName"]?.ifBlank { null },
            authority = queryMap["authority"]?.ifBlank { null },
            headerType = queryMap["headerType"]?.ifBlank { null },
            remarks = remarks.ifBlank { "$host:$port" },
            rawUrl = trimmed
        )
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<String, String>()
        val pairs = rawQuery.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf('=')
            if (idx > 0) {
                val key = decode(pair.substring(0, idx)).trim()
                val value = decode(pair.substring(idx + 1)).trim()
                result[key] = value
            } else if (pair.isNotBlank()) {
                result[decode(pair).trim()] = ""
            }
        }
        return result
    }

    private data class ManualParsedUri(
        val uuid: String,
        val host: String,
        val port: Int,
        val query: Map<String, String>,
        val remarks: String
    )

    private fun parseManually(url: String): ManualParsedUri {
        // format: vless://uuid@host:port?query#fragment
        val afterScheme = url.substringAfter("vless://")
        val fragmentSplit = afterScheme.split('#', limit = 2)
        val remarks = if (fragmentSplit.size > 1) decode(fragmentSplit[1]) else ""
        val withoutFragment = fragmentSplit[0]

        val querySplit = withoutFragment.split('?', limit = 2)
        val query = if (querySplit.size > 1) parseQuery(querySplit[1]) else emptyMap()
        val mainPart = querySplit[0]

        val atSplit = mainPart.split('@', limit = 2)
        val uuid = if (atSplit.size > 1) decode(atSplit[0]) else ""
        val hostPortPart = if (atSplit.size > 1) atSplit[1] else atSplit[0]

        val (host, port) = if (hostPortPart.startsWith("[")) {
            val closingBracket = hostPortPart.indexOf(']')
            val h = hostPortPart.substring(1, closingBracket)
            val pStr = hostPortPart.substringAfter("]:", "")
            val p = pStr.toIntOrNull() ?: 443
            Pair(h, p)
        } else {
            val colonIdx = hostPortPart.lastIndexOf(':')
            if (colonIdx > 0) {
                val h = hostPortPart.substring(0, colonIdx)
                val p = hostPortPart.substring(colonIdx + 1).toIntOrNull() ?: 443
                Pair(h, p)
            } else {
                Pair(hostPortPart, 443)
            }
        }

        return ManualParsedUri(uuid, host, port, query, remarks)
    }

    private fun decode(s: String): String {
        return try {
            URLDecoder.decode(s, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            s
        }
    }
}
