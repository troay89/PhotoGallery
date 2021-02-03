//package com.bignerdranch.android.photogallery.model
//
//import android.annotation.SuppressLint
//import android.graphics.Bitmap
//import android.os.Handler
//import android.os.HandlerThread
//import android.os.Message
//import android.util.Log
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.OnLifecycleEvent
//import com.bignerdranch.android.photogallery.model.FlickrFetchr
//import java.util.concurrent.ConcurrentHashMap
//
//private const val TAG = "ThumbnailDownloader"
//
////поле what для класса Message
//private const val MESSAGE_DOWNLOAD = 0
//
//
//class ThumbnailDownloader<in T>(
//    private val responseHandler: Handler, private val onThumbnailDownloaded: (T, Bitmap) -> Unit
//) : HandlerThread(TAG) {
//
//    //    загрузчик эскизов запускается при вызове onCreate(...) и останавливается при вызове функции onDestroy().
//    val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
//
//        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//        fun setup() {
//            Log.i(TAG, "Starting background thread")
//            start()
//            looper
//        }
//
//        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//        fun tearDown() {
//            Log.i(TAG, "Destroying background thread")
//            quit()
//        }
//    }
//
//
//    //    удаляет все запросы из очереди при уничтожении представления фрагмента см. 581
//    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
//        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//        fun clearQueue() {
//            Log.i(TAG, "Clearing all requests from queue")
//            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
//            requestMap.clear()
//        }
//    }
//
//
//    //    индификатор открытие, хакрытие фонового потока
//    private var hasQuit = false
//
//    //    поле отвечающие за приём обработку и отправку сообщений между потоками
//    private lateinit var requestHandler: Handler
//
//    //    разновидность HashMap, безопасную по отношению к потокам
//    private val requestMap = ConcurrentHashMap<T, String>()
//
//    private val flickrFetchr = FlickrFetchr()
//
//
//    //    завершение потока ThumbnailDownloader
//    override fun quit(): Boolean {
//        hasQuit = true
//        return super.quit()
//    }
//
//
//    //  получение сообщение от основного потока и поставка его в очередь
//    fun queueThumbnail(target: T, url: String) {
//        Log.i(TAG, "Получил URL: $url")
////    переменная requestMap обновляется связью между идентификатором запроса (PhotoHolder) и URL-адресом запроса
//        requestMap[target] = url      //????
//        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
//    }
//
//
//    //получение Message из очереди и отправка его на обработку
//    @Suppress("UNCHECKED_CAST")
//    @SuppressLint("HandlerLeak")
//    override fun onLooperPrepared() {
//        requestHandler = object : Handler() {
//            //            вызывается, когда сообщение загрузки извлечено из очереди и готово к обработке
//            override fun handleMessage(msg: Message) {
//                if (msg.what == MESSAGE_DOWNLOAD) {
//                    val target = msg.obj as T   //холдер
//                    Log.i(TAG, "Получил запрос на URL: ${requestMap[target]}")
//                    handleRequest(target)
//                }
//            }
//        }
//    }
//
//
//    //обработка Message
//    private fun handleRequest(target: T) {
//        val url = requestMap[target] ?: return
////        отправка запроса FlickrFetchr и потом на api.FlickrApi
//        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
//
//        //отправка полдера и картинки в основной поток
//        responseHandler.post(Runnable {
//            if (requestMap[target] != url || hasQuit) {
//                return@Runnable
//            }
//            requestMap.remove(target)
//            onThumbnailDownloaded(target, bitmap)
//        })
//    }
//}