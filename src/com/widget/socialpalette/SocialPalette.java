package com.widget.socialpalette;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class SocialPalette extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
									int[] appWidgetIds) {
	
		Intent intent = new Intent(context, MessageActivity.class);		
	    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
	    
	    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
	    views.setOnClickPendingIntent(R.id.mywallBtn, pendingIntent);
	    
	    appWidgetManager.updateAppWidget(appWidgetIds, views);
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID);
			
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
			
		} else {
			super.onReceive(context, intent);
		}
	}
	
}
