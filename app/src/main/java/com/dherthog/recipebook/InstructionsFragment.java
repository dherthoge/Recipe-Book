package com.dherthog.recipebook;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

/**
 * A Fragment to display Recipe instructions.
 */
public class InstructionsFragment extends Fragment {

    private final FavoriteCommunicator favoriteCommunicator;
    private final Instructions instructions;
    private boolean isFavorite; // If the instructions are for a favorite Recipe
    private ImageButton ibFavorite;

    /**
     * Constructor for passing an ActivityCommunicator, the application context, and recipes to
     * display.
     * @param instructions A Instructions to display in the fragment
     * @param isFavorite True if the instructions are for a favorite Recipe
     * @param favoriteCommunicator The observer for Recipe favoriting
     */
    public InstructionsFragment(Instructions instructions, boolean isFavorite, FavoriteCommunicator favoriteCommunicator) {
        this.favoriteCommunicator = favoriteCommunicator;
        this.instructions = instructions;
        this.isFavorite = isFavorite;
    }

    /**
     * Called to have the fragment instantiate its user interface view. Instantiates root View and
     * all information in the Fragment's Instructions.
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
        // The root View of the Fragment
        View view = inflater.inflate(R.layout.fragment_instructions, parent, false);

        // Get references for necessary Views
        TextView tvName = view.findViewById(R.id.tv_recipe_name);
        TextView tvCategory = view.findViewById(R.id.tv_recipe_category);
        TextView tvArea = view.findViewById(R.id.tv_recipe_area);
        TextView tvInstructions = view.findViewById(R.id.tv_recipe_instructions);
        ImageView ivImage = view.findViewById(R.id.iv_recipe_image);
        ibFavorite = view.findViewById(R.id.ib_favorite);

        // Set the favorite image
        setFavoriteButtonImage();

        // Populate the instructions
        tvName.setText(instructions.getName());
        tvCategory.setText(instructions.getCategory());
        tvArea.setText(instructions.getArea());
        tvInstructions.setText(instructions.getInstructions());
        tvInstructions.setMovementMethod(new ScrollingMovementMethod());

        // Sets listener for user highlighting the Recipe
        ibFavorite.setOnClickListener(v -> {
            favoriteCommunicator.recipeFavorited(instructions);
            isFavorite = !isFavorite;
            setFavoriteButtonImage();
        });

        // Load image for the intructions
        Picasso.with(requireContext()).load(instructions.getImageUrl()).fit().into(ivImage);

        return view;
    }

    /**
     * Determines which image to display for the favorite ImageButton.
     */
    private void setFavoriteButtonImage() {
        if (isFavorite)
            ibFavorite.setImageResource(R.drawable.star_on);
        else
            ibFavorite.setImageResource(R.drawable.star_off);
    }
}
