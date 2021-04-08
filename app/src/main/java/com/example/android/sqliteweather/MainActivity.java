package com.example.android.sqliteweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sqliteweather.data.AppDatabase;
import com.example.android.sqliteweather.data.FiveDayForecast;
import com.example.android.sqliteweather.data.ForecastCity;
import com.example.android.sqliteweather.data.ForecastData;
import com.example.android.sqliteweather.data.LoadingStatus;
import com.example.android.sqliteweather.data.Location;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ForecastAdapter.OnForecastItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener,
        LocationAdapter.OnClickLocationItemListener {
    private static final String TAG = MainActivity.class.getSimpleName();

  
    private static final String OPENWEATHER_APPID = "9e3b45989b7e3ea079d8b68e9d7182c6";

    private ForecastAdapter forecastAdapter;
    private FiveDayForecastViewModel fiveDayForecastViewModel;

    private SharedPreferences sharedPreferences;

    private ForecastCity forecastCity;

    private RecyclerView forecastListRV;
    private ProgressBar loadingIndicatorPB;
    private TextView errorMessageTV;

    private Toast errorToast;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.loadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        this.errorMessageTV = findViewById(R.id.tv_error_message);
        this.forecastListRV = findViewById(R.id.rv_forecast_list);
        this.forecastListRV.setLayoutManager(new LinearLayoutManager(this));
        this.forecastListRV.setHasFixedSize(true);

        this.forecastAdapter = new ForecastAdapter(this);
        this.forecastListRV.setAdapter(this.forecastAdapter);

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        this.fiveDayForecastViewModel = new ViewModelProvider(this)
                .get(FiveDayForecastViewModel.class);
        this.loadForecast();

        /*
         * Update UI to reflect newly fetched forecast data.
         */
        this.fiveDayForecastViewModel.getFiveDayForecast().observe(
                this,
                new Observer<FiveDayForecast>() {
                    @Override
                    public void onChanged(FiveDayForecast fiveDayForecast) {
                        forecastAdapter.updateForecastData(fiveDayForecast);
                        if (fiveDayForecast != null) {
                            forecastCity = fiveDayForecast.getForecastCity();
                            ActionBar actionBar = getSupportActionBar();
                            actionBar.setTitle(forecastCity.getName());
                        }
                    }
                }
        );

        /*
         * Update UI to reflect changes in loading status.
         */
        this.fiveDayForecastViewModel.getLoadingStatus().observe(
                this,
                new Observer<LoadingStatus>() {
                    @Override
                    public void onChanged(LoadingStatus loadingStatus) {
                        if (loadingStatus == LoadingStatus.LOADING) {
                            loadingIndicatorPB.setVisibility(View.VISIBLE);
                        } else if (loadingStatus == LoadingStatus.SUCCESS) {
                            loadingIndicatorPB.setVisibility(View.INVISIBLE);
                            forecastListRV.setVisibility(View.VISIBLE);
                            errorMessageTV.setVisibility(View.INVISIBLE);
                        } else {
                            loadingIndicatorPB.setVisibility(View.INVISIBLE);
                            forecastListRV.setVisibility(View.INVISIBLE);
                            errorMessageTV.setVisibility(View.VISIBLE);
                            errorMessageTV.setText(getString(R.string.loading_error, "ヽ(。_°)ノ"));
                        }
                    }
                }
        );
        // render the adapter
        LocationAdapter locationAdapter = new LocationAdapter(this);
        RecyclerView rvLocation = findViewById(R.id.rv_location);
        rvLocation.setLayoutManager(new LinearLayoutManager(this));
        rvLocation.setAdapter(locationAdapter);
        AppDatabase.getDb(this).forecastDao().getAll().observe(
                this,
                new Observer<List<Location>>() {
                    @Override
                    public void onChanged(List<Location> locations) {
                        locationAdapter.setData(locations);
                    }
                }
        );
        setUpDrawer();
    }

    private void setUpDrawer() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);

        drawerToggle.syncState();
    }

    @Override
    public void onForecastItemClick(ForecastData forecastData) {
        Intent intent = new Intent(this, ForecastDetailActivity.class);
        intent.putExtra(ForecastDetailActivity.EXTRA_FORECAST_DATA, forecastData);
        intent.putExtra(ForecastDetailActivity.EXTRA_FORECAST_CITY, this.forecastCity);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_map:
                viewForecastCityInMap();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        this.loadForecast();
    }

    private void loadForecast() {
        String queryLocation = this.sharedPreferences.getString(
                getString(R.string.pref_location_key),
                "Corvallis,OR,US"
        );
        Location location = new Location(queryLocation, System.currentTimeMillis());
        AppDatabase.getDb(this).forecastDao().insert(location);

        this.fiveDayForecastViewModel.loadForecast(
                queryLocation,
                this.sharedPreferences.getString(
                        getString(R.string.pref_units_key),
                        getString(R.string.pref_units_default_value)
                ),
                OPENWEATHER_APPID
        );
    }

    private void viewForecastCityInMap() {
        if (this.forecastCity != null) {
            Uri forecastCityGeoUri = Uri.parse(getString(
                    R.string.geo_uri,
                    this.forecastCity.getLatitude(),
                    this.forecastCity.getLongitude(),
                    12
            ));
            Intent intent = new Intent(Intent.ACTION_VIEW, forecastCityGeoUri);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                if (this.errorToast != null) {
                    this.errorToast.cancel();
                }
                this.errorToast = Toast.makeText(
                        this,
                        getString(R.string.action_map_error),
                        Toast.LENGTH_LONG
                );
                this.errorToast.show();
            }
        }
    }

    @Override
    public void onClickLocationItem(Location location) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(getString(R.string.pref_location_key), location.location);
        editor.commit();
        drawerLayout.closeDrawer(Gravity.LEFT);
    }
}
