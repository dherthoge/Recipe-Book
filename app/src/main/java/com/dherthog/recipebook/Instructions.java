package com.dherthog.recipebook;

/**
 * An Object to store instructions for a Recipe
 */
public class Instructions extends Recipe {

    // The id of the Instructions
    private final int id;
    // imageUrl: the url for the Recipe's image
    private final String name, category, area, imageUrl, instructions;

    /**
     * @param id The ID of the Recipe in the TMDB
     * @param name The name of the Recipe in the TMDB
     * @param category The category of the Recipe in the TMDB
     * @param area The area of the Recipe in the TMDB
     * @param imageUrl The imageUrl of the Recipe in the TMDB
     * @param instructions The instructions of the Recipe in the TMDB
     */
    public Instructions(int id, String name, String category, String area, String imageUrl, String instructions) {
        super(id, name, imageUrl);
        this.id = id;
        this.name = name;
        this.category = category;
        this.area = area;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getArea() {
        return area;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getInstructions() {
        return instructions;
    }
}
