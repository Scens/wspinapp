package com.example.wspinapp

import android.util.Log
import com.example.wspinapp.model.AddWall
import com.example.wspinapp.model.Wall
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

val client = HttpClient(CIO)
class Datasource {
    suspend fun loadWalls(): List<Wall> {
        val response: HttpResponse = client.get("http://wspinapp-backend.ddns.net/walls") {
            basicAuth("wspinapp", "wspinapp")
        }
        val responseBody = response.bodyAsText()

        val listType = object : TypeToken<List<Wall>>() {}.type

        Log.println(Log.DEBUG, "response body from wspinapi ", responseBody)

        val res : List<Wall> = Gson().fromJson(responseBody, listType)
        Log.println(Log.DEBUG, "response body as a wall list", res.toString())
        return res
    }

    suspend fun addWall(wall: AddWall) {
        val json = Gson().toJson(wall).toString()
        val response: HttpResponse = client.post("http://wspinapp-backend.ddns.net/walls") {
            basicAuth("wspinapp", "wspinapp")
            setBody(json)
        }

        Log.println(Log.DEBUG, "Request to wspinapi ", json )
        Log.println(Log.DEBUG, "response body from wspinapi ", response.bodyAsText())
    }
}
