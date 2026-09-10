package com.example.safepath_test1.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.safepath_test1.model.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

private val SeoulFallback = Point.fromLngLat(126.9780, 37.5665)

@Composable
fun SafePathMapboxView(
    currentLocation: GeoPoint?,
    hasLocationPermission: Boolean,
    recenterToken: Int,
    modifier: Modifier = Modifier,
) {
    val centerPoint = currentLocation?.let {
        Point.fromLngLat(it.longitude, it.latitude)
    } ?: SeoulFallback

    var hasCentered by remember { mutableStateOf(false) }
    var lastRecenterToken by remember { mutableStateOf(recenterToken) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(centerPoint)
            zoom(15.5)
            pitch(0.0)
        }
    }

    LaunchedEffect(currentLocation, recenterToken) {
        val location = currentLocation ?: return@LaunchedEffect
        val point = Point.fromLngLat(location.longitude, location.latitude)
        val shouldRecenter = !hasCentered || recenterToken != lastRecenterToken
        if (!shouldRecenter) return@LaunchedEffect

        if (recenterToken != lastRecenterToken) {
            mapViewportState.transitionToFollowPuckState()
        } else {
            mapViewportState.setCameraOptions {
                center(point)
                zoom(15.5)
                pitch(0.0)
            }
        }
        hasCentered = true
        lastRecenterToken = recenterToken
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        compass = {},
        scaleBar = {},
        logo = {},
        attribution = {},
    ) {
        if (hasLocationPermission) {
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.HEADING
                    enabled = true
                }
            }
        }
    }
}
