package com.rubyhuntersky.jtts

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val ttsDirectory = File("tts").apply { mkdirs() }
fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        fetchAudio(args.toList())
    } else {
        fetchLessonAudio()
    }
    println(ttsDirectory.toURI())
}

private fun fetchLessonAudio() {
    val lessonsFile = File("LESSONS.LST")
    if (lessonsFile.exists()) {
        val romaji = loadLessonRomaji(lessonsFile)
        fetchAudio(romaji)
    } else {
        println("${lessonsFile.absolutePath} not found")
    }
}

private fun loadLessonRomaji(lessonsFile: File): List<String> {
    return lessonsFile.readLines().mapNotNull {
        val parts = it.split(":")
        if (parts[0] == "listen") {
            parts[1].trim()
        } else {
            null
        }
    }
}

private fun fetchAudio(soundList: List<String>) {
    soundList.fold(false, { fetched, romanji ->
        if (fetched) {
            println("Wait before next download")
            Thread.sleep(1000)
        }
        fetchAudio(romanji)
    })
}

private fun fetchAudio(sound: String): Boolean {
    val parts = sound.split("/")
    val filename = parts[0]
    val search = if (parts.size > 1) URLEncoder.encode(parts[1], StandardCharsets.UTF_8.toString()) else parts[0]
    println("Processing: $sound")
    val file = File(ttsDirectory, "${filename}_tts.mp3")
    return if (file.exists()) {
        println("Already exists. Skipping.")
        false
    } else {
        val url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&q=$search&tl=ja"
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
