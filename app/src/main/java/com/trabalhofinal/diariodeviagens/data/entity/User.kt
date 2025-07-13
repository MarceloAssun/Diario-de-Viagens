package com.trabalhofinal.diariodeviagens.data.entity

/**
 * Entidade que representa um usuário no sistema
 * Usada para autenticação local
 */
data class User(
    val id: Long = 0,
    
    /**
     * Nome de usuário único para login
     */
    val username: String,
    
    /**
     * Senha do usuário (em produção deveria ser criptografada)
     */
    val password: String,
    
    /**
     * Nome completo do usuário
     */
    val fullName: String
) 