package co.rikin.squiggles

import android.graphics.Paint
import android.graphics.PathMeasure
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.rikin.squiggles.ui.theme.Pink80
import co.rikin.squiggles.ui.theme.Purple80
import co.rikin.squiggles.ui.theme.PurpleGrey40
import co.rikin.squiggles.ui.theme.PurpleGrey80
import co.rikin.squiggles.ui.theme.SquigglesTheme
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SquigglesTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          var sliderValue by remember { mutableStateOf(0.5F) }
          SquigglySlider(
            modifier = Modifier.wrapContentSize(),
            value = sliderValue,
            low = 0F,
            high = 64F,
            onValueChanged = { sliderValue = it / 64F }
          )
        }
      }
    }
  }
}


@Composable
fun SquigglySlider(
  modifier: Modifier,
  value: Float,
  low: Float = 0F,
  high: Float = 1F,
  color: Color = MaterialTheme.colorScheme.primary,
  animateWave: Boolean = true,
  onValueChanged: (Float) -> Unit,
) {

  val waveHeight by animateFloatAsState(
    targetValue = if (animateWave) 1 / 8f else 0f,
    animationSpec = tween(500),
    label = ""
  )

  val animatedValue by animateFloatAsState(
    targetValue = value,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessHigh
    ),
    label = ""
  )

  val animTransition = rememberInfiniteTransition()
  val anim = animTransition.animateFloat(
    initialValue = -1f,
    targetValue = 0f,
    animationSpec = InfiniteRepeatableSpec(
      animation = tween(1000, easing = LinearEasing),
    )
  )

  var width = remember { 1 }
  Box(
    modifier = modifier
      .onSizeChanged {
        width = it.width
      }
      .pointerInput(Unit) {
        detectDragGestures { change, _ ->
          val position = (change.position.x) / width // position on a scale from 0 to 1
          val settingsValue = (position * high).coerceIn(low, high)
          onValueChanged(settingsValue)
        }
      }
      .height(35.dp),
    contentAlignment = Alignment.Center
  ) {
    val path by remember { mutableStateOf(Path()) }
    Canvas(
      modifier = Modifier
        .clip(
          FractionClip(fraction = animatedValue, start = true)
        )
        .height(30.dp)
        .fillMaxWidth(),
      onDraw = {
        path.reset()
        path.moveTo(0f, size.height / 2)

        val inc = 12f
        var stepper = anim.value * (inc * 4)
        for (i in 1..(size.width / (inc * 4)).roundToInt() + 1) {

          path.quadraticBezierTo(
            x1 = stepper + (inc * 1),
            y1 = (size.height / 2) - (size.height * waveHeight),
            x2 = stepper + (inc * 2),
            y2 = size.height / 2,
          )

          path.quadraticBezierTo(
            x1 = stepper + (inc * 3),
            y1 = (size.height / 2) + (size.height * waveHeight),
            x2 = stepper + (inc * 4),
            y2 = size.height / 2,
          )

          stepper += (inc * 4)
        }

        drawPath(path = path, color = color, style = Stroke(10f, cap = StrokeCap.Round))
      }
    )

    // Clip Rect to
    Box(
      modifier = Modifier
        .clip(
          FractionClip(fraction = animatedValue, start = false)
        )
        .fillMaxWidth(0.95f)
        .height(2.dp)
        .background(
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f),
          shape = RoundedCornerShape(2.dp)
        )
    )

    // The Circle Knob
    Box(
      modifier = Modifier
        .align(
          BiasAlignment(
            horizontalBias = (animatedValue * 2) - 1f,
            verticalBias = 0f
          )
        )
        .size(30.dp)
        .background(color = color, shape = CircleShape)
    )
  }
}

class FractionClip(val fraction: Float, val start: Boolean) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
  ): Outline {
    return Outline.Rectangle(
      rect = Rect(
        left = when (start) {
          true -> 0f
          false -> size.width * fraction
        },
        top = 0f,
        right = when (start) {
          true -> size.width * fraction
          false -> size.width
        },
        bottom = size.height,
      )
    )
  }
}

//@Preview
//@Composable
//fun SquigglySliderPreview() {
//  SquigglesTheme() {
//    Box(
//      modifier = Modifier
//        .wrapContentHeight()
//        .fillMaxWidth()
//        .background(MaterialTheme.colorScheme.background),
//      contentAlignment = Alignment.Center
//    ) {
//      var sliderValue by remember { mutableStateOf(0.5F) }
//      SquigglySlider(
//        modifier = Modifier.wrapContentSize(),
//        value = sliderValue,
//        low = 0F,
//        high = 64F,
//        onValueChanged = { sliderValue = it / 64F }
//      )
//    }
//  }
//}
//
//@Composable
//fun GoalSlider(
//  low: Int,
//  high: Int,
//  current: Int,
//  sliderName: String,
//  color: Color = MaterialTheme.colorScheme.primary,
//  onUpdate: (Float) -> Unit
//) {
//  val progress by animateFloatAsState(targetValue = (current.toFloat() / high))
//
//  Column(
//    verticalArrangement = Arrangement.spacedBy(8.dp)
//  ) {
//    Text(
//      modifier = Modifier.padding(start = 8.dp),
//      text = sliderName,
//      fontSize = 16.sp,
//      color = MaterialTheme.colorScheme.primary
//    )
//    Row(
//      modifier = Modifier
//        .fillMaxWidth(),
//      verticalAlignment = Alignment.CenterVertically
//    ) {
//      SquigglySlider(
//        modifier = Modifier
//          .weight(1F)
//          .wrapContentHeight(),
//        value = progress,
//        low = low.toFloat(),
//        high = high.toFloat(),
//        color = color,
//        animateWave = true,
//        onValueChanged = { onUpdate(it) }
//      )
//      Text(
//        modifier = Modifier
//          .wrapContentSize()
//          .padding(start = 16.dp),
//        text = "$current oz",
//        fontSize = 16.sp,
//        textAlign = TextAlign.Start,
//        color = color
//      )
//    }
//  }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GoalSliderPreview() {
//  SquigglesTheme {
//    var progress by remember {
//      mutableStateOf(64)
//    }
//
//    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
//      GoalSlider(
//        low = 30,
//        high = 200,
//        current = progress,
//        sliderName = "Goal",
//        onUpdate = { progress = it.roundToInt() }
//      )
//    }
//  }
//}

//@Preview
//@Composable
//fun SinaSquiggles() {
//  Canvas(
//    modifier = Modifier
//      .fillMaxWidth()
//      .height(60.dp)
//      .background(color = Purple40)
//  ) {
//    val wavelength = size.width / 12
//    val centerY = size.height / 2
//    val increment = wavelength / 4
//    val waveHeight = 24f
//
//    var distance = 0f
//    var direction = 1
//    val path = Path().apply {
//      moveTo(x = distance, y = size.height / 2)
//      while (distance < size.width) {
//        quadraticBezierTo(
//          x1 = increment + distance,
//          y1 = centerY + waveHeight * direction,
//          x2 = increment * 2 + distance,
//          y2 = centerY
//        )
//
//        distance += wavelength / 2
//        direction *= -1
//      }
//    }
//    drawPath(
//      path = path,
//      color = Purple80,
//      style = Stroke(
//        width = 10f,
//        cap = StrokeCap.Round
//      )
//    )
//  }
//}
//
//private fun DrawScope.buildSquigglesFor(
//  bound: Rect,
//  waveOffset: Float = 0f,
//  wavelength: Float = 16.dp.toPx(),
//  amplitude: Float = 2.dp.toPx()
//): Path {
//  val segmentWidth = wavelength / 10
//  val numOfPoints = ceil(bound.width / segmentWidth).toInt() + 1
//
//  // I'm creating a new Path object for brevity, but you'll
//  // want to cache it somewhere to reuse across draw frames.
//  return Path().apply {
//    var pointX = bound.left
//    for (point in 0..numOfPoints) {
//      val proportionOfWavelength = (pointX - bound.left) / wavelength
//      val radiansX = proportionOfWavelength * 2.pi()
//      val offsetY = bound.center.y + (sin(radiansX + waveOffset) * amplitude)
//
//      when (point) {
//        0 -> moveTo(pointX, offsetY)
//        else -> lineTo(pointX, offsetY)
//      }
//      pointX += segmentWidth
//    }
//  }
//}

const val PI = Math.PI.toFloat()
fun Int.pi() = this * PI

//@Preview
//@Composable
//fun SaketSquiggles() {
//  val animationProgress by rememberInfiniteTransition().animateFloat(
//    initialValue = 0f,
//    targetValue = 1f,
//    animationSpec = infiniteRepeatable(
//      animation = tween(1_000, easing = LinearEasing),
//      repeatMode = RepeatMode.Restart
//    )
//  )
//
//  Canvas(
//    modifier = Modifier
//      .fillMaxWidth()
//      .height(60.dp)
//      .background(color = Pink40)
//  ) {
//    val wavelength = 32.dp.toPx()
//    val amplitude = 4.dp.toPx()
//    val path = buildSquigglesFor(
//      bound = Rect(
//        offset = Offset(0f, 0f),
//        size = size
//      ),
//      waveOffset = 2.pi() * animationProgress,
//      wavelength = wavelength,
//      amplitude = amplitude
//    )
//
//    drawPath(
//      path = path,
//      style = Stroke(
//        width = 10f,
//        cap = StrokeCap.Round,
//        pathEffect = PathEffect.cornerPathEffect(radius = amplitude)
//      ),
//      color = Pink80,
//    )
//  }
//}
//
@Composable
fun NewSquiggleSlider(modifier: Modifier = Modifier, state: Float, update: (Float) -> Unit) {
  var percent by remember { mutableStateOf(state) }

  val infiniteTransition = rememberInfiniteTransition()
  val wave by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1_500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )
  Box(
    modifier = modifier
      .height(35.dp)
      .background(color = PurpleGrey40)
      .pointerInput(Unit) {
        forEachGesture {
          awaitPointerEventScope {
            awaitFirstDown()
            do {
              val event = awaitPointerEvent()
              val x = event.changes.last().position.x.coerceIn(
                minimumValue = 0f,
                maximumValue = size.width.toFloat()
              )
              val normalizedX = x / size.width
              percent = normalizedX
              update(normalizedX)
            } while (event.changes.none { it.changedToUp() })
          }
        }
      }
      .drawBehind {
        val padding = 16.dp.toPx()
        val wavelength = 48.dp.toPx()
        val amplitude = 4.dp.toPx()
        val yShift = size.height / 2

        val segment = wavelength / 10f
        val numSegments = ceil(size.width / segment).toInt()
        var pointX = padding
        val phase = wave * 2.pi()
        val collectedPoints = mutableListOf<Offset>()
        val path = Path().apply {
          for (point in 0..numSegments) {
            val b = 2.pi() / wavelength
            val pointY = amplitude * sin((b * pointX) - phase) + yShift

            when (point) {
              0 -> moveTo(pointX, pointY)
              else -> lineTo(pointX, pointY)
            }

            collectedPoints.add(Offset(pointX, pointY))
            pointX += segment
          }
        }
        clipRect(right = size.width * percent - (padding / 2)) {
          drawPath(
            path = path,
            color = Purple80,
            style = Stroke(
              width = 15f,
              cap = StrokeCap.Round,
              pathEffect = PathEffect.cornerPathEffect(radius = amplitude)
            )
          )
        }
        clipRect(left = size.width * percent, right = size.width - padding) {
          drawLine(
            color = Color.LightGray,
            start = Offset(0f, yShift),
            end = Offset(size.width, yShift),
            strokeWidth = 5f
          )
        }
        val circleX = (size.width * percent).coerceIn(padding, size.width - padding)
        drawCircle(
          color = Purple80,
          radius = padding,
          center = Offset(circleX, yShift)
        )
      }
    )
}

@Preview
@Composable
fun NewSquiggleSliderPreview() {
  SquigglesTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(color = PurpleGrey40)
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      val low = 0
      val high = 128
      var percent by remember { mutableStateOf(0f) }
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        NewSquiggleSlider(
          modifier = Modifier.weight(.80f),
          state = percent,
          update = { percent = it }
        )
        Text(
          modifier = Modifier.weight(.20f),
          text = "${(percent * (high - low)).toInt()} oz",
          color = Purple80,
          fontSize = 16.sp,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Preview
@Composable
fun RebuildingSinaSquiggles() {
  val infiniteTransition = rememberInfiniteTransition()
  val wave by infiniteTransition.animateFloat(
    initialValue = -1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )
  Canvas(
    modifier = Modifier
      .fillMaxWidth()
      .height(60.dp)
  ) {
    val wavelength = 32.dp.toPx()
    val amplitude = 8.dp.toPx()
    val segment = wavelength / 4f
    val centerY = size.height / 2
    val step = wave * wavelength
    val points = mutableListOf<Offset>()

    var distance = 0f
    val path = Path().apply {
      reset()
      moveTo(step + 0f, centerY)
      points.add(Offset(step + 0f, centerY))
      while (distance < (size.width + wavelength)) {
        val x1 = segment + distance + step
        val x2 = segment * 2 + distance + step
        val x3 = segment * 3 + distance + step
        val x4 = segment * 4 + distance + step
        val y1 = centerY - amplitude
        val y2 = centerY
        val y3 = centerY + amplitude
        val y4 = centerY

        points.add(Offset(x1, y1))
        points.add(Offset(x2, y2))
        points.add(Offset(x3, y3))
        points.add(Offset(x4, y4))

        quadraticBezierTo(x1, y1, x2, y2)
        quadraticBezierTo(x3, y3, x4, y4)
        distance += wavelength
      }
    }

    clipRect {
      drawPath(path = path, color = Pink80, style = Stroke(width = 15f, cap = StrokeCap.Round))
//      points.forEach {
//        drawCircle(color = Color.Black, radius = 2f, center = it)
//      }
    }
  }
}


@Preview
@Composable
fun RebuildingSaketSquiggles() {
  val infiniteTransition = rememberInfiniteTransition()
  val wave by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )
  Canvas(
    modifier = Modifier
      .fillMaxWidth()
      .height(60.dp)
  ) {
    val wavelength = 32.dp.toPx()
    val amplitude = 4.dp.toPx()
    val yShift = size.height / 2

    val segment = wavelength / 10f
    val numSegments = ceil(size.width / segment).toInt()
    var pointX = 0f
    val phase = wave * 2.pi()
    val collectedPoints = mutableListOf<Offset>()
    val path = Path().apply {
      for (point in 0..numSegments) {
        val b = 2.pi() / wavelength
        val pointY = amplitude * sin((b * pointX) - phase) + yShift

        when (point) {
          0 -> moveTo(pointX, pointY)
          else -> lineTo(pointX, pointY)
        }

        collectedPoints.add(Offset(pointX, pointY))
        pointX += segment
      }
    }
    drawPath(
      path = path,
      color = Purple80,
      style = Stroke(
        width = 15f,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(radius = amplitude)
      )
    )

//    collectedPoints.forEach {
//      drawCircle(color = Color.Black, radius = 2f, center = it)
//    }
  }
}

@Preview
@Composable
fun PathEffectSquiggles() {
  val infiniteTransition = rememberInfiniteTransition()
  val phaseMultiplier by infiniteTransition.animateFloat(
    initialValue = -1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )

  Canvas(
    modifier = Modifier
      .fillMaxWidth()
      .height(60.dp)
  ) {
    val wavelength = 32.dp.toPx()
    val amplitude = 8.dp.toPx()
    val step = wavelength / 4
    val phase = wavelength * phaseMultiplier
    val centerY = size.height / 2
    val waveWidth = size.width + wavelength

    val wavePath = Path().apply {
      moveTo(-wavelength, 0f)
      relativeQuadraticBezierTo(step, amplitude, step * 2, 0f)
      relativeQuadraticBezierTo(step, -amplitude, step * 2, 0f)
    }

    val stampPath = Path()

    Paint().apply {
      style = Paint.Style.STROKE
      strokeWidth = 15f
      strokeCap = Paint.Cap.ROUND
      getFillPath(wavePath.asAndroidPath(), stampPath.asAndroidPath())
    }

    clipRect {
      drawLine(
        color = PurpleGrey80,
        start = Offset(-wavelength, centerY),
        end = Offset(waveWidth, centerY),
        strokeWidth = 0f,
        pathEffect = PathEffect.stampedPathEffect(
          shape = stampPath,
          advance = wavelength,
          phase = -phase,
          style = StampedPathEffectStyle.Morph
        )
      )
    }
  }
}

@Preview
@Composable
fun Squiggles() {
  SquigglesTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(color = PurpleGrey40)
        .padding(start = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(text = "Bezier Curves", color = Pink80, fontSize = 16.sp)
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          RebuildingSinaSquiggles()
        }
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(text = "Sin Wave", color = Purple80, fontSize = 16.sp)
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          RebuildingSaketSquiggles()
        }
      }
      Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(text = "Line + Stamp Path Effect", color = PurpleGrey80, fontSize = 16.sp)
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          PathEffectSquiggles()
        }
      }
    }
  }
}