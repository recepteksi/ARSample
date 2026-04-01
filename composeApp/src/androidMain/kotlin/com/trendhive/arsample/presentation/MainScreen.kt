package com.trendhive.arsample.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.trendhive.arsample.ar.ARView
import com.trendhive.arsample.domain.model.ModelType
import com.trendhive.arsample.domain.model.Vector3
import com.trendhive.arsample.presentation.viewmodel.ARViewModel
import com.trendhive.arsample.presentation.viewmodel.ObjectListViewModel
import com.trendhive.arsample.presentation.ui.screens.ObjectListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    objectListViewModel: ObjectListViewModel,
    arViewModel: ARViewModel
) {
    var showObjectList by remember { mutableStateOf(true) }
    var selectedModelPath by remember { mutableStateOf<String?>(null) }

    val objectListUiState by objectListViewModel.uiState.collectAsState()
    val arUiState by arViewModel.uiState.collectAsState()

    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showObjectList) {
        ObjectListScreen(
            uiState = objectListUiState,
            onObjectSelected = { objectId ->
                val obj = objectListUiState.objects.find { it.id == objectId }
                if (obj != null) {
                    selectedModelPath = obj.modelUri
                    showObjectList = false
                }
            },
            onStartAR = {
                if (hasCameraPermission) {
                    showObjectList = false
                }
            },
            onImportClick = { uri, name, type ->
                objectListViewModel.importObject(uri, name, type)
            },
            onDeleteObject = { objectListViewModel.deleteObject(it) }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AR View") },
                    actions = {
                        TextButton(onClick = { showObjectList = true }) {
                            Text("Done")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (hasCameraPermission) {
                    ARView(
                        modifier = Modifier.fillMaxSize(),
                        modelPathToLoad = selectedModelPath,
                        onModelPlaced = { modelPath, posX, posY, posZ, scale ->
                            // Find the object ID from the path
                            val obj = objectListUiState.objects.find { it.modelUri == modelPath }
                            obj?.let {
                                arViewModel.placeObject(
                                    objectId = it.id,
                                    position = Vector3(posX, posY, posZ)
                                )
                            }
                        },
                        onModelRemoved = { anchorId ->
                            // Handle model removal
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Camera permission required for AR")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}
