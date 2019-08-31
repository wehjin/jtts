package com.rubyhuntersky.jtts

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

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
        println("LESSONS.LST not found")
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

private fun fetchAudio(romanjiList: List<String>) {
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
