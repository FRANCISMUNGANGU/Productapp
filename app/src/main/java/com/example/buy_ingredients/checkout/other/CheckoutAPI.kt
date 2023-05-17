package com.example.buy_ingredients.checkout.other

import com.example.buy_ingredients.checkout.model.Checkoutmodel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckoutAPI {
    @POST("newCheckout")
    fun simulateCheckout(@Body checkoutmodel : Checkoutmodel?) : Call<Checkoutmodel?>?
}