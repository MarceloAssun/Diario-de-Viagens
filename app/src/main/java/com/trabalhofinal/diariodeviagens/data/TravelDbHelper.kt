package com.trabalhofinal.diariodeviagens.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.trabalhofinal.diariodeviagens.data.entity.Place
import com.trabalhofinal.diariodeviagens.data.entity.Trip
import com.trabalhofinal.diariodeviagens.data.entity.User

/**
 * Classe helper para gerenciar o banco de dados SQLite do aplicativo
 * Responsável por criar, atualizar e gerenciar as tabelas do banco
 */
class TravelDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "travel_diary.db"
        private const val DATABASE_VERSION = 1

        // Tabela de usuários
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_FULL_NAME = "full_name"

        // Tabela de viagens
        private const val TABLE_TRIPS = "trips"
        private const val COLUMN_TRIP_ID = "id"
        private const val COLUMN_TRIP_NAME = "name"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_START_DATE = "start_date"
        private const val COLUMN_END_DATE = "end_date"
        private const val COLUMN_USER_ID_FK = "user_id"
        private const val COLUMN_COMMENT = "comment"
        private const val COLUMN_RATING = "rating"

        // Tabela de lugares
        private const val TABLE_PLACES = "places"
        private const val COLUMN_PLACE_ID = "id"
        private const val COLUMN_PLACE_NAME = "name"
        private const val COLUMN_NOTES = "notes"
        private const val COLUMN_COMMENTS = "comments"
        private const val COLUMN_VISIT_DATE = "visit_date"
        private const val COLUMN_TRIP_ID_FK = "trip_id"
    }

    /**
     * Cria as tabelas do banco de dados quando ele é criado pela primeira vez
     */
    override fun onCreate(db: SQLiteDatabase) {
        // Criação da tabela de usuários
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_FULL_NAME TEXT NOT NULL
            )
        """.trimIndent()

        // Criação da tabela de viagens
        val createTripsTable = """
            CREATE TABLE $TABLE_TRIPS (
                $COLUMN_TRIP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRIP_NAME TEXT NOT NULL,
                $COLUMN_COUNTRY TEXT NOT NULL,
                $COLUMN_START_DATE TEXT NOT NULL,
                $COLUMN_END_DATE TEXT NOT NULL,
                $COLUMN_USER_ID_FK INTEGER NOT NULL,
                $COLUMN_COMMENT TEXT,
                $COLUMN_RATING INTEGER DEFAULT 0,
                FOREIGN KEY ($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        // Criação da tabela de lugares
        val createPlacesTable = """
            CREATE TABLE $TABLE_PLACES (
                $COLUMN_PLACE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PLACE_NAME TEXT NOT NULL,
                $COLUMN_NOTES TEXT,
                $COLUMN_COMMENTS TEXT,
                $COLUMN_VISIT_DATE TEXT NOT NULL,
                $COLUMN_TRIP_ID_FK INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_TRIP_ID_FK) REFERENCES $TABLE_TRIPS($COLUMN_TRIP_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createTripsTable)
        db.execSQL(createPlacesTable)
    }

    /**
     * Atualiza o banco de dados quando há mudanças na versão
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Remove as tabelas antigas e recria
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLACES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ==================== OPERAÇÕES COM USUÁRIOS ====================

    /**
     * Insere um novo usuário no banco de dados
     * @param user usuário a ser inserido
     * @return ID do usuário inserido ou -1 se falhar
     */
    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_FULL_NAME, user.fullName)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    /**
     * Verifica se um usuário existe com o nome de usuário fornecido
     * @param username nome de usuário para verificar
     * @return true se o usuário existe, false caso contrário
     */
    fun userExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    /**
     * Autentica um usuário com nome de usuário e senha
     * @param username nome de usuário
     * @param password senha do usuário
     * @return usuário se autenticado ou null
     */
    fun authenticateUser(username: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    // ==================== OPERAÇÕES COM VIAGENS ====================

    /**
     * Insere uma nova viagem no banco de dados
     * @param trip viagem a ser inserida
     * @return ID da viagem inserida ou -1 se falhar
     */
    fun insertTrip(trip: Trip): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRIP_NAME, trip.name)
            put(COLUMN_COUNTRY, trip.country)
            put(COLUMN_START_DATE, trip.startDate)
            put(COLUMN_END_DATE, trip.endDate)
            put(COLUMN_USER_ID_FK, trip.userId)
            put(COLUMN_COMMENT, trip.comment)
            put(COLUMN_RATING, trip.rating)
        }
        return db.insert(TABLE_TRIPS, null, values)
    }

    /**
     * Busca todas as viagens de um usuário específico
     * @param userId ID do usuário
     * @return lista de viagens do usuário
     */
    fun getTripsByUserId(userId: Long): List<Trip> {
        val trips = mutableListOf<Trip>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TRIPS,
            null,
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_START_DATE DESC"
        )

        while (cursor.moveToNext()) {
            val trip = Trip(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_NAME)),
                country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)),
                endDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE)),
                userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                rating = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATING))
            )
            trips.add(trip)
        }
        cursor.close()
        return trips
    }

    /**
     * Busca viagens por país
     * @param country nome do país
     * @return lista de viagens para o país
     */
    fun getTripsByCountry(country: String): List<Trip> {
        val trips = mutableListOf<Trip>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TRIPS,
            null,
            "$COLUMN_COUNTRY = ?",
            arrayOf(country),
            null, null,
            "$COLUMN_START_DATE DESC"
        )

        while (cursor.moveToNext()) {
            val trip = Trip(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_NAME)),
                country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)),
                endDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE)),
                userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID_FK))
            )
            trips.add(trip)
        }
        cursor.close()
        return trips
    }

    /**
     * Atualiza uma viagem existente
     * @param trip viagem com dados atualizados
     * @return número de linhas afetadas
     */
    fun updateTrip(trip: Trip): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRIP_NAME, trip.name)
            put(COLUMN_COUNTRY, trip.country)
            put(COLUMN_START_DATE, trip.startDate)
            put(COLUMN_END_DATE, trip.endDate)
            put(COLUMN_COMMENT, trip.comment)
            put(COLUMN_RATING, trip.rating)
        }
        return db.update(
            TABLE_TRIPS,
            values,
            "$COLUMN_TRIP_ID = ?",
            arrayOf(trip.id.toString())
        )
    }

    /**
     * Remove uma viagem do banco de dados
     * @param tripId ID da viagem a ser removida
     * @return número de linhas afetadas
     */
    fun deleteTrip(tripId: Long): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLE_TRIPS,
            "$COLUMN_TRIP_ID = ?",
            arrayOf(tripId.toString())
        )
    }

    // ==================== OPERAÇÕES COM LUGARES ====================

    /**
     * Insere um novo lugar no banco de dados
     * @param place lugar a ser inserido
     * @return ID do lugar inserido ou -1 se falhar
     */
    fun insertPlace(place: Place): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLACE_NAME, place.name)
            put(COLUMN_NOTES, place.notes)
            put(COLUMN_COMMENTS, place.comments)
            put(COLUMN_VISIT_DATE, place.visitDate)
            put(COLUMN_TRIP_ID_FK, place.tripId)
        }
        return db.insert(TABLE_PLACES, null, values)
    }

    /**
     * Busca todos os lugares de uma viagem específica
     * @param tripId ID da viagem
     * @return lista de lugares da viagem
     */
    fun getPlacesByTripId(tripId: Long): List<Place> {
        val places = mutableListOf<Place>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PLACES,
            null,
            "$COLUMN_TRIP_ID_FK = ?",
            arrayOf(tripId.toString()),
            null, null,
            "$COLUMN_VISIT_DATE ASC"
        )

        while (cursor.moveToNext()) {
            val place = Place(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PLACE_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLACE_NAME)),
                notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)),
                visitDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISIT_DATE)),
                tripId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID_FK))
            )
            places.add(place)
        }
        cursor.close()
        return places
    }

    /**
     * Atualiza um lugar existente
     * @param place lugar com dados atualizados
     * @return número de linhas afetadas
     */
    fun updatePlace(place: Place): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLACE_NAME, place.name)
            put(COLUMN_NOTES, place.notes)
            put(COLUMN_COMMENTS, place.comments)
            put(COLUMN_VISIT_DATE, place.visitDate)
        }
        return db.update(
            TABLE_PLACES,
            values,
            "$COLUMN_PLACE_ID = ?",
            arrayOf(place.id.toString())
        )
    }

    /**
     * Remove um lugar do banco de dados
     * @param placeId ID do lugar a ser removido
     * @return número de linhas afetadas
     */
    fun deletePlace(placeId: Long): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLE_PLACES,
            "$COLUMN_PLACE_ID = ?",
            arrayOf(placeId.toString())
        )
    }
} 