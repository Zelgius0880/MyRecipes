package zelgius.com.myrecipes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class DataStoreRepository(context: Context) {
    private val dataStore = context.dataStore

    suspend fun unit(name: String) =
        dataStore.data.first()[stringPreferencesKey(name)]

    suspend fun saveUnit(ingredient: String, unit: String) {
        dataStore.edit {
            it[stringPreferencesKey(ingredient)] = unit
        }
    }

}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "units")