package com.skb.tasktracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skb.tasktracker.TaskTrackerApp

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (TaskTrackerApp) -> VM
): VM {
    val app = LocalContext.current.applicationContext as TaskTrackerApp
    return viewModel(
        viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
        key = key,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create(app) as T
        }
    )
}
