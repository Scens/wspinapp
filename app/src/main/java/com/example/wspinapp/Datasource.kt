package com.example.wspinapp

import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.BackendClient

class Datasource {
    suspend fun loadWalls(): List<Wall> {
        return BackendClient().fetchWalls()
    }
}
