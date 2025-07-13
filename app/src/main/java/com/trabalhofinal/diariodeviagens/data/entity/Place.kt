package com.trabalhofinal.diariodeviagens.data.entity

/**
 * Entidade que representa um lugar visitado durante uma viagem
 * Relacionada com a entidade Trip através de uma chave estrangeira
 */
data class Place(
    val id: Long = 0,
    
    /**
     * Nome do lugar visitado
     */
    val name: String,
    
    /**
     * Notas sobre o lugar (opcional)
     */
    val notes: String?,
    
    /**
     * Comentários adicionais sobre o lugar (opcional)
     */
    val comments: String?,
    
    /**
     * Data da visita (formato: yyyy-MM-dd)
     */
    val visitDate: String,
    
    /**
     * ID da viagem à qual este lugar pertence
     */
    val tripId: Long
) 