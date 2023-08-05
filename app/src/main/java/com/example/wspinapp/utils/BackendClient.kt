package com.example.wspinapp.utils

import android.util.Log
import android.widget.Toast
import com.example.wspinapp.model.Hold
import com.example.wspinapp.model.Route
import com.example.wspinapp.model.Wall
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.ConnectException


const val BACKEND_URL = "http://wspinapp-backend.ddns.net"
val client = HttpClient(CIO) {
    install(HttpTimeout)
}
val backendClient = BackendClient()

class BackendClient {
    private fun logResponse(response: HttpResponse) {
        Log.println(Log.INFO, "backend-client", "Request for ${response.request.url} resulted in status ${response.status}")
    }


    suspend fun fetchWalls(): MutableList<Wall>? {
        try {
            val response: HttpResponse = client.get("$BACKEND_URL/walls") {
                basicAuth("wspinapp", "wspinapp")
            }
            val responseBody = response.bodyAsText()

            val res : MutableList<Wall> = Json.decodeFromString(responseBody)

            logResponse(response)

            return res

        } catch (e: Exception) {
            when(e) {
                is HttpRequestTimeoutException,
                is ConnectTimeoutException,
                is SocketTimeoutException,
                is ConnectException -> {
                    return null
                }
                else -> throw e
            }
        }
    }


    suspend fun addWall(wall: Wall): Wall {
        val response: HttpResponse = client.post("$BACKEND_URL/walls") {
            basicAuth("wspinapp", "wspinapp")
            setBody(Json.encodeToString(wall))
        }

        if (response.status != HttpStatusCode.Created) {
            Log.println(Log.INFO, "backend-client", "Failed to create wall, status=${response.status} for ${response.request.url} ")
            return Wall(ID = 0u, Holds =  emptyArray())
        }
        val res : Wall = Json.decodeFromString(response.bodyAsText())

        logResponse(response)

        return res
    }


    suspend fun addImageToWall(wallId: UInt, file: File, path: String? = "image"): String {
        val response: HttpResponse =
            client.patch("$BACKEND_URL/walls/$wallId/$path") {
                basicAuth("wspinapp", "wspinapp")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("description", "wall_image_id_$wallId")
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "image/webp")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"wall_image_id_$wallId.webp\""
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

                timeout {
                    requestTimeoutMillis = 100000
                }
            }
        logResponse(response)
        return Json.decodeFromString(response.bodyAsText())
    }

    suspend fun getWall(wallId: UInt): Wall? {
        val response: HttpResponse = client.get("$BACKEND_URL/walls/$wallId") {
            basicAuth("wspinapp", "wspinapp")
        }
        logResponse(response)
        val responseBody = response.bodyAsText()
        return if (responseBody == "") {
            null
        } else {
            return Json.decodeFromString(responseBody)
        }
    }

    suspend fun fetchRoutes(wallId: UInt): MutableList<Route> {
        val response: HttpResponse = client.get("$BACKEND_URL/walls/$wallId/routes") {
            basicAuth("wspinapp", "wspinapp")
        }
        if (response.status != HttpStatusCode.OK) {
            Log.println(Log.INFO, "backend-client", "Failed to get routes for wall=$wallId, status=${response.status}")
            return mutableListOf()
        }

        val routes: MutableList<Route> = Json.decodeFromString(response.bodyAsText())
        logResponse(response)

        return routes
    }

    suspend fun addRoute(route: Route): Route? {
        val response: HttpResponse = client.post("$BACKEND_URL/walls/${route.WallID}/routes") {
            basicAuth("wspinapp", "wspinapp")
            setBody(Json.encodeToString(route))
        }

        if (response.status != HttpStatusCode.Created) {
            Log.println(
                Log.INFO,
                "backend-client",
                "Failed to create route, status=${response.status}"
            )
            return null
        }
        logResponse(response)
        return Json.decodeFromString<Route>(response.bodyAsText())
    }

    suspend fun deleteWall(wallId: UInt) {
        val response: HttpResponse = client.delete("$BACKEND_URL/walls/${wallId}") {
            basicAuth("wspinapp", "wspinapp")
        }
        logResponse(response)
    }
}