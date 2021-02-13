package com.bignerdranch.android.photogallery.model

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.entity.GalleryItem
import com.bignerdranch.android.photogallery.remote.FlickrFetchr
import com.bignerdranch.android.photogallery.ui.NOTIFICATION_CHANNEL_ID
import com.bignerdranch.android.photogallery.ui.PhotoGalleryActivity

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
    //запрос на получение последних фотографий
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
//            FlickrFetchr().fetchPhotosRequest()
//                .execute()
//                .body()
//                ?.photos
//                ?.galleryItems
            null
        } else {
            null
//            FlickrFetchr()
//                .searchPhotosRequest(query)
//                .execute()
//                .body()
//                ?.photos
//                ?.galleryItems
        } ?: emptyList()

//        Проверка, нет ли новых фотографий
        if (items.isEmpty()) {
            return Result.success()
        }
        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Получил старый результат: $resultId")
        } else {
            Log.i(TAG, "Получил новый результат: $resultId")
            QueryPreferences.setLastResultId(context, resultId)

//            создание отложеного интента и добовление канала PhotoGalleryApplication
            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            showBackgroundNotification(notification)
        }
        return Result.success()
    }


    //    Отправка широковещательного интента
    private fun showBackgroundNotification(notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, 0)
            putExtra(NOTIFICATION, notification)
        }
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"

        //        уникальным идентификатором пользовательского разрешения
        const val PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}