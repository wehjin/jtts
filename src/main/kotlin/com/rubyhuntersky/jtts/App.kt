package com.rubyhuntersky.jtts

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

private val ttsDirectory = File("tts").apply { mkdirs() }
fun main(args: Array<String>) {
    val romanjiList = listOf("odorokuhodo", "oozei")
    romanjiList.fold(false, { fetched, romanji ->
        if (fetched) {
            println("Wait before next download")
            Thread.sleep(1000)
        }
        fetchAudio(romanji)
    })
}

private fun fetchAudio(romanji: String): Boolean {
    println("Processing: $romanji")
    val file = File(ttsDirectory, "${romanji}_tts.mp3")
    return if (file.exists()) {
        println("Already exists. Skipping.")
        false
    } else {
        val url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&q=$romanji&tl=ja"
        println("Download: $url")
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            file.writeBytes(response.body!!.bytes())
        } else {
            error("Http request failed: $response")
        }
        true
    }
}
