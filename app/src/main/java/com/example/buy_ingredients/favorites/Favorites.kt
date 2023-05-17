package com.example.buy_ingredients.favorites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "favourites")
data class Favourites (
    @PrimaryKey
    @ColumnInfo(name = "favoriteId")
    val favouriteId: String,
    @ColumnInfo(name = "favoriteName")
    val favouriteName: String,
    @ColumnInfo(name = "favoriteContact")
    val favouriteContact: String,
    @ColumnInfo(name = "favoriteImage")
    val favouriteImage: String,
    @ColumnInfo(name = "favoritePrice")
    val favouritePrice: String,
    @ColumnInfo(name = "dateAdded")
    val date: Date
)


// primary key : integer


// @PrimaryKey(autoGenerate = true)