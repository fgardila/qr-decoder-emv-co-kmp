package dev.code93.kmp.qrd.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.code93.android.emvreaderqr.MainViewModel
import dev.code93.kmp.qrd.android.Constants.BUTTON_TEXT_CAMERA_READER
import dev.code93.kmp.qrd.android.Constants.DATA_QR_DUMMY
import dev.code93.kmp.qrd.android.Constants.DIALOG_CANCEL_MESSAGE
import dev.code93.kmp.qrd.android.Constants.DIALOG_CANCEL_TITLE
import dev.code93.kmp.qrd.android.Constants.DIALOG_ERROR_MESSAGE
import dev.code93.kmp.qrd.android.Constants.DIALOG_ERROR_TITLE
import dev.code93.kmp.qrd.android.camera.CameraContract
import dev.code93.kmp.qrd.android.camera.ReadState

class MainActivity : ComponentActivity() {

    private lateinit var cameraContract: ActivityResultLauncher<Unit>

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraContract = registerForActivityResult(CameraContract()) { readState ->
            when (readState) {
                ReadState.Cancel -> viewModel.showDialog(
                    DIALOG_CANCEL_TITLE,
                    DIALOG_CANCEL_MESSAGE
                )

                ReadState.Error -> viewModel.showDialog(DIALOG_ERROR_TITLE, DIALOG_ERROR_MESSAGE)
                is ReadState.Read -> viewModel.processQRCode(readState.data)
            }
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QRReaderScreen(
                        modifier = Modifier.padding(innerPadding),
                        expandableList = viewModel.expandableList,
                        onMLKitButtonClick = {
                            if (true) {
                                // viewModel.processQRCode(DATA_QR_DUMMY)
                                cameraContract.launch(Unit)
                            } else {
                                cameraContract.launch(Unit)
                            }
                        }
                    )
                }
            }
        }
    }

    companion object {
        val alertDialogState = mutableStateOf(Triple(false, "", ""))
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Composable
fun QRReaderScreen(
    modifier: Modifier = Modifier,
    expandableList: List<ExpandableItem>,
    onMLKitButtonClick: () -> Unit
) {
    val alertDialogState = MainActivity.alertDialogState

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text(
                text = "EMV Colombia QR Reader Android",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onMLKitButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF6A1B9A))
            ) {
                Text(text = BUTTON_TEXT_CAMERA_READER, color = Color.White)
            }
        }

        items(expandableList.size) { item ->
            ExpandableCard(item = expandableList[item])
        }

        item {
            if (alertDialogState.value.first) {
                AlertDialog(
                    onDismissRequest = {
                        alertDialogState.value = alertDialogState.value.copy(first = false)
                    },
                    title = { Text(text = alertDialogState.value.second) },
                    text = { Text(text = alertDialogState.value.third) },
                    confirmButton = {
                        Button(onClick = {
                            alertDialogState.value = alertDialogState.value.copy(first = false)
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}


data class ExpandableItem(
    val title: String,
    val items: List<Pair<String, String?>> // Menu label and description
)

@Composable
fun ExpandableCard(item: ExpandableItem) {
    if (item.items.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }

        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        item.items.forEach { (label, description) ->
                            if (!description.isNullOrEmpty()) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = description,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
