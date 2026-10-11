package com.maxrave.simpmusic.service.rss

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Retained so article jobs persisted by earlier versions finish without fetching or notifying. */
class RssFeedNotifyWork(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.success()
}
