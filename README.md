# Android-App
Weather Forecast App


# Description:

1. Use the RecyclerView framework to display the entire array of forecast data as a scrollable vertical list of cards.
2. Implement a subclass of AsyncTask to fetch forecast data from the OpenWeather API and to display that weather data in the RecyclerView.
3. Use GSON to parse the JSON com.example.android.connectedweather.data returned by the OpenWeather API.
4. Implement a Repository class to perform the data operations associated with communicating with the OpenWeather API.
5. Implement a ViewModel class to serve as the intermediary between the Repository class and the UI. 
6. Create a new activity named SettingsActivity that implements a user preferences screen using a PreferenceFragment.
7. Add a navigation drawer which be accessible via a "hamburger" icon in the navigation bar in the app's main activity.
8. Incorporate an SQLite database to save forecast locations.
