package com.rei.app.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.domain.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(vm: QuizViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Anime Quiz", fontWeight = FontWeight.Bold) },
            actions = {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFC107).copy(alpha = 0.15f)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("\u2605", fontSize = 14.sp, color = Color(0xFFFFC107))
                        Text("${state.score}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        )
    }) { pv ->
        Box(Modifier.fillMaxSize().padding(pv)) {
            when {
                state.isLoading -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Generating question...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                state.isFinished -> {
                    QuizResult(state.score, state.total, Modifier.align(Alignment.Center).padding(32.dp)) { vm.restart() }
                }
                state.question != null -> {
                    QuizQuestion(
                        question = state.question!!,
                        questionNum = state.currentQuestion,
                        total = state.total,
                        onAnswer = { vm.answer(it) },
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }
                else -> {
                    // Start screen
                    Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Box(Modifier.size(100.dp).clip(RoundedCornerShape(50.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Text("\u2726", fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("Anime Quiz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Text("Test your anime knowledge!\nGuess the anime from its cover and score.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("10", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+2", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
                                Text("Coins/Correct", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        FilledTonalButton({ vm.start() }, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Quiz", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizQuestion(question: QuizQuestionData, questionNum: Int, total: Int, onAnswer: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Progress
        LinearProgressIndicator(progress = { questionNum.toFloat() / total }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Text("Question $questionNum of $total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Cover image
        Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.size(200.dp)) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(question.coverUrl).crossfade(true).build(), contentDescription = "Anime cover", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }

        Text("What anime is this?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Answer options
        question.options.forEachIndexed { index, option ->
            val colors = when {
                question.selectedAnswer != null && index == question.correctIndex -> ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f), contentColor = Color(0xFF4CAF50))
                question.selectedAnswer == index && index != question.correctIndex -> ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFF5722).copy(alpha = 0.2f), contentColor = Color(0xFFFF5722))
                question.selectedAnswer != null -> ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                else -> ButtonDefaults.filledTonalButtonColors()
            }
            FilledTonalButton(
                onClick = { if (question.selectedAnswer == null) onAnswer(index) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = colors,
                enabled = question.selectedAnswer == null
            ) {
                Text(option, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun QuizResult(score: Int, total: Int, modifier: Modifier = Modifier, onRestart: () -> Unit) {
    val percent = (score.toFloat() / total * 100).toInt()
    val (emoji, color, title) = when {
        percent >= 80 -> Triple("\u2605", Color(0xFF4CAF50), "Anime Master!")
        percent >= 60 -> Triple("\u2726", Color(0xFF8BC34A), "Great Job!")
        percent >= 40 -> Triple("\u25C6", Color(0xFFFFC107), "Not Bad!")
        else -> Triple("\u25B3", Color(0xFFFF5722), "Keep Watching!")
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(emoji, fontSize = 48.sp, color = color)
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = color)
        Text("$score / $total correct", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("${percent}%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text("+${score * 2} Rei Coins earned!", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF7C4DFF), fontWeight = FontWeight.SemiBold)
        FilledTonalButton(onRestart, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Play Again", fontWeight = FontWeight.Bold)
        }
    }
}

data class QuizState(
    val isLoading: Boolean = false,
    val question: QuizQuestionData? = null,
    val currentQuestion: Int = 0,
    val total: Int = 10,
    val score: Int = 0,
    val isFinished: Boolean = false
)

data class QuizQuestionData(
    val coverUrl: String,
    val correctAnswer: String,
    val options: List<String>,
    val correctIndex: Int,
    val selectedAnswer: Int? = null
)
