
package com.example.clima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cotacaodolar.Previsao
import com.example.cotacaodolar.PrevisaoRepository
import kotlinx.coroutines.launch

private val AzulEscuro = Color(0xFF071B38)
private val AzulMedio = Color(0xFF123D70)
private val AzulClaro = Color(0xFF6DD5FA)
private val Branco = Color(0xFFF4F8FF)
private val TextoSuave = Color(0xFFB8CBE4)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var cidadeTexto by remember {
                    mutableStateOf("")
                }

                var previsao by remember {
                    mutableStateOf<Previsao?>(null)
                }

                var carregando by remember {
                    mutableStateOf(false)
                }

                var mensagem by remember {
                    mutableStateOf(
                        "Digite uma cidade para descobrir a temperatura."
                    )
                }

                val escopo = rememberCoroutineScope()
                val repository = remember {
                    PrevisaoRepository()
                }

                val fundo = Brush.verticalGradient(
                    colors = listOf(
                        AzulEscuro,
                        Color(0xFF0B2A50),
                        Color(0xFF164A78)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fundo)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 32.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Cabeçalho
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☀",
                                    fontSize = 29.sp,
                                    color = Color(0xFFFFD166)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "CLIMA",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Branco,
                                    letterSpacing = 3.sp
                                )

                                Text(
                                    text = "PREVISÃO DO TEMPO",
                                    fontSize = 10.sp,
                                    color = TextoSuave,
                                    letterSpacing = 2.sp
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(42.dp))

                        // Título principal
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Como está",
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Light,
                                color = Branco
                            )

                            Text(
                                text = "o céu hoje?",
                                fontSize = 35.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AzulClaro
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Descubra a temperatura atual\n"
                                        + "da sua cidade em segundos.",
                                fontSize = 15.sp,
                                color = TextoSuave,
                                lineHeight = 23.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // Inout pesquisa
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF102D50)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.10f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = "LOCALIZAÇÃO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulClaro,
                                    letterSpacing = 1.5.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = cidadeTexto,
                                    onValueChange = {
                                        cidadeTexto = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Ex.: São Paulo",
                                            color = TextoSuave
                                        )
                                    },
                                    leadingIcon = {
                                        Text(
                                            text = "⌖",
                                            fontSize = 25.sp,
                                            color = AzulClaro
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            if (
                                                cidadeTexto.isNotBlank() &&
                                                !carregando
                                            ) {
                                                escopo.launch {
                                                    carregando = true
                                                    previsao = null

                                                    try {
                                                        previsao =
                                                            repository.buscarPrevisao(
                                                                cidadeTexto.trim()
                                                            )
                                                        mensagem =
                                                            "Consulta realizada com sucesso."
                                                    } catch (e: Exception) {
                                                        mensagem = e.message
                                                            ?: "Não foi possível consultar o clima."
                                                    } finally {
                                                        carregando = false
                                                    }
                                                }
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Branco,
                                        unfocusedTextColor = Branco,
                                        focusedBorderColor = AzulClaro,
                                        unfocusedBorderColor =
                                            Color.White.copy(alpha = 0.20f),
                                        cursorColor = AzulClaro,
                                        focusedContainerColor =
                                            Color(0xFF0A2342),
                                        unfocusedContainerColor =
                                            Color(0xFF0A2342)
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val cidade = cidadeTexto.trim()

                                        if (cidade.isBlank()) {
                                            mensagem =
                                                "Digite o nome de uma cidade."
                                            previsao = null
                                        } else {
                                            escopo.launch {
                                                carregando = true
                                                previsao = null
                                                mensagem =
                                                    "Buscando informações do clima..."

                                                try {
                                                    previsao =
                                                        repository.buscarPrevisao(
                                                            cidade
                                                        )
                                                    mensagem =
                                                        "Consulta realizada com sucesso."
                                                } catch (e: Exception) {
                                                    mensagem = e.message
                                                        ?: "Falha na consulta. Tente novamente."
                                                } finally {
                                                    carregando = false
                                                }
                                            }
                                        }
                                    },
                                    enabled = !carregando,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AzulClaro,
                                        contentColor = AzulEscuro
                                    )
                                ) {
                                    if (carregando) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = AzulEscuro,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(
                                            modifier = Modifier.width(10.dp)
                                        )
                                        Text("Consultando...")
                                    } else {
                                        Text(
                                            text = "Consultar temperatura  →",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Resultado da pesquisa
                        if (previsao != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFEAF6FF)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    Spacer(
                                        modifier = Modifier.height(20.dp)
                                    )

                                    Text(
                                        text = "☀️",
                                        fontSize = 64.sp
                                    )

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    Text(
                                        text = previsao!!.cidade,
                                        color = AzulEscuro,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    Row(
                                        verticalAlignment =
                                            Alignment.Top
                                    ) {
                                        Text(
                                            text = previsao!!.temperatura
                                                .toString(),
                                            color = AzulEscuro,
                                            fontSize = 54.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )

                                        Text(
                                            text = "°C",
                                            color = AzulMedio,
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Text(
                                        text = "Temperatura atual",
                                        color = AzulMedio,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else if (!carregando) {
                            // Estado inicial
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        Color.White.copy(alpha = 0.06f)
                                ),
                                border =
                                    androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = 0.10f)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🌤️",
                                        fontSize = 48.sp
                                    )

                                    Spacer(
                                        modifier = Modifier.height(12.dp)
                                    )

                                    Text(
                                        text = "Seu clima começa aqui",
                                        color = Branco,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    Text(
                                        text = mensagem,
                                        color = TextoSuave,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
