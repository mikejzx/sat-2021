import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.bind.annotation.*;

/*
 * Recipe.java
 *
 * This class represents a single recipe object.
 */
@XmlRootElement
public class Recipe
{
    // The name of the recipe
    private String title = "";

    // Recipe description
    private String description = "";

    // List of ingredients in the recipe.
    private ArrayList<String> ingredients = new ArrayList<String>();

    // List of steps in preparation procedure of the recipe
    private ArrayList<String> procedure = new ArrayList<String>();

    // List of categorisation tags attached to this recipe
    // Note: we use an arraylist internally here as it's the most convenient
    // and has the helper methods we need, but ideally this should be a HashSet
    // to avoid duplicate tags.  For now we only allow constructing with a
    // HashSet, so the only way to create duplicates is for the user to
    // manually edit the recipes file and add them, which they are very
    // unlikely to do.
    private ArrayList<String> tags = new ArrayList<String>();

    // Need no-arg constructor for XML marshalling
    public Recipe() {}

    /*
     * Construct a new recipe
     *
     * @param title        Recipe title
     * @param desc         Recipe description
     * @param ingredients  Recipe's list of ingredients
     * @param procedure    Recipe procedure
     * @param tags         Recipe tags for categorisation
     */
    public Recipe(String title, String desc, ArrayList<String> ingredients,
        ArrayList<String> procedure, HashSet<String> tags)
    {
        this.title = title;
        this.description = desc;
        this.ingredients = ingredients;
        this.procedure = procedure;

        this.tags = new ArrayList<String>(tags.size());
        for (String tag : tags)
        {
            this.tags.add(tag);
        }

        // Set all tags to lowercase
        lowercaseAllTags();
    }

    /*
     * Sets all tags to be lowercase
     */
    private void lowercaseAllTags()
    {
        for (int i = 0; i < tags.size(); ++i)
        {
            tags.set(i, tags.get(i).toLowerCase());
        }
    }

    /*
     * Construct a new recipe (no tags)
     *
     * @param title        Recipe title
     * @param desc         Recipe description
     * @param ingredients  Recipe's list of ingredients
     * @param procedure    Recipe procedure
     */
    public Recipe(String title, String desc, ArrayList<String> ingredients,
        ArrayList<String> procedure)
    {
        this(title, desc, ingredients, procedure, new HashSet<String>());
    }

    // Getters and setters

    // Title
    @XmlElement
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getTitle()
    {
        return title;
    }

    // Description
    @XmlElement
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getDescription()
    {
        return description;
    }

    // Ingredients
    @XmlElementWrapper(name="Ingredients")
    @XmlElement(name="Ingredient")
    public void setIngredients(ArrayList<String> ingredients)
    {
        this.ingredients = ingredients;
    }
    public ArrayList<String> getIngredients()
    {
        return ingredients;
    }

    // Procedure
    @XmlElementWrapper(name="Procedure")
    @XmlElement(name="Step")
    public void setProcedure(ArrayList<String> procedure)
    {
        this.procedure = procedure;
    }
    public ArrayList<String> getProcedure()
    {
        return procedure;
    }

    // Tags
    @XmlElementWrapper(name="Tags")
    @XmlElement(name="Tag")
    public void setTags(ArrayList<String> tags)
    {
        this.tags = tags;
        lowercaseAllTags();
    }
    public ArrayList<String> getTags()
    {
        return tags;
    }

    // Convenience getters

    // @return number of ingredeints
    public int getIngredientsCount()
    {
        return ingredients.size();
    }

    // @return number of steps
    public int getProcedureSize()
    {
        return procedure.size();
    }

    // @return procedure step at index
    public String getProcedureItem(int idx)
    {
        return procedure.get(idx);
    }

    // @return whether recipe has tags
    public boolean hasTags()
    {
        return tags.size() > 0;
    }
}
