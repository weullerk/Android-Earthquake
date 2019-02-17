package com.alienonwork.earthquake;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeSearchResultActivity extends AppCompatActivity {

    private ArrayList<Earthquake> mEarthquakes = new ArrayList<>();
    private EarthquakeRecyclerViewAdapter mEarthquakeAdapter = new EarthquakeRecyclerViewAdapter(mEarthquakes);

    MutableLiveData<String> searchQuery;
    MutableLiveData<String> selectedSearchSuggestionId;
    LiveData<List<Earthquake>> searchResults;
    LiveData<Earthquake> selectedSearchSuggestion;

    private void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    private void setSelectedSearchSuggestion(Uri dataString) {
        String id = dataString.getPathSegments().get(1);
        selectedSearchSuggestionId.setValue(id);
    }

    final Observer<Earthquake> selectedSearchSuggestionObserver = selectedSearchSuggestion -> {
        if (selectedSearchSuggestion != null) {
            setSearchQuery(selectedSearchSuggestion.getDetails());
        }
    };

    private final Observer<List<Earthquake>> searchQueryResultObserver = updatedEarthquakes -> {
        mEarthquakes.clear();
        if (updatedEarthquakes != null)
            mEarthquakes.addAll(updatedEarthquakes);
        mEarthquakeAdapter.notifyDataSetChanged();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_search_result);

        RecyclerView recyclerView = findViewById(R.id.search_result_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mEarthquakeAdapter);

        searchQuery = new MutableLiveData<>();
        searchQuery.setValue(null);

        searchResults = Transformations.switchMap(searchQuery,
                query -> EarthquakeDatabaseAccessor
                            .getInstance(getApplicationContext())
                            .earthquakeDAO()
                            .searchEarthquakes("%" + query + "%"));

        searchResults.observe(EarthquakeSearchResultActivity.this, searchQueryResultObserver);

        selectedSearchSuggestionId = new MutableLiveData<>();
        selectedSearchSuggestionId.setValue(null);

        selectedSearchSuggestion = Transformations.switchMap(selectedSearchSuggestionId,
                id -> EarthquakeDatabaseAccessor
                        .getInstance(getApplicationContext())
                        .earthquakeDAO()
                        .getEarthquake(id));

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            selectedSearchSuggestion.observe(this, selectedSearchSuggestionObserver);
        } else {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            setSearchQuery(query);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            setSelectedSearchSuggestion(getIntent().getData());
        } else {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            setSearchQuery(query);
        }
    }

}
