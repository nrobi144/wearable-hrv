/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.nagyrobi144.wearable.hrv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.nagyrobi144.wearable.hrv.theme.HrvTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

const val TAG = "nrobi144 HRV Wearable"

@AndroidEntryPoint
class HrvActivity : ComponentActivity() {

    private val viewModel: HrvViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                when (result) {
                    true -> {
                        Log.i(TAG, "Body sensors permission granted")
                        viewModel.togglePassiveData(true)
                    }
                    false -> {
                        Log.i(TAG, "Body sensors permission not granted")
                        viewModel.togglePassiveData(false)
                    }
                }
            }
        setContent {
            WearApp(viewModel, permissionLauncher)
        }
    }
}

@Composable
fun WearApp(viewModel: HrvViewModel, permissionLauncher: ActivityResultLauncher<String>) {
    HrvTrackerTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        val context = LocalContext.current
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BODY_SENSORS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.togglePassiveData(false)
        }

        val isTrackingEnabled by viewModel.passiveDataEnabled.collectAsState()

        if (isTrackingEnabled) {
            val rMSSDs by viewModel.rMSSDs.collectAsState()

            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enable")
//                Checkbox(checked = isTrackingEnabled, onCheckedChange = viewModel::togglePassiveData)
                    }
                }
                items(rMSSDs) { rMSSD ->
                    Text(rMSSD.toString())
                }
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
        }
    }
}
