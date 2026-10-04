package com.example.radar

data class PortStatus(
    val name: String,
    val statusText: String,
    val isOpen: Boolean
)

data class CellularRadarData(
    val isMasterOn: Boolean = true,
    val operatorName: String = "Dialog",
    val networkType: String = "5G NR Sub-6 / LTE-A",
    val signalDbm: Int = -82,
    val signalPercent: Int = 88,
    val signalRating: String = "EXCELLENT",
    val servingBand: String = "Band 3 (1800 MHz FDD)",
    val earfcn: Int = 1650,
    val pci: Int = 184,
    val timingAdvance: Int = 5,
    val towerDistanceMeters: Int = 390,
    val enbId: Long = 57926,
    val cellId: Long = 14829104,
    val rsrp: Int = -82,
    val rsrq: Int = -9,
    val sinr: Int = 18,
    val availableBands: List<String> = listOf("B3", "B1", "B8", "B40"),
    val activeBand: String = "B3",
    val carrierAggregation: String = "CA 2CC Active (B3 + B1)",
    val portStatusList: List<PortStatus> = listOf(
        PortStatus("Port 443 (HTTPS / VLESS Reality)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 8443 (Custom Secure Port)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 2053 (Cloudflare Proxy Port)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 2087 (VMess / CDN Port)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 80 (HTTP Fallback)", "OPEN / UNBLOCKED", true)
    )
)
