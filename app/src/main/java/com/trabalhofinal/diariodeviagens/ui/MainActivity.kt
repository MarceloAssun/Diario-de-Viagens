package com.trabalhofinal.diariodeviagens.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.trabalhofinal.diariodeviagens.R
import com.trabalhofinal.diariodeviagens.data.TravelDbHelper
import com.trabalhofinal.diariodeviagens.data.entity.Trip
import com.trabalhofinal.diariodeviagens.databinding.ActivityMainBinding
import com.trabalhofinal.diariodeviagens.databinding.DialogAddEditTripBinding
import com.trabalhofinal.diariodeviagens.ui.adapter.TripAdapter
import com.google.android.material.slider.Slider
import java.text.SimpleDateFormat
import java.util.*
import android.view.LayoutInflater
import com.trabalhofinal.diariodeviagens.data.entity.Place
import com.trabalhofinal.diariodeviagens.databinding.ItemPlaceEditBinding
import com.trabalhofinal.diariodeviagens.databinding.DialogTripDetailsBinding
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import android.view.ViewGroup
import com.trabalhofinal.diariodeviagens.databinding.DialogAddEditPlaceBinding

/**
 * Activity principal do aplicativo
 * Exibe a lista de viagens e permite adicionar, editar e excluir viagens
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: TravelDbHelper
    private lateinit var tripAdapter: TripAdapter
    private var currentUserId: Long = 0
    private var currentUsername: String = ""
    private var editingTrip: Trip? = null

    private val countryList = listOf(
        "Brasil", "Estados Unidos", "França", "Itália", "Japão", "Espanha", "Inglaterra", "Alemanha", "Canadá", "Austrália", "Argentina", "Portugal", "Chile", "México", "China", "Índia", "Rússia", "África do Sul", "Egito", "Turquia"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtém os dados do usuário logado
        currentUserId = intent.getLongExtra("USER_ID", 0)
        currentUsername = intent.getStringExtra("USERNAME") ?: ""

        // Inicializa o helper do banco de dados
        dbHelper = TravelDbHelper(this)

        // Configura a interface
        setupUI()
        setupRecyclerView()
        loadTrips()

        // Configura os listeners
        setupListeners()
    }

    /**
     * Configura a interface inicial
     */
    private fun setupUI() {
        // Define o título com o nome do usuário
        supportActionBar?.title = "Diário de Viagens - $currentUsername"
        
        // Configura o spinner de filtro por país
        setupCountryFilter()
    }

    /**
     * Configura o RecyclerView
     */
    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onEditClick = { trip -> showEditTripDialog(trip) },
            onDeleteClick = { trip -> showDeleteConfirmation(trip) },
            onItemClick = { trip -> openTripDetails(trip) }
        )

        binding.recyclerViewTrips.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = tripAdapter
        }
    }

    /**
     * Configura o filtro por país
     */
    private fun setupCountryFilter() {
        val countries = listOf("Todos os países", "Brasil", "Estados Unidos", "França", "Itália", "Japão", "Espanha", "Inglaterra", "Alemanha", "Canadá", "Austrália")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCountry.adapter = adapter

        binding.spinnerCountry.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position == 0) {
                    loadTrips() // Carrega todas as viagens
                } else {
                    filterTripsByCountry(countries[position])
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Não faz nada quando nada é selecionado
            }
        }
    }

    /**
     * Configura os listeners dos botões
     */
    private fun setupListeners() {
        binding.fabAddTrip.setOnClickListener {
            showAddTripDialog()
        }
    }

    /**
     * Carrega todas as viagens do usuário
     */
    private fun loadTrips() {
        val trips = dbHelper.getTripsByUserId(currentUserId)
        tripAdapter.updateTrips(trips)
        updateEmptyState(trips.isEmpty())
    }

    /**
     * Filtra viagens por país
     */
    private fun filterTripsByCountry(country: String) {
        val allTrips = dbHelper.getTripsByUserId(currentUserId)
        val filteredTrips = allTrips.filter { it.country == country }
        tripAdapter.updateTrips(filteredTrips)
        updateEmptyState(filteredTrips.isEmpty())
    }

    /**
     * Atualiza o estado da tela quando não há viagens
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.recyclerViewTrips.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.recyclerViewTrips.visibility = View.VISIBLE
        }
    }

    /**
     * Exibe o diálogo para adicionar uma nova viagem
     */
    private fun showAddTripDialog() {
        editingTrip = null
        showTripDialog(null)
    }

    /**
     * Exibe o diálogo para editar uma viagem
     */
    private fun showEditTripDialog(trip: Trip) {
        editingTrip = trip
        showTripDialog(trip)
    }

    /**
     * Exibe o diálogo de adição/edição de viagem
     */
    private fun showTripDialog(trip: Trip?) {
        val dialogBinding = DialogAddEditTripBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (trip == null) "Nova Viagem" else "Editar Viagem")
            .setView(dialogBinding.root)
            .setPositiveButton(if (trip == null) "Adicionar" else "Atualizar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        // Lista temporária de locais visitados
        val places = mutableListOf<Place>()
        if (trip != null) {
            // Carrega locais do banco se estiver editando
            places.addAll(dbHelper.getPlacesByTripId(trip.id))
        }

        // Função para atualizar a UI dos locais (apenas visual)
        fun updatePlacesUI() {
            dialogBinding.layoutPlaces.removeAllViews()
            places.forEachIndexed { index, place ->
                val placeView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, dialogBinding.layoutPlaces, false)
                val tv1 = placeView.findViewById<TextView>(android.R.id.text1)
                val tv2 = placeView.findViewById<TextView>(android.R.id.text2)
                tv1.text = "${place.name} (${place.visitDate})"
                tv2.text = place.comments ?: ""
                // Editar local ao clicar
                placeView.setOnClickListener {
                    val placeDialogBinding = DialogAddEditPlaceBinding.inflate(layoutInflater)
                    placeDialogBinding.etPlaceName.setText(place.name)
                    placeDialogBinding.etPlaceDate.setText(place.visitDate)
                    placeDialogBinding.etPlaceComment.setText(place.comments ?: "")
                    val placeDialog = AlertDialog.Builder(this)
                        .setTitle("Editar Local Visitado")
                        .setView(placeDialogBinding.root)
                        .setPositiveButton("Salvar", null)
                        .setNegativeButton("Cancelar", null)
                        .create()
                    // Date picker
                    placeDialogBinding.btnPickPlaceDate.setOnClickListener {
                        val calendar = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        DatePickerDialog(this, { _, year, month, day ->
                            calendar.set(year, month, day)
                            placeDialogBinding.etPlaceDate.setText(dateFormat.format(calendar.time))
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }
                    placeDialog.show()
                    placeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = placeDialogBinding.etPlaceName.text.toString().trim()
                        val date = placeDialogBinding.etPlaceDate.text.toString().trim()
                        val comment = placeDialogBinding.etPlaceComment.text.toString().trim()
                        if (name.isEmpty()) {
                            placeDialogBinding.etPlaceName.error = "Nome obrigatório"
                            return@setOnClickListener
                        }
                        if (date.isEmpty()) {
                            placeDialogBinding.etPlaceDate.error = "Data obrigatória"
                            return@setOnClickListener
                        }
                        places[index] = place.copy(name = name, visitDate = date, comments = comment)
                        updatePlacesUI()
                        placeDialog.dismiss()
                    }
                }
                // Remover local ao long click
                placeView.setOnLongClickListener {
                    places.removeAt(index)
                    updatePlacesUI()
                    true
                }
                dialogBinding.layoutPlaces.addView(placeView)
            }
        }

        // Novo fluxo: abrir diálogo para adicionar local
        dialogBinding.btnAddPlace.setOnClickListener {
            val placeDialogBinding = DialogAddEditPlaceBinding.inflate(layoutInflater)
            val placeDialog = AlertDialog.Builder(this)
                .setTitle("Adicionar Local Visitado")
                .setView(placeDialogBinding.root)
                .setPositiveButton("Salvar", null)
                .setNegativeButton("Cancelar", null)
                .create()

            // Date picker
            placeDialogBinding.btnPickPlaceDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                DatePickerDialog(this, { _, year, month, day ->
                    calendar.set(year, month, day)
                    placeDialogBinding.etPlaceDate.setText(dateFormat.format(calendar.time))
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            placeDialog.show()

            placeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = placeDialogBinding.etPlaceName.text.toString().trim()
                val date = placeDialogBinding.etPlaceDate.text.toString().trim()
                val comment = placeDialogBinding.etPlaceComment.text.toString().trim()
                if (name.isEmpty()) {
                    placeDialogBinding.etPlaceName.error = "Nome obrigatório"
                    return@setOnClickListener
                }
                if (date.isEmpty()) {
                    placeDialogBinding.etPlaceDate.error = "Data obrigatória"
                    return@setOnClickListener
                }
                places.add(Place(name = name, notes = null, comments = comment, visitDate = date, tripId = trip?.id ?: 0))
                updatePlacesUI()
                placeDialog.dismiss()
            }
        }

        // Preenche o AutoCompleteTextView de países
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countryList)
        (dialogBinding.etCountry as? AutoCompleteTextView)?.setAdapter(countryAdapter)

        // Preenche os campos se estiver editando
        trip?.let {
            dialogBinding.etTripName.setText(it.name)
            dialogBinding.etCountry.setText(it.country, false)
            dialogBinding.etStartDate.setText(it.startDate)
            dialogBinding.etEndDate.setText(it.endDate)
            dialogBinding.etTripComment.setText(it.comment ?: "")
            dialogBinding.sliderTripRating.value = it.rating.toFloat()
            dialogBinding.tvTripRatingValue.text = it.rating.toString()
        }

        // Atualiza o valor da nota ao mover o slider
        dialogBinding.sliderTripRating.addOnChangeListener { slider, value, fromUser ->
            dialogBinding.tvTripRatingValue.text = value.toInt().toString()
        }

        // Configura os DatePickers
        setupDatePickers(dialogBinding)

        // Inicializa UI dos locais
        updatePlacesUI()

        dialog.show()

        // Configura o botão positivo para validar antes de fechar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (validateTripForm(dialogBinding)) {
                // Atualiza os dados dos locais antes de salvar
                for (i in 0 until dialogBinding.layoutPlaces.childCount) {
                    val placeView = dialogBinding.layoutPlaces.getChildAt(i)
                    val tv1 = placeView.findViewById<TextView>(android.R.id.text1)
                    val tv2 = placeView.findViewById<TextView>(android.R.id.text2)
                    val name = tv1.text.toString().split("(")[0].trim()
                    val date = tv1.text.toString().split("(")[1].replace(")", "").trim()
                    val comment = tv2.text.toString().trim()
                    places[i] = Place(name = name, notes = null, comments = comment, visitDate = date, tripId = trip?.id ?: 0)
                }
                saveTrip(dialogBinding, trip, places)
                dialog.dismiss()
            }
        }
    }

    /**
     * Configura os DatePickers para seleção de datas
     */
    private fun setupDatePickers(dialogBinding: DialogAddEditTripBinding) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // DatePicker para data de início
        dialogBinding.btnStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dialogBinding.etStartDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // DatePicker para data de fim
        dialogBinding.btnEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dialogBinding.etEndDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    /**
     * Valida o formulário de viagem
     */
    private fun validateTripForm(dialogBinding: DialogAddEditTripBinding): Boolean {
        val name = dialogBinding.etTripName.text.toString().trim()
        val country = dialogBinding.etCountry.text.toString().trim()
        val startDate = dialogBinding.etStartDate.text.toString().trim()
        val endDate = dialogBinding.etEndDate.text.toString().trim()

        if (name.isEmpty()) {
            dialogBinding.etTripName.error = "Nome da viagem é obrigatório"
            return false
        }

        if (country.isEmpty()) {
            dialogBinding.etCountry.error = "País é obrigatório"
            return false
        }

        if (startDate.isEmpty()) {
            dialogBinding.etStartDate.error = "Data de início é obrigatória"
            return false
        }

        if (endDate.isEmpty()) {
            dialogBinding.etEndDate.error = "Data de fim é obrigatória"
            return false
        }

        return true
    }

    /**
     * Salva a viagem no banco de dados
     */
    private fun saveTrip(dialogBinding: DialogAddEditTripBinding, existingTrip: Trip?, places: List<Place> = emptyList()) {
        val name = dialogBinding.etTripName.text.toString().trim()
        val country = dialogBinding.etCountry.text.toString().trim()
        val startDate = dialogBinding.etStartDate.text.toString().trim()
        val endDate = dialogBinding.etEndDate.text.toString().trim()
        val comment = dialogBinding.etTripComment.text.toString().trim()
        val rating = dialogBinding.sliderTripRating.value.toInt()

        if (existingTrip == null) {
            // Adicionando nova viagem
            val newTrip = Trip(
                name = name,
                country = country,
                startDate = startDate,
                endDate = endDate,
                userId = currentUserId,
                comment = comment,
                rating = rating
            )

            val tripId = dbHelper.insertTrip(newTrip)
            if (tripId != -1L) {
                // Salva locais visitados
                places.forEach { place ->
                    if (place.name.isNotEmpty() && place.visitDate.isNotEmpty()) {
                        dbHelper.insertPlace(place.copy(tripId = tripId))
                    }
                }
                val tripWithId = newTrip.copy(id = tripId)
                tripAdapter.addTrip(tripWithId)
                Toast.makeText(this, "Viagem adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                updateEmptyState(false)
            } else {
                Toast.makeText(this, "Erro ao adicionar viagem!", Toast.LENGTH_LONG).show()
            }
        } else {
            // Editando viagem existente
            val updatedTrip = existingTrip.copy(
                name = name,
                country = country,
                startDate = startDate,
                endDate = endDate,
                comment = comment,
                rating = rating
            )

            val rowsAffected = dbHelper.updateTrip(updatedTrip)
            if (rowsAffected > 0) {
                // Remove locais antigos e salva os novos
                dbHelper.getPlacesByTripId(updatedTrip.id).forEach { dbHelper.deletePlace(it.id) }
                places.forEach { place ->
                    if (place.name.isNotEmpty() && place.visitDate.isNotEmpty()) {
                        dbHelper.insertPlace(place.copy(tripId = updatedTrip.id))
                    }
                }
                tripAdapter.updateTrip(updatedTrip)
                Toast.makeText(this, "Viagem atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao atualizar viagem!", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Exibe confirmação para excluir viagem
     */
    private fun showDeleteConfirmation(trip: Trip) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Viagem")
            .setMessage("Tem certeza que deseja excluir a viagem '${trip.name}'?")
            .setPositiveButton("Excluir") { _, _ ->
                deleteTrip(trip)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Exclui uma viagem
     */
    private fun deleteTrip(trip: Trip) {
        val rowsAffected = dbHelper.deleteTrip(trip.id)
        if (rowsAffected > 0) {
            tripAdapter.removeTrip(trip)
            Toast.makeText(this, "Viagem excluída com sucesso!", Toast.LENGTH_SHORT).show()
            
            // Verifica se ainda há viagens na lista
            if (tripAdapter.itemCount == 0) {
                updateEmptyState(true)
            }
        } else {
            Toast.makeText(this, "Erro ao excluir viagem!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Abre os detalhes da viagem (para implementação futura)
     */
    private fun openTripDetails(trip: Trip) {
        val dialogBinding = DialogTripDetailsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Detalhes da Viagem")
            .setView(dialogBinding.root)
            .setPositiveButton("Fechar", null)
            .create()

        dialogBinding.tvDetailTripName.text = trip.name
        dialogBinding.tvDetailCountry.text = trip.country
        dialogBinding.tvDetailDateRange.text = "${trip.startDate} - ${trip.endDate}"
        dialogBinding.tvDetailRating.text = "Nota: ${trip.rating}"
        dialogBinding.tvDetailComment.text = trip.comment ?: ""

        // Carrega locais visitados
        val places = dbHelper.getPlacesByTripId(trip.id)
        dialogBinding.layoutDetailPlaces.removeAllViews()
        if (places.isEmpty()) {
            val tv = TextView(this)
            tv.text = "Nenhum local visitado cadastrado."
            tv.setPadding(0, 8, 0, 8)
            dialogBinding.layoutDetailPlaces.addView(tv)
        } else {
            places.forEach { place ->
                val tv = TextView(this)
                tv.text = "• ${place.name} (${place.visitDate})\n${place.comments ?: ""}"
                tv.setPadding(0, 8, 0, 8)
                dialogBinding.layoutDetailPlaces.addView(tv)
            }
        }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
} 