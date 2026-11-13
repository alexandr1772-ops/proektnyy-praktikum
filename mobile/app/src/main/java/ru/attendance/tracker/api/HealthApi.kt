package ru.attendance.tracker.api

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Адрес бэкенда. Эмулятор Android видит ноутбук как 10.0.2.2.
 * Для показа на живом телефоне заменить на http://<IP-ноутбука>:8000 (ноутбук и телефон в одной Wi-Fi).
 */
const val API_BASE_URL = "http://192.168.2.217:8000"

/** Запрашивает GET /health и возвращает тело ответа, например {"status":"ok"}. Бросает исключение при ошибке сети. */
suspend fun fetchHealth(baseUrl: String = API_BASE_URL): String = withContext(Dispatchers.IO) {
    val connection = URL("$baseUrl/health").openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 3000
        connection.readTimeout = 3000
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}
