package com.example.wspinapp

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.wspinapp.model.Wall
import com.example.wspinapp.utils.backendClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object WallManager {
    private val walls: MutableMap<UInt, Wall> = mutableMapOf()
    var dataset: MutableList<Wall> = mutableListOf()
    var haveNewItems: Boolean = false

    suspend fun fetchWalls() {
        val fetchedWalls = backendClient.fetchWalls() ?: throw Exception("Failed to fetch walls")

        dataset.clear()
        walls.clear()

        dataset.addAll(fetchedWalls)
        fetchedWalls.forEach { wall ->
            walls[wall.ID!!] = wall
        }
    }

    fun addWall(wall: Wall) {
        dataset.add(wall)
        walls[wall.ID!!] = wall

        haveNewItems = true
    }

    suspend fun removeWallById(id: UInt) {
        backendClient.deleteWall(id)

        val wall = walls[id]
        dataset.remove(wall)
        walls.remove(id)
    }

    fun getWallById(id: UInt): Wall? {
        return walls[id]
    }

}
