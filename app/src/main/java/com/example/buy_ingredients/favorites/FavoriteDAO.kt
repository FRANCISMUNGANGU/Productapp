package com.example.buy_ingredients.favorites

import androidx.room.*


import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorites(favourites: Favourites) : Unit
    @Query("SELECT * FROM favourites")
    fun getFavorites() : Flow<List<Favourites>>
    @Update
    suspend fun updateFav(favourites: Favourites) : Unit
    @Delete
    suspend fun deleteFav(favourites: Favourites) : Unit
}