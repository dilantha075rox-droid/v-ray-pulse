package com.example.builder

import com.example.model.VlessConfig
import org.json.JSONArray
import org.json.JSONObject

object XrayConfigBuilder {

    /**
     * Builds a complete, valid Xray JSON configuration for the given [VlessConfig]
     * configured for Android TUN inbound routing.
     */
    fun build(config: VlessConfig): String {
        val root = JSONObject()

        // Log config
        val logObj = JSONObject()
        logObj.put("loglevel", "warning")
        root.put("log", logObj)

        // Inbounds - TUN device
        val inboundsArray = JSONArray()
        val tunInbound = JSONObject()
        tunInbound.put("tag", "tun")
        tunInbound.put("protocol", "tun")

        val tunSettings = JSONObject()
        tunSettings.put("name", "xray0")
        tunSettings.put("MTU", 1500)
        tunSettings.put("userLevel", 8)
        tunInbound.put("settings", tunSettings)

        val sniffing = JSONObject()
        sniffing.put("enabled", true)
        val destOverride = JSONArray()
        destOverride.put("http")
        destOverride.put("tls")
        destOverride.put("quic")
        sniffing.put("destOverride", destOverride)
        tunInbound.put("sniffing", sniffing)

        inboundsArray.put(tunInbound)
        root.put("inbounds", inboundsArray)

        // Outbounds - Primary VLESS + Direct + Block
        val outboundsArray = JSONArray()

        // 1. Primary VLESS outbound
        val vlessOutbound = JSONObject()
        vlessOutbound.put("tag", "proxy")
        vlessOutbound.put("protocol", "vless")

        // Settings / vnext
        val vnextArray = JSONArray()
        val serverObj = JSONObject()
        serverObj.put("address", config.server)
        serverObj.put("port", config.port)

        val usersArray = JSONArray()
        val userObj = JSONObject()
        userObj.put("id", config.uuid)
        userObj.put("encryption", config.encryption.ifBlank { "none" })
        if (!config.flow.isNullOrBlank()) {
            userObj.put("flow", config.flow)
        }
        userObj.put("level", 8)
        usersArray.put(userObj)

        serverObj.put("users", usersArray)
        vnextArray.put(serverObj)

        val vlessSettings = JSONObject()
        vlessSettings.put("vnext", vnextArray)
        vlessOutbound.put("settings", vlessSettings)

        // Stream Settings
        val streamSettings = JSONObject()
        val net = config.network.lowercase().ifBlank { "tcp" }
        streamSettings.put("network", net)

        val sec = config.security?.lowercase()
        if (sec == "reality") {
            streamSettings.put("security", "reality")
            val realityObj = JSONObject()
            realityObj.put("show", false)
            realityObj.put("fingerprint", config.fingerprint ?: "chrome")
            realityObj.put("serverName", config.sni ?: config.server)
            realityObj.put("publicKey", config.publicKey.orEmpty())
            realityObj.put("shortId", config.shortId.orEmpty())
            realityObj.put("spiderX", config.spiderX ?: "/")
            streamSettings.put("realitySettings", realityObj)
        } else if (sec == "tls") {
            streamSettings.put("security", "tls")
            val tlsObj = JSONObject()
            tlsObj.put("allowInsecure", false)
            tlsObj.put("serverName", config.sni ?: config.server)
            tlsObj.put("fingerprint", config.fingerprint ?: "chrome")
            if (!config.alpn.isNullOrBlank()) {
                val alpnArray = JSONArray()
                config.alpn.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                    alpnArray.put(it)
                }
                tlsObj.put("alpn", alpnArray)
            }
            streamSettings.put("tlsSettings", tlsObj)
        } else {
            streamSettings.put("security", "none")
        }

        // Transport specifics
        when (net) {
            "ws" -> {
                val wsObj = JSONObject()
                wsObj.put("path", config.path ?: "/")
                val headers = JSONObject()
                headers.put("Host", config.host ?: config.sni ?: config.server)
                wsObj.put("headers", headers)
                streamSettings.put("wsSettings", wsObj)
            }
            "grpc" -> {
                val grpcObj = JSONObject()
                grpcObj.put("serviceName", config.serviceName.orEmpty())
                grpcObj.put("authority", config.authority ?: config.sni ?: config.server)
                grpcObj.put("multiMode", false)
                streamSettings.put("grpcSettings", grpcObj)
            }
            "httpupgrade" -> {
                val httpUpgradeObj = JSONObject()
                httpUpgradeObj.put("path", config.path ?: "/")
                httpUpgradeObj.put("host", config.host ?: config.sni ?: config.server)
                streamSettings.put("httpupgradeSettings", httpUpgradeObj)
            }
            "xhttp" -> {
                val xhttpObj = JSONObject()
                xhttpObj.put("path", config.path ?: "/")
                xhttpObj.put("host", config.host ?: config.sni ?: config.server)
                streamSettings.put("xhttpSettings", xhttpObj)
            }
            "tcp" -> {
                if (config.headerType.equals("http", ignoreCase = true)) {
                    val tcpObj = JSONObject()
                    val header = JSONObject()
                    header.put("type", "http")
                    val req = JSONObject()
                    req.put("version", "1.1")
                    req.put("method", "GET")
                    val pathArr = JSONArray()
                    pathArr.put(config.path ?: "/")
                    req.put("path", pathArr)
                    val hdr = JSONObject()
                    val hostArr = JSONArray()
                    hostArr.put(config.host ?: config.sni ?: config.server)
                    hdr.put("Host", hostArr)
                    req.put("headers", hdr)
                    header.put("request", req)
                    tcpObj.put("header", header)
                    streamSettings.put("tcpSettings", tcpObj)
                }
            }
        }

        val sockopt = JSONObject()
        sockopt.put("domainStrategy", "UseIP")
        streamSettings.put("sockopt", sockopt)

        vlessOutbound.put("streamSettings", streamSettings)
        outboundsArray.put(vlessOutbound)

        // 2. Direct outbound
        val directOutbound = JSONObject()
        directOutbound.put("tag", "direct")
        directOutbound.put("protocol", "freedom")
        val directSettings = JSONObject()
        directSettings.put("domainStrategy", "UseIP")
        directOutbound.put("settings", directSettings)
        outboundsArray.put(directOutbound)

        // 3. Block outbound
        val blockOutbound = JSONObject()
        blockOutbound.put("tag", "block")
        blockOutbound.put("protocol", "blackhole")
        val blockSettings = JSONObject()
        val respObj = JSONObject()
        respObj.put("type", "http")
        blockSettings.put("response", respObj)
        blockOutbound.put("settings", blockSettings)
        outboundsArray.put(blockOutbound)

        root.put("outbounds", outboundsArray)

        // DNS
        val dnsObj = JSONObject()
        val dnsServers = JSONArray()
        dnsServers.put("1.1.1.1")
        dnsServers.put("8.8.8.8")
        dnsObj.put("servers", dnsServers)
        root.put("dns", dnsObj)

        // Routing
        val routingObj = JSONObject()
        routingObj.put("domainStrategy", "AsIs")
        routingObj.put("rules", JSONArray())
        root.put("routing", routingObj)

        return root.toString(2)
    }
}
