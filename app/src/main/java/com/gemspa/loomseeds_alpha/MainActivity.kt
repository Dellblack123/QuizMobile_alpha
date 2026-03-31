package com.gemspa.loomseeds_alpha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gemspa.loomseeds_alpha.viewmodel.QuizState
import com.gemspa.loomseeds_alpha.viewmodel.QuizViewModel
import com.gemspa.loomseeds_alpha.ui.theme.Loomseeds_alphaTheme
import com.gemspa.loomseeds_alpha.ui.views.PantallaBienvenida
import com.gemspa.loomseeds_alpha.ui.views.QuizScreen

class MainActivity : ComponentActivity() {

    private val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Loomseeds_alphaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val quizState by viewModel.quizState.collectAsState()
                    val listaCuestionarios by viewModel.listaNombres.collectAsState()
                    val tiempo by viewModel.tiempoRestante.collectAsState()

                    when (val state = quizState) {
                        is QuizState.Empty -> {
                            PantallaBienvenida(listaCuestionarios) {nombre ->
                                viewModel.loadQuizFromAsset(nombre)
                            }
                        }

                        is QuizState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is QuizState.Success -> {
                            QuizScreen(
                                quiz = state.quiz,
                                listaDeNombres = listaCuestionarios,
                                tiempoRestante = tiempo,
                                onQuizSelected = { nombre ->
                                    viewModel.loadQuizFromAsset(nombre)
                                },
                                onConfirmStart = {}
                            )
                        }
                        is QuizState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}