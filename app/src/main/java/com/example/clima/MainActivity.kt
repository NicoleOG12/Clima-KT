package com.example.clima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.cotacaodolar.Previsao
import com.example.cotacaodolar.PrevisaoRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var cidadeTexto by remember { mutableStateOf("") }
                    var previsao by remember { mutableStateOf<Previsao?>(null) }
                    var carregando by remember { mutableStateOf(false) }
                    var mensagem by remember {
                        mutableStateOf("Informe uma cidade para consultar a temperatura atual.")
                    }

                    val escopo = rememberCoroutineScope()
                    val repository = remember { PrevisaoRepository() }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Previsão do tempo",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(text = "Consulte a temperatura atual de uma cidade.")

                        OutlinedTextField(
                            value = cidadeTexto,
                            onValueChange = { cidadeTexto = it },
                            label = { Text("Nome da cidade") },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val cidade = cidadeTexto.trim()

                                if (cidade.isBlank()) {
                                    mensagem = "Informe o nome de uma cidade."
                                    previsao = null
                                    return@Button
                                }

                                escopo.launch {
                                    carregando = true
                                    mensagem = "Consultando a previsão do tempo..."
                                    previsao = null

                                    try {
                                        previsao = repository.buscarPrevisao(cidade)
                                        mensagem = "Consulta realizada com sucesso."
                                    } catch (e: Exception) {
                                        mensagem = e.message
                                            ?: "Ocorreu uma falha na consulta. Tente novamente."
                                    } finally {
                                        carregando = false
                                    }
                                }
                            },
                            enabled = !carregando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Consultar temperatura")
                        }

                        if (carregando) {
                            CircularProgressIndicator()
                        }

                        Text(text = mensagem)

                        previsao?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it.cidade,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Temperatura atual: ${it.temperatura} °C",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
