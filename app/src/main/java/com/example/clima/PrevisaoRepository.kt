package com.example.cotacaodolar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class PrevisaoRepository {

    suspend fun buscarPrevisao(nomeCidade: String): Previsao = withContext(Dispatchers.IO) {
        val cidadeCodificada = URLEncoder.encode(nomeCidade, "UTF-8")
        val urlCidade = URL(
            "https://geocoding-api.open-meteo.com/v1/search?name=$cidadeCodificada&count=1&language=pt&format=json"
        )

        val respostaCidade = realizarRequisicao(urlCidade)
        val jsonCidade = JSONObject(respostaCidade)
        val resultados = jsonCidade.optJSONArray("results")

        if (resultados == null || resultados.length() == 0) {
            throw IOException("Cidade não encontrada. Verifique o nome e tente novamente.")
        }

        val dadosCidade = resultados.getJSONObject(0)
        val nomeEncontrado = dadosCidade.getString("name")
        val pais = dadosCidade.optString("country", "")
        val latitude = dadosCidade.getDouble("latitude")
        val longitude = dadosCidade.getDouble("longitude")

        val urlTempo = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m"
        )

        val respostaTempo = realizarRequisicao(urlTempo)
        val jsonTempo = JSONObject(respostaTempo)
        val dadosAtuais = jsonTempo.optJSONObject("current")
            ?: throw IOException("Não foi possível obter a temperatura atual.")

        if (!dadosAtuais.has("temperature_2m") || dadosAtuais.isNull("temperature_2m")) {
            throw IOException("A API não retornou a temperatura para essa cidade.")
        }

        val temperatura = dadosAtuais.getDouble("temperature_2m")
        val cidadeCompleta = if (pais.isNotBlank()) "$nomeEncontrado, $pais" else nomeEncontrado

        Previsao(
            cidade = cidadeCompleta,
            temperatura = String.format(Locale("pt", "BR"), "%.1f", temperatura)
        )
    }

    private fun realizarRequisicao(url: URL): String {
        val conexao = url.openConnection() as HttpURLConnection

        try {
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 10000
            conexao.readTimeout = 10000

            val codigoResposta = conexao.responseCode
            val fluxo = if (codigoResposta in 200..299) {
                conexao.inputStream
            } else {
                conexao.errorStream
            }

            val resposta = fluxo?.bufferedReader()?.use { it.readText() }
                ?: throw IOException("A API não retornou conteúdo.")

            if (codigoResposta !in 200..299) {
                throw IOException("Falha na consulta. Código HTTP: $codigoResposta.")
            }

            return resposta
        } finally {
            conexao.disconnect()
        }
    }
}
