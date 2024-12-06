package agnello.vinicola.appvinicolla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import agnello.vinicola.appvinicolla.ui.theme.VinicolaAgnelloTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import kotlinx.coroutines.launch
import androidx.room.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp


@Entity(tableName = "produtos")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val teor: String,
    val preco: Double
)

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM produtos")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM produtos WHERE id = :id")
    suspend fun getProductById(id: Int): Product?
}

@Entity(tableName = "usuarios")
data class User(
    @PrimaryKey val email: String,
    val password: String
)

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): User?
}

@Database(entities = [User::class, Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
}

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "vinicola_db")
            .fallbackToDestructiveMigration()
            .build()

        setContent {
            VinicolaAgnelloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showLogin by remember { mutableStateOf(true) }
                    var isLoggedIn by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()

                    if (isLoggedIn) {
                        Column {
                            ProductScreen(db)
                        }
                    } else {
                        if (showLogin) {
                            LoginScreen(
                                onLogin = { email, password ->
                                    coroutineScope.launch {
                                        val user = db.userDao().getUser(email, password)
                                        if (user != null) {
                                            isLoggedIn = true
                                        }
                                    }
                                },
                                onRegister = { showLogin = false }
                            )
                        } else {
                            RegisterScreen(
                                onRegister = { email, password ->
                                    coroutineScope.launch {
                                        db.userDao().insertUser(User(email, password))
                                        showLogin = true
                                    }
                                },
                                onBack = { showLogin = true }
                            )
                        }
                    }
                }
            }
        }
    }
}


    @Composable
    fun LoginScreen(onLogin: (String, String) -> Unit, onRegister: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login", color = MaterialTheme.colorScheme.secondary)
            }
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text("Não possui uma conta?", modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }

    @Composable
    fun RegisterScreen(onRegister: (String, String) -> Unit, onBack: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onRegister(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar", color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar para Login", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }

@Composable
fun ProductScreen(db: AppDatabase) {
    val coroutineScope = rememberCoroutineScope()
    var produtos by remember { mutableStateOf(listOf<Product>()) }
    var nome by remember { mutableStateOf("") }
    var alcoolico by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        produtos = db.productDao().getAllProducts()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = alcoolico,
            onValueChange = { alcoolico = it },
            label = { Text("Teor Alcoólico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = preco,
            onValueChange = { preco = it },
            label = { Text("Preço") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val product =
                            Product(nome = nome, teor = alcoolico, preco = preco.toDouble())
                        db.productDao().insertProduct(product)
                        produtos = db.productDao().getAllProducts()
                        nome = ""
                        alcoolico = ""
                        preco = ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar Produto", color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(produtos) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${product.nome} | ${product.teor} | ${product.preco}",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 20.sp
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    db.productDao().deleteProduct(product)
                                    produtos = db.productDao().getAllProducts()
                                }
                            }
                        ) {
                            Text("Deletar", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
