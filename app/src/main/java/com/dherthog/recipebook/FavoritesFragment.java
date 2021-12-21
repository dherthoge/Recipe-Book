package com.dherthog.recipebook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A Fragment to display favorited meal Recipes.
 */
public class FavoritesFragment extends Fragment {

    private final RecipeSelectedCommunicator recipeSelectedCommunicator;
    private final ArrayList<Recipe> recipes;
    private View view; // The root View of the Fragment

    /**
     * Constructor for passing a RecipeSelectedCommunicator, the application context, and Recipes to
     * display.
     * @param recipeSelectedCommunicator The observer of ListView clicks.
     * @param recipes An ArrayList of Recipes
     */
    public FavoritesFragment(RecipeSelectedCommunicator recipeSelectedCommunicator, ArrayList<Recipe> recipes) {
        this.recipeSelectedCommunicator = recipeSelectedCommunicator;
        this.recipes = recipes;
    }

    /**
     * Called to have the fragment instantiate its root View and ListView to display Recipes.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the
     *                 fragment
     * @param parent If non-null, this is the parent view that the fragment's UI should be attached
     *               to. The fragment should not add the view itself, but this can be used to
     *               generate the LayoutParams of the view. This value may be null.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return Return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        view = inflater.inflate(R.layout.fragment_recipes, parent, false);

        // Displays the favorited Recipes
        displayListView();

        return view;
    }

    /**
     * Instantiates the Fragment's ListView to display Recipe and sets an OnItemClickedListener for
     * the items in the list.
     */
    private void displayListView() {
        ListAdapter adapter = new ListAdapter();
        ListView listView = (ListView) view.findViewById(R.id.lv_recipes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                recipeSelectedCommunicator.recipeSelected(recipes.get(position).getId());
            }
        });
    }

    /**
     * An ArrayAdapter class to instantiate each item's View.
     */
    private class ListAdapter extends ArrayAdapter<Recipe> {

        /**
         * Create a new ListViewAdapter.
         */
        public ListAdapter() {
            super(view.getContext(), R.layout.list_item);
        }

        /**
         * Instantiates a View for a single item in the list of Recipes.
         * @param position The position the the item in the list.
         * @param convertView This value may be null
         * @param parent The parent of the newly instantiated View
         * @return The newly instantiated View
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Recipe recipe = recipes.get(position);

            // Check if the View is being reused
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            TextView tvRecipieName = convertView.findViewById(R.id.tv_item_title);
            ImageView ivRecipeImage = convertView.findViewById(R.id.iv_item);

            // Set the name
            tvRecipieName.setText(recipe.getName());
            // Set the image
            Picasso.with(getContext()).load(recipe.getImageUrl()).fit().into(ivRecipeImage);

            return convertView;
        }

        /**
         * @return The number of elements in the list.
         */
        @Override
        public int getCount() {
            return recipes.size();
        }
    }
}
