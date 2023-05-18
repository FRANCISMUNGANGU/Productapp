package com.example.buy_ingredients.model

data class  IngredientsObj (
    var ingredientId: String,
    var ingredientName:String,
    var contactPhone:String,
    var ingredientImage:String,
    var ingredientPrice:String,
    var ingredientMethod:String
)
{
    constructor() : this("","","","","","")
}

