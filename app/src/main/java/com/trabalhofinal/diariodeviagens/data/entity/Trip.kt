package com.trabalhofinal.diariodeviagens.data.entity

/**
 * Entidade que representa uma viagem no sistema
 * Contém informações básicas da viagem como nome, país e datas
 */
data class Trip(
    val id: Long = 0,
    
    /**
     * Nome da viagem
     */
    val name: String,
    
    /**
     * País de destino da viagem
     */
    val country: String,
    
    /**
     * Data de início da viagem (formato: yyyy-MM-dd)
     */
    val startDate: String,
    
    /**
     * Data de fim da viagem (formato: yyyy-MM-dd)
     */
    val endDate: String,
    
    /**
     * ID do usuário que criou a viagem
     */
    val userId: Long,
    val comment: String? = null,
    val rating: Int = 0
) 