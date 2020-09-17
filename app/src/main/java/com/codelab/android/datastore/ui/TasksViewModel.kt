/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelab.android.datastore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codelab.android.datastore.data.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

//TasksUiModel object wraps the list of tasks, the show completed
// and sort order - data that needs to be displayed in the UI.
data class TasksUiModel(
    val tasks: List<Task>,
    val showCompleted: Boolean,
    val sortOrder: SortOrder
)

// MutableStateFlow is an experimental API so we're annotating the class accordingly
class TasksViewModel(
    repository: TasksRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // TasksUIModel most recent value depends on the most
    // recent values of the corresponding flows.
    // Every time the "sort order", the "show completed" filter
    // or the list of tasks emits, we should recreate tasksUiModelFlow.
   // Compose it with "combine"
    private val tasksUiModelFlow = combine(repository.tasks, userPreferencesFlow){
            tasks: List<Task>, userPreferences: UserPreferences ->
        return@combine TasksUiModel(
            tasks = filterSortTasks(tasks, userPreferences.showCompleted, userPreferences.sortOrder),
            showCompleted = userPreferences.showCompleted,
            sortOrder = userPreferences.sortOrder
        )
    }

    //To ensure that we're updating the UI correctly, only when
    // the Activity is started, we expose as a LiveData.
    val tasksUiModel = tasksUiModelFlow.asLiveData()

    //Helper method
    private fun filterSortTasks(
        tasks: List<Task>,
        showCompleted: Boolean,
        sortOrder: SortOrder
    ): List<Task> {
        // filter the tasks
        val filteredTasks = if (showCompleted) {
            tasks
        } else {
            tasks.filter { !it.completed }
        }
        // sort the tasks
        return when (sortOrder) {
            SortOrder.NONE -> filteredTasks
            SortOrder.BY_DEADLINE -> filteredTasks.sortedByDescending { it.deadline }
            SortOrder.BY_PRIORITY -> filteredTasks.sortedBy {it.priority}
            SortOrder.BY_DEADLINE_AND_PRIORITY -> filteredTasks.sortedWith(
                compareByDescending<Task> { it.deadline }.thenBy { it.priority }
            )
        }
    }

    fun showCompletedTasks(showCompleted: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAndSaveShowCompleted(showCompleted)
        }
    }

    fun enableSortByDeadline(isChecked: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.enableSortByDeadline(isChecked)
        }
    }

    fun enableSortByPriority(isChecked: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.enableSortByPriority(isChecked)
        }
    }
}

class TasksViewModelFactory(
    private val repository: TasksRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// **** BEFORE MIGRETING TO SHARED PREFS: ****
//TasksUiModel object wraps the list of tasks, the show completed
// and sort order - data that needs to be displayed in the UI.
//data class TasksUiModel(
//    val tasks: List<Task>,
//    val showCompleted: Boolean,
//    val sortOrder: SortOrder
//)
//
//// MutableStateFlow is an experimental API so we're annotating the class accordingly
//class TasksViewModel(
//    repository: TasksRepository,
//    private val userPreferencesRepository: UserPreferencesRepository
//) : ViewModel() {
//
//
//    // Holds the latest "show completed" value, which is only kept in memory.
//    // Keeps the "show completed" filter as a stream of changes.
//    private val showCompletedFlow = MutableStateFlow(false)
//
//
//    // Holds the latest SortOrder value. Keep the "sort order" as a stream of changes.
//    private val sortOrderFlow = userPreferencesRepository.sortOrderFlow
//
//    // TasksUIModel most recent value depends on the most
//    // recent values of the corresponding flows.
//    // Every time the "sort order", the "show completed" filter
//    // or the list of tasks emits, we should recreate tasksUiModelFlow.
//    // Compose it with "combine"
//    private val tasksUiModelFlow = combine(repository.tasks, showCompletedFlow, sortOrderFlow){
//            tasks: List<Task>, showCompleted: Boolean, sortOrder: SortOrder ->
//        return@combine TasksUiModel(
//            tasks = filterSortTasks(tasks, showCompleted, sortOrder),
//            showCompleted = showCompleted,
//            sortOrder = sortOrder
//        )
//    }
//
//    //To ensure that we're updating the UI correctly, only when
//    // the Activity is started, we expose as a LiveData.
//    val tasksUiModel = tasksUiModelFlow.asLiveData()
//
//    //Helper method
//    private fun filterSortTasks(
//        tasks: List<Task>,
//        showCompleted: Boolean,
//        sortOrder: SortOrder
//    ): List<Task> {
//        // filter the tasks
//        val filteredTasks = if (showCompleted) {
//            tasks
//        } else {
//            tasks.filter { !it.completed }
//        }
//        // sort the tasks
//        return when (sortOrder) {
//            SortOrder.NONE -> filteredTasks
//            SortOrder.BY_DEADLINE -> filteredTasks.sortedByDescending { it.deadline }
//            SortOrder.BY_PRIORITY -> filteredTasks.sortedBy {it.priority}
//            SortOrder.BY_DEADLINE_AND_PRIORITY -> filteredTasks.sortedWith(
//                compareByDescending<Task> { it.deadline }.thenBy { it.priority }
//            )
//        }
//    }
//
//    fun showCompletedTasks(show: Boolean) {
//        showCompletedFlow.value = show
//    }
//
//    fun enableSortByDeadline(isChecked: Boolean) {
//        userPreferencesRepository.enableSortByDeadline(isChecked)
//    }
//
//    fun enableSortByPriority(isChecked: Boolean) {
//        userPreferencesRepository.enableSortByPriority(isChecked)
//    }
//}