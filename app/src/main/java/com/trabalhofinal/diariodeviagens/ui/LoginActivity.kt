package com.trabalhofinal.diariodeviagens.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trabalhofinal.diariodeviagens.R
import com.trabalhofinal.diariodeviagens.data.TravelDbHelper
import com.trabalhofinal.diariodeviagens.data.entity.User
import com.trabalhofinal.diariodeviagens.databinding.ActivityLoginBinding

/**
 * Activity responsável pela tela de login do aplicativo
 * Permite autenticação local com validação de campos
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: TravelDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o helper do banco de dados
        dbHelper = TravelDbHelper(this)

        // Configura os listeners dos botões
        setupListeners()

        // Cria um usuário padrão se não existir nenhum
        createDefaultUser()
    }

    /**
     * Configura os listeners dos botões da tela
     */
    private fun setupListeners() {
        // Listener do botão de login
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // Listener do botão de cadastro
        binding.btnRegister.setOnClickListener {
            performRegister()
        }
    }

    /**
     * Cria um usuário padrão para facilitar os testes
     */
    private fun createDefaultUser() {
        if (!dbHelper.userExists("admin")) {
            val defaultUser = User(
                username = "admin",
                password = "123456",
                fullName = "Administrador"
            )
            dbHelper.insertUser(defaultUser)
        }
    }

    /**
     * Realiza o processo de login
     */
    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validação dos campos
        if (!validateFields(username, password)) {
            return
        }

        // Tenta autenticar o usuário
        val user = dbHelper.authenticateUser(username, password)

        if (user != null) {
            // Login bem-sucedido
            Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
            
            // Inicia a tela principal passando o ID do usuário
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USER_ID", user.id)
                putExtra("USERNAME", user.username)
            }
            startActivity(intent)
            finish() // Fecha a tela de login
        } else {
            // Login falhou
            Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Realiza o processo de cadastro de novo usuário
     */
    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validação dos campos
        if (!validateFields(username, password)) {
            return
        }

        // Verifica se o usuário já existe
        if (dbHelper.userExists(username)) {
            Toast.makeText(this, "Nome de usuário já existe!", Toast.LENGTH_LONG).show()
            return
        }

        // Cria o novo usuário
        val newUser = User(
            username = username,
            password = password,
            fullName = username // Usa o username como nome completo por simplicidade
        )

        val userId = dbHelper.insertUser(newUser)

        if (userId != -1L) {
            Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
            
            // Inicia a tela principal passando o ID do usuário
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("USERNAME", username)
            }
            startActivity(intent)
            finish() // Fecha a tela de login
        } else {
            Toast.makeText(this, "Erro ao cadastrar usuário!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Valida os campos de entrada
     * @param username nome de usuário
     * @param password senha
     * @return true se os campos são válidos, false caso contrário
     */
    private fun validateFields(username: String, password: String): Boolean {
        // Validação do nome de usuário
        if (username.isEmpty()) {
            binding.etUsername.error = "Nome de usuário é obrigatório"
            binding.etUsername.requestFocus()
            return false
        }

        if (username.length < 3) {
            binding.etUsername.error = "Nome de usuário deve ter pelo menos 3 caracteres"
            binding.etUsername.requestFocus()
            return false
        }

        // Validação da senha
        if (password.isEmpty()) {
            binding.etPassword.error = "Senha é obrigatória"
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 4) {
            binding.etPassword.error = "Senha deve ter pelo menos 4 caracteres"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
} 