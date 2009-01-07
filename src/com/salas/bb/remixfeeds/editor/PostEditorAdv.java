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
// $Id: PostEditorAdv.java,v 1.10 2007/03/23 12:40:53 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.remixfeeds.api.WeblogPost;
import com.salas.bb.remixfeeds.prefs.TargetBlog;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.CheckBoxList;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.List;

/**
 * Advanced version of the post editor.
 */
public class PostEditorAdv extends AbstractPostEditor
{
    protected JButton           btnPostAndContinue;
    protected JEditorPane       tfExcerpt;
    protected JCheckBox         chAllowComments;
    protected JCheckBox         chAllowTrackbacks;
    private JDateChooser        pkDate;
    private JLabel              lbDate;

    private TargetBlog[]        targetBlogs;
    private JCheckBox[]         chTargetBlog;

    private CategoriesLabel[]   pcCategories;
    private ArrayListModel[]    categories;
    private TargetBlog.Category[][] category;
    private SyntheticCategoryProperty[] categoryProperties;

    /**
     * Creates advanced post editor.
     *
     * @param frame frame.
     * @param richText <code>TRUE</code> to show rich text editor.
     */
    public PostEditorAdv(Frame frame, boolean richText)
    {
        super(frame, richText);

        btnPostAndContinue = new JButton(new PostToBlogAction(true));
        tfExcerpt = new JEditorPane();
        chAllowComments = new JCheckBox(Strings.message("ptb.editor.allow.comments"), true);
        chAllowTrackbacks = new JCheckBox(Strings.message("ptb.editor.allow.trackbacks"), true);
    }

    /**
     * Sets the list of target blogs. Initializes their category lists and creates checkboxes for the
     * layout. This method must be called before opening the dialog.
     *
     * @param targetBlogs   the list of target blogs.
     * @param selected      the list of selected blogs.
     */
    public void setTargetBlogs(TargetBlog[] targetBlogs, TargetBlog[] selected)
    {
        int blogs = targetBlogs.length;
        this.targetBlogs = targetBlogs;
        java.util.List<TargetBlog> selBlogs = Arrays.asList(selected);

        // Initialize the arrays
        pcCategories = new CategoriesLabel[blogs];
        chTargetBlog = new JCheckBox[blogs];
        categories = new ArrayListModel[blogs];
        category = new TargetBlog.Category[blogs][];
        categoryProperties = new SyntheticCategoryProperty[blogs];

        for (int i = 0; i < targetBlogs.length; i++)
        {
            categoryProperties[i] = new SyntheticCategoryProperty(i);

            TargetBlog blog = targetBlogs[i];
            initCategories(blog, i, new TargetBlog.Category[] { blog.getDefaultCategory() });

            PropertyAdapter adapter = new PropertyAdapter(categoryProperties[i], SyntheticCategoryProperty.PROP, true);
            pcCategories[i] = new CategoriesLabel(categories[i], adapter);

            chTargetBlog[i] = new JCheckBox(blog.getTitle());

            if (selBlogs.contains(blog))
            {
                chTargetBlog[i].setSelected(true);
                setDraft(blog.isDraft());
            }
        }

        updateTitle(selected);
    }

    /**
     * Initializes the list of categories for a given blog. If the categories aren't
     * available, loads them and then selects the given default.
     *
     * @param blog      blog to initialize.
     * @param index     index of the blog to locate the correct categories / category object.
     * @param cat       category to select.
     */
    private void initCategories(final TargetBlog blog, final int index, final TargetBlog.Category[] cat)
    {
        // If there are no categories in the blog object yet, let it be the default category.
        // We'll start loading them right away.
        TargetBlog.Category[] cats = blog.getCategories();
        boolean noCategories = cats.length == 0;
        if (noCategories) cats = cat;

        // Populate the list of categories with defaults
        ArrayListModel categories = this.categories[index];
        if (categories == null)
        {
            categories = new ArrayListModel();
            this.categories[index] = categories;
        }
        categories.clear();
        categories.addAll(Arrays.asList(cats));

        // Select the category
        setCategories(index, cat);

        if (noCategories)
        {
            // Load categories for this blog
            blog.loadCategories(new Runnable()
            {
                public void run()
                {
                    initCategories(blog, index, category[index]);
                }
            });
        }
    }

    /**
     * Sets the categories for a given blog.
     *
     * @param index blog index.
     * @param cat   categories.
     */
    private void setCategories(int index, TargetBlog.Category[] cat)
    {
        cat = findEqualCategories(categories[index], cat);

        // Set and fire
        TargetBlog.Category[] old = category[index];
        category[index] = cat;
        categoryProperties[index].fireChange(old, cat);
    }
    /**
     * Scans the list of given categories and returns the one which is like the given.
     *
     * @param categories    categories.
     * @param cat           category to find among these in the list.
     *
     * @return a category or <code>NULL</code>.
     */
    protected static TargetBlog.Category[] findEqualCategories(ArrayListModel categories, TargetBlog.Category[] cat)
    {
        List<TargetBlog.Category> newCats = new ArrayList<TargetBlog.Category>();

        if (cat != null)
        {
            List<TargetBlog.Category> cats = Arrays.asList(cat);

            for (Object o : categories)
            {
                TargetBlog.Category c = (TargetBlog.Category)o;
                if (c != null && cats.contains(c)) newCats.add(c);
            }
        }

        return newCats.toArray(new TargetBlog.Category[newCats.size()]);
    }


    /**
     * The hook to add more buttons.
     *
     * @param builder builder.
     */
    protected void addCustomButtons(ButtonBarBuilder builder)
    {
        builder.addFixed(btnPostAndContinue);
        builder.addRelatedGap();
    }

    /**
     * The hook to add more custom panels after the main part.
     *
     * @param builder builder.
     */
    protected void addCustomPanels(BBFormBuilder builder)
    {
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu");
        builder.append(new JScrollPane(tfExcerpt), 2, CellConstraints.FILL, CellConstraints.FILL);

        builder.append(buildOptionsPanel(), 2);
    }

    /**
     * Builds the options panel for below the excerpt.
     *
     * @return options panel.
     */
    private Component buildOptionsPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("p");
        for (int i = 0; i < targetBlogs.length; i++)
        {
            BBFormBuilder b = new BBFormBuilder("min(p;100dlu), 2dlu, min(p;100dlu)");
            b.append(chTargetBlog[i], pcCategories[i]);
            builder.append(b.getPanel());
        }
        JPanel pnlBlogs = builder.getPanel();

        builder = new BBFormBuilder("p");
        builder.append(chDraft);
        builder.append(chAllowComments);
        builder.append(chAllowTrackbacks);
        JPanel pnlOptions = builder.getPanel();

        builder = new BBFormBuilder(new FormLayout("p, 4dlu:grow, p", "top:p"));
        builder.append(pnlBlogs, pnlOptions);
        return builder.getPanel();
    }

    /**
     * The hook to add custom control elements to the toolbar.
     *
     * @param builder builder.
     */
    protected void addCustomControls(BBFormBuilder builder)
    {
        pkDate = new JDateChooser(new JTextFieldDateEditor("MM/dd/yyyy", "##/##/####", '_'));
        pkDate.setDate(new Date());

        Component[] cmps = pkDate.getComponents();
        for (Component cmp : cmps) if (cmp instanceof JButton) pkDate.remove(cmp);
        
        lbDate = new JLabel(Strings.message("ptb.editor.publication.date"));

        BBFormBuilder b = new BBFormBuilder("p, 2dlu, p");
        b.append(lbDate, pkDate);

        builder.append(b.getPanel());
    }

    /**
     * Returns the list of indices of all selected target blogs.
     *
     * @return list of indices.
     */
    public java.util.List<Integer> getSelectedBlogIndices()
    {
        java.util.List<Integer> sel = new ArrayList<Integer>();
        for (int i = 0; i < chTargetBlog.length; i++)
        {
            if (chTargetBlog[i].isSelected()) sel.add(i);
        }

        return sel;
    }


    /**
     * Adds more specific properties before sending.
     *
     * @param post post.
     */
    protected void addProperties(WeblogPost post)
    {
        post.excerpt = tfExcerpt.getText();
        post.allowComments = chAllowComments.isSelected();
        post.allowTrackbacks = chAllowTrackbacks.isSelected();
        post.dateCreated = pkDate.getDate();
    }

    /**
     * Returns a blog at a given index.
     *
     * @param i index.
     *
     * @return blog.
     */
    protected TargetBlog getBlogAt(int i)
    {
        return targetBlogs[i];
    }

    /**
     * Returns the list of categories selected for the given blog.
     *
     * @param blogIndex index of blog in list.
     *
     * @return categories.
     */
    protected java.util.List<TargetBlog.Category> getCategoriesForBlog(int blogIndex)
    {
        return Arrays.asList(category[blogIndex]);
    }

    /**
     * Enables / disables all controls during posting.
     *
     * @param en <code>TRUE</code> to enable.
     */
    protected void enableControls(boolean en)
    {
        super.enableControls(en);

        btnPostAndContinue.setEnabled(en);
        tfExcerpt.setEnabled(en);
        chAllowComments.setEnabled(en);
        chAllowTrackbacks.setEnabled(en);

        for (int i = 0; i < targetBlogs.length; i++)
        {
            chTargetBlog[i].setEnabled(en);
            pcCategories[i].setEnabled(en);
        }

        pkDate.setEnabled(en);
        lbDate.setEnabled(en);
    }

    /**
     * Synthetic property to watch categories in array.
     */
    public class SyntheticCategoryProperty
    {
        public static final String PROP = "value";

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final int index;

        public SyntheticCategoryProperty(int index)
        {
            this.index = index;
        }

        public void addPropertyChangeListener(PropertyChangeListener x)
        {
            pcs.addPropertyChangeListener(x);
        }

        public void removePropertyChangeListener(PropertyChangeListener x)
        {
            pcs.removePropertyChangeListener(x);
        }

        public Object getValue()
        {
            return category[index];
        }

        public void setValue(Object c)
        {
            category[index] = (TargetBlog.Category[])c;
        }

        public void fireChange(TargetBlog.Category[] old, TargetBlog.Category[] cat)
        {
            pcs.firePropertyChange(PROP, old, cat);
        }
    }

    // ------------------------------------------------------------------------
    // Categories picker
    // ------------------------------------------------------------------------

    /**
     * The label with auto-updatable categories name.
     */
    private class CategoriesLabel extends JLabel
    {
        private final Color NORMAL = Color.BLUE;

        private final ArrayListModel allCategories;
        private final ValueModel     selCategories;

        /**
         * Creates a label for selected categories.
         *
         * @param allCategories all categories list model to pass to the picker.
         * @param selCategories selected categories to display and pass to the picker.
         */
        public CategoriesLabel(ArrayListModel allCategories, ValueModel selCategories)
        {
            this.allCategories = allCategories;
            this.selCategories = selCategories;

            setForeground(NORMAL);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            updateLabelText();
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }

        /** Updates the text according to what is selected. */
        private void updateLabelText()
        {
            String text;
            TargetBlog.Category[] cs = (TargetBlog.Category[])selCategories.getValue();
            if (cs == null || cs.length == 0)
            {
                text = "Uncategorized";
            } else if (cs.length == 1)
            {
                text = "category: " + cs[0].name;
            } else
            {
                text = cs.length + " categories";
            }

            setText("(" + text + ")");
        }

        @Override
        protected void processMouseEvent(MouseEvent e)
        {
            if (e.getID() == MouseEvent.MOUSE_CLICKED)
            {
                CategoriesPicker dlg = new CategoriesPicker(allCategories,
                    (TargetBlog.Category[])selCategories.getValue());

                dlg.open();
                if (!dlg.hasBeenCanceled())
                {
                    selCategories.setValue(dlg.getSelectedCategories());
                    updateLabelText();
                }
            }
        }
    }

    /**
     * Categories picker.
     */
    private class CategoriesPicker extends AbstractDialog
    {
        private CategoriesList lstCategories;

        /**
         * Creates categories picker component.
         *
         * @param categories    categories.
         * @param selected      selected categories.
         */
        public CategoriesPicker(ArrayListModel categories, TargetBlog.Category[] selected)
        {
            super(PostEditorAdv.this, Strings.message("ptb.editor.categories"));
            lstCategories = new CategoriesList(categories, selected);
        }

        /**
         * Builds the content pane.
         *
         * @return pane.
         */
        protected JComponent buildContent()
        {
            JPanel content = new JPanel(new BorderLayout());
            content.add(new JScrollPane(lstCategories), BorderLayout.CENTER);
            content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
            return content;
        }

        /**
         * Returns the selected categories.
         *
         * @return selected.
         */
        public TargetBlog.Category[] getSelectedCategories()
        {
            return lstCategories.getChecked();
        }
    }

    /**
     * Categories list control.
     */
    private static class CategoriesList extends CheckBoxList
    {
        /**
         * Creates and initializes categories list.
         *
         * @param categories    the list of all known categories.
         * @param selected      the list of selected.
         */
        public CategoriesList(ArrayListModel categories, TargetBlog.Category[] selected)
        {
            populateList(categories, selected);
            check(selected);
        }

        /**
         * Returns the list of checked categories.
         *
         * @return checked categories.
         */
        public TargetBlog.Category[] getChecked()
        {
            java.util.List<TargetBlog.Category> cats = new ArrayList<TargetBlog.Category>();
            ListModel model = getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
                CategoryCheckBox cb = (CategoryCheckBox)model.getElementAt(i);
                if (cb.isSelected()) cats.add(cb.getCategory());
            }

            return cats.toArray(new TargetBlog.Category[cats.size()]);
        }

        /**
         * Fetches all categories from the model and shows them.
         *
         * @param categories    categories.
         * @param selected      selected categories.
         */
        private void populateList(ArrayListModel categories, TargetBlog.Category[] selected)
        {
            Vector<CategoryCheckBox> wrappedCategories = new Vector<CategoryCheckBox>(categories.size());
            for (Object c : categories)
            {
                wrappedCategories.add(new CategoryCheckBox((TargetBlog.Category)c));
            }
            for (TargetBlog.Category c : selected)
            {
                TargetBlog.Category eqCat = findEqualCategory(categories, c);
                if (eqCat == null) wrappedCategories.add(new CategoryCheckBox(c));
            }
            setListData(wrappedCategories);
        }

        /**
         * Sets checkmarks for the given categories.
         *
         * @param selected the categories to be checked.
         */
        private void check(TargetBlog.Category[] selected)
        {
            java.util.List<TargetBlog.Category> cats = Arrays.asList(selected);
            ListModel model = getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
                CategoryCheckBox cb = (CategoryCheckBox)model.getElementAt(i);
                TargetBlog.Category c = cb.getCategory();
                if (cats.contains(c)) cb.setSelected(true);
            }
        }

        /**
         * Custom checkbox for the category.
         */
        private static class CategoryCheckBox extends JCheckBox
        {
            private final TargetBlog.Category cat;

            /**
             * Creates a checkbox for the category.
             *
             * @param cat category.
             */
            public CategoryCheckBox(TargetBlog.Category cat)
            {
                this.cat = cat;
                setText(cat.name);
            }

            /**
             * Returns the assigned category.
             *
             * @return category.
             */
            public TargetBlog.Category getCategory()
            {
                return cat;
            }

            /**
             * Returns string representation.
             *
             * @return string representation.
             */
            public String toString()
            {
                return cat.name;
            }
        }
    }

}
