package com.example.lifelogger.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifelogger.data.LifeLogEntry
import com.example.lifelogger.data.LogDao
import com.example.lifelogger.data.NetworkMonitor
import com.example.lifelogger.data.SupabaseHelper
import com.example.lifelogger.data.SupabaseLog
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class LifeLogViewModel(
    application: Application,
    private val logDao: LogDao,
    private val networkMonitor: NetworkMonitor
) : AndroidViewModel(application) {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLogs: StateFlow<List<LifeLogEntry>> = _currentUserId.flatMapLatest { userId ->
        logDao.getLogsForUser(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered logs for the "Today" section on the Home Screen
    val todayLogs: `StateFlow`<List<LifeLogEntry>> = allLogs.map { logs ->
        val today = Calendar.getInstance()
        logs.filter { log ->
            val logDate = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            logDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            logDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search and Category states for the AllLogsScreen
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow<Long?>(null)
    val selectedDateFilter = _selectedDateFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredLogs: StateFlow<List<LifeLogEntry>> = combine(
        allLogs, _searchQuery, _selectedCategoryFilter, _selectedDateFilter
    ) { logs, query, category, dateMillis ->
        logs.filter { log ->
            val matchesQuery = log.title.contains(query, ignoreCase = true) || 
                             log.description.contains(query, ignoreCase = true)
            val matchesCategory = if (category == "All") true else log.category == category
            
            val matchesDate = if (dateMillis == null) true else {
                val filterDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
                val logDate = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                logDate.get(Calendar.YEAR) == filterDate.get(Calendar.YEAR) &&
                logDate.get(Calendar.DAY_OF_YEAR) == filterDate.get(Calendar.DAY_OF_YEAR)
            }
            
            matchesQuery && matchesCategory && matchesDate
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun updateDateFilter(dateMillis: Long?) {
        _selectedDateFilter.value = dateMillis
    }

    fun setUserId(userId: String?) {
        _currentUserId.value = userId
        if (userId != null) {
            fetchRemoteLogs(userId)
        }
    }

    private fun fetchRemoteLogs(userId: String) {
        viewModelScope.launch {
            try {
                val remoteLogs = SupabaseHelper.client.postgrest["life_logs"]
                    .select {
                        filter { eq("user_id", userId) }
                    }.decodeList<SupabaseLog>()
                
                remoteLogs.forEach { remote ->
                    val local = LifeLogEntry(
                        id = remote.id,
                        title = remote.title,
                        description = remote.description,
                        timestamp = remote.timestamp,
                        imageFilePath = remote.image_file_path,
                        audioFilePath = remote.audio_file_path,
                        category = remote.category,
                        userId = remote.user_id,
                        isSyncedWithCloud = true
                    )
                    logDao.insertLog(local)
                }
            } catch (e: Exception) {
                android.util.Log.e("LifeLogViewModel", "Fetch remote logs failed: ${e.message}")
            }
        }
    }

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    private suspend fun uploadFile(bucketName: String, localPath: String?): String? {
        if (localPath == null || localPath.isBlank()) return null
        // If it's already a URL, don't re-upload
        if (localPath.startsWith("http")) return localPath

        return try {
            val bytes = if (localPath.startsWith("content://")) {
                val uri = Uri.parse(localPath)
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } else {
                val file = File(localPath)
                if (file.exists()) file.readBytes() else null
            }

            if (bytes == null) {
                android.util.Log.e("LifeLogViewModel", "Could not read bytes for: $localPath")
                return localPath
            }

            val fileExtension = if (bucketName == "images") "jpg" else "3gp"
            val fileName = "${System.currentTimeMillis()}.${fileExtension}"
            
            android.util.Log.d("SupabaseSync", "Uploading to $bucketName: $fileName")
            val bucket = SupabaseHelper.client.storage[bucketName]
            
            // Perform the upload
            bucket.upload(fileName, bytes)
            
            // Get the public URL
            val publicUrl = bucket.publicUrl(fileName)
            android.util.Log.d("SupabaseSync", "Upload successful! URL: $publicUrl")
            publicUrl
        } catch (e: Exception) {
            android.util.Log.e("LifeLogViewModel", "Upload to $bucketName failed: ${e.message}", e)
            // Return local path so we can try again later, 
            // but we won't sync this specific field to Supabase as a URL
            localPath
        }
    }

    fun syncLogs(userId: String?) {
        if (userId == null) {
            _syncMessage.value = "You should login to sync logs"
            return
        }
        
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                // 1. Adopt guest logs locally
                logDao.adoptGuestLogs(userId)
                setUserId(userId)

                // 2. Fetch all unsynced logs
                val unsynced = logDao.getUnsyncedLogs()
                unsynced.forEach { log ->
                    // Attempt to upload media and get URLs
                    val imageUrl = uploadFile("images", log.imageFilePath)
                    val audioUrl = uploadFile("audio", log.audioFilePath)

                    // IMPORTANT: Only sync to Supabase if the paths are now valid URLs
                    // or if there was no media to begin with.
                    val isImageReady = log.imageFilePath == null || imageUrl?.startsWith("http") == true
                    val isAudioReady = log.audioFilePath == null || audioUrl?.startsWith("http") == true

                    if (isImageReady && isAudioReady) {
                        val supabaseLog = SupabaseLog(
                            id = log.id,
                            title = log.title,
                            description = log.description,
                            timestamp = log.timestamp,
                            image_file_path = imageUrl,
                            audio_file_path = audioUrl,
                            category = log.category,
                            user_id = userId,
                            is_synced_with_cloud = true
                        )
                        
                        SupabaseHelper.client.postgrest["life_logs"].upsert(supabaseLog)
                        
                        // Update local Room DB with the URLs and mark as synced
                        val updatedLocal = log.copy(
                            imageFilePath = imageUrl,
                            audioFilePath = audioUrl,
                            isSyncedWithCloud = true,
                            userId = userId
                        )
                        logDao.insertLog(updatedLocal)
                        android.util.Log.d("SupabaseSync", "Successfully synced log: ${log.title}")
                    } else {
                        android.util.Log.w("SupabaseSync", "Skipping sync for ${log.title} because media upload failed.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LifeLogViewModel", "Sync cycle failed: ${e.message}", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Update the signature to accept category
    fun saveEntry(title: String, description: String, imagePath: String?, audioPath: String?, category: String, userId: String?) {
        viewModelScope.launch {
            val newEntry = LifeLogEntry(
                title = title,
                description = description,
                imageFilePath = imagePath,
                audioFilePath = audioPath,
                category = category,
                userId = userId
            )
            logDao.insertLog(newEntry)
            
            if (userId != null) {
                _syncMessage.value = "Successfully created"
                syncLogs(userId)
            } else {
                _syncMessage.value = "Successfully created. Login to sync logs."
            }
        }
    }

    fun deleteEntry(logId: String, userId: String?) {
        viewModelScope.launch {
            logDao.deleteLogById(logId)
            _syncMessage.value = "Entry deleted"
            if (userId != null) {
                try {
                    SupabaseHelper.client.postgrest["life_logs"].delete {
                        filter {
                            eq("id", logId)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LifeLogViewModel", "Failed to delete from Supabase: ${e.message}")
                }
            }
        }
    }

    fun updateEntry(updatedEntry: LifeLogEntry, userId: String?) {
        viewModelScope.launch {
            // Room handles replace if ID exists
            logDao.insertLog(updatedEntry)
            _syncMessage.value = "Entry updated"
            
            if (userId != null) {
                try {
                    // Re-sync including possible new media uploads
                    syncLogs(userId)
                } catch (e: Exception) {
                    android.util.Log.e("LifeLogViewModel", "Update sync failed: ${e.message}")
                }
            }
        }
    }

    suspend fun getLogById(logId: String): LifeLogEntry? {
        return logDao.getLogById(logId)
    }
}


// A Factory is required to pass the LogDao into the ViewModel when the app starts
class LifeLogViewModelFactory(
    private val application: Application,
    private val logDao: LogDao,
    private val networkMonitor: NetworkMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LifeLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LifeLogViewModel(application, logDao, networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}