package net.stiller.encrypt.data.repository

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import net.stiller.encrypt.data.models.User
import net.stiller.encrypt.data.source.FirestoreDataSource


class FirestoreRepository(application: Application) {

    private var store: DataStore<Preferences>
    private var firestoreDataSource: FirestoreDataSource = FirestoreDataSource()
    private val mapper = ObjectMapper()

    init {
        store = application.applicationContext.dataStore
    }

    fun register(user: User): Completable {
        return firestoreDataSource.register(user)
    }

    fun login(email: String, password: String): Flowable<User> {
        return firestoreDataSource.login(email, password)
    }

    fun update(id: String, field: String, value: String): Completable {
        return firestoreDataSource.update(id, field, value)
    }

    fun readFirestore(id: String): Flowable<User> {
        return firestoreDataSource.read(id)
    }

    suspend fun logout() {
        store.edit { preferences ->
            preferences.remove(USER_PREF)
        }
    }

    suspend fun data(): String? {
        return store.data
            .map { preferences -> preferences[USER_PREF] }
            .first()
    }

    suspend fun writeUserStore(user: User) {
        val sUser = mapper.writeValueAsString(user)
        Log.d(TAG, sUser)
        store.edit { preferences ->
            preferences[USER_PREF] = sUser
        }
    }

    suspend fun readUserStore(): Flowable<User> {
        return Flowable.create({ emitter ->
            runBlocking {
                store.data.map { preferences ->
                    val sUser = preferences[USER_PREF]
                    val user = mapper.readValue(sUser, User::class.java)
                    emitter.onNext(user)
                    emitter.onComplete()
                }.first()
            }
        }, BackpressureStrategy.BUFFER)
    }

    fun isSession(): Flowable<Boolean> {
        return Flowable.create({ emitter ->
            runBlocking {
                store.data
                    .map { emitter.onNext(it.contains(USER_PREF)) }
                    .first()
            }
        }, BackpressureStrategy.BUFFER)
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "PreferenceDataStore"
        )
        private val USER_PREF = stringPreferencesKey("user_preferences")
        private const val TAG: String = "FirestoreDataSource"
    }
}