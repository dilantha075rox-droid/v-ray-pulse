package com.example

import com.example.parser.VlessParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseVlessRealityTcp() {
        val url = "vless://b831381d-6324-4d53-ad4f-8cda48b30811@147.45.43.12:443?security=reality&encryption=none&pbk=1JpGZ9kK6qYw51XyA&sid=abcd1234&type=tcp&flow=xtls-rprx-vision&sni=example.com&fp=chrome#TestServer"
        val config = VlessParser.parse(url)

        assertEquals("b831381d-6324-4d53-ad4f-8cda48b30811", config.uuid)
        assertEquals("147.45.43.12", config.server)
        assertEquals(443, config.port)
        assertEquals("reality", config.security)
        assertEquals("tcp", config.network)
        assertEquals("xtls-rprx-vision", config.flow)
        assertEquals("example.com", config.sni)
        assertEquals("chrome", config.fingerprint)
        assertEquals("1JpGZ9kK6qYw51XyA", config.publicKey)
        assertEquals("abcd1234", config.shortId)
        assertEquals("TestServer", config.remarks)
    }

    @Test
    fun parseVlessWebSocketTls() {
        val url = "vless://a1b2c3d4-e5f6-7890-abcd-ef1234567890@cf.example.com:8443?type=ws&security=tls&path=%2Fvless-ws&host=origin.example.com&sni=sni.example.com#CloudflareNode"
        val config = VlessParser.parse(url)

        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", config.uuid)
        assertEquals("cf.example.com", config.server)
        assertEquals(8443, config.port)
        assertEquals("ws", config.network)
        assertEquals("tls", config.security)
        assertEquals("/vless-ws", config.path)
        assertEquals("origin.example.com", config.host)
        assertEquals("sni.example.com", config.sni)
        assertEquals("CloudflareNode", config.remarks)
    }

    @Test
    fun parseVlessGrpcReality() {
        val url = "vless://c1d2e3f4-5678-90ab-cdef-1234567890ab@grpc.example.org:443?security=reality&type=grpc&serviceName=vless-grpc&pbk=AbCdEfGhIjKlMnOp&sni=zoom.us#GrpcNode"
        val config = VlessParser.parse(url)

        assertEquals("c1d2e3f4-5678-90ab-cdef-1234567890ab", config.uuid)
        assertEquals("grpc.example.org", config.server)
        assertEquals(443, config.port)
        assertEquals("grpc", config.network)
        assertEquals("reality", config.security)
        assertEquals("vless-grpc", config.serviceName)
        assertEquals("zoom.us", config.sni)
        assertEquals("AbCdEfGhIjKlMnOp", config.publicKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseInvalidSchemeThrows() {
        VlessParser.parse("vmess://invalid_url_format")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseMissingUuidThrows() {
        VlessParser.parse("vless://@example.com:443")
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseMissingHostThrows() {
        VlessParser.parse("vless://uuid-1234@:443")
    }
}
