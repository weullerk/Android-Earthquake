package com.alienonwork.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.SimpleJobService;
import com.firebase.jobdispatcher.Trigger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarthquakeUpdateJobService extends SimpleJobService {
    private static final String TAG = "EarthquakeUpdateJob";
    private static final String UPDATE_JOB_TAG = "update_job";
    private static final String PERIODIC_JOB_TAG = "periodic_job";

    public static void scheduleUpdateJob(Context context) {
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        jobDispatcher.schedule(jobDispatcher.newJobBuilder()
                .setTag(UPDATE_JOB_TAG)
                .setService(EarthquakeUpdateJobService.class)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build());
    }

    @Override
    public int onRunJob(JobParameters job) {
        ArrayList<Earthquake> earthquakes = new ArrayList<>(0);

        URL url;
        try {
            String quakeFeed = getApplication().getString(R.string.earthquake_feed);
            url = new URL(quakeFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element entry = (Element)nl.item(i);
                        Element id = (Element)entry.getElementsByTagName("id").item(0);
                        Element title = (Element)entry.getElementsByTagName("title").item(0);
                        Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element)entry.getElementsByTagName("updated").item(0);
                        Element link = (Element)entry.getElementsByTagName("link").item(0);

                        String idString = id.getFirstChild().getNodeValue();
                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkString = hostname + link.getAttribute("href");
                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                        Date qdate = new GregorianCalendar(0,0,0).getTime();
                        try {
                            qdate = sdf.parse(dt);
                        } catch (ParseException e) {
                            Log.e(TAG, "Data parsing exception.", e);
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        int end = magnitudeString.length() - 1;
                        double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

                        if (details.contains("-"))
                            details = details.split("-")[1].trim();
                        else
                            details = "";

                        final Earthquake earthquake = new Earthquake(idString, qdate, details, l, magnitude, linkString);
                        earthquakes.add(earthquake);
                    }
                }
            }
            httpConnection.disconnect();

            EarthquakeDatabaseAccessor
                    .getInstance(getApplication())
                    .earthquakeDAO()
                    .insertEarthquakes(earthquakes);

            scheduleNextUpdate(job);

            return RESULT_SUCCESS;
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);
            return RESULT_FAIL_NORETRY;
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            return RESULT_FAIL_RETRY;
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "ParserConfigurationException", e);
            return RESULT_FAIL_NORETRY;
        } catch (SAXException e) {
            Log.e(TAG, "SAXException", e);
            return RESULT_FAIL_NORETRY;
        }
    }

    private void scheduleNextUpdate(JobParameters job) {
        if (job.getTag().equals(UPDATE_JOB_TAG)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int updateFreq = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
            boolean autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);

            if (autoUpdateChecked) {
                FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));

                jobDispatcher.schedule(jobDispatcher.newJobBuilder()
                        .setTag(PERIODIC_JOB_TAG)
                        .setService(EarthquakeUpdateJobService.class)
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .setReplaceCurrent(true)
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(updateFreq*60 / 2, updateFreq*60))
                        .setLifetime(Lifetime.FOREVER)
                        .build());
            }
        }
    }
}
