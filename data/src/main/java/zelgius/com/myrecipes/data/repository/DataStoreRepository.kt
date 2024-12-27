package zelgius.com.myrecipes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreRepository(context: Context) {
    private val dataStore = context.dataStore
    private val iaGenerationKey = booleanPreferencesKey("isIAGenerationChecked")
    private val textReadingKey = booleanPreferencesKey("isTextReadingChecked")
    private val gestureRecognitionKey = booleanPreferencesKey("isGestureRecognitionChecked")
    private val stillNeedToGenerateKey = booleanPreferencesKey("stillNeedToGenerate")

    suspend fun unit(name: String) =
        dataStore.data.first()[stringPreferencesKey(name)]

    suspend fun saveUnit(ingredient: String, unit: String) {
        dataStore.edit {
            it[stringPreferencesKey(ingredient)] = unit
        }
    }

    val isIAGenerationChecked: Flow<Boolean> get() = dataStore.data.map { preferences ->
        preferences[iaGenerationKey] != false
    }

    val isTextReadingChecked: Flow<Boolean> get() = dataStore.data.map { preferences ->
        preferences[textReadingKey] != false
    }


    val isGestureRecognitionChecked: Flow<Boolean> get() = dataStore.data.map { preferences ->
        preferences[gestureRecognitionKey] != false
    }


    val stillNeedToGenerate: Flow<Boolean> get() = dataStore.data.map { preferences ->
        preferences[stillNeedToGenerateKey] == true
    }

    suspend fun setIAGenerationChecked(checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[iaGenerationKey] = checked
        }
    }

    suspend fun setTextReadingChecked(checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[textReadingKey] = checked
        }
    }

    suspend fun setStillNeedToGenerate(b: Boolean) {
        dataStore.edit { preferences ->
            preferences[stillNeedToGenerateKey] = b
        }
    }

    suspend fun setGestureRecognitionChecked(checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[gestureRecognitionKey] = checked
        }
    }

}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "units")