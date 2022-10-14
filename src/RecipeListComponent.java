import java.awt.*;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Enumeration;

public class RecipeListComponent extends JPanel
{
    // Internal list model
    private DefaultListModel listModel;

    // List component
    private JList list;

    /*
     * Initialises a new RecipeListComponent
     */
    public RecipeListComponent()
    {
        // Use border layout so list fills whole panel
        setLayout(new BorderLayout());

        // Prepare list model
        listModel = new DefaultListModel();

        refresh();

        // Create the main recipe list object
        list = new JList(listModel);

        // Disable multi-select
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Disable dragging
        list.setDragEnabled(false);

        // Add mouse click listener
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                // Check for single clicks
                if (e.getClickCount() != 1)
                {
                    return;
                }

                // Deselect everything (we reselect item that is clicked)
                list.clearSelection();

                // Check if click was in cell boundaries.
                // https://stackoverflow.com/a/4344762
                Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
                if (r != null && r.contains(e.getPoint()))
                {
                    // Get index of cell
                    int index = list.locationToIndex(e.getPoint());

                    // Reselect the item
                    list.setSelectedIndex(index);

                    // Get name of cell (recipe title)
                    String title = (String)listModel.getElementAt(index);

                    // Show the recipe in the recipe viewer (if we aren't
                    // already on it)
                    if (!(SAT.recipeViewer.currentRecipe != null &&
                        title == SAT.recipeViewer.currentRecipe.getTitle()))
                    {
                        SAT.viewRecipe(title);
                    }
                }
            }
        });

        // Add to this panel
        add(list);

        // Set min/pref size
        setPreferredSize(new Dimension(150, 100));
    }

    /*
     * Updates the list model
     *
     * Use this to refresh the recipe list.
     */
    public void refresh()
    {
        // Clear old data
        listModel.clear();

        // Reserve space for the elements
        listModel.ensureCapacity(SAT.savedRecipes.size());

        // Add all elements
        for (Recipe recipe : SAT.savedRecipes.values())
        {
            listModel.addElement(recipe.getTitle());
        }

        // Stop here if we haven't got any selected recipe
        if (SAT.recipeViewer == null || SAT.recipeViewer.currentRecipe == null)
        {
            return;
        }

        // Refresh selection
        refreshSelection();
    }

    /*
     * Refresh the selected index on the list
     */
    public void refreshSelection()
    {
        // Search for the selected recipe index, and select the element
        Enumeration<?> enumRecipes = listModel.elements();
        boolean found = false;
        int index;
        for (index = 0; enumRecipes.hasMoreElements(); ++index)
        {
            // Check if this element's string matches the current recipe's title
            if (((String)enumRecipes.nextElement()).equals(SAT.recipeViewer.currentRecipe.getTitle()))
            {
                // Found the index
                found = true;
                break;
            }
        }

        // Set the selected index on the list
        if (found)
        {
            list.setSelectedIndex(index);
        }
    }
}
