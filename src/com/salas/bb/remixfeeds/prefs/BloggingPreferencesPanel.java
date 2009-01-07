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
// $Id: BloggingPreferencesPanel.java,v 1.20 2008/02/15 15:36:59 spyromus Exp $
//

package com.salas.bb.remixfeeds.prefs;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.feed.FeedLinkPostToBlogAction;
import com.salas.bb.remixfeeds.PostToBlogAction;
import com.salas.bb.remixfeeds.api.WeblogAPIs;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * Blogging preferences panel.
 */
public class BloggingPreferencesPanel extends JPanel
{
    private final BloggingPreferences originalPrefs;
    private final BloggingPreferences prefs;
    private final BlogDetailsPreferencesPanel pnlBlogDetails;

    private JComboBox cbBlogs;
    private JButton btnAdd;
    private JButton btnDelete;
    private boolean ptbAdvanced;

    /**
     * Creates the panel.
     *
     * @param parent parent dialog.
     * @param trigger trigger.
     * @param preferences blogging preferences object to manipulate.
     */
    public BloggingPreferencesPanel(JDialog parent, ValueModel trigger, BloggingPreferences preferences)
    {
        ptbAdvanced = GlobalController.SINGLETON.getFeatureManager().isPtbAdvanced();

        originalPrefs = preferences;
        prefs = preferences.createClone();
        trigger.addValueChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                Object newValue = evt.getNewValue();
                if (Boolean.TRUE.equals(newValue))
                {
                    originalPrefs.copyFrom(prefs);
                    PostToBlogAction.update();
                    FeedLinkPostToBlogAction.update();
                }
            }
        });

        Component tfWording = ComponentsFactory.createWrappedMultilineLabel(Strings.message("ptb.prefs.wording"));

        Dimension btnSize = new Dimension(21, 21);
        btnAdd = new JButton(new AddAction());
        btnAdd.setPreferredSize(btnSize);
        btnDelete = new JButton(new DeleteAction());
        btnDelete.setPreferredSize(btnSize);

        pnlBlogDetails = new BlogDetailsPreferencesPanel(parent);

        cbBlogs = new JComboBox();
        cbBlogs.addItemListener(new BlogSelectionListener());
        onBlogSelected(null);
        populateListAndSelect(prefs.getDefaultBlog());

        // Layout
        BBFormBuilder builder = new BBFormBuilder("max(p;100dlu), 2dlu, p, 2dlu, p, 2dlu, p, 0:grow", this);
        builder.setDefaultDialogBorder();

        builder.append(tfWording, 8);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(buildPostTextEditorOptionsPanel(), 8);

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(cbBlogs, btnAdd);
        builder.append(btnDelete);

        builder.append(UifUtilities.makePublisherPlanIcon(!ptbAdvanced));
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(pnlBlogDetails, 8);
    }

    private Component buildPostTextEditorOptionsPanel()
    {
        JLabel lbPostEditor = new JLabel(Strings.message("ptb.prefs.editor.label"));
        ValueModel orientMdl = new PropertyAdapter(new EditorModeFilter(), BloggingPreferences.PROP_RICH_EDITOR, true);
        JRadioButton rbRichEditor = BasicComponentFactory.createRadioButton(orientMdl, true,
                Strings.message("ptb.prefs.editor.rich"));
        rbRichEditor.setToolTipText(Strings.message("ptb.prefs.editor.rich.tt"));
        JRadioButton rbPlainEditor = BasicComponentFactory.createRadioButton(orientMdl, false,
                Strings.message("ptb.prefs.editor.plain"));
        rbPlainEditor.setToolTipText(Strings.message("ptb.prefs.editor.plain.tt"));

        lbPostEditor.setEnabled(ptbAdvanced);
        rbRichEditor.setEnabled(ptbAdvanced);
        rbPlainEditor.setEnabled(ptbAdvanced);
        
        BBFormBuilder builder = new BBFormBuilder("max(60dlu;p), 4dlu, p, 2dlu, p, 2dlu, p");
        builder.append(lbPostEditor);
        builder.append(rbRichEditor);
        builder.append(rbPlainEditor);
        builder.append(UifUtilities.makePublisherPlanIcon(!ptbAdvanced));

        return builder.getPanel();
    }

    /**
     * Invoked when a targetBlog is selected in the list.
     *
     * @param targetBlog selected targetBlog or <code>NULL</code>.
     */
    private void onBlogSelected(TargetBlog targetBlog)
    {
        btnDelete.setEnabled(targetBlog != null);
        if (!ptbAdvanced) btnAdd.setEnabled(targetBlog == null);
        pnlBlogDetails.setBlogPreferences(targetBlog);
    }

    /**
     * Populates blogs list with blogs.
     *
     * @param targetBlog targetBlog to select.
     */
    private void populateListAndSelect(TargetBlog targetBlog)
    {
        List blogs = prefs.getBlogs();

        if (blogs.size() > 0)
        {
            Object sel = targetBlog == null ? cbBlogs.getSelectedItem() : targetBlog;
            for (int i = 0; i < blogs.size(); i++) cbBlogs.addItem(blogs.get(i));
            if (sel != null) cbBlogs.setSelectedItem(sel);
        }

        checkDropDownAndUpdateStatus();
    }

    private void checkDropDownAndUpdateStatus()
    {
        if (cbBlogs.getItemCount() == 0)
        {
            cbBlogs.addItem(Strings.message("ptb.prefs.no.blogs"));
            cbBlogs.setEnabled(false);
        } else
        {
            cbBlogs.setEnabled(true);
        }
    }

    /**
     * Add new blog record action.
     */
    private class AddAction extends AbstractAction
    {
        /**
         * Creates action.
         */
        public AddAction()
        {
            super(null, ResourceUtils.getIcon("add.icon"));
        }

        /**
         * Invoked when the action is performed.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (cbBlogs.getItemCount() == 1 && cbBlogs.getItemAt(0) instanceof String)
            {
                cbBlogs.removeAllItems();
                cbBlogs.setEnabled(true);
            }

            TargetBlog targetBlog = new TargetBlog();
            targetBlog.setTitle("Blog");
            targetBlog.setApiType(WeblogAPIs.getDefaultWeblogAPI());

            prefs.addBlog(targetBlog);
            cbBlogs.addItem(targetBlog);
            cbBlogs.setSelectedItem(targetBlog);
        }
    }

    /**
     * Deletes the selected blog record.
     */
    private class DeleteAction extends AbstractAction
    {
        /**
         * Creates action.
         */
        public DeleteAction()
        {
            super(null, ResourceUtils.getIcon("delete.icon"));
        }

        /**
         * Invoked when the action is performed.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            TargetBlog targetBlog = (TargetBlog)cbBlogs.getSelectedItem();
            if (targetBlog != null)
            {
                cbBlogs.removeItem(targetBlog);
                prefs.removeBlog(targetBlog);

                checkDropDownAndUpdateStatus();
            }
        }
    }

    /**
     * The listener for blog selection change events.
     */
    private class BlogSelectionListener implements ItemListener
    {
        /**
         * Invoked when another blog is selected in the drop-down menu.
         *
         * @param e event.
         */
        public void itemStateChanged(ItemEvent e)
        {
            Object sel = cbBlogs.getSelectedItem();
            TargetBlog targetBlog = sel instanceof TargetBlog ? (TargetBlog)sel : null;
            prefs.setDefaultBlog(targetBlog);
            onBlogSelected(targetBlog);
        }
    }

    public class EditorModeFilter
    {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public boolean isRichEditor()
        {
            return !ptbAdvanced || prefs.isRichEditor();
        }

        public void setRichEditor(boolean rich)
        {
            boolean old = prefs.isRichEditor();
            prefs.setRichEditor(rich);

            pcs.firePropertyChange(BloggingPreferences.PROP_RICH_EDITOR, old, rich);
        }

        public void addPropertyChangeListener(PropertyChangeListener l)
        {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l)
        {
            pcs.removePropertyChangeListener(l);
        }
    }
}
