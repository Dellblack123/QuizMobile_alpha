package com.gemspa.loomseeds_alpha.ui.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemspa.loomseeds_alpha.models.Pregunta
import com.gemspa.loomseeds_alpha.models.Quiz
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quiz: Quiz,
    tiempoRestante: String,
    onBackToDashboard: () -> Unit
) {
    val azulPrimario = Color(0xFF0D47A1)
    val naranjaAccent = Color(0xFFFF9800)

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var isReviewMode by remember { mutableStateOf(false) }

    val userAnswers = remember { mutableStateMapOf<Int, List<Int>>() }

    val currentPregunta = quiz.preguntas[currentQuestionIndex]
    val esUltimaPregunta = currentQuestionIndex == quiz.preguntas.size - 1
    val selectedIndices = remember(currentQuestionIndex) { mutableStateListOf<Int>() }

    val bitmap = remember(currentPregunta.imagen) {
        currentPregunta.imagen?.let { base64String ->
            if (base64String.isNotEmpty()) {
                try {
                    val pureBase64 = base64String.substringAfter(",")
                    val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } catch (e: Exception) { null }
            } else null
        }
    }

    if (showScoreDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color.White,
            title = { Text("Evaluación Finalizada", color = azulPrimario, fontWeight = FontWeight.Bold) },
            text = { Text("Has obtenido un puntaje de $score sobre ${quiz.preguntas.size}.") },
            confirmButton = {
                Button(
                    onClick = {
                        showScoreDialog = false
                        isReviewMode = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = naranjaAccent)
                ) { Text("Revisar Respuestas", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = onBackToDashboard) { Text("Salir", color = azulPrimario) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReviewMode) "Revisión" else "Pregunta ${currentQuestionIndex + 1}/${quiz.preguntas.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        if (!isReviewMode) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = tiempoRestante,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = azulPrimario)
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                if (isReviewMode) {
                    Button(
                        onClick = onBackToDashboard,
                        modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = azulPrimario),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("VOLVER AL DASHBOARD", fontWeight = FontWeight.Bold) }
                } else {
                    Button(
                        onClick = {
                            userAnswers[currentQuestionIndex] = selectedIndices.toList()
                            if (selectedIndices.any { currentPregunta.opciones[it].esCorrecta }) score++
                            if (esUltimaPregunta) showScoreDialog = true else currentQuestionIndex++
                        },
                        modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = naranjaAccent),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedIndices.isNotEmpty()
                    ) {
                        Text(if (esUltimaPregunta) "FINALIZAR EVALUACIÓN" else "SIGUIENTE PREGUNTA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isReviewMode) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LeyendaItem(Color(0xFF4CAF50), "Correcta")
                        LeyendaItem(Color(0xFFFFD600), "Seleccionada")
                        LeyendaItem(Color(0xFFF44336), "Incorrecta")
                    }
                }
                itemsIndexed(quiz.preguntas) { index, pregunta ->
                    ReviewCard(
                        numero = index + 1,
                        pregunta = pregunta,
                        seleccionUser = userAnswers[index] ?: emptyList(),
                        azul = azulPrimario
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Text(
                                text = currentPregunta.texto,
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.titleLarge,
                                color = azulPrimario,
                                fontWeight = FontWeight.Medium
                            )

                            bitmap?.let { btm ->
                                androidx.compose.foundation.Image(
                                    bitmap = btm.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            if (currentPregunta.opciones.count { it.esCorrecta } > 1) {
                                Text(
                                    text = "Selección múltiple",
                                    modifier = Modifier.padding(start = 24.dp, bottom = 16.dp, top = 12.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = naranjaAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                itemsIndexed(currentPregunta.opciones) { index, opcion ->
                    val isSelected = selectedIndices.contains(index)
                    Surface(
                        onClick = {
                            val esMultipleOp = currentPregunta.opciones.count { it.esCorrecta } > 1
                            if (esMultipleOp) {
                                if (isSelected) selectedIndices.remove(index) else selectedIndices.add(index)
                            } else {
                                selectedIndices.clear()
                                selectedIndices.add(index)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, if (isSelected) naranjaAccent else Color.Transparent),
                        color = if (isSelected) Color(0xFFFFF8F1) else Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isSelected) naranjaAccent else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = ('A' + index).toString(), color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = opcion.texto, modifier = Modifier.weight(1f), color = if (isSelected) azulPrimario else Color.DarkGray)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ReviewCard(numero: Int, pregunta: Pregunta, seleccionUser: List<Int>, azul: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$numero. ${pregunta.texto}",
                fontWeight = FontWeight.Bold,
                color = azul,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            pregunta.opciones.forEachIndexed { index, opcion ->
                val fueElegida = seleccionUser.contains(index)
                val esCorrecta = opcion.esCorrecta

                val colorFondo = when {
                    esCorrecta -> Color(0xFFE8F5E9)
                    fueElegida && !esCorrecta -> Color(0xFFFFF9C4)
                    else -> Color(0xFFFFEBEE)
                }

                val colorBorde = when {
                    esCorrecta -> Color(0xFF4CAF50)
                    fueElegida && !esCorrecta -> Color(0xFFFFD600)
                    else -> Color(0xFFF44336)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = colorFondo,
                    border = BorderStroke(2.dp, colorBorde)
                ) {
                    Text(
                        text = opcion.texto,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = if (fueElegida || esCorrecta) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBienvenida(
    listaDeNombres: List<String>,
    onQuizSelected: (String) -> Unit
) {
    val azulPrimario = Color(0xFF0D47A1)
    val naranjaAccent = Color(0xFFFF9800)
    val backgroundGradient = Brush.verticalGradient(colors = listOf(Color(0xFFE3F2FD), Color.White))

    Scaffold(containerColor = Color.Transparent, modifier = Modifier.background(backgroundGradient)) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFBBDEFB), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = azulPrimario)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cesar Matos", fontWeight = FontWeight.Bold, color = azulPrimario)
                            Text("@BioCode.Builder", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                        }
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = naranjaAccent) {
                        Text("1200 PTS", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Column {
                    Text("Categorías", fontWeight = FontWeight.Bold, color = azulPrimario)
                    LazyRow(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val categorias = listOf("Java", "Kotlin", "Spring", "Docker")
                        items(categorias) { cat ->
                            Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 2.dp) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(when(cat) { "Java" -> "☕"; "Kotlin" -> "📱"; "Spring" -> "🍃"; else -> "⚙️" }, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) { Text("Retos Disponibles", fontWeight = FontWeight.Bold, color = azulPrimario) }

            items(listaDeNombres) { nombreArchivo ->
                val tituloLimpio = nombreArchivo.replace(".json", "").replace("_", " ").uppercase()
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onQuizSelected(nombreArchivo) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(modifier = Modifier.size(80.dp).padding(8.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFF1F8FE)) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = azulPrimario, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = tituloLimpio, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = azulPrimario, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "15 Questions", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = naranjaAccent, modifier = Modifier.size(14.dp))
                                Text(" 24K", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = naranjaAccent) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }
            }
            item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}