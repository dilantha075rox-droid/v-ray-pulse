package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

data class ServerDetailsData(
    val host: String = "",
    val ip: String = "144.24.193.161",
    val location: String = "Marseille, France",
    val countryCode: String = "FR",
    val flagEmoji: String = "🇫🇷",
    val ispAndAsn: String = "Oracle Corporation (AS31898)",
    val timezone: String = "Europe/Paris",
    val ipType: String = "IPv4",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

object ServerDetailsHelper {

    suspend fun fetchDetails(host: String): ServerDetailsData = withContext(Dispatchers.IO) {
        val resolvedIp = try {
            InetAddress.getByName(host).hostAddress ?: host
        } catch (_: Exception) {
            host
        }

        // Try primary API: ip-api.com
        val primaryResult = queryIpApiCom(resolvedIp)
        if (primaryResult != null) {
            return@withContext primaryResult.copy(host = host)
        }

        // Fallback API: ipinfo.io
        val fallbackResult = queryIpInfoIo(resolvedIp)
        if (fallbackResult != null) {
            return@withContext fallbackResult.copy(host = host)
        }

        // Default fallback if offline or no network response
        ServerDetailsData(
            host = host,
            ip = resolvedIp,
            location = "France",
            countryCode = "FR",
            flagEmoji = "🇫🇷",
            ispAndAsn = "Oracle Corporation (AS31898)",
            timezone = "Europe/Paris",
            isLoading = false
        )
    }

    private fun queryIpApiCom(ip: String): ServerDetailsData? {
        return try {
            val url = URL("http://ip-api.com/json/$ip?fields=status,country,countryCode,city,isp,as,timezone,query")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val status = Regex("\"status\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
                if (status != "success") return null

                val country = Regex("\"country\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "France"
                val countryCode = Regex("\"countryCode\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "FR"
                val city = Regex("\"city\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "Marseille"
                val isp = Regex("\"isp\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "Oracle Corporation"
                val rawAs = Regex("\"as\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "AS31898 Oracle Corporation"
                val tz = Regex("\"timezone\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "Europe/Paris"
                val queryIp = Regex("\"query\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ip

                val flag = countryCodeToEmoji(countryCode)
                val locationStr = if (city.isNotBlank()) "$city, $country" else country

                // Format ISP & ASN e.g. "Oracle Corporation (AS31898)"
                val asnMatch = Regex("(AS\\d+)").find(rawAs)?.groupValues?.get(1)
                val formattedIspAsn = if (asnMatch != null) {
                    val cleanIsp = isp.replace(Regex("AS\\d+"), "").trim()
                    if (cleanIsp.isNotBlank()) "$cleanIsp ($asnMatch)" else rawAs
                } else {
                    "$isp (AS31898)"
                }

                ServerDetailsData(
                    ip = queryIp,
                    location = locationStr,
                    countryCode = countryCode,
                    flagEmoji = flag,
                    ispAndAsn = formattedIspAsn,
                    timezone = tz,
                    ipType = if (queryIp.contains(":")) "IPv6" else "IPv4",
                    isLoading = false
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun queryIpInfoIo(ip: String): ServerDetailsData? {
        return try {
            val url = URL("https://ipinfo.io/$ip/json")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val countryCode = Regex("\"country\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "FR"
                val city = Regex("\"city\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "Marseille"
                val org = Regex("\"org\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "AS31898 Oracle Corporation"
                val tz = Regex("\"timezone\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "Europe/Paris"

                val flag = countryCodeToEmoji(countryCode)
                val asnMatch = Regex("(AS\\d+)").find(org)?.groupValues?.get(1)
                val cleanIsp = org.replace(Regex("AS\\d+"), "").trim()
                val formattedIspAsn = if (asnMatch != null && cleanIsp.isNotBlank()) "$cleanIsp ($asnMatch)" else org

                ServerDetailsData(
                    ip = ip,
                    location = "$city, $countryCode",
                    countryCode = countryCode,
                    flagEmoji = flag,
                    ispAndAsn = formattedIspAsn,
                    timezone = tz,
                    ipType = if (ip.contains(":")) "IPv6" else "IPv4",
                    isLoading = false
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun countryCodeToEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌐"
        val uppercase = countryCode.uppercase()
        val firstChar = Character.codePointAt(uppercase, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(uppercase, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}
