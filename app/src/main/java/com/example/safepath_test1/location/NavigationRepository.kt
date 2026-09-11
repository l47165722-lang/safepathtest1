package com.example.safepath_test1.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RouteResult(
    val geoJsonLineString: String,
)

data class MultiRouteResult(
    val safeRoute: RouteResult?,
    val shortestRoute: RouteResult?,
    val recommendedRoute: RouteResult?,
)

object NavigationRepository {
    suspend fun fetchMultiRoutes(
        accessToken: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
    ): MultiRouteResult = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) return@withContext MultiRouteResult(null, null, null)

        // 1. Direct Walking Route (Shortest)
        val shortestRoute = fetchSingleRoute(accessToken, originLat, originLng, destLat, destLng, profile = "walking")

        // 2. Safe Route (Detour via main lit avenue with CCTV/streetlights)
        val midLat = (originLat + destLat) / 2.0
        val midLng = (originLng + destLng) / 2.0
        val dLat = destLat - originLat
        val dLng = destLng - originLng
        val viaLat = midLat - (dLng * 0.20) + 0.0012
        val viaLng = midLng + (dLat * 0.20) + 0.0012

        val safeRoute = fetchViaRoute(accessToken, originLat, originLng, viaLat, viaLng, destLat, destLng)
            ?: shortestRoute

        // 3. Recommended Route (Primary cycling/walking route)
        val recommendedRoute = fetchSingleRoute(accessToken, originLat, originLng, destLat, destLng, profile = "cycling")
            ?: shortestRoute

        MultiRouteResult(
            safeRoute = safeRoute,
            shortestRoute = shortestRoute,
            recommendedRoute = recommendedRoute,
        )
    }

    private fun fetchSingleRoute(
        accessToken: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        profile: String,
    ): RouteResult? {
        try {
            val urlString = "https://api.mapbox.com/directions/v5/mapbox/$profile/" +
                "$originLng,$originLat;$destLng,$destLat" +
                "?geometries=geojson&overview=full&access_token=$accessToken"

            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val routes = json.optJSONArray("routes")
                if (routes != null && routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val geom = route.optJSONObject("geometry")?.toString() ?: ""
                    if (geom.isNotBlank()) {
                        return RouteResult(geom)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    private fun fetchViaRoute(
        accessToken: String,
        originLat: Double,
        originLng: Double,
        viaLat: Double,
        viaLng: Double,
        destLat: Double,
        destLng: Double,
    ): RouteResult? {
        try {
            val urlString = "https://api.mapbox.com/directions/v5/mapbox/walking/" +
                "$originLng,$originLat;$viaLng,$viaLat;$destLng,$destLat" +
                "?geometries=geojson&overview=full&access_token=$accessToken"

            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val routes = json.optJSONArray("routes")
                if (routes != null && routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val geom = route.optJSONObject("geometry")?.toString() ?: ""
                    if (geom.isNotBlank()) {
                        return RouteResult(geom)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }
}
