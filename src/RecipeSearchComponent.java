import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.util.HashSet;
import java.util.ArrayList;

public class RecipeSearchComponent extends JPanel
{
    private final JFrame frame;

    // List of all recipe tags the user has
    private HashSet<String> allTags;

    // Tag-filtered list of recipes
    private ArrayList<String> tagFilteredRecipes;

    // List models
    private DefaultListModel<String> recipeListModel, tagsListModel;

    // Recipe search query
    private String searchQuery = "";

    // Text to display for 'untagged' option in tags list
    private static final String UNTAGGED_TAG_NAME = "(untagged)";

    /*
     * Initialise new RecipeSearchComponent
     *
     * @param frame  Main frame this component appears in
     */
    public RecipeSearchComponent(final JFrame frame)
    {
        this.frame = frame;

        // Add 16px padding around panel
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Create main single-cell grid layout for the frame, and set up the
        // main parent panel
        setLayout(new GridLayout(1, 1));
        final JPanel parentPanel = new JPanel(new BorderLayout());
        add(parentPanel);

        // Create text search label and field field
        JPanel topPanel = new JPanel(new FlowLayout());
        parentPanel.add(topPanel, BorderLayout.NORTH);
        topPanel.add(new JLabel("Search:"));
        final JTextField searchField = new JTextField(48);
        topPanel.add(searchField);

        // Focus the textfield by default
        searchField.requestFocus();

        // Create left-side panel
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Add recipe list to the left panel with a scrollpane
        leftPanel.add(new JLabel("Recipes:"), BorderLayout.NORTH);
        recipeListModel = new DefaultListModel<String>();
        final JList<String> recipeList = new JList<String>(recipeListModel);
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeList.setDragEnabled(false);
        JScrollPane leftScrollPane = new JScrollPane(recipeList);
        leftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        leftScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        leftPanel.add(leftScrollPane, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(500, 100));

        // Sub controls for recipes panel
        final JButton btnView;
        {
            JPanel ctrlsPanel = new JPanel(new GridLayout(1, 3));
            leftPanel.add(ctrlsPanel, BorderLayout.SOUTH);

            // First spacer (left)
            ctrlsPanel.add(Box.createRigidArea(new Dimension(16, 0)));

            // 'View recipe' button
            btnView = new JButton("View recipe");
            ctrlsPanel.add(btnView);
            btnView.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // View the selected recipe.
                    String selected = (String)recipeList.getSelectedValue();
                    if (selected != null)
                    {
                        // Focus the main window
                        SAT.focusMainWindow();

                        // Display the selected recipe in the recipe viewer
                        SAT.viewRecipe(selected);
                    }
                }
            });

            // By default set the view button to disabled
            btnView.setEnabled(false);

            // Second spacer (right)
            ctrlsPanel.add(Box.createRigidArea(new Dimension(16, 0)));
        }

        // Create right-side panel (tag filter)
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Add tag filter list to the right panel with a scrollpane
        rightPanel.add(new JLabel("Tag filter (click to select)"), BorderLayout.NORTH);
        tagsListModel = new DefaultListModel<String>();
        final JList<String> tagsList = new JList<String>(tagsListModel);
        JScrollPane rightScrollPane = new JScrollPane(tagsList);
        rightScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rightScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        rightPanel.add(rightScrollPane, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(150, 100));

        // Sub controls for tags panel
        {
            JPanel ctrlsPanel = new JPanel(new GridLayout(1, 3));
            rightPanel.add(ctrlsPanel, BorderLayout.SOUTH);

            // Button to deselect all tags
            JButton btnDeselect = new JButton("Deselect All");
            ctrlsPanel.add(btnDeselect);
            btnDeselect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // Deselect everything in the list
                    ((TagsListSelectionModel)tagsList.getSelectionModel()).clearSelectionForced();
                }
            });

            // Button to select all tags
            JButton btnSelect = new JButton("Select All");
            ctrlsPanel.add(btnSelect);
            btnSelect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // Select everything in the list
                    ((TagsListSelectionModel)tagsList.getSelectionModel()).selectAll(tagsListModel);
                }
            });

            // Button to "invert" selection
            JButton btnInvert = new JButton("Invert");
            ctrlsPanel.add(btnInvert);
            btnInvert.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // Select everything in the list
                    ((TagsListSelectionModel)tagsList.getSelectionModel()).invertSelection(tagsListModel);
                }
            });
        }

        // Create the main split-pane with the left/right panes to it
        final JSplitPane mainpanel = new
            JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        parentPanel.add(mainpanel, BorderLayout.CENTER);
        mainpanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Button control panel
        {
            // Create button control panel
            final JPanel controlsPanel = new JPanel(new GridLayout(1, 3));
            parentPanel.add(controlsPanel, BorderLayout.SOUTH);

            // Fill first cell of controls panel with spacer
            controlsPanel.add(Box.createRigidArea(new Dimension(16, 0)));

            // Add exit button to the controls panel
            final JButton btnExit = new JButton("Exit");
            btnExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // Hide the frame
                    frame.setVisible(false);
                }
            });
            controlsPanel.add(btnExit);

            // Fill third cell of controls panel with spacer
            controlsPanel.add(Box.createRigidArea(new Dimension(16, 0)));
        }

        // Set up initial values of  tags lists
        refreshTagsList();

        // Set the selection model for the tag list
        tagsList.setSelectionModel(new TagsListSelectionModel());

        // Don't allow dragging
        tagsList.setDragEnabled(false);

        // Listen for selection changes, and update our recipe list depending on it
        tagsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                // Whether to show untagged
                boolean showUntagged = false;

                // Get the tags that are selected
                int[] selectedIndices = tagsList.getSelectedIndices();
                ArrayList<String> selectedTags = new ArrayList<String>(selectedIndices.length);
                for (int index : selectedIndices)
                {
                    // For some reason the selected indices always includes an
                    // additional index which is the size of the list.  We skip
                    // this obviously
                    if (index >= tagsListModel.getSize())
                    {
                        continue;
                    }

                    // Get the tag
                    String tag = (String)tagsListModel.get(index);

                    if (tag.equals(UNTAGGED_TAG_NAME))
                    {
                        // Allow untagged recipes to show
                        showUntagged = true;
                    }
                    else
                    {
                        // Add the element at this index to the tags list
                        selectedTags.add(tag);
                    }
                }

                // Refresh recipe list to only include recipes that have selected tags on them

                // Initialise filtered list
                tagFilteredRecipes = new ArrayList<String>(SAT.savedRecipes.size());

                // Iterate over all the recipes we have
                for (Recipe recipe : SAT.savedRecipes.values())
                {
                    // Add untagged recipes if user selected untagged option
                    if (!recipe.hasTags() && showUntagged)
                    {
                        tagFilteredRecipes.add(recipe.getTitle());
                        continue;
                    }

                    // Check if the recipe has one of the selected tags
                    for (String tag : recipe.getTags())
                    {
                        if (!selectedTags.contains(tag))
                        {
                            // Doesn't have a tag; move to next tag
                            continue;
                        }

                        // Recipe has a tag--add to the list and move to
                        // next recipe
                        tagFilteredRecipes.add(recipe.getTitle());
                        break;
                    }
                }

                // Refresh the recipe list
                refreshRecipeList();
            }
        });

        // Select all items in the tags list
        ((TagsListSelectionModel)tagsList.getSelectionModel()).selectAll(tagsListModel);

        // Add list selection listener to recipe list
        recipeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                btnView.setEnabled(recipeList.getSelectedIndex() > -1);
            }
        });

        // Add the mouse click listener to the recipe list
        recipeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                // Check for double clicks
                if (e.getClickCount() != 2)
                {
                    return;
                }

                String selected = (String)recipeList.getSelectedValue();
                if (selected != null)
                {
                    // Focus the main window
                    SAT.focusMainWindow();

                    // Display the selected recipe in the recipe viewer
                    SAT.viewRecipe(selected);
                }
            }
        });

        // Add DocumentListener to the search text field, so that we get realtime search results.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { fieldChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { fieldChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { fieldChanged(); }

            // Handle field changes
            protected void fieldChanged()
            {
                // Update search query
                // Bug fix: force lowercase on the search query so that
                //          comparisons only occur in lowercase
                searchQuery = searchField.getText().toLowerCase();

                // Refresh the recipe list
                refreshRecipeList();
            }
        });
    }

    /*
     * Refreshes the internal recipe list model
     */
    public void refreshRecipeList()
    {
        // Iterate over all the filtered recipes and add it to the recipe list
        recipeListModel.clear();
        for (String recipeTitle : tagFilteredRecipes)
        {
            Recipe recipe = SAT.savedRecipes.get(recipeTitle);

            // Just add the recipe if there is no search query
            if (searchQuery.length() <= 0)
            {
                recipeListModel.addElement(recipeTitle);
                continue;
            }

            // Check if title or description contains the search query
            if (recipeTitle.toLowerCase().contains(searchQuery) ||
                recipe.getDescription().toLowerCase().contains(searchQuery))
            {
                // Add this element
                recipeListModel.addElement(recipeTitle);
            }
        }
    }

    /*
     * Updates the tags list
     */
    public void refreshTagsList()
    {
        // Tags list:
        // First we iterate over all the user's recipes, and search for all
        // unique tags under the recipes.  We do this with a hashset so we
        // don't have to waste time checking for uniqueness ourselves
        allTags = new HashSet<String>(SAT.savedRecipes.size());
        for (Recipe r : SAT.savedRecipes.values())
        {
            // Iterate over all the tags
            for (String tag : r.getTags())
            {
                // Add tag to the hashset
                allTags.add(tag);
            }
        }

        // Clear tags list model
        tagsListModel.clear();

        // Add the 'untagged' element first
        tagsListModel.addElement(UNTAGGED_TAG_NAME);

        // Add all the tags we found to the list model
        tagsListModel.ensureCapacity(allTags.size());
        for (String tag : allTags)
        {
            tagsListModel.addElement(tag);
        }
    }

    // Custom selection model for the tags list.
    // This model works using single-click for everything.  Items are never
    // deselected until user actually clicks an already selected item
    public class TagsListSelectionModel extends DefaultListSelectionModel
    {
        // This is our own custom handler to clear selection, as we only
        // allow it through the "Deselect" button
        public void clearSelectionForced()
        {
            // Just call base clearSelection function
            super.clearSelection();
        }

        // Select everything
        // We need to pass the model so we can determine the size
        public void selectAll(DefaultListModel model)
        {
            // Use the base method as our overridden one doesn't allow this
            super.setSelectionInterval(0, model.size());
        }

        // Invert the selection
        public void invertSelection(DefaultListModel model)
        {
            // Iterate over all indices
            for (int i = 0; i < model.getSize(); ++i)
            {
                if (isSelectedIndex(i))
                {
                    // Already selected; deselect it
                    super.removeSelectionInterval(i, i);
                }
                else
                {
                    // Not selected; select it
                    super.addSelectionInterval(i, i);
                }
            }
        }

        @Override
        public void clearSelection()
        {
            // Don't allow clearing selection by normal means
        }

        @Override
        public void removeSelectionInterval(int index0, int index1)
        {
            // This hack disallows deselection of the whole list, instead
            // we only allow deselection of single elements at a time.
            if (index0 != index1)
            {
                return;
            }

            // Call the base method to deselect the single item
            super.removeSelectionInterval(index0, index1);
        }

        // Derived from https://stackoverflow.com/a/2531617
        // The two following methods allow us to deselect items that are
        // selected, by simply clicking them
        @Override
        public void setSelectionInterval(int index0, int index1)
        {
            // If selecting a single item
            if (index0 == index1)
            {
                // If the item is selected
                if (isSelectedIndex(index0))
                {
                    // Deselect it and return
                    removeSelectionInterval(index0, index0);
                    return;
                }
            }

            // Add the selection to the interval
            addSelectionInterval(index0, index1);
        }

        @Override
        public void addSelectionInterval(int index0, int index1)
        {
            // If selecting a single item
            if (index0 == index1)
            {
                // If the item is already selected
                if (isSelectedIndex(index0))
                {
                    // Deselect the item and return
                    removeSelectionInterval(index0, index0);
                    return;
                }

                // Add the items to selection as normal
                super.addSelectionInterval(index0, index1);
            }
        }
    }
}
