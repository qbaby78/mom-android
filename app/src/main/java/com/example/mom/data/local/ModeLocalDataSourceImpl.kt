package com.example.mom.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mom.domain.model.AppMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.momPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mom_preferences",
)

private const val MONITORED_CHILD_NONE = -1L

@Singleton
class ModeLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ModeLocalDataSource {

    private val dataStore = context.momPreferencesDataStore

    override fun observeMode(): Flow<AppMode> =
        dataStore.data.map { preferences ->
            val raw = preferences[KEY_APP_MODE] ?: AppMode.PARENT.name
            AppMode.valueOf(raw)
        }

    override suspend fun setMode(mode: AppMode) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_MODE] = mode.name
        }
    }

    override fun observeMonitoredChildId(): Flow<Long?> =
        dataStore.data.map { preferences ->
            val raw = preferences[KEY_MONITORED_CHILD_ID] ?: MONITORED_CHILD_NONE
            raw.takeIf { it != MONITORED_CHILD_NONE }
        }

    override suspend fun setMonitoredChildId(childId: Long?) {
        dataStore.edit { preferences ->
            preferences[KEY_MONITORED_CHILD_ID] = childId ?: MONITORED_CHILD_NONE
        }
    }

    override suspend fun clearMonitoredChildIdIf(childId: Long) {
        dataStore.edit { preferences ->
            val current = preferences[KEY_MONITORED_CHILD_ID] ?: MONITORED_CHILD_NONE
            if (current == childId) {
                preferences[KEY_MONITORED_CHILD_ID] = MONITORED_CHILD_NONE
            }
        }
    }

    companion object {
        private val KEY_APP_MODE = stringPreferencesKey("app_mode")
        private val KEY_MONITORED_CHILD_ID = longPreferencesKey("monitored_child_id")
    }
}