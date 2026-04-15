package com.gemspa.loomseeds_alpha.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gemspa.loomseeds_alpha.models.Quiz
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

sealed class QuizState {
    object Empty : QuizState()
    object Loading : QuizState()
    data class Success(val quiz: Quiz) : QuizState()
    data class Error(val message: String) : QuizState()
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val gson = Gson()

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Empty)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _listaNombres = MutableStateFlow<List<String>>(emptyList())
    val listaNombres: StateFlow<List<String>> = _listaNombres.asStateFlow()

    private val _tiempoRestante = MutableStateFlow("00:00")
    val tiempoRestante: StateFlow<String> = _tiempoRestante.asStateFlow()

    private var timerJob: Job? = null

    init {
        cargarListaDeAssetsSoloNombres()
    }

    private fun cargarListaDeAssetsSoloNombres() {
        viewModelScope.launch {
            try {
                val files = context.assets.list("")?.filter { it.endsWith(".json") } ?: emptyList()
                _listaNombres.value = files
            } catch (e: Exception) {
                _quizState.value = QuizState.Error("Error al listar archivos: ${e.message}")
            }
        }
    }

    private fun iniciarTimer(minutos: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var segundosTotales = minutos * 60
            while (segundosTotales >= 0) {
                val mins = segundosTotales / 60
                val segs = segundosTotales % 60
                _tiempoRestante.value = String.format(Locale.getDefault(), "%02d:%02d", mins, segs)

                if (segundosTotales == 0) break

                delay(1000)
                segundosTotales--
            }
        }
    }

    fun loadQuizFromAsset(fileName: String) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            timerJob?.cancel()
            _tiempoRestante.value = "00:00"

            try {
                val quiz = withContext(Dispatchers.IO) {
                    val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                    gson.fromJson(jsonString, Quiz::class.java)
                }

                if (quiz != null) {
                    _quizState.value = QuizState.Success(quiz)
                    iniciarTimer(quiz.tiempo)
                } else {
                    _quizState.value = QuizState.Error("Formato de JSON inválido")
                }
            } catch (e: Exception) {
                _quizState.value = QuizState.Error("Error al cargar: ${e.message}")
            }
        }
    }

    fun resetQuizState() {
        timerJob?.cancel()
        _tiempoRestante.value = "00:00"
        _quizState.value = QuizState.Empty
    }
}