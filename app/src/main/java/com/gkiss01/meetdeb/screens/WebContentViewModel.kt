package com.gkiss01.meetdeb.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gkiss01.meetdeb.network.WebApi
import com.gkiss01.meetdeb.network.data.AlbumProperty
import kotlinx.coroutines.launch

class WebContentViewModel : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    private val _albums = MutableLiveData<List<AlbumProperty>>()
    val albums: LiveData<List<AlbumProperty>>
        get() = _albums

    private fun getAlbums() {
        viewModelScope.launch {
            val getAlbumsDeferred = WebApi.retrofitService.getAlbums()
            try {
                val listResult = getAlbumsDeferred.await()
                _response.value = "Success: ${listResult.size} Album properties retrieved"
                _albums.value = listResult
            }
            catch (e: Exception) {
                _response.value = "Failure: ${e.message}"
            }
        }
    }

    init {
        getAlbums()
    }
}
