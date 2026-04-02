package com.trendhive.arsample.ar

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.trendhive.arsample.domain.model.PlacedObject

@Composable
actual fun PlatformARView(
    modifier: Modifier,
    placedObjects: List<PlacedObject>,
    onModelPlaced: (modelPath: String, posX: Float, posY: Float, posZ: Float, scale: Float) -> Unit,
    onModelRemoved: (anchorId: String) -> Unit,
    modelPathToLoad: String?,
    onObjectScaleChanged: (objectId: String, newScale: Float) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var arAvailability by remember { mutableStateOf(ArCoreApk.Availability.UNKNOWN_CHECKING) }
    var arError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        arAvailability = ArCoreApk.getInstance().checkAvailability(context)
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(arAvailability, hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect
        if (activity == null) {
            arError = "AR requires an Activity context."
            return@LaunchedEffect
        }

        if (arAvailability.isSupported) {
            try {
                val installStatus = ArCoreApk.getInstance()
                    .requestInstall(activity, /* userRequestedInstall = */ true)
                if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    // User will return after installing; we’ll retry on next recomposition.
                }
            } catch (t: Throwable) {
                arError = t.message ?: "Failed to initialize ARCore."
            }
        } else if (arAvailability.isUnsupported) {
            arError = "This device does not support ARCore."
        }
    }

    when {
        arError != null -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(arError ?: "AR error")
            }
        }
        !hasCameraPermission -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission required for AR")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
        arAvailability.isSupported -> {
            println("PlatformARView: Calling ARView with modelPathToLoad=$modelPathToLoad")
            ARView(
                modifier = modifier,
                placedObjects = placedObjects,
                onModelPlaced = onModelPlaced,
                onModelRemoved = onModelRemoved,
                modelPathToLoad = modelPathToLoad,
                onObjectScaleChanged = onObjectScaleChanged
            )
        }
        else -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Checking AR availability…")
            }
        }
    }
}
