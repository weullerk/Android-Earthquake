package com.alienonwork.earthquake;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

public class EarthquakeRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new EarthquakeRemoteViewsFactory(this);
    }

    class EarthquakeRemoteViewsFactory implements RemoteViewsFactory {

        private Context mContext;
        private List<Earthquake> mEarthquakes;

        public EarthquakeRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            mEarthquakes = EarthquakeDatabaseAccessor.getInstance(mContext).earthquakeDAO().loadAllEarthquakeBlocking();
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            if (mEarthquakes == null) return 0;
            return mEarthquakes.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (mEarthquakes != null) {
                Earthquake earthquake = mEarthquakes.get(position);

                String id = earthquake.getId();
                String magnitude = String.valueOf(earthquake.getMagnitude());
                String details = earthquake.getDetails();

                RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.quake_widget);
                rv.setTextViewText(R.id.widget_magnitude, magnitude);
                rv.setTextViewText(R.id.widget_details, details);

                Intent intent = new Intent(mContext, EarthquakeMainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                rv.setOnClickPendingIntent(R.id.widget_magnitude, pendingIntent);
                rv.setOnClickPendingIntent(R.id.widget_details, pendingIntent);

                return rv;
            } else {
                return null;
            }
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mEarthquakes == null) return position;
            return mEarthquakes.get(position).getDate().getTime();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
