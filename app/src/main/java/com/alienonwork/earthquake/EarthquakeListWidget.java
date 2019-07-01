package com.alienonwork.earthquake;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class EarthquakeListWidget extends AppWidgetProvider {

    static void updateAppWidgets(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds, final PendingResult pendingResult) {
        Thread thread = new Thread() {
            public void run() {
                for (int appWidgetId : appWidgetIds) {
                    Intent intent = new Intent(context, EarthquakeRemoteViewsService.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.quake_collection_widget);
                    views.setRemoteAdapter(R.id.widget_list_view, intent);
                    views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_text);

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
                if (pendingResult != null)
                    pendingResult.finish();
            }
        };
        thread.start();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PendingResult pendingResult = goAsync();

        updateAppWidgets(context, appWidgetManager, appWidgetIds, pendingResult);
    }

    @Override
    public void onEnabled(Context context) {
        final PendingResult pendingResult = goAsync();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName earthquakeListWidget = new ComponentName(context, EarthquakeListWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(earthquakeListWidget);

        updateAppWidgets(context, appWidgetManager, appWidgetIds, pendingResult);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);

        if (EarthquakeWidget.NEW_QUAKE_BROADCAST.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName earthquakeListWidget = new ComponentName(context, EarthquakeListWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(earthquakeListWidget);

            final PendingResult pendingResult = goAsync();
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        }
    }
}
