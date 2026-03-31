package com.gemspa.loomseeds_alpha.ui.views

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.gemspa.loomseeds_alpha.models.Opcion
import com.gemspa.loomseeds_alpha.models.Pregunta
import com.gemspa.loomseeds_alpha.models.Quiz
import kotlinx.coroutines.launch

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizPreview() {
    val dummyQuiz = Quiz(
        titulo = "Backend Java & Spring",
        tiempo = 30,
        preguntas = listOf(
            Pregunta(
                texto = "¿Cuál es la anotación para crear un controlador REST en Spring Boot?",
                imagen = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=",
                opciones = listOf(
                    Opcion("RestController", true),
                    Opcion("Controller", false),
                    Opcion("Service", false)
                )
            )
        )
    )

    val dummyLista = listOf("backend_java.json", "android_kotlin.json", "sql_expert.json")

    MaterialTheme {
        QuizScreen(
            quiz = dummyQuiz,
            listaDeNombres = dummyLista,
            tiempoRestante = "30:00",
            onQuizSelected = {},
            onConfirmStart = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quiz: Quiz,
    listaDeNombres: List<String>,
    tiempoRestante: String,
    onQuizSelected: (String) -> Unit,
    onConfirmStart: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var cuestionarioPendiente by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmación") },
            text = { Text("¿Deseas realizar el cuestionario: ${cuestionarioPendiente.replace(".json", "").replace("_", " ").uppercase()}?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    onQuizSelected(cuestionarioPendiente)
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("No") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Mis Cuestionarios",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                LazyColumn {
                    itemsIndexed(listaDeNombres) { _, nombreArchivo ->
                        val tituloLimpio = nombreArchivo.replace(".json", "").replace("_", " ").uppercase()

                        NavigationDrawerItem(
                            label = { Text(text = tituloLimpio) },
                            selected = false,
                            onClick = {
                                cuestionarioPendiente = nombreArchivo
                                showDialog = true
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(quiz.titulo, style = MaterialTheme.typography.titleMedium)
                            Text("${quiz.preguntas.size} preguntas", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        Text(
                            text = tiempoRestante,
                            modifier = Modifier.padding(16.dp),
                            color = if (tiempoRestante.startsWith("00:")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(quiz.preguntas) { index, pregunta ->
                    PreguntaCard(index + 1, pregunta)
                }

                item {
                    Button(
                        onClick = { /* Finalizar */ },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Finalizar Evaluación")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PreguntaCard(numero: Int, pregunta: Pregunta) {
    val esMultiple = remember(pregunta) {
        pregunta.opciones.count { it.esCorrecta } > 1
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }
    val selectedIndices = remember { mutableStateListOf<Int>() }

    val bitmap = remember(pregunta.imagen) {
        pregunta.imagen?.let { base64String ->
            try {
                val pureBase64 = base64String.substringAfter(",")
                val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) { null }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$numero. ${pregunta.texto}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            )

            bitmap?.let { btm ->
                Spacer(modifier = Modifier.height(12.dp))
                GlideImage(
                    model = btm,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            pregunta.opciones.forEachIndexed { index, opcion ->
                val isSelected = if (esMultiple) selectedIndices.contains(index) else selectedIndex == index

                Surface(
                    onClick = {
                        if (esMultiple) {
                            if (selectedIndices.contains(index)) selectedIndices.remove(index)
                            else selectedIndices.add(index)
                        } else {
                            selectedIndex = index
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (esMultiple) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                        } else {
                            RadioButton(selected = isSelected, onClick = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = opcion.texto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBienvenida(
    listaDeNombres: List<String>,
    onQuizSelected: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Mis Cuestionarios",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()

                LazyColumn {
                    itemsIndexed(listaDeNombres) { _, nombre ->
                        NavigationDrawerItem(
                            label = { Text(nombre.replace(".json", "").uppercase()) },
                            selected = false,
                            onClick = {
                                onQuizSelected(nombre)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("LoomSeeds Alpha v1.5") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir Menú")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "¡Bienvenido!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Prepárate para tu próxima certificación.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Explorar Cuestionarios")
                    }
                }
            }
        }
    }
}