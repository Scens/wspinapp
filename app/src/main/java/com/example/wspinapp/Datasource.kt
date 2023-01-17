package com.example.wspinapp

import android.util.Log
import com.example.wspinapp.model.AddWall
import com.example.wspinapp.model.Wall
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File

val client = HttpClient(CIO)
class Datasource {
    // todo move most of this code to client class
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

    suspend fun addWall(wall: AddWall): UInt {
        val json = Gson().toJson(wall).toString()
        val response: HttpResponse = client.post("http://wspinapp-backend.ddns.net/walls") {
            basicAuth("wspinapp", "wspinapp")
            setBody(json)
        }

        Log.println(Log.DEBUG, "Request to wspinapi ", json )
        Log.println(Log.DEBUG, "response body from wspinapi ", response.bodyAsText())
        return 1U // TODO this should be a real id taken from response :)

    }


    suspend fun addImage(wallId: UInt, file: File) {
        val response: HttpResponse = client.patch(String.format("http://wspinapp-backend.ddns.net/walls/%s/image", wallId)) {
            basicAuth("wspinapp", "wspinapp")
            setBody(MultiPartFormDataContent(
                formData {
                    append("description", "wall_image_test")
                    append("file", file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=\"wall_image_test.png\"")
                    })
                },
                boundary = "WebAppBoundary"
            )
            )
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }

        Log.println(Log.DEBUG, "uploading image to wspinapi", response.bodyAsText())
    }
}
