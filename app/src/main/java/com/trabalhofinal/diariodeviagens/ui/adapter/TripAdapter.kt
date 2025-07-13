package com.trabalhofinal.diariodeviagens.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trabalhofinal.diariodeviagens.data.entity.Trip
import com.trabalhofinal.diariodeviagens.databinding.ItemTripBinding

/**
 * Adapter para o RecyclerView que exibe a lista de viagens
 * Responsável por criar e gerenciar os ViewHolders dos itens da lista
 */
class TripAdapter(
    private val trips: MutableList<Trip> = mutableListOf(),
    private val onEditClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit,
    private val onItemClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    /**
     * ViewHolder para cada item da lista de viagens
     */
    class TripViewHolder(
        private val binding: ItemTripBinding,
        private val onEditClick: (Trip) -> Unit,
        private val onDeleteClick: (Trip) -> Unit,
        private val onItemClick: (Trip) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula os dados da viagem aos elementos da view
         * @param trip viagem a ser exibida
         */
        fun bind(trip: Trip) {
            binding.apply {
                tvTripName.text = trip.name
                tvCountry.text = trip.country
                tvDateRange.text = "${trip.startDate} - ${trip.endDate}"
                tvTripComment.text = trip.comment ?: ""
                tvTripRating.text = trip.rating.toString()

                // Configura os listeners dos botões
                btnEdit.setOnClickListener {
                    onEditClick(trip)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(trip)
                }

                // Listener para clicar no item inteiro
                root.setOnClickListener {
                    onItemClick(trip)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding, onEditClick, onDeleteClick, onItemClick)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    /**
     * Atualiza a lista de viagens
     * @param newTrips nova lista de viagens
     */
    fun updateTrips(newTrips: List<Trip>) {
        trips.clear()
        trips.addAll(newTrips)
        notifyDataSetChanged()
    }

    /**
     * Adiciona uma nova viagem à lista
     * @param trip viagem a ser adicionada
     */
    fun addTrip(trip: Trip) {
        trips.add(0, trip) // Adiciona no início da lista
        notifyItemInserted(0)
    }

    /**
     * Remove uma viagem da lista
     * @param trip viagem a ser removida
     */
    fun removeTrip(trip: Trip) {
        val position = trips.indexOf(trip)
        if (position != -1) {
            trips.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Atualiza uma viagem na lista
     * @param trip viagem atualizada
     */
    fun updateTrip(trip: Trip) {
        val position = trips.indexOfFirst { it.id == trip.id }
        if (position != -1) {
            trips[position] = trip
            notifyItemChanged(position)
        }
    }
} 