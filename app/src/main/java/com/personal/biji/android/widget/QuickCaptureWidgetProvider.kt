package com.personal.biji.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.personal.biji.android.MainActivity
import com.personal.biji.android.R
import com.personal.biji.android.ui.ACTION_NEW_TEXT_NOTE
import com.personal.biji.android.ui.ACTION_SCAN_CAMERA
import com.personal.biji.android.ui.ACTION_SCAN_GALLERY

class QuickCaptureWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, views(context)) }
    }

    private fun views(context: Context) =
        RemoteViews(context.packageName, R.layout.widget_quick_capture).apply {
            setOnClickPendingIntent(R.id.widget_text, pendingIntent(context, ACTION_NEW_TEXT_NOTE, 1))
            setOnClickPendingIntent(R.id.widget_camera, pendingIntent(context, ACTION_SCAN_CAMERA, 2))
            setOnClickPendingIntent(R.id.widget_gallery, pendingIntent(context, ACTION_SCAN_GALLERY, 3))
        }

    private fun pendingIntent(context: Context, action: String, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
