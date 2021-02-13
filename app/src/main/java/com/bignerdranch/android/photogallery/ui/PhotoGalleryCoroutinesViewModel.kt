package com.bignerdranch.android.photogallery.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.photogallery.entity.GalleryItem
import com.bignerdranch.android.photogallery.model.QueryPreferences
import com.bignerdranch.android.photogallery.remote.FlickrFetchr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoGalleryCoroutinesViewModel(private val app: Application) : AndroidViewModel(app) {

    //лист для хранение объектов
    val galleryItemLiveData = MutableLiveData<List<GalleryItem>>()

//    var galleryItemLiveData: LiveData<List<GalleryItem>>

    //лист для хранение поисового запроса
    private val mutableSearchTerm = MutableLiveData<String>()

    private val flickrFetchr = FlickrFetchr()


    val searchTerm: String get() = mutableSearchTerm.value ?: ""

    init {
//        получает последний поисковый запрос
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        Log.d("sanay", "мы в методе init")

        viewModelScope.launch(Dispatchers.Default) {
            //        запрашивает даные у класса FlickrFetchr

            if (searchTerm.isBlank()) {
                galleryItemLiveData.postValue(flickrFetchr.fetchPhotosRequestCoroutines().photos.galleryItems)
            } else {
                galleryItemLiveData.postValue(flickrFetchr.searchPhotosRequestCoroutines(searchTerm).photos.galleryItems)
            }
        }
    }

    //принимает поисковый запрос от пользавателя и сохраняет его
    fun fetchPhotos(query: String = "") {
        Log.d("sanay", "мы в методе fetchPhotos")
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}