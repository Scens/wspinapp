package com.example.wspinapp.utils

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
import java.util.*


const val BACKEND_URL = "http://wspinapp-backend.ddns.net"
val client = HttpClient(CIO)

class BackendClient {
    suspend fun fetchWalls(): List<Wall> {
        val response: HttpResponse = client.get("$BACKEND_URL/walls") {
            basicAuth("wspinapp", "wspinapp")
        }
        val responseBody = response.bodyAsText()

        val listType = object : TypeToken<List<Wall>>() {}.type

        Log.println(Log.DEBUG, "response body from wspinapi ", responseBody)

        val res : List<Wall> = if (responseBody == "") {
            emptyList()
        } else {
            Gson().fromJson(responseBody, listType)
        }
        Log.println(Log.DEBUG, "response body as a wall list", res.toString())
        return res
    }

    suspend fun addWall(wall: AddWall): UInt {
        val json = Gson().toJson(wall).toString()
        val response: HttpResponse = client.post("$BACKEND_URL/walls") {
            basicAuth("wspinapp", "wspinapp")
            setBody(json)
        }

        Log.println(Log.DEBUG, "Request to wspinapi ", json )
        Log.println(Log.DEBUG, "response body from wspinapi ", response.bodyAsText())
        val res : Wall = Gson().fromJson(response.bodyAsText(), Wall::class.java)

        return res.ID!!

    }


    suspend fun addImageToWall(wallId: UInt, file: File): HttpResponse {
        val response: HttpResponse =
            client.patch("$BACKEND_URL/walls/$wallId/image") {
                basicAuth("wspinapp", "wspinapp")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("description", "wall_image_id_$wallId")
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"wall_image_id_$wallId.png\""
                                )
                            })
                        },
                        boundary = "WebAppBoundary"
                    )
                )
                onUpload { bytesSentTotal, contentLength ->
                    Log.println(
                        Log.DEBUG,
                        "backend-client",
                        "Sent $bytesSentTotal bytes from $contentLength"
                    )
                }
            }
        Log.println(Log.DEBUG, "backend-client", response.status.toString())
        return response
    }

    suspend fun getWall(wallId: UInt): Wall? {
        val response: HttpResponse = client.get("$BACKEND_URL/walls/$wallId") {
            basicAuth("wspinapp", "wspinapp")
        }
        val responseBody = response.bodyAsText()
        return if (responseBody == "") {
            null
        } else {
            val res : Wall = Gson().fromJson(responseBody, Wall::class.java)
            res
        }
    }
}