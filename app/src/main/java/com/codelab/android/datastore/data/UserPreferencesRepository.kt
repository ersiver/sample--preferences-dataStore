package com.codelab.android.datastore.data

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.*
import java.io.IOException

private const val USER_PREFERENCES_NAME = "user_preferences"

enum class SortOrder {
    NONE,
    BY_DEADLINE,
    BY_PRIORITY,
    BY_DEADLINE_AND_PRIORITY
}

// Store user preferences that will be writted to and read from DatStore:
data class UserPreferences(val showCompleted: Boolean, val sortOrder: SortOrder)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(context: Context) {
    // *** DataStore builder *** :
    // Only in case migrating from SharePreferences add the
    // SharedPreferencesMigration to the list of migrations
    // passing the name of prefs.
    private val dataStore: DataStore<Preferences> =
        context.createDataStore(
            name = USER_PREFERENCES_NAME,
            migrations = listOf(SharedPreferencesMigration(context, USER_PREFERENCES_NAME))
        )

    //*** Setup key for a key-value DataStore Preferences: ***
    private object PreferencesKeys {
        val SHOW_COMPLETED = preferencesKey<Boolean>("show_completed")

        // Note: we migrate the sorOrder preferences, this has
        // the the same name that we used with SharedPreferences.
        //All keys will be migrated to our DataStore and
        // deleted from the user preferences SharedPreferences.
        val SORT_ORDER = preferencesKey<String>("sort_order")
    }

    /**
     * Get the user preferences flow.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            //Read the showCompleted:
            val showCompleted = preferences[PreferencesKeys.SHOW_COMPLETED] ?: false

            //Read the sort order from preferences and convert it to a [SortOrder] object
            val sortOrder =
                SortOrder.valueOf(preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name)

            //Construct the UserPreferences object:
            UserPreferences(showCompleted, sortOrder)
        }

    // *** Writing data showCompleted to Preferences DataStore: ***
    suspend fun updateAndSaveShowCompleted(showCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COMPLETED] = showCompleted
        }
    }

    // *** Writing data sortOrder to Preferences DataStore: ***
    suspend fun enableSortByDeadline(isChecked: Boolean) {

        dataStore.edit { preferences ->
            // Get the current SortOrder as an enum
            val currentOrder =
                SortOrder.valueOf(preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name)

            val newSortOrder =
                if (isChecked) {
                    //User checked to sort by deadline
                    if (currentOrder == SortOrder.BY_PRIORITY) {
                        //Priority button is already checked
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_DEADLINE
                    }
                } else {
                    //User unchecked deadline button.
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_PRIORITY
                    } else {
                        //No button is checked.
                        SortOrder.NONE
                    }
                }

            preferences[PreferencesKeys.SORT_ORDER] = newSortOrder.name

        }
    }

    // *** Writing data sortOrder to Preferences DataStore: ***
    suspend fun enableSortByPriority(isChecked: Boolean) {
        dataStore.edit { preferences ->
            val currentOrder =
                SortOrder.valueOf(preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name)

            val newSortOrder =
                if (isChecked) {
                    //User checked to sort by priority.
                    if (currentOrder == SortOrder.BY_DEADLINE) {
                        //Deadline button is already checked
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_PRIORITY
                    }
                } else {
                    //User unchecked to sort by priority.
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_DEADLINE
                    } else {
                        //No button checked.
                        SortOrder.NONE
                    }
                }

            preferences[PreferencesKeys.SORT_ORDER] = newSortOrder.name
        }

    }
}
