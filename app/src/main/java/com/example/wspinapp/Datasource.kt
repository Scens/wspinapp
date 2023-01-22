package com.example.wspinapp

import android.util.Log
import com.example.wspinapp.model.AddWall
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.BackendClient
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

class Datasource {
    suspend fun loadWalls(): List<Wall> {
        return BackendClient().fetchWalls()
    }
}
