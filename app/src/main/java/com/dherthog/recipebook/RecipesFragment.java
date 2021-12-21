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
 * A Fragment to display Recipes.
 */
public class RecipesFragment extends Fragment {

    private final RecipeSelectedCommunicator recipeSelectedCommunicator;
    private final ArrayList<Recipe> recipes;
    private View rootView; // The root View of the Fragment

    /**
     * Called to have the fragment instantiate it's View.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the
     *                 fragment
     * @param parent If non-null, this is the parent view that the fragment's UI should be attached
     *               to. The fragment should not add the view itself, but this can be used to
     *               generate the LayoutParams of the view. This value may be null.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here
     * @return Return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        rootView = inflater.inflate(R.layout.fragment_recipes, parent, false);
        return rootView;
    }

    /**
     * Instantiates the ListView to display Recipes.
     * @param view The root View of the fragment
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListView();
    }

    /**
     * Constructor for passing a RecipeSelectedCommunicator and Recipes to display.
     * @param recipeSelectedCommunicator The observer of ListView clicks.
     * @param recipes An ArrayList of category-image URL pairs
     */
    public RecipesFragment(RecipeSelectedCommunicator recipeSelectedCommunicator, ArrayList<Recipe> recipes) {
        this.recipeSelectedCommunicator = recipeSelectedCommunicator;
        this.recipes = recipes;
    }

    /**
     * Instantiates the Fragment's ListView to display category data and sets an
     * OnItemClickedListener for the items in the list.
     */
    private void setListView() {
        ListAdapter adapter = new ListAdapter();
        ListView listView = (ListView) rootView.findViewById(R.id.lv_recipes);
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
            super(rootView.getContext(), R.layout.list_item);
        }

        /**
         * Instantiates a View for a single item in the list of recipes.
         * @param position The position the the item in the list.
         * @param convertView This value may be null
         * @param parent The parent of the newly instantiated View
         * @return The newly instantiated View
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the Recipe to populate the View with
            Recipe recipe = recipes.get(position);

            // Inflate the convertView if it's not being reused
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

            // Get references for the necessary views
            TextView tvRecipeName = convertView.findViewById(R.id.tv_item_title);
            ImageView ivRecipeImage = convertView.findViewById(R.id.iv_item);

            // Set the name and image of the Recipe
            tvRecipeName.setText(recipe.getName());
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
