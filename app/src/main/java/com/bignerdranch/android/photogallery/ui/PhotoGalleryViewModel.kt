package com.bignerdranch.android.photogallery.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.bignerdranch.android.photogallery.entity.GalleryItem
import com.bignerdranch.android.photogallery.model.QueryPreferences
import com.bignerdranch.android.photogallery.remote.FlickrFetchr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PhotoGalleryViewModel(private val app: Application): AndroidViewModel(app) {

    //лист для хранение объектов
    var galleryItemLiveData: LiveData<List<GalleryItem>>
    //лист для хранение поисового запроса
    private val mutableSearchTerm = MutableLiveData<String>()

    private val flickrFetchr = FlickrFetchr()


    val searchTerm: String get() = mutableSearchTerm.value ?: ""

    init {
//        получает последний поисковый запрос
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        //        запрашивает даные у класса FlickrFetchr
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            } else {
                flickrFetchr.searchPhotos(searchTerm)
            }
        }
    }

    //принимает поисковый запрос от пользавателя и сохраняет его
    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}