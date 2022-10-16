import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

public class RecipeEditorComponent extends JPanel
{
    private final JFrame frame;

    // Internal list models for ingredients and procedure lists.
    private DefaultListModel<String> recipeIngredientsListModel, recipeProcedureListModel;

    /*
     * Initialises new RecipeEditor
     *
     * @param frame         The frame this panel exists in.
     * @param recipeToEdit  Recipe being edited.  Pass null for new recipe
     */
    public RecipeEditorComponent(final JFrame frame, final Recipe recipeToEdit)
    {
        // Store the frame so we can close it manually
        this.frame = frame;

        // Add padding around the panel (https://stackoverflow.com/a/5328475)
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Set all the layouts and create the main container panels
        // * This panel is on a single-cell grid layout (to make child
        //   borderlayout fit the whole frame)
        // * 'parent' panel is a border layout, which splits the main panels
        //   from the save/cancel buttons.
        // * 'subpanel' is a grid layout of two columns, for the two main
        //   editor panels
        setLayout(new GridLayout(1, 1));
        JPanel parentPanel = new JPanel(new BorderLayout());
        add(parentPanel);

        // Create the main subpanel
        JPanel subpanel = new JPanel(new GridLayout(1, 2, 16, 16));
        parentPanel.add(subpanel, BorderLayout.CENTER);

        // Create the two main panels and add to the subpanel
        JPanel leftPanel = new JPanel(),
            rightPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        subpanel.add(leftPanel);
        subpanel.add(rightPanel);

        // Recipe name label and text field
        leftPanel.add(SAT.genLeftJLabel("Recipe title:"));
        final JTextField recipeTitleField = new JTextField(32);
        recipeTitleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, recipeTitleField.getPreferredSize().height));
        leftPanel.add(recipeTitleField);

        // Spacer
        leftPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Recipe description label and text field
        leftPanel.add(SAT.genLeftJLabel("Recipe description:"));
        final JTextArea recipeDescArea = new JTextArea();
        leftPanel.add(recipeDescArea);

        // Spacer
        leftPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Recipe tags label and textfield
        leftPanel.add(SAT.genLeftJLabel("(Optional) Search tags"));
        leftPanel.add(SAT.genLeftJLabel("e.g. Italian dish; easy; breakfast"));
        final JTextField recipeTagsField = new JTextField();
        recipeTagsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, recipeTagsField.getPreferredSize().height));
        leftPanel.add(recipeTagsField);

        // Spacer
        leftPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Ingredients label
        rightPanel.add(SAT.genLeftJLabel("Ingredients:"));

        // Ingredients list
        {
            // Create the grid layout
            JPanel ingredientsPanel = new JPanel();
            ingredientsPanel.setLayout(new BorderLayout());

            // Prepare list model
            recipeIngredientsListModel = new DefaultListModel<String>();

            // Create list component
            final JList<String> ingredientsList = new JList<String>(recipeIngredientsListModel);

            // Add to a scrollbox and then to panel
            ingredientsPanel.add(new JScrollPane(ingredientsList), BorderLayout.CENTER);

            // Create list controls
            ingredientsPanel.add(createListControls(ingredientsList, "Add ingredient", "Remove ingredient", "Enter ingredient:", "New ingredient"), BorderLayout.EAST);

            // Add the panel to the right-panel, and add spacer
            rightPanel.add(ingredientsPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        // Procedure label
        rightPanel.add(SAT.genLeftJLabel("Directions"));

        // Procedure list
        {
            // Create the grid panel
            JPanel procPanel = new JPanel();
            procPanel.setLayout(new BorderLayout());

            // Prepare list model
            recipeProcedureListModel = new DefaultListModel<String>();

            // Create list component
            final JList<String> procedureList = new JList<String>(recipeProcedureListModel);

            // Add to a scrollbox and then to panel
            procPanel.add(new JScrollPane(procedureList), BorderLayout.CENTER);

            // Create list controls
            procPanel.add(createListControls(procedureList, "Add step", "Remove step", "Add step to procedure:", "New step"), BorderLayout.EAST);

            // Add the panel to the right-panel, and add spacer
            rightPanel.add(procPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        }

        // Set initial values
        if (recipeToEdit != null)
        {
            // Set title
            recipeTitleField.setText(recipeToEdit.getTitle());

            // Set description
            recipeDescArea.setText(recipeToEdit.getDescription());

            // Set ingredients
            recipeIngredientsListModel.clear();
            recipeIngredientsListModel.ensureCapacity(recipeToEdit.getIngredientsCount());
            for (String ingredient : recipeToEdit.getIngredients())
            {
                recipeIngredientsListModel.addElement(ingredient);
            }

            // Set procedure
            recipeProcedureListModel.clear();
            recipeProcedureListModel.ensureCapacity(recipeToEdit.getProcedureSize());
            for (String step : recipeToEdit.getProcedure())
            {
                recipeProcedureListModel.addElement(step);
            }

            // Set tags
            recipeTagsField.setText("");
            for (String tag : recipeToEdit.getTags())
            {
                // First tag
                if (recipeTagsField.getText().length() <= 0)
                {
                    recipeTagsField.setText(tag);
                    continue;
                }

                // Subsequent tags
                recipeTagsField.setText(String.format("%s; %s", recipeTagsField.getText(), tag));
            }
        }

        // Create the controls panel
        JPanel controlsPanel = new JPanel(new GridLayout(1, 2));
        parentPanel.add(controlsPanel, BorderLayout.SOUTH);

        // Add confirm button to controls panel
        final JButton btnConfirm = new JButton("Save recipe");
        btnConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Iterate over ingredients from the ingredients list and store
                // in an Arraylist that we can use
                ArrayList<String> recipeIngredients
                    = new ArrayList<String>(recipeIngredientsListModel.size());
                Enumeration<String> enumIngredients
                    = recipeIngredientsListModel.elements();
                while(enumIngredients.hasMoreElements())
                {
                    // Add next element in enumeration
                    recipeIngredients.add((String)enumIngredients.nextElement());
                }

                // Iterate over steps from the procedure list and store
                // in an Arraylist that we can use
                ArrayList<String> recipeProcedure = new ArrayList<String>(recipeProcedureListModel.size());
                Enumeration<String> enumProcedure = recipeProcedureListModel.elements();
                while(enumProcedure.hasMoreElements())
                {
                    // Add next element in enumeration
                    recipeProcedure.add((String)enumProcedure.nextElement());
                }

                // Get the tags from the tags list field.  We do this by
                // splitting at every semi-colon, and using the trimmed tokens
                // as the tags
                String[] recipeTagTokens = recipeTagsField.getText().split(";");
                HashSet<String> recipeTags = new HashSet<String>(recipeTagTokens.length);
                for (int i = 0; i < recipeTagTokens.length; ++i)
                {
                    // Remove any whitespace from tag values.
                    // Bug fix: force lowercase to prevent duplicates.
                    //          due to different case.
                    String tag = recipeTagTokens[i].trim().toLowerCase();
                    if (tag.length() > 0)
                    {
                        recipeTags.add(tag);
                    }
                }

                // Create recipe object with info from the fields
                Recipe recipeToSave = new Recipe(
                    recipeTitleField.getText(),
                    recipeDescArea.getText(),
                    recipeIngredients,
                    recipeProcedure,
                    recipeTags);

                // Make sure we have a title on the r ecipe
                if (recipeToSave.getTitle().length() == 0)
                {
                    // Show error message
                    JOptionPane.showMessageDialog(frame,
                        "Please enter a title for the recipe",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (recipeToEdit != null)
                {
                    // Remove old recipe that was edited
                    SAT.savedRecipes.remove(recipeToEdit.getTitle());
                }

                // Make sure the recipe title doesn't already exist
                if (SAT.savedRecipes.containsKey(recipeToSave.getTitle()))
                {
                    // Show error message
                    JOptionPane.showMessageDialog(frame,
                        "A recipe with that title already exists!  Please enter a different title.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Save new recipe
                SAT.savedRecipes.put(recipeToSave.getTitle(), recipeToSave);

                // Show the recipe in the viewer
                SAT.recipeViewer.viewRecipe(recipeToSave);

                // Refresh the recipe list
                SAT.recipeList.refresh();

                // Refresh tags list in recipe searcher
                SAT.recipeSearchUpdate();

                // Remove from editor frames list
                SAT.editorFrames.remove(frame);

                // Close the window
                SAT.closeWindow(frame);
            }
        });
        controlsPanel.add(btnConfirm);

        // Add cancel button
        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Remove from editor frames list
                SAT.editorFrames.remove(frame);

                // Close the window
                SAT.closeWindow(frame);
            }
        });
        controlsPanel.add(btnCancel);
    }

    /*
     * Helper method to generate two control buttons (add/remove item) intended
     * to be used with lists such as ingredient lists.
     *
     * @param model           List model that buttons are for
     * @param btnAddText      Text for the 'add' button
     * @param btnRemoveText   Text for the 'remove' button
     * @param addPrompt       Prompt for input dialog when adding items.
     * @param addPromptTitle  Title for input dialog when adding items.
     *
     * @return resulting panel with the controls, ready to the be added to a
     *         panel
     */
    private JPanel createListControls(
        final JList<String> list, String btnAddText, String btnRemoveText,
        final String addPrompt, final String addPromptTitle)
    {
        // Two cell grid layout panel for the controls
        JPanel ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new GridLayout(4, 1));
        add(ctrlPanel);

        // 'Add to list' button
        final JButton btnAdd = new JButton("+");
        btnAdd.setToolTipText(btnAddText);
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Create an input dialog
                String itemName = JOptionPane.showInputDialog(
                    frame,
                    addPrompt,
                    addPromptTitle,
                    JOptionPane.QUESTION_MESSAGE);

                // Skip if ingredient is empty
                if (itemName == null || itemName.length() <= 0)
                {
                    return;
                }

                // Add the item to the list
                ((DefaultListModel<String>)list.getModel()).addElement(itemName);
            }
        });
        ctrlPanel.add(btnAdd);

        // 'Remove from list' button
        final JButton btnRemove = new JButton("-");
        btnRemove.setToolTipText(btnRemoveText);
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Get selected item
                int selected = list.getSelectedIndex();
                if (selected < 0)
                {
                    // No item: exit
                    return;
                }

                // Remove from the list
                ((DefaultListModel<String>)list.getModel()).remove(selected);
            }
        });
        ctrlPanel.add(btnRemove);

        // 'Move up' button (text is unicode up arrow)
        final JButton btnUp = new JButton("\u2191");
        btnUp.setToolTipText("Move this item up in the list");
        btnUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Get the item that we want to move.
                int selected = list.getSelectedIndex();
                if (selected < 0)
                {
                    // No item: exit
                    return;
                }

                // Now we move the item 'up' (backwards in list).  We do this
                // by removing the old item from the list, and reinserting at
                // the location just before it.

                // Get list model
                DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();

                // Store the element so we can reinsert it
                String element = (String)model.get(selected);

                // Check if this element is the first element in the list
                if  (element == model.firstElement())
                {
                    // Return as we can't move the element back any further
                    return;
                }

                // Remove the element from the list
                model.removeElementAt(selected);

                int newloc = selected - 1;

                // Insert at the new location
                model.add(newloc, element);

                // Select the newly added element
                list.setSelectedIndex(newloc);
            }
        });
        ctrlPanel.add(btnUp);

        // 'Move down' button (text is unicode down arrow)
        final JButton btnDown = new JButton("\u2193");
        btnDown.setToolTipText("Move this item down in the list");
        btnDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Get the item that we want to move.
                int selected = list.getSelectedIndex();
                if (selected < 0)
                {
                    // No item: exit
                    return;
                }

                // Now we move the item 'down' (forwards in list).  We do this
                // by removing the old item from the list, and reinserting at
                // the location just after it.

                // Get list model
                DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();

                // Store the element so we can reinsert it
                String element = (String)model.get(selected);

                // Check if this element is the first element in the list
                if  (element == model.lastElement())
                {
                    // Return as we can't move element any more forward
                    return;
                }

                // Remove the element from the list
                model.removeElementAt(selected);

                // Calculate new index
                int newloc = selected + 1;

                // Insert at the new location
                model.add(newloc, element);

                // Select the newly added element
                list.setSelectedIndex(newloc);
            }
        });
        ctrlPanel.add(btnDown);

        // Set the maximum width of the buttons so that the panel is reduced.
        ctrlPanel.setMaximumSize(new Dimension(btnAdd.getPreferredSize().width, Integer.MAX_VALUE));

        // Set buttons to disabled by default
        btnDown.setEnabled(false);
        btnUp.setEnabled(false);

        // Add listener to the list to enable/disable move up/down buttons as
        // needed
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                // Enable if we have a selected item
                boolean enable = list.getSelectedIndex() > -1;
                btnDown.setEnabled(enable);
                btnUp.setEnabled(enable);
            }
        });

        // Return the new panel
        return ctrlPanel;
    }
}
