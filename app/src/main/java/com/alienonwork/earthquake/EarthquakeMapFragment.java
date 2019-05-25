package com.alienonwork.earthquake;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EarthquakeMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int mMinimumMagnitude = 0;
    Map<String, Marker> mMarkers = new HashMap<>();
    List<Earthquake> mEarthquakes;

    EarthquakeViewModel earthquakeViewModel;

    private SharedPreferences.OnSharedPreferenceChangeListener mPListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PreferencesActivity.PREF_MIN_MAG.equals(key)) {
                List<Earthquake> earthquakes = earthquakeViewModel.getEarthquakes().getValue();
                if (earthquakes != null)
                    setEarthquakeMarkers(earthquakes);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earthquake_map, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(mPListener);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        earthquakeViewModel = ViewModelProviders.of(getActivity()).get(EarthquakeViewModel.class);

        earthquakeViewModel.getEarthquakes().observe(this, new Observer<List<Earthquake>>() {
            @Override
            public void onChanged(@Nullable List<Earthquake> earthquakes) {
                if (earthquakes != null)
                    setEarthquakeMarkers(earthquakes);
            }
        });
    }

    private void updateFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mMinimumMagnitude = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3"));
    }

    public void setEarthquakeMarkers(List<Earthquake> earthquakes) {
        updateFromPreferences();
        mEarthquakes = earthquakes;
        if (mMap == null || earthquakes == null) return;
        Map<String, Earthquake> newEarthquakes = new HashMap<>();

        for (Earthquake earthquake : earthquakes) {
            if (earthquake.getMagnitude() >= mMinimumMagnitude) {
                newEarthquakes.put(earthquake.getId(), earthquake);

                if (!mMarkers.containsKey(earthquake.getId())) {
                    Location location = earthquake.getLocation();
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("M:" + earthquake.getMagnitude()));

                    mMarkers.put(earthquake.getId(), marker);
                }
            }
        }

        for (Iterator<String> iterator = mMarkers.keySet().iterator(); iterator.hasNext();) {
            String earthquakeID = iterator.next();
            if (!newEarthquakes.containsKey(earthquakeID)) {
                mMarkers.get(earthquakeID).remove();
                iterator.remove();
            }
        }
    }


}
