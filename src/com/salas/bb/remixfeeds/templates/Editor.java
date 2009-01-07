// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2006 by R. Pito Salas
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software Foundation;
// either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
// without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program;
// if not, write to the Free Software Foundation, Inc., 59 Temple Place,
// Suite 330, Boston, MA 02111-1307 USA
//
// Contact: R. Pito Salas
// mailto:pitosalas@users.sourceforge.net
// More information: about BlogBridge
// http://www.blogbridge.com
// http://sourceforge.net/projects/blogbridge
//
// $Id: Editor.java,v 1.17 2008/04/09 04:34:38 spyromus Exp $
//

package com.salas.bb.remixfeeds.templates;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.utils.Resources;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

/**
 * Templates editor lets the user to see available templates
 * and edit them (not system ones).
 */
public class Editor extends AbstractDialog
{
    private final String STR_DIALOG_TITLE        = Strings.message("te.dialog.title");
    private final String STR_DIALOG_HEADER       = Strings.message("te.dialog.header");
    private final String STR_TEMPLATE            = Strings.message("te.template");
    private final String STR_HELP                = Strings.message("te.help");
    private final String STR_HELP_TEXT           = Strings.message("te.help.text");
    private final String STR_NEW_PROMT           = Strings.message("te.new.prompt");
    private final String STR_NEW_TITLE           = Strings.message("te.new.title");
    private final String STR_COPY_TITLE          = Strings.message("te.copy.title");
    private final String STR_NEW_PROMPT_EMPTY    = Strings.message("te.new.prompt.empty");
    private final String STR_NEW_PROMPT_UNIQUE   = Strings.message("te.new.prompt.unique");
    private final String STR_DELETE_TEXT         = Strings.message("te.delete.text");
    private final String STR_DELETE_TITLE        = Strings.message("te.delete.title");
    private final String STR_SAVE_SAVE           = Strings.message("te.save");
    private final String STR_SAVE_DONT_SAVE      = Strings.message("te.dont.save");
    private final String STR_SAVE_CANCEL         = Strings.message("te.cancel");
    private final String STR_SAVE_MODIFIED       = Strings.message("te.save.modified");
    private final String STR_SAVE_INVALID        = Strings.message("te.save.invalid");
    private final String STR_NEW                 = Strings.message("te.new");
    private final String STR_DELETE              = Strings.message("te.delete");
    private final String STR_COPY                = Strings.message("te.copy");
    private final String STR_SAVE                = Strings.message("te.save");
    private final String STR_REVERT              = Strings.message("te.revert");

    /** Set to TRUE when the editor template text differs from the current. */
    private volatile boolean templateModified;
    /** Set to TRUE when the template in the editor has a valid syntax. */
    private volatile boolean templateValid;

    /** Currently selected template. */
    private volatile Template selectedTemplate;

    /** Templates list. */
    private JComboBox cbTemplates;
    private JTextArea taEditor;
    private JButton btnSave;
    private JButton btnRevert;
    private JButton btnDelete;
    private JButton btnCopy;
    private JButton btnNew;

    /**
     * Creates the dialog.
     *
     * @param parent parent.
     */
    public Editor(Dialog parent)
    {
        super(parent);
        initComponents();
    }

    /**
     * Creates the dialog.
     *
     * @param parent parent.
     */
    public Editor(MainFrame parent)
    {
        super(parent);
        initComponents();
    }

    /**
     * Build the standard header.
     *
     * @return header
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(STR_DIALOG_TITLE, STR_DIALOG_HEADER);
    }

    /**
     * Makes the dialog resizable.
     */
    protected void setResizable()
    {
        setResizable(true);
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        setTitle(STR_DIALOG_TITLE);
        
        btnNew      = new JButton(new NewTemplateAction());
        btnCopy     = new JButton(new CopyTemplateAction());
        btnDelete   = new JButton(new DeleteTemplateAction());
        btnSave     = new JButton(new SaveTemplateAction());
        btnRevert   = new JButton(new RevertTemplateAction());

        cbTemplates = new JComboBox();
        cbTemplates.addActionListener(new TemplateDropDownListener());

        taEditor    = new JTextArea("");
        taEditor.setFont(new Font("Monospaced", Font.PLAIN, btnNew.getFont().getSize()));
        taEditor.setTabSize(2);
        taEditor.addKeyListener(new EditorListener());
    }

    /**
     * Returns the contents.
     *
     * @return pane.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonBar(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Main panel.
     *
     * @return panel.
     */
    private Component buildMainPanel()
    {
        // Buttons
        ButtonBarBuilder buttons = new ButtonBarBuilder();
        buttons.addFixed(btnNew);
        buttons.addRelatedGap();
        buttons.addFixed(btnCopy);
        buttons.addRelatedGap();
        buttons.addFixed(btnDelete);

        // --- Panel construction
        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, max(50dlu;p):grow, 7dlu, p, 0dlu:grow");

        // First row
        builder.append(STR_TEMPLATE, cbTemplates);
        builder.append(buttons.getPanel());

        // Editor
        builder.appendRelatedComponentsGapRow();
        builder.appendRow("200px:grow");
        builder.nextLine(2);
        builder.append(new JScrollPane(taEditor), 6,
            CellConstraints.FILL, CellConstraints.FILL);

        // Help section
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(STR_HELP, 6);
        builder.appendRelatedComponentsGapRow();
        builder.appendRow("150px");
        builder.nextLine(2);
        builder.append(ComponentsFactory.createInstructionsBox(STR_HELP_TEXT), 6,
            CellConstraints.FILL, CellConstraints.FILL);

        builder.appendUnrelatedComponentsGapRow(2);

        return builder.getPanel();
    }

    /**
     * Button bar.
     *
     * @return bar.
     */
    private Component buildButtonBar()
    {
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGriddedButtons(new JButton[] {btnSave, btnRevert});
        builder.addGlue();
        builder.addGridded(createCloseButton(true));

        return builder.getPanel();
    }

    /**
     * Opens the editor.
     *
     * @param template template to select or NULL to select the first.
     */
    public void open(Template template)
    {
        updateDropDown(template);

        super.open();
    }

    // ------------------------------------------------------------------------
    // Action events
    // ------------------------------------------------------------------------

    /**
     * Invoked when a template text changes.
     */
    private void onTemplateChange()
    {
        // Check the syntax
        // See if the text differs from what's in the rep and
        // update the state of cancel / save buttons
        String text = taEditor.getText();
        templateModified = !text.equals(selectedTemplate.getText());
        templateValid    = SyntaxChecker.validate(text).isEmpty();

        btnSave.setEnabled(templateModified && templateValid);
        btnRevert.setEnabled(templateModified);
    }

    /**
     * Invoked when the template is selected.
     *
     * @param template selected template.
     */
    private void onTemplateSelect(Template template)
    {
        if (template == null || template == selectedTemplate) return;
        if (!saveIfNecessary())
        {
            updateDropDown(selectedTemplate);
            return;
        }

        // Reset the state
        selectedTemplate = template;
        templateModified = false;
        templateValid    = true;

        // Set text and cursor location
        taEditor.setText(template.getText());
        taEditor.setCaretPosition(0);

        // Disable the editor and some action buttons for the system templates
        boolean userDefined = !template.isSystem();
        btnDelete.setEnabled(userDefined);
        btnSave.setEnabled(userDefined);
        btnRevert.setEnabled(userDefined);

        taEditor.setEditable(userDefined);
        taEditor.setEnabled(userDefined);

        onTemplateChange();
    }

    /**
     * Invoked when a selected template is duplicated.
     */
    private void onTemplateCopy()
    {
        if (selectedTemplate == null) return;

        String title = selectedTemplate.getName();
        onTemplateNew(createUniqueTitleFrom(title), selectedTemplate.getText());
    }

    /**
     * Invoked when a new template is added.
     *
     * @param newTitle  new title or NULL.
     * @param text      new template text or NULL.
     */
    private void onTemplateNew(String newTitle, String text)
    {
        String message = STR_NEW_PROMT;

        String dialogTitle = newTitle == null ? STR_NEW_TITLE : STR_COPY_TITLE;

        String name = null;
        while (message != null)
        {
            // Ask for the name
            name = (String)JOptionPane.showInputDialog(Editor.this, message, dialogTitle, 
                JOptionPane.QUESTION_MESSAGE, getIcon(), null, newTitle);

            // Analyze the result
            if (name == null)
            {
                // Cancelled
                return;
            } else
            {
                // Entered something or simply confirmed

                if (StringUtils.isEmpty(name))
                {
                    message = STR_NEW_PROMPT_EMPTY;
                } else if (Templates.isExisting(name))
                {
                    message = STR_NEW_PROMPT_UNIQUE;
                } else
                {
                    // Finally, no error
                    message = null;
                }
            }
        }

        // Create a template and register
        if (text == null) text = "";
        Template newTemplate = new Template(name, false, text);
        Templates.addTemplate(newTemplate);

        updateDropDown(newTemplate);
    }

    /**
     * Invoked when a selected template is removed.
     */
    private void onTemplateDelete()
    {
        if (selectedTemplate == null) return;

        int answer = JOptionPane.showConfirmDialog(Editor.this,
            STR_DELETE_TEXT, STR_DELETE_TITLE,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
            getIcon());

        if (answer == JOptionPane.YES_OPTION)
        {
            // Delete the template
            Templates.deleteTemplate(selectedTemplate);
            selectedTemplate = null;

            updateDropDown(null);
        }
    }

    /**
     * Returns the icon to use in dialogs.
     *
     * @return icon.
     */
    private Icon getIcon()
    {
        return Resources.getLargeApplicationIcon();
    }

    /**
     * Invoked when the changes are saved.
     */
    private void onTemplateSave()
    {
        saveTemplate(selectedTemplate);
    }

    /**
     * Invoked when a user cancels his changes to the template.
     */
    private void onTemplateRevert()
    {
        taEditor.setText(selectedTemplate.getText());
        onTemplateChange();
    }

    /**
     * Invoked when a user presses the close button.
     */
    public void doClose()
    {
        if (saveIfNecessary()) super.doClose();
    }

    /**
     * Invoked when a user closes the window.
     */
    protected void doCloseWindow()
    {
        doClose();
    }

    // ------------------------------------------------------------------------
    // Functions
    // ------------------------------------------------------------------------

    /**
     * Saves the text from the editor to the target template.
     *
     * @param target target.
     */
    private void saveTemplate(Template target)
    {
        target.setText(taEditor.getText());
        onTemplateChange();
    }

    /**
     * Updates the templates drop-down.
     *
     * @param select template to select or NULL to select what was previously selected.
     */
    private void updateDropDown(Template select)
    {
        // Find what was previously selected if there's nothing to select given
        if (select == null)
        {
            select = (Template)cbTemplates.getSelectedItem();
        }

        // Refill the dialog
        cbTemplates.removeAllItems();

        Collection<Template> templates = Templates.getUserTemplates().values();
        for (Template template : templates)
        {
            cbTemplates.addItem(template);
        }

        // Set the selection
        if (select != null) cbTemplates.setSelectedItem(select);
    }

    /**
     * Creates a unique title from the given name by adding "(N)".
     *
     * @param title base title.
     *
     * @return unique title.
     */
    private static String createUniqueTitleFrom(String title)
    {
        String newTitle;

        int cnt = 2;
        do
        {
            newTitle = title + " (" + cnt + ")";
            cnt++;
        } while (Templates.isExisting(newTitle));

        return newTitle;
    }

    /**
     * Dialog resizer.
     *
     * @param component component.
     */
    protected void resizeHook(JComponent component)
    {
        Resizer.ONE2ONE.resize(component);
    }

    /**
     * Shows the dialog and saves the template if necessary.
     *
     * @return TRUE if saved and successfully and the operation can continue; FALSE if canceled.
     */
    private boolean saveIfNecessary()
    {
        if (templateModified)
        {
            // Template is modified, the user may want to save it,
            // but with bad syntax saving isn't possible, and the user
            // needs to fix it first, or drop the changes
            String answerSave       = STR_SAVE_SAVE;
            String answerDontSave   = STR_SAVE_DONT_SAVE;
            String answerCancel     = STR_SAVE_CANCEL;

            Object answer;

            if (templateValid)
            {
                Object[] options = { answerSave, answerDontSave, answerCancel };
                answer = JOptionPane.showOptionDialog(Editor.this,
                    STR_SAVE_MODIFIED,
                    STR_DIALOG_TITLE, -1, JOptionPane.QUESTION_MESSAGE, getIcon(),
                    options, answerSave);
                answer = options[(Integer)answer];
            } else
            {
                Object[] options = {answerDontSave, answerCancel};
                answer = JOptionPane.showOptionDialog(Editor.this,
                    STR_SAVE_INVALID,
                    STR_DIALOG_TITLE, -1, JOptionPane.QUESTION_MESSAGE, getIcon(),
                    options, answerCancel);
                answer = options[(Integer)answer];
            }

            if (answer == answerSave)
            {
                // Save template
                saveTemplate(selectedTemplate);
            } else if (answer == answerCancel)
            {
                // Cancel
                return false;
            }
        }

        return true;
    }

    // ------------------------------------------------------------------------
    // Actions and Listeners
    // ------------------------------------------------------------------------

    /**
     * Listens to the template drop-down.
     */
    private class TemplateDropDownListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            Object newSelection = cbTemplates.getSelectedItem();
            if (newSelection != selectedTemplate)
            {
                onTemplateSelect((Template)newSelection);
            }
        }
    }

    /**
     * Listens to the editor changes and validates the text.
     */
    private class EditorListener extends KeyAdapter
    {
        public void keyReleased(KeyEvent e)
        {
            if (!e.isActionKey()) onTemplateChange();
        }
    }

    /**
     * Intercepts the call to the new-template command.
     */
    private class NewTemplateAction extends AbstractAction
    {
        private NewTemplateAction()
        {
            super(STR_NEW);
        }

        public void actionPerformed(ActionEvent e)
        {
            onTemplateNew(null, null);
        }
    }

    /**
     * Intercepts the call to the delete-template command.
     */
    private class DeleteTemplateAction extends AbstractAction
    {
        private DeleteTemplateAction()
        {
            super(STR_DELETE);
        }

        public void actionPerformed(ActionEvent e)
        {
            onTemplateDelete();
        }
    }

    /**
     * Intercepts the call to the copy-template command.
     */
    private class CopyTemplateAction extends AbstractAction
    {
        private CopyTemplateAction()
        {
            super(STR_COPY);
        }

        public void actionPerformed(ActionEvent e)
        {
            onTemplateCopy();
        }
    }

    /**
     * Intercepts the call to the save-template command.
     */
    private class SaveTemplateAction extends AbstractAction
    {
        private SaveTemplateAction()
        {
            super(STR_SAVE);
        }

        public void actionPerformed(ActionEvent e)
        {
            onTemplateSave();
        }
    }

    /**
     * Intercepts the call to the revert-template command.
     */
    private class RevertTemplateAction extends AbstractAction
    {
        private RevertTemplateAction()
        {
            super(STR_REVERT);
        }

        public void actionPerformed(ActionEvent e)
        {
            onTemplateRevert();
        }
    }
}
