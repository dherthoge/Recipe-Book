package com.dherthog.recipebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Controller of the application. Manages the database, ads, fragments, navigation drawer, and
 * api calls.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecipeSelectedCommunicator, CategorySelectedCommunicator, FavoriteCommunicator {

    // Used for Navigation Drawer
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    // Stores categories since they may changed by The Meal DB
    private ArrayList<Pair<String, String>> categories;

    // Stores RecipeDao for queries
    private RecipeDao recipeDao;

    // Stores the next ad to display
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;

    /**
     * Initializes the Activity, MobileAds, the DB, navigation drawer, CategoriesFragment, and loads
     * interstitial and rewarded ads.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                          down then this Bundle contains the data it most recently supplied in
     *                          onSaveInstanceState(Bundle). Note: Otherwise it is null. This value
     *                           may be null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);

        RecipeDatabase database = Room.databaseBuilder(this, RecipeDatabase.class, "recipeDB")
                .allowMainThreadQueries()
                .build();
        recipeDao = database.getRecipeDAO();

        initializeNavigationDrawer();
        setCategoriesFragment();
        loadInterstitialAd();
        loadRewardedAd();
    }

    /**
     * Sets toggle for Navigation Drawer open/close, attaches listener for navigation item
     * selection.
     */
    private void initializeNavigationDrawer() {
        // Initialize toggles for the nav drawer.
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Set click listener for menu items
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Makes the nav drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Listener for Navigation Drawer open/close.
     * @param item The selected menu item
     * @return True if the selection was consumed, false if not
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens the selected option's corresponding Fragment.
     * @param item The selected menu item
     * @return True if the selection was consumed, false if not
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        if (item.getItemId() == R.id.nav_categories)
            setCategoriesFragment();

        if (item.getItemId() == R.id.nav_favorites)
            setFavoritesFragment();

        return true;
    }

    /**
     * Displays recipe categories from TMDB. Makes a network call to get recipe categories if they
     * have not already been downloaded.
     */
    private void setCategoriesFragment() {

        // Categories have already been downloaded
        if (categories != null)
            displayCategories();

        // Make a network to get the categories
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Create a new task in a separate thread! (not to get UI thread stuck!)
            PrepareCategoriesTask prepareCategoriesTask = new PrepareCategoriesTask();
            prepareCategoriesTask.execute("https://www.themealdb.com/api/json/v1/1/categories.php");
        } else {
            Toast.makeText(this, "No network connection available!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays recipes from the given categoryName. Makes a network call to get Recipe list.
     * @param categoryName The name of the Recipe category to fetch instructions for
     */
    private void setRecipesFragment(String categoryName) {

        // Make a network to get the recipies
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Create a new task in a separate thread! (not to get UI thread stuck!)
            PrepareRecipiesTask prepareRecipiesTask = new PrepareRecipiesTask();
            prepareRecipiesTask.execute("https://www.themealdb.com/api/json/v1/1/filter.php?c=" + categoryName);
        } else {
            Toast.makeText(this, "No network connection available!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays recipes from the given Recipe id. Makes a network call to get recipe list.
     * @param id The id of the Recipe to fetch instructions for
     */
    private void setInstructionsFragment(int id) {

        // Make a network to get the recipies
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Create a new task in a separate thread! (not to get UI thread stuck!)
            PrepareInstructionsTask prepareInstructionsTask = new PrepareInstructionsTask();
            prepareInstructionsTask.execute("https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + id);
        } else {
            Toast.makeText(this, "No network connection available!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays user's favorited Recipes from the DB.
     */
    private void setFavoritesFragment() {

        // Get the Recipes from the DB
        ArrayList<Recipe> favoriteRecipes = (ArrayList<Recipe>) recipeDao.getAllRecipes();

        // Display the Recipes
        displayFavorites(favoriteRecipes);
    }

    /**
     * Creates a CategoriesFragment to display downloaded categories and loads it into a
     * placeholder.
     */
    private void displayCategories() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        CategoriesFragment cf = new CategoriesFragment(this, categories);
        ft.replace(R.id.fragmentPlaceholder, cf)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates a RecipesFragment to display downloaded Recipes and loads it into a
     * placeholder.
     * @param recipes The ArrayList<Recipe> of Recipes to display
     */
    private void displayRecipes(ArrayList<Recipe> recipes) {
        // Shows an interstitial add before displaying the recipes
        showInterstitialAd();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        RecipesFragment rf = new RecipesFragment(this, recipes);
        ft.replace(R.id.fragmentPlaceholder, rf)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates an InstructionsFragment to display downloaded Instructions and loads it into a
     * placeholder.
     * @param instructions The Instructions to display
     */
    private void displayInstructions(Instructions instructions) {
        // Determine if the Recipe is a favorite
        boolean isFavorite = false;
        if (recipeDao.getRecipeByID(instructions.getId()).size() > 0)
            isFavorite = true;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        InstructionsFragment instructionsFragment = new InstructionsFragment(instructions, isFavorite, this);
        ft.replace(R.id.fragmentPlaceholder, instructionsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Creates an FavoritesFragment to display favorite Recipes from the DB and loads it into a
     * placeholder.
     * @param favoriteRecipes The Recipes to display
     */
    private void displayFavorites(ArrayList<Recipe> favoriteRecipes) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FavoritesFragment ff = new FavoritesFragment(this, favoriteRecipes);
        ft.replace(R.id.fragmentPlaceholder, ff)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Loads a new interstitial ad.
     */
    private void loadInterstitialAd() {
        // ca-app-pub-... is the test ad id for interstitial ads
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    /**
     * Loads a new rewarded ad.
     */
    private void loadRewardedAd() {
        // ca-app-pub-... is the test ad id for interstitial ads
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mRewardedAd = null;
                    }
                });
    }

    /**
     * Displays the current interstitial ad.
     */
    private void showInterstitialAd() {
        // If the ad is not loaded, cannot show the ad
        if (mInterstitialAd == null)
            return;

        // Load a new ad after the current on has played
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                // Load a new ad to display next
                loadInterstitialAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content failed to show.
                // Load a new ad to display next
                loadInterstitialAd();
            }

            @Override
            public void onAdShowedFullScreenContent() { }
        });
        mInterstitialAd.show(this);
    }

    /**
     * Displays the current rewarded ad.
     */
    private void showRewardedAd(Instructions instructions) {
        // If the ad is not loaded, cannot show the ad
        if (mRewardedAd == null) {
            displayInstructions(instructions);
            return;
        }

        // Load a new ad after the current on has played
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                // Load a new ad to display next
                loadRewardedAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content is dismissed.
                // Load a new ad to display next
                loadRewardedAd();
            }

            @Override
            public void onAdDismissedFullScreenContent() { }
        });

        // Show the ad
        Activity activityContext = MainActivity.this;
        mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                // Display the selected instructions if the user watched the ad to completion
                displayInstructions(instructions);
            }
        });
    }

    /**
     * Starts the flow of displaying Recipes from the selected category. (Download recipes, display
     * interstitial ad, create and attach fragment)
     * @param title The title of the selected category.
     */
    @Override
    public void categorySelected(String title) {
        setRecipesFragment(title);
    }

    /**
     * Starts the flow of displaying Instructions from the selected Recipe. (Download instructions,
     * display rewareded ad, create and attach fragment)
     * @param id The id of the selected Recipe.
     */
    @Override
    public void recipeSelected(int id) {
        setInstructionsFragment(id);
    }

    /**
     * Inserts/deletes the selected Recipe from the DB
     * @param recipe The Recipe
     */
    @Override
    public void recipeFavorited(Recipe recipe) {
        // Delete the corresponding Recipe if it is stored in the DB
        if (recipeDao.getRecipeByID(recipe.getId()).size() > 0) {
            recipeDao.delete(recipe);
            return;
        }

        // Insert the Recipe into the DB
        recipeDao.insert(recipe);
    }

    /**
     * @param categories An ArrayList of categories
     */
    private void setCategories(ArrayList<Pair<String, String>> categories) {
        this.categories = categories;
    }

    /**
     * Inner class used to download category data from TheMealDB.
     */
    private class PrepareCategoriesTask extends AsyncTask<String, Void, String> {

        // The downloaded list of categories
        ArrayList<Pair<String, String>> categories;

        /**
         * Downloads and parses category data.
         * @param urls A single URL to get category data from
         * @return An empty string
         */
        @Override
        protected String doInBackground(String... urls) {
            String categoryData = downloadCategoryData(urls[0]);
            categories = parseCategoryData(categoryData);

            return "";
        }

        /**
         * Calls TMDB api to get category data.
         * @return The returned JSONObject
         */
        private String downloadCategoryData(String url) {
            InputStream is;
            StringBuilder result  = new StringBuilder();
            URL myUrl;
            try {
                myUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();

                // If the connection was bad, do not read data
                if (responseCode != HttpURLConnection.HTTP_OK){
                    throw new IOException("Connection not successful!");
                }
                is = connection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        /**
         * Creates an ArrayList<Pair<String, String>> to store category titles and their
         * corresponding images.
         * @param categoryData The JSONObject to parse
         * @return A HashMap of Integer-String pairs for genre mappings
         */
        private ArrayList<Pair<String, String>> parseCategoryData(String categoryData) {
            ArrayList<Pair<String, String>> result = new ArrayList<>();
            try{
                JSONArray jsonGenresArray = new JSONObject(categoryData).getJSONArray("categories");

                for (int i = 0; i < jsonGenresArray.length(); i++) {
                    JSONObject curObj = jsonGenresArray.getJSONObject(i);
                    result.add(new Pair<>(curObj.getString("strCategory"), curObj.getString("strCategoryThumb")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * Triggers UI to display the returned categories and sets the categories of MainActivity.
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            setCategories(categories);
            displayCategories();
        }
    }

    /**
     * Inner class used to download recipe data from TheMealDB.
     */
    private class PrepareRecipiesTask extends AsyncTask<String, Void, String> {

        // The downloaded list of Recipes
        ArrayList<Recipe> recipes;

        /**
         * Downloads and parses Recipe data.
         * @param urls A single URL to get Recipe data from
         * @return An empty string
         */
        @Override
        protected String doInBackground(String... urls) {
            String recipeData = downloadRecipeData(urls[0]);
            recipes = parseRecipeData(recipeData);

            return "";
        }

        /**
         * Calls TMDB api to get recipe data.
         * @return The returned JSONObject
         */
        private String downloadRecipeData(String url) {
            InputStream is;
            StringBuilder result  = new StringBuilder();
            URL myUrl;
            try {
                myUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();

                // If the connection was bad, do not read data
                if (responseCode != HttpURLConnection.HTTP_OK){
                    throw new IOException("Connection not successful!");
                }
                is = connection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        /**
         * Creates an ArrayList<Pair<Recipe> to store Recipe data.
         * @param recipeData The JSONObject to parse
         * @return A ArrayList of Recipes
         */
        private ArrayList<Recipe> parseRecipeData(String recipeData) {
            ArrayList<Recipe> result = new ArrayList<>();
            try{
                JSONArray jsonGenresArray = new JSONObject(recipeData).getJSONArray("meals");

                for (int i = 0; i < jsonGenresArray.length(); i++) {
                    JSONObject curObj = jsonGenresArray.getJSONObject(i);
                    result.add(new Recipe(Integer.parseInt(curObj.getString("idMeal")), curObj.getString("strMeal"), curObj.getString("strMealThumb")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * Triggers UI to display the returned Recipes
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            displayRecipes(recipes);
        }
    }

    /**
     * Inner class used to download instructions from TheMealDB for the chosen Recipe.
     */
    private class PrepareInstructionsTask extends AsyncTask<String, Void, String> {

        // The returned instructions
        Instructions instructions;

        /**
         * Downloads and parses instructions.
         * @param urls A single URL to get instructions data from
         * @return An empty string
         */
        @Override
        protected String doInBackground(String... urls) {
            String instructionsData = downloadInstructionsData(urls[0]);
            instructions = parseRecipeData(instructionsData);

            return "";
        }

        /**
         * Calls TMDB api to get instructions
         * @return The returned JSONObject
         */
        private String downloadInstructionsData(String url) {
            InputStream is;
            StringBuilder result  = new StringBuilder();
            URL myUrl;
            try {
                myUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();

                // If the connection was bad, do not read data
                if (responseCode != HttpURLConnection.HTTP_OK){
                    throw new IOException("Connection not successful!");
                }
                is = connection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        /**
         * Creates an Instructions object to store the returned instructions.
         * @param categoryData The JSONObject to parse
         * @return The parsed instructions
         */
        private Instructions parseRecipeData(String categoryData) {
            Instructions instructions = null;
            try {
                JSONArray jsonGenresArray = new JSONObject(categoryData).getJSONArray("meals");
                JSONObject instructionsObj = jsonGenresArray.getJSONObject(0);
                instructions = new Instructions(Integer.parseInt(instructionsObj.getString("idMeal")),
                        instructionsObj.getString("strMeal"),
                        instructionsObj.getString("strCategory"),
                        instructionsObj.getString("strArea"),
                        instructionsObj.getString("strMealThumb"),
                        instructionsObj.getString("strInstructions"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return instructions;
        }

        /**
         * Triggers UI to display the returned Instructions after a rewarded ad.
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            showRewardedAd(instructions);
        }
    }
}