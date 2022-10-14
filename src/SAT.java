/*
 * SAT.java
 *
 * @author Michael Skec
 *
 * This is the main class of the SAT project, which is a piece of recipe
 * management software.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import javax.xml.*;
import javax.xml.bind.*;

public class SAT
{
    // The main window frame
    public static JFrame frameMain;

    // Current frame being used to search, as we only want to allow one instance of it.
    public static JFrame currentSearchFrame;
    public static RecipeSearchComponent currentSearchComponent;

    // List of saved recipes, indexed by their titles
    public static HashMap<String, Recipe> savedRecipes;

    // Recipe viewer component
    public static RecipeViewComponent recipeViewer;

    // Recipe list component
    public static RecipeListComponent recipeList;

    // List of currently-open editor frames
    public static HashMap<String, JFrame> editorFrames = new HashMap<String, JFrame>();

    // Toolbar buttons
    private static JButton tbBtnNew, tbBtnEdit, tbBtnDelete, tbBtnSearch;

    /*
     * Entry-point method
     *
     * @param args  Command-line arguments (unused).
     */
    public static void main(String[] args)
    {
        // Check for unsupported Java versions
        String jvmVersion = System.getProperty("java.version");
        if (jvmVersion.startsWith("11") || jvmVersion.startsWith("12"))
        {
            // Show error
            System.err.println("Unsupported Java version!  Please run only with Java 6 to Java 10");
            JOptionPane.showMessageDialog(null,
                "Unsupported Java version!\nPlease run the program with Java versions 6 to 10.\n" +
                "You are currently using Java " + jvmVersion,
                "Fatal error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Read recipes from disk
        if (!readRecipesFromDisk())
        {
            System.err.println("Error while reading recipes from disk!");

            // Show the error dialog
            JOptionPane.showMessageDialog(null,
                "Could not read recipes from storage.  Your recipes file may be corrupted!",
                "Fatal error",
                JOptionPane.ERROR_MESSAGE);

            return;
        }

        // Enable font anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set look & feel theme
        try
        {
            // Use system GTK theme so the program looks a bit better on Linux
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

			// System theme for Windows
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            System.err.println("Failed to set look and feel theme");
        }

        // Create main window frame
        frameMain = new JFrame("Recipe Management Software");
        frameMain.setLayout(new BorderLayout());

        // Listen for window close event
        frameMain.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                // Save recipes on exit
                saveRecipesToDisk();

                // Close search window if we have one
                if (currentSearchFrame != null)
                {
                    closeWindow(currentSearchFrame);
                }

                // Close editor frames
                for (JFrame f : editorFrames.values())
                {
                    closeWindow(f);
                }

                super.windowClosing(e);

                System.exit(0);
            }
        });

        // Create the toolbar
        final JToolBar toolbar = new JToolBar();
        final JPanel toolbarPanel = new JPanel();

        // Toolbar: New
        tbBtnNew = new JButton("New");
        toolbarPanel.add(tbBtnNew);
        tbBtnNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Show the recipe creation window
                showRecipeEditorWindow(null, "New Recipe");
            }
        });

        // Toolbar: Edit
        tbBtnEdit = new JButton("Edit");
        toolbarPanel.add(tbBtnEdit);
        tbBtnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Can't edit if there's no recipe
                if (recipeViewer.currentRecipe == null)
                {
                    return;
                }

                // Show recipe creation window with initial value
                showRecipeEditorWindow(recipeViewer.currentRecipe, String.format("Editing '%s'", recipeViewer.currentRecipe.getTitle()));
            }
        });

        // Toolbar: Delete
        tbBtnDelete = new JButton("Delete");
        toolbarPanel.add(tbBtnDelete);
        tbBtnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Can't delete if there's no recipe
                if (recipeViewer.currentRecipe == null)
                {
                    return;
                }

                // Show confirmation dialog
                if (JOptionPane.showConfirmDialog(frameMain,
                    String.format(
                        "<html><center>" +
                        "Are you sure you want to the following recipe?<br>" +
                        "<span color=blue><strong>%s</strong></span><br>" +
                        "<span color=red>This cannot be undone.</span>" +
                        "</center></html>",
                        recipeViewer.currentRecipe.getTitle()),
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
                {
                    return;
                }

                // Delete the recipe
                savedRecipes.remove(recipeViewer.currentRecipe.getTitle());

                // Update viewer state
                recipeViewer.viewNoRecipe();

                // Refresh recipe list
                recipeList.refresh();

                // Refresh tags list in recipe searcher
                SAT.recipeSearchUpdate();
            }
        });

        // Toolbar: Search
        tbBtnSearch = new JButton("Search");
        toolbarPanel.add(tbBtnSearch);
        tbBtnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Show the recipe search window
                showRecipeSearchWindow();
            }
        });
        toolbar.add(toolbarPanel);

        // Create recipe list pane (scrollable)
        recipeList = new RecipeListComponent();
        final JScrollPane paneRecipeList = new JScrollPane(recipeList);
        paneRecipeList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paneRecipeList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Create current recipe pane (scrollable)
        recipeViewer = new RecipeViewComponent();
        final JScrollPane paneCurrentRecipe = new JScrollPane(recipeViewer);
        paneCurrentRecipe.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paneCurrentRecipe.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create main container panel.  This is a split panel with the recipe
        // list and current recipe panes contained inside.
        final JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            paneRecipeList, paneCurrentRecipe);

        // Add panels to the frame
        frameMain.add(toolbar, BorderLayout.NORTH);
        frameMain.add(mainPanel);

        // Show the window
        frameMain.setSize(1200, 800);
        frameMain.setLocationRelativeTo(null);
        frameMain.setVisible(true);
    }

    /*
     * Displays a recipe in the recipe viewer pane.
     *
     * @param title  Title of the recipe to view.
     */
    public static void viewRecipe(String title)
    {
        recipeViewer.viewRecipe(savedRecipes.get(title));
    }

    /*
     * Focuses the main window frame
     */
    public static void focusMainWindow()
    {
        frameMain.setVisible(true);
        frameMain.toFront();
        frameMain.requestFocus();
    }

    /*
     * Reads saved recipes from the disk.
     *
     * @return true if successfully read from disk.
     */
    public static boolean readRecipesFromDisk()
    {
        try
        {
            // Create the saved recipes list
            savedRecipes = new HashMap<String, Recipe>();

            // Create the container that we need to use
            JAXBContext ctx = JAXBContext.newInstance(RecipeContainer.class);
            Unmarshaller unmarshal = ctx.createUnmarshaller();

			// Get the file context
            File file = new File("recipes.xml");
			if (!file.exists())
			{
				// No recipes file yet
				return true;
			}

            // Unmarshal the file
            RecipeContainer container = (RecipeContainer)unmarshal.unmarshal(file);

			if (container == null || !container.hasRecipes())
			{
				// No recipes
				return true;
			}

            // Create the saved recipe hashmap from the arraylist values
            savedRecipes = new HashMap<String, Recipe>(container.getSize());
            for (Recipe r : container.getRecipes())
            {
                savedRecipes.put(r.getTitle(), r);
            }

            return true;
        }
        catch (Exception e)
        {
            // Print stack trace to stdout
            e.printStackTrace();
        }
        return false;
    }

    /*
     * Saves the user's recipes to an XML file on disk
     */
    public static void saveRecipesToDisk()
    {
        try
        {
            // Create the file
            File file = new File("recipes.xml");

            // Create the container
            RecipeContainer container = new RecipeContainer(
                new ArrayList<Recipe>(savedRecipes.values()));

            JAXBContext ctx = JAXBContext.newInstance(RecipeContainer.class);

            // Create the marshaller, and enable pretty-print
            Marshaller marshal = ctx.createMarshaller();
            marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Convert to XML file
            marshal.marshal(container, file);
        }
        catch (Exception e)
        {
            System.err.println("Error while saving recipes.");
            e.printStackTrace();

            // Show the error dialog
            JOptionPane.showMessageDialog(null,
                "Failed to save recipes to file.  If this problem persists, try re-installing the program.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Shows a window where the user can create/modify recipes
     *
     * @param recipeToEdit  Recipe to fill input fields with initially (for
     *                      editing recipes).
     * @param title         Title of the window.
     */
    public static void showRecipeEditorWindow(final Recipe recipeToEdit, String title)
    {
        // Iterate over the existing frames dictionary, see if one already exists.
        if (editorFrames.containsKey(recipeToEdit.getTitle()))
        {
            // Recipe window already exists
            System.out.println("focusing existing recipe editor window");
            JFrame f = editorFrames.get(recipeToEdit.getTitle());
            f.setVisible(true);
            f.toFront();
            f.requestFocus();
            return;
        }

        // Set up the frame
        final JFrame frame = new JFrame(title);

        // Create the RecipeEditor component
        frame.add(new RecipeEditorComponent(frame, recipeToEdit));

        // Show the window
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Add to active editor frames list
        editorFrames.put(recipeToEdit.getTitle(), frame);
    }

    /*
     * Shows a window where the user can search for recipes
     */
    public static void showRecipeSearchWindow()
    {
        // Don't allow more than one search frame
        if (currentSearchFrame != null)
        {
            // Focus the existing frame
            currentSearchFrame.setVisible(true);
            currentSearchFrame.toFront();
            currentSearchFrame.requestFocus();
            return;
        }

        // Set up the frame
        currentSearchFrame = new JFrame("Recipe Search");

        // Create the RecipeSearch component
        currentSearchComponent = new RecipeSearchComponent(currentSearchFrame);
        currentSearchFrame.add(currentSearchComponent);

        // Show the window
        currentSearchFrame.setSize(800, 600);
        currentSearchFrame.setLocationRelativeTo(null);
        currentSearchFrame.setVisible(true);
    }

    /*
     * Updates info in recipe search window when recipe data has changed
     */
    public static void recipeSearchUpdate()
    {
        if (currentSearchComponent != null)
        {
            // Refresh tags and recipe lists
            currentSearchComponent.refreshTagsList();
            currentSearchComponent.refreshRecipeList();
        }
    }

    /*
     * Helper method to close JFrames
     *
     * @param frame  Frame to close
     */
    public static void closeWindow(final JFrame frame)
    {
        // https://stackoverflow.com/a/1235994
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    /*
     * Callback to run when a recipe is/is not shown in the viewer
     *
     * @param hasRecipe  Whether a recipe is being shown or not.
     */
    public static void onRecipeViewChanged(boolean hasRecipe)
    {
        // Enable/disable edit and delete buttons depending on wheteher a
        // recipe is being viewed.  We do this because if there is no recipe
        // viewed then these buttons are not functional.
        tbBtnEdit.setEnabled(hasRecipe);
        tbBtnDelete.setEnabled(hasRecipe);
    }

    /*
     * Helper method to create left-aligned JPanel for use in a BoxLayout.
     * This works by using a stupid hack where the label is contained in a
     * single-cell grid layout
     *
     * @param text  Label text
     *
     * @return container panel of label
     */
    public static JPanel genLeftJLabel(String text)
    {
        // Create the container
        JPanel container = new JPanel(new GridLayout(1, 1));

        // Create the label
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add the label to the container
        container.add(label);

        return container;
    }
}
