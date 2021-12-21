package com.dherthog.recipebook;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A Fragment to display meal Categories.
 */
public class CategoriesFragment extends Fragment {

    private final CategorySelectedCommunicator categorySelectedCommunicator;
    private final ArrayList<Pair<String, String>> categories;
    private View view; // The root View of the Fragment

    /**
     * Constructor for passing an ActivityCommunicator, the application context, and categories to
     * display.
     * @param categorySelectedCommunicator The observer of ListView clicks.
     * @param categories An ArrayList of category-image URL pairs
     */
    public CategoriesFragment(CategorySelectedCommunicator categorySelectedCommunicator, ArrayList<Pair<String, String>> categories) {
        this.categorySelectedCommunicator = categorySelectedCommunicator;
        this.categories = categories;
    }

    /**
     * Called to have the fragment instantiate its root View and display a banner ad and ListView
     * for categories.
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
        view = inflater.inflate(R.layout.fragment_categories, parent, false);

        displayBannerAd();
        displayListView();

        return view;
    }

    /**
     * Loads an ad into the Fragment's adView.
     */
    private void displayBannerAd() {
        AdView adView = view.findViewById(R.id.av_categories);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
    }

    /**
     * Instantiates the Fragment's ListView to display category data and sets an
     * OnItemClickedListener for the items in the list.
     */
    private void displayListView() {
        ListAdapter adapter = new ListAdapter();
        ListView listView = (ListView) view.findViewById(R.id.lv_categories);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categorySelectedCommunicator.categorySelected(categories.get(position).first);
            }
        });
    }

    /**
     * An ArrayAdapter class to instantiate each Recipe's View.
     */
    private class ListAdapter extends ArrayAdapter<Pair<String, String>> {

        /**
         * Create a new ListViewAdapter.
         */
        public ListAdapter() {
            super(view.getContext(), R.layout.list_item);
        }

        /**
         * Instantiates a View for a single item in the list of categories.
         * @param position The position the the item in the list.
         * @param convertView This value may be null
         * @param parent The parent of the newly instantiated View
         * @return The newly instantiated View
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Pair<String, String> category = categories.get(position);

            // Inflates the View if it hasn't been already
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

            // Get references for Views
            TextView tvCategoryTitle = convertView.findViewById(R.id.tv_item_title);
            ImageView ivCategoryImage = convertView.findViewById(R.id.iv_item);

            // Set the title of the category
            tvCategoryTitle.setText(category.first);
            // Set the image of the category
            Picasso.with(getContext()).load(category.second).resize(256, 160).into(ivCategoryImage);

            return convertView;
        }

        /**
         * @return The number of elements in the list.
         */
        @Override
        public int getCount() {
            return categories.size();
        }
    }
}
