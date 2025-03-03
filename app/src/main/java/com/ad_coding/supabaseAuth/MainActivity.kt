package com.supabaseAuth

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


// Define the response types for authentication.
sealed interface AuthResponse {
    object Success : AuthResponse
    data class Error(val message: String?) : AuthResponse
}

// AuthManager with the provided signUpWithEmail and signInWithEmail methods.
class AuthManager(private val context: Context) {
    private val supabase = createSupabaseClient(
        supabaseUrl = "",  // replace with your Supabase URL
        supabaseKey = ""            // replace with your Supabase anon key
    ) {
        install(Auth)
    }

    fun signUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AuthenticationScreen()
            }
        }
    }
}

@Composable
fun AuthenticationScreen() {
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var authStatus by remember { mutableStateOf("") }  // State variable to show messages to the user
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Simple layout with email and password input fields and buttons.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = emailValue,
            onValueChange = { emailValue = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = passwordValue,
            onValueChange = { passwordValue = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Sign Up Button
        Button(
            onClick = {
                coroutineScope.launch {
                    authManager.signUpWithEmail(emailValue, passwordValue)
                        .onEach { result ->
                            when (result) {
                                is AuthResponse.Success -> {
                                    Log.d("Auth", "Signup successful")
                                    authStatus = "Signup successful!"
                                    emailValue = ""
                                    passwordValue = ""
                                }
                                is AuthResponse.Error -> {
                                    Log.e("Auth", "Signup failed: ${result.message}")
                                    authStatus = "Signup failed: ${result.message}"
                                }
                            }
                        }
                        .launchIn(this)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Sign In Button
        Button(
            onClick = {
                coroutineScope.launch {
                    authManager.signInWithEmail(emailValue, passwordValue)
                        .onEach { result ->
                            when (result) {
                                is AuthResponse.Success -> {
                                    Log.d("Auth", "Signin successful")
                                    authStatus = "Signin successful!"
                                    emailValue = ""
                                    passwordValue = ""
                                }
                                is AuthResponse.Error -> {
                                    Log.e("Auth", "Signin failed: ${result.message}")
                                    authStatus = "Signin failed: ${result.message}"
                                }
                            }
                        }
                        .launchIn(this)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign In")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (authStatus.isNotEmpty()) {
            Text(
                text = authStatus,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAuthenticationScreen() {
    MaterialTheme {
        AuthenticationScreen()
    }
}
