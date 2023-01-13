package pseudoankit.droid.stopwatch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private var isStopwatchRunning = false
    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timeReceiver: BroadcastReceiver
    private var stopWatchText: MutableState<String> = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stopWatchText.value, color = Color.Black)

                Button(onClick = {
                    updateService(StopWatchAction.Reset)
                }) {
                    Text(text = "Reset")
                }

                Button(onClick = {
                    updateService(if (isStopwatchRunning) StopWatchAction.Pause else StopWatchAction.Start)
                }) {
                    Text(text = "Toggle")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateService(StopWatchAction.MoveToBackground)
    }

    override fun onResume() {
        super.onResume()
        updateService(StopWatchAction.GetStatus)
        listenStopWatchStatusFromService()
        listenStopWatchTimeFromService()
    }

    private fun listenStopWatchTimeFromService() {
        val timeFilter = IntentFilter()
        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)
        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0)!!
                updateStopwatchValue(timeElapsed)
            }
        }
        registerReceiver(timeReceiver, timeFilter)
    }

    private fun listenStopWatchStatusFromService() {
        val statusFilter = IntentFilter()
        statusFilter.addAction(StopwatchService.STOPWATCH_STATUS)
        statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context ?: return
                intent ?: return

                val isRunning = intent.getBooleanExtra(StopwatchService.IS_STOPWATCH_RUNNING, false)
                isStopwatchRunning = isRunning
                val timeElapsed = intent.getIntExtra(StopwatchService.TIME_ELAPSED, 0)

                updateStopwatchValue(timeElapsed)
            }
        }
        registerReceiver(statusReceiver, statusFilter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(statusReceiver)
        unregisterReceiver(timeReceiver)

        updateService(StopWatchAction.MoveToForeground)
    }

    private fun updateStopwatchValue(timeElapsed: Int) {
        val hours: Int = (timeElapsed / 60) / 60
        val minutes: Int = timeElapsed / 60
        val seconds: Int = timeElapsed % 60
        stopWatchText.value =
            "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
    }

    private fun updateService(action: StopWatchAction) {
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, action.name)
        startService(stopwatchService)
    }
}