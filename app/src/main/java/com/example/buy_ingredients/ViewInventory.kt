package com.example.buy_ingredients

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.buy_ingredients.favorites.FavoriteDAO
import com.example.buy_ingredients.favorites.Favourites
import com.example.buy_ingredients.favorites.FavouritesDatabase
import com.example.buy_ingredients.model.IngredientsObj
import com.example.buy_ingredients.navigation.NavigationItem
import com.example.buy_ingredients.vendor.VendorDashboard
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ViewInventory : ComponentActivity() {
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
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(topBar = {
                        TopAppBar(backgroundColor = Color.Black,
                            title = {
                                Text(
                                    text = "Vendor Inventory",
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
                                    Toast.makeText(this@ViewInventory,"Error !!," + error.message, Toast.LENGTH_LONG).show()
                                    Log.d("FirebaseReading","Error is " + error.message)
                                    Log.d("FirebaseReading","Error is " + error.details)
                                    Log.d("FirebaseReading","Error is " + error.code)
                                }

                            })
                            // call to composable to display our user interface
                            ListOfProducts(LocalContext.current,ingredientList,lifecycleScope,favouritesDatabase, onDetailsClick = {
                                id: Long -> Unit
                            })

                        }
                    }
                }
            }
        }
    }
    override fun onBackPressed() {
        val intent = Intent(applicationContext, VendorDashboard::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun ListOfProducts(
    context: Context,
    IngredientList: SnapshotStateList<IngredientsObj?>,
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavoriteDAO,
    onDetailsClick: (id : Long) -> Unit
){
    Column(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .background(Color.White),
        verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Products World",
            modifier = Modifier.padding(10.dp),
            style = TextStyle(
                color = Color.Black, fontSize = 16.sp
            ), fontWeight = FontWeight.Bold
        )
        ShowFavorites(favouritesDatabase, lifecycleScope )
        val navController = rememberNavController()

        Scaffold (
            bottomBar = { BottomNavigation(navController) },
            content = {
                Box(modifier = Modifier.padding(it)) {
                    Navigate(navController = navController, favouritesDatabase, lifecycleScope)
                    LazyColumn{
                        items(IngredientList) {ingredient ->
                            // here have a custom UI for the list or quick set up
//                 make my composable
                            // !! this is called the safe call operator
                            // its use here is to unwrap the opting String? value from product list.
                            IngredientCard(ingredient = ingredient!!, context,lifecycleScope,favouritesDatabase, onClick =
                            {})
                            //,//lifecycleScope,favouritesDatabase
                        }
                    }
                }
            }

        )


    }
}



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IngredientCard(
    ingredient: IngredientsObj,
    context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavoriteDAO,
    onClick : () -> Unit
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
            DetailsCard(showDialog = showDialog.value, onDismiss = {showDialog.value = false}, ingredient = ingredient, lifecycleScope = lifecycleScope, favouritesDatabase = favouritesDatabase)
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
                    if (isLoading){
                        LoadingProgress()
//                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Rounded.Favorite, contentDescription = "like", modifier = Modifier.padding(20.dp))
                    }

                }


            }
        }
    }
}

@Composable
fun ShowFavorites(favouritesDatabase: FavoriteDAO, lifecycleScope: LifecycleCoroutineScope) {
    // state variable to track whether my pop up interface is open or closed
    var showFavDialog by remember { mutableStateOf(false) }
    // we need our list of favourites.
    var favouriteList by remember {
        mutableStateOf(emptyList<Favourites>())
    }
    // call the button
    var isLoading by remember {
        mutableStateOf(false)
    }
    Button(onClick = {
        isLoading = true
        // fetching data from the room database and setting the state of our alert box
        showFavDialog = true
        // get our list
        lifecycleScope.launch{
            favouriteList = favouritesDatabase.getFavorites().first()
            delay(300)
            isLoading = false
        }

    }) {
        if(isLoading){
            LoadingProgress()
        }
        else{
            Text(text = "View Favourites", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }

    }
    var dialogHeight  = 600.dp
    if (showFavDialog){
        AlertDialog(
            onDismissRequest = {  showFavDialog = false},
            title = { Text(text = "My Favourites")},
            text = {
                Box(modifier = Modifier
                    .height(dialogHeight)
                    .fillMaxWidth() ){
                    LazyColumn(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()){
                        items(favouriteList) {
                            Card(modifier = Modifier.padding(8.dp), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = it.favouriteName, fontWeight = FontWeight.Bold)
                                    Text("Image download link : ${it.favouriteImage}")
                                    Text("Product Seller : ${it.favouriteContact}")
                                    Text("Product Price : ${it.favouritePrice}")
                                }
                            }
                        }
                    }
                }


            } ,
            confirmButton = {
                Button(onClick = { showFavDialog = false}) {
                    Text(text = "Close Dialog")
                }
            }
        )
    }

}

@Composable
fun LoadingProgress() {
    val strokeWidth = 5.dp
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = Color.Magenta,
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun BottomNavigation(
    navController: NavHostController
) {
    val items = listOf(
        NavigationItem.VendorDashboard,
        NavigationItem.VendorInventory
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = colorResource(id = R.color.black),
        contentColor = Color.White
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }

}

@Composable
fun Navigate(navController : NavHostController,favouritesDatabase: FavoriteDAO, lifecycleScope: LifecycleCoroutineScope) {
    NavHost(
        navController,
        startDestination = NavigationItem.VendorInventory.route
    ) {
        composable(NavigationItem.VendorDashboard.route) {
            val context = LocalContext.current
            val intent = Intent(context, VendorDashboard::class.java)
            context.startActivity(intent)
        }
        composable(NavigationItem.VendorInventory.route) {
            }

    }
}


@Composable
fun DetailsCard(
    lifecycleScope: LifecycleCoroutineScope,
    favouritesDatabase: FavoriteDAO,
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
                                        Text(text = "Ingredient Name : ${ingredient.ingredientName}",
                                             modifier = Modifier.padding(16.dp))
                                        Text(
                                            text = "Vendor Phone Number: ${ingredient.contactPhone}",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Text(
                                            text = "Price: ${ingredient.ingredientPrice}",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }

                                }
                            }
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

                        }


                    }

                }
            }
        }
    }
}