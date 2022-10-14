import java.util.ArrayList;
import javax.xml.bind.annotation.*;

/*
 * RecipeContainer.java
 *
 * This class is used to contain the list of recipes.  This is only used for
 * XML marshalling!
 */
@XmlRootElement(name="Recipes")
public class RecipeContainer
{
    // The recipes we are containing
    private ArrayList<Recipe> recipes;

    // Need no-arg constructor for XML
    public RecipeContainer() {}

    /*
     * Construct recipe container
     *
     * @param recipes  List of recipes
     */
    public RecipeContainer(ArrayList<Recipe> recipes)
    {
        this.recipes = recipes;
    }

    // Getters and setters

    // Title
    @XmlElement(name="Recipe")
    public void setRecipes(ArrayList<Recipe> recipes)
    {
        this.recipes = recipes;
    }
    public ArrayList<Recipe> getRecipes()
    {
        return recipes;
    }

    // Get size of the recipe list (for convenience)
    public int getSize()
    {
        return recipes.size();
    }

	// Whether the container contains recipes
	public boolean hasRecipes()
	{
		return recipes != null;
	}
}
