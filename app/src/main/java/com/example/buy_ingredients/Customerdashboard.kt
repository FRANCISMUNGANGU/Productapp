package com.example.buy_ingredients

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.example.buy_ingredients.checkout.model.Checkoutmodel
import com.example.buy_ingredients.checkout.other.CheckoutAPI
import com.example.buy_ingredients.favorites.FavoriteDAO
import com.example.buy_ingredients.favorites.Favourites
import com.example.buy_ingredients.favorites.FavouritesDatabase
import com.example.buy_ingredients.model.IngredientsObj
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class Customerdashboard : ComponentActivity() {
    // lazy is used to avoid unnecessary intialization of props
    // can improve performance and reduce memory usage
    private val favouritesDatabase by lazy { FavouritesDatabase.getDatabase(this).favoriteDao() }

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            com.example.buy_ingredients.ui.theme.BuyingredientsTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    Scaffold(topBar = {
                        TopAppBar(backgroundColor = Color.Black,
                            title = {
                                Text(
                                    text = "Ingredients store",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            })
                    }) {
                        Column(modifier = Modifier.padding(it)) {
                            // mutableStateListOf<String?>()
                            var ingredientList = mutableStateListOf<IngredientsObj?>()
                            // getting firebase instance and the database reference
                            val firebaseDatabase = FirebaseDatabase.getInstance()
                            val databaseReference = firebaseDatabase.getReference("ProductDB")
                            // to read data values ,we use the addChildEventListener
                            databaseReference.addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    // this method is called when a new child/record is added to our db
                                    // we are adding that item to the list
                                    val product = snapshot.getValue(IngredientsObj::class.java)
                                    ingredientList.add(product)
                                }

                                override fun onChildChanged(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    // this method is called when a new child is added
                                    // when a new child is added to our list of
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {
                                    // method is called when we remove a child from the db
                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    // method is called when we move a record/child in the db.
                                }

                                override fun onCancelled(error: DatabaseError) {
//                                    if we get any firebase error
                                    Toast.makeText(this@Customerdashboard,"Error !!," + error.message, Toast.LENGTH_LONG).show()
                                    Log.d("FirebaseReading","Error is " + error.message)
                                    Log.d("FirebaseReading","Error is " + error.details)
                                    Log.d("FirebaseReading","Error is " + error.code)
                                }

                            })
                            // call to composable to display our user interface
                            ListOfProducts(LocalContext.current,ingredientList,lifecycleScope,favouritesDatabase)

                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ListOfProducts(
    context: Context,
    IngredientList: SnapshotStateList<IngredientsObj?>,
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavoriteDAO
){
    val backgroundImage = painterResource(R.drawable.backscreen)
    ShowFavorites(favouritesDatabase, lifecycleScope )
    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Text(
            text = "Products World",
            modifier = Modifier.padding(10.dp),
            style = TextStyle(
                color = Color.Black, fontSize = 16.sp
            ), fontWeight = FontWeight.Bold
        )
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


                    LazyColumn{
                        items(IngredientList) {ingredient ->
                            // here have a custom UI for the list or quick set up
//                 make my composable
                            // !! this is called the safe call operator
                            // its use here is to unwrap the opting String? value from product list.
                            IngredientCard(ingredient = ingredient!!, context,lifecycleScope,favouritesDatabase)
                            //,//lifecycleScope,favouritesDatabase
                        }
                    }
                }

}


fun simulateCheckout(context: Context, ingredient: IngredientsObj) {
    val response = ""
    val baseUrl = "http://127.0.0.1.8000/"
    val retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
    val checkoutApi = retrofit.create(CheckoutAPI::class.java)
    val datamodel = Checkoutmodel(ingredient.ingredientPrice, ingredient.contactPhone)
    val call : Call<Checkoutmodel?>? = checkoutApi.simulateCheckout(datamodel)
    call!!.enqueue(object : Callback<Checkoutmodel?>{
        override fun onResponse(call: Call<Checkoutmodel?>, response: Response<Checkoutmodel?>) {
            Toast.makeText(context, "Data posted to API", Toast.LENGTH_SHORT).show()
            Log.d("Response from server", response.toString())
        }

        override fun onFailure(call: Call<Checkoutmodel?>, t: Throwable) {
            Toast.makeText(context, "Failure to post to API", Toast.LENGTH_SHORT).show()
            t.message?.let{Log.d("Response from server", it)}
        }
    })
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IngredientCard(
    ingredient: IngredientsObj,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavoriteDAO,

) {
    val showDialog = remember {
        mutableStateOf(false)
    }
    val onClick = {
        showDialog.value = true
    }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // load our image composable
            Image(
                painter = rememberImagePainter(data = ingredient.ingredientImage),
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                text = ingredient.ingredientName,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Seller Contact: ${ingredient.contactPhone}")
            Text(text = "Seller Price: ${ingredient.ingredientPrice}")
            detailsCard(showDialog = showDialog.value, onDismiss = {showDialog.value = false}, ingredient = ingredient, context = LocalContext.current, lifecycleScope = lifecycleScope, favouritesDatabase = favouritesDatabase)
            Spacer(modifier = Modifier.height(5.dp))
            var isLoading by remember {
                mutableStateOf(false)
            }
            Row() {
                IconButton(onClick = {
                    isLoading = true
                    // get the current time and date
                    val newFavoriteAdded  = Date()
                    // add product to favourite
                    val newFavorites = Favourites(ingredient.ingredientId,ingredient.ingredientName,ingredient.contactPhone,ingredient.ingredientImage
                        ,ingredient.ingredientPrice,newFavoriteAdded)
                    // adding the product to the room db
                    lifecycleScope.launch{
                        favouritesDatabase.addFavorites(newFavorites)
                        delay(3000)
                        isLoading = false
                    }
                }) {
                    if (isLoading) {
                        LoadingProgress()
//                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Rounded.Favorite, contentDescription = "like", modifier = Modifier.padding(20.dp))
                    }
                    }}
                    Spacer(modifier = Modifier.padding(10.dp))
                    Row() {
                        Button(onClick = {
                            isLoading = true
                            // get the current time and date
                            val newFavoriteAdded = Date()
                            // add product to favourite
                            val newFavorites = Favourites(
                                ingredient.ingredientId,
                                ingredient.ingredientName,
                                ingredient.contactPhone,
                                ingredient.ingredientImage,
                                ingredient.ingredientPrice,
                                newFavoriteAdded
                            )

                            simulateCheckout(context, ingredient)
                            isLoading = false

                        }) {
                            if (isLoading) {
                                LoadingProgress()
//                        CircularProgressIndicator()
                            } else {
                                Text(text = "Checkout")
                            }

                        }


                    }
                }

    }
}

@Composable
fun detailsCard(
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavoriteDAO,
    context : Context,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    ingredient: IngredientsObj
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { onDismiss() }
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        if (ingredient != null) {
                            Image(
                                painter = rememberImagePainter(data = ingredient.ingredientImage),
                                contentDescription = "Goods Photo",
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {

                        item {
                            if (ingredient != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = 4.dp
                                ) {
                                    Column() {
                                        Text(
                                            text = "Phone Number: ${ingredient.ingredientName}",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Text(
                                            text = "Phone Number: ${ingredient.contactPhone}",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Text(
                                            text = "Price: ${ingredient.ingredientPrice}",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                }
                            }
                            //like
                            var isLoading by remember {
                                mutableStateOf(false)
                            }
                            Row() {
                                IconButton(onClick = {
                                    isLoading = true
                                    // get the current time and date
                                    val newFavoriteAdded  = Date()
                                    // add product to favourite
                                    val newFavorites = Favourites(ingredient.ingredientId,ingredient.ingredientName,ingredient.contactPhone,ingredient.ingredientImage
                                        ,ingredient.ingredientPrice,newFavoriteAdded)
                                    // adding the product to the room db
                                    lifecycleScope.launch{
                                        favouritesDatabase.addFavorites(newFavorites)
                                        delay(3000)
                                        isLoading = false
                                    }
                                }) {
                                    if (isLoading){
                                        LoadingProgress()
//                        CircularProgressIndicator()
                                    } else {
                                        Icon(Icons.Rounded.Favorite, contentDescription = "like", modifier = Modifier.padding(20.dp))
                                    }

                                }


                            }
                            Row() {
                                Button(onClick = {
                                    isLoading = true
                                    // get the current time and date
                                    val newFavoriteAdded = Date()
                                    // add product to favourite
                                    val newFavorites = Favourites(
                                        ingredient.ingredientId,
                                        ingredient.ingredientName,
                                        ingredient.contactPhone,
                                        ingredient.ingredientImage,
                                        ingredient.ingredientPrice,
                                        newFavoriteAdded
                                    )

                                    simulateCheckout(context, ingredient)
                                    isLoading = false

                                }) {
                                    if (isLoading) {
                                        LoadingProgress()
//                        CircularProgressIndicator()
                                    } else {
                                        Text(text = "Checkout")
                                    }

                                }


                            }
                        }


                    }

                }
            }
        }
    }
}