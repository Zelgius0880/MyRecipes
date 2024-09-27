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

    suspend fun unit(name: String) =
        dataStore.data.first()[stringPreferencesKey(name)]

    suspend fun saveUnit(ingredient: String, unit: String) {
        dataStore.edit {
            it[stringPreferencesKey(ingredient)] = unit
        }
    }

    val isIAGenerationChecked: Flow<Boolean> get() = dataStore.data.map { preferences ->
        preferences[iaGenerationKey] ?: false
    }

    suspend fun setIAGenerationChecked(checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[iaGenerationKey] = checked
        }
    }

}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "units")