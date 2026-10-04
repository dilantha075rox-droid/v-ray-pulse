package com.example.radar

data class PortStatus(
    val name: String,
    val statusText: String,
    val isOpen: Boolean
)

data class CellularRadarData(
    val isMasterOn: Boolean = false, // OFF by default to save battery
    val operatorName: String = "Dialog",
    val networkType: String = "Dialog 4G • LTE-A 1800",
    val signalDbm: Int = -108,
    val signalPercent: Int = 66,
    val signalRating: String = "GOOD",
    val servingBand: String = "Band 3 (1800 MHz FDD)",
    val earfcn: Int = 1725,
    val pci: Int = 278,
    val tac: Int = 50037,
    val ci: Long = 6240512L,
    val enbId: Long = 24377L,
    val cellId: Long = 0L,
    val bandwidth: String = "20 + 15 + 5 MHz",
    val rssi: Int = -95,
    val rsrp: Int = -108,
    val rsrq: Int = -13,
    val sinr: Int = 4,
    val timingAdvance: Int = 15,
    val towerDistanceMeters: Int = 1200,
    val availableBands: List<String> = listOf("B3", "B1", "B8", "B40"),
    val activeBand: String = "B3",
    val carrierAggregation: String = "CA 2CC Active (B3 + B1)",
    val lastRefreshedText: String = "OFFLINE",
    val portStatusList: List<PortStatus> = listOf(
        PortStatus("Port 443 (HTTPS / VLESS Reality)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 8443 (Custom Secure Port)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 2053 (Cloudflare Proxy Port)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 2087 (VMess / CDN Port)", "OPEN / UNBLOCKED", true),
        PortStatus("Port 80 (HTTP Fallback)", "OPEN / UNBLOCKED", true)
    )
)
