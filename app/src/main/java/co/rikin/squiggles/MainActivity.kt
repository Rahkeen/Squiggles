package co.rikin.squiggles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.rikin.squiggles.ui.components.NewSquigglySlider
import co.rikin.squiggles.ui.components.SquigglySlider
import co.rikin.squiggles.ui.theme.SquigglesTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SquigglesTheme {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
          verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          var sliderValue by remember { mutableStateOf(0.5F) }
          SquigglySlider(modifier = Modifier.fillMaxWidth(), value = sliderValue, onValueChanged = {sliderValue = it})
          NewSquigglySlider(modifier = Modifier.fillMaxWidth(), state = sliderValue, update = { sliderValue = it })
        }
      }
    }
  }
}
