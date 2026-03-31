package com.gemspa.loomseeds_alpha.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Quiz(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("tiempo") val tiempo: Int,
    @SerializedName("preguntas") val preguntas: List<Pregunta>
)

@Keep
data class Pregunta(
    @SerializedName("texto") val texto: String,
    @SerializedName("imagen") val imagen: String?,
    @SerializedName("opciones") val opciones: List<Opcion>
)

@Keep
data class Opcion(
    @SerializedName("texto") val texto: String,
    @SerializedName("esCorrecta") val esCorrecta: Boolean
)