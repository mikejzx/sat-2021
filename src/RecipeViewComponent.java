import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class RecipeViewComponent extends JPanel
{
    // Current recipe being viewed
    public Recipe currentRecipe = null;

    // Main document that is displayed in the viewer, where we write our text.
    private final DefaultStyledDocument doc;

    // The text pane containing our document
    private final JTextPane pane;

    // Styles for the document display
    private final Style mainStyle, headingStyle, subheadingStyle, tagsStyle;

    private static final String INGREDIENT_PREFIX = "\u2022 ";

    /*
     * Initialises a new RecipeViewComponent
     */
    public RecipeViewComponent()
    {
        // Use border layout so we fill whole panel
        setLayout(new BorderLayout());

        // Prepare StyleContext, and the document to display
        StyleContext ctx = new StyleContext();
        doc = new DefaultStyledDocument(ctx);

        // Set up the text pane and add it to the component
        pane = new JTextPane(doc);
        pane.setEditable(false);
        add(pane);

        // Set up main document style (used for paragraph text)
        Style defaultStyle = ctx.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = ctx.addStyle("MainStyle", defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "serif");
        StyleConstants.setFontSize(mainStyle, 18);
        StyleConstants.setAlignment(mainStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setLeftIndent(mainStyle, 32);
        StyleConstants.setRightIndent(mainStyle, 32);
        StyleConstants.setLineSpacing(mainStyle, 0.2f);
        doc.setLogicalStyle(0, mainStyle);

        // Set up heading document style
        headingStyle = ctx.addStyle("HeadingStyle", null);
        StyleConstants.setForeground(headingStyle, Color.red);
        StyleConstants.setFontFamily(headingStyle, "serif");
        StyleConstants.setFontSize(headingStyle, 32);
        StyleConstants.setBold(headingStyle, true);
        StyleConstants.setItalic(headingStyle, true);
        StyleConstants.setAlignment(headingStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(headingStyle, 0.1f);

        // Set up heading document style
        subheadingStyle = ctx.addStyle("SubheadingStyle", null);
        StyleConstants.setForeground(subheadingStyle, Color.blue);
        StyleConstants.setFontFamily(subheadingStyle, "serif");
        StyleConstants.setFontSize(subheadingStyle, 26);
        StyleConstants.setAlignment(subheadingStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(subheadingStyle, 0.1f);

        // Set up tags section document style
        tagsStyle = ctx.addStyle("TagsStyle", null);
        StyleConstants.setForeground(tagsStyle, Color.gray);
        StyleConstants.setFontFamily(tagsStyle, "serif");
        StyleConstants.setFontSize(tagsStyle, 14);
        StyleConstants.setAlignment(tagsStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(tagsStyle, 0.1f);

        // Show welcome message
        appendLine("Recipe Management Software\n\nClick \"New\" to get started.", mainStyle);

        // No recipe is shown by default, so we disable some of the toolbar
        // buttons which are not functional unless a recipe is shown.
        SAT.onRecipeViewChanged(false);
    }

    /*
     * Show a recipe in the recipe viewer
     *
     * @paramr recipe  Recipe to view
     */
    public void viewRecipe(Recipe recipe)
    {
        System.out.printf("Viewing recipe '%s'\n", recipe.getTitle());

        // Update state
        currentRecipe = recipe;

        // Clear old content of the component
        clearText();

        // First print out the recipe title in the heading style
        appendLine(recipe.getTitle(), headingStyle);

        // Print out description
        appendLine(recipe.getDescription(), mainStyle);

        // Print out the recipe's tags
        if (recipe.hasTags())
        {
            final ArrayList<String> tags = recipe.getTags();
            appendText("Tags: ", tagsStyle);
            for (int i = 0; i < tags.size(); ++i)
            {
                if (i == tags.size() - 1)
                {
                    // Final tag: no comma
                    appendText(String.format("%s", tags.get(i)), tagsStyle);
                    continue;
                }
                // Tags not at end get a comma with comma
                appendText(String.format("%s, ", tags.get(i)), tagsStyle);
            }
        }
        // Add a line break after the tag list
        appendLine("\n", tagsStyle);

        // Ingredients subheading
        appendLine("Ingredients", subheadingStyle);

        // Write all the ingredients
        for (String ingredient : recipe.getIngredients())
        {
            // Ingredients are prefixed with constant prefix (bullet point character)
            appendLine(String.format("%s%s", INGREDIENT_PREFIX, ingredient), mainStyle);
        }

        // Procedure subheading
        appendLine("Directions", subheadingStyle);

        // Write all the steps
        for (int i = 0; i < recipe.getProcedureSize(); ++i)
        {
            // Steps are prefixed with step number
            appendLine(String.format("%,d. %s", i + 1, recipe.getProcedureItem(i)), mainStyle);
        }

        // We need to move textpane caret to the top so that the scrollbar
        // is moved to the top also.
        pane.setCaretPosition(0);

        // We are showing recipe so toolbar buttons (edit, delete) can be
        // enabled
        SAT.onRecipeViewChanged(true);

        // Refresh the recipe list selection
        SAT.recipeList.refreshSelection();
    }

    /*
     * Show the default text in the recipe viewer, and stop showing recipe that
     * was being viewed.
     */
    public void viewNoRecipe()
    {
        // No recipe
        currentRecipe = null;

        // Clear old content of the component
        clearText();

        // No recipe so toolbar buttons (edit, delete) are disabled
        SAT.onRecipeViewChanged(false);
    }

    /*
     * Append text to the displayed document.
     *
     * @param text   Text to write
     * @param style  Style to display text with
     */
    private void appendText(String text, Style style)
    {
        try
        {
            // Insert text at end of the document
            doc.insertString(doc.getLength(), text, style);
        }
        catch (Exception e)
        {
            // Print error to stdout
            System.err.println("Error while displaying recipe text content.");
            e.printStackTrace();

            // Don't show error dialog as this will be run in a loop and may
            // make the program irritating to work with
        }
    }

    /*
     * Append text to the displayed document.
     * New-lines are automatically inserted at end of text
     *
     * @param text   Text to write
     * @param style  Style to display text with
     */
    private void appendLine(String text, Style style)
    {
        appendText(text + "\n", style);
    }

    /*
     * Clear all text from the displayed document
     */
    private void clearText()
    {
        try
        {
            // Just remove from index 0 to the length of the doc
            doc.remove(0, doc.getLength());
        }
        catch (Exception e)
        {
            // No need to handle this
        }
    }
}
