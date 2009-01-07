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
// $Id: PostEditor.java,v 1.39 2007/02/21 16:58:29 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.salas.bb.remixfeeds.prefs.TargetBlog;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/** Post editor. */
public class PostEditor extends AbstractPostEditor
{
    private JComboBox cbCategories;

    private ArrayListModel categories;
    private TargetBlog.Category category;
    private TargetBlog targetBlog;

    private JLabel lbCategories;

    /**
     * Creates post editor.
     *
     * @param frame main frame.
     * @param richText <code>TRUE</code> to show rich text editor.
     */
    public PostEditor(Frame frame, boolean richText)
    {
        super(frame, richText);

        categories = new ArrayListModel();

        lbCategories = new JLabel(Strings.message("ptb.category"));
    }

    /**
     * Enables / disables all controls during posting.
     *
     * @param en <code>TRUE</code> to enable.
     */
    protected void enableControls(boolean en)
    {
        super.enableControls(en);

        lbCategories.setEnabled(en);
        cbCategories.setEnabled(en);
    }

    // ----------------------------------------------------------------------------------
    // Initial properties
    // ----------------------------------------------------------------------------------

    /**
     * Sets the target blog.
     *
     * @param targetBlog   the target blog.
     */
    public void setTargetBlog(TargetBlog targetBlog)
    {
        this.targetBlog = targetBlog;

        // Initialize the arrays
        cbCategories = new JComboBox();
        categories = new ArrayListModel();
        category = targetBlog.getDefaultCategory();

        initCategories(targetBlog, targetBlog.getDefaultCategory());
        PropertyAdapter adapter = new PropertyAdapter(this, "category", true);
        cbCategories = new JComboBox(new ComboBoxAdapter((ListModel)categories, adapter));
        setDraft(targetBlog.isDraft());

        updateTitle(new TargetBlog[] { targetBlog });
    }

    /**
     * Initializes the list of categories for a given blog. If the categories aren't
     * available, loads them and then selects the given default.
     *
     * @param blog      blog to initialize.
     * @param cat       category to select.
     */
    private void initCategories(final TargetBlog blog, final TargetBlog.Category cat)
    {
        // If there are no categories in the blog object yet, let it be the default category.
        // We'll start loading them right away.
        TargetBlog.Category[] cats = blog.getCategories();
        boolean noCategories = cats.length == 0;
        if (noCategories) cats = new TargetBlog.Category[] { cat };

        // Populate the list of categories with defaults
        ArrayListModel categories = this.categories;
        if (categories == null)
        {
            categories = new ArrayListModel();
            this.categories = categories;
        }
        categories.clear();
        categories.addAll(Arrays.asList(cats));

        // Select the category
        setCategory(cat);

        if (noCategories)
        {
            // Load categories for this blog
            blog.loadCategories(new Runnable()
            {
                public void run()
                {
                    initCategories(blog, category);
                }
            });
        }
    }

    /**
     * Sets the category for a given blog.
     *
     * @param cat   category.
     */
    public void setCategory(TargetBlog.Category cat)
    {
        cat = findEqualCategory(categories, cat);

        // Set and fire
        TargetBlog.Category old = category;
        category = cat;
        firePropertyChange("category", old, cat);
    }

    /**
     * Returns the selected category.
     *
     * @return category.
     */
    public TargetBlog.Category getCategory()
    {
        return category;
    }


    /**
     * The hook to add custom control elements to the toolbar.
     *
     * @param builder builder.
     */
    protected void addCustomControls(BBFormBuilder builder)
    {
        BBFormBuilder b = new BBFormBuilder("p, 2dlu, p, 4dlu, p");
        b.append(lbCategories, cbCategories);
        b.append(chDraft);

        builder.append(b.getPanel());
    }

    // ------------------------------------------------------------------------
    // Implementations
    // ------------------------------------------------------------------------

    /**
     * Returns the list of categories selected for the given blog.
     *
     * @param blogIndex index of blog in list.
     *
     * @return categories.
     */
    protected java.util.List<TargetBlog.Category> getCategoriesForBlog(int blogIndex)
    {
        java.util.List<TargetBlog.Category> l = new ArrayList<TargetBlog.Category>();
        l.add(category);
        return l;
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
        return targetBlog;
    }
}
