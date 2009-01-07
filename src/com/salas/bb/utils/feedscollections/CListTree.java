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
// $Id $
//

package com.salas.bb.utils.feedscollections;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.html.CustomHTMLEditorKit;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * List/Tree component.
 */
class CListTree extends JPanel
{
    private static final String TEXT_STYLE = "normal";

    private Boolean treeMode;
    private Collection collection;
    private final boolean readingLists;

    private JComponent tree;
    private JComponent list;
    private JEditorPane taDescription;

    private CItemListener itemListener;
    private CList listComp;

    /**
     * Creates component.
     *
     * @param collection    collection to show.
     * @param treeMode      <code>TRUE</code> to set tree mode by default.
     * @param readingLists  <code>TRUE</code> if showing reading lists.
     */
    public CListTree(Collection collection, boolean treeMode, boolean readingLists)
    {
        this.collection = collection;
        this.readingLists = readingLists;

        itemListener = new CItemListener();

        setLayout(new BorderLayout());
        setTreeMode(treeMode);

        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, p, 0:grow");

        JComboBox cbViewMode = new JComboBox(new Object[] {
            Strings.message("collections.viewmode.tree"),
            Strings.message("collections.viewmode.list") });
        cbViewMode.setSelectedIndex(treeMode ? 0 : 1);
        cbViewMode.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                JComboBox box = (JComboBox)e.getSource();
                boolean isTree = box.getSelectedIndex() == 0;
                setTreeMode(isTree);
            }
        });

        builder.append(Strings.message("collections.viewmode"), cbViewMode);
        builder.appendRelatedComponentsGapRow(2);

        add(builder.getPanel(), BorderLayout.NORTH);

        // Description area
        taDescription = new JEditorPane();
        Color back = taDescription.getBackground();
        taDescription.setEditable(false);
        taDescription.setBackground(back);
        taDescription.setEditorKit(new CustomHTMLEditorKit());

        HTMLDocument doc = (HTMLDocument)taDescription.getDocument();
        Style def = doc.getStyle("default");
        Font font = UIManager.getFont("TextArea.font");
        if (SystemUtils.IS_OS_MAC) font = UifUtilities.applyFontBias(font, -2);
        UifUtilities.setFontAttributes(doc.addStyle(TEXT_STYLE, def), font);

        builder = new BBFormBuilder("0:grow");
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("p");

        builder.append(Strings.message("collections.description"), 1);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50px");
        builder.append(taDescription, 1, CellConstraints.FILL, CellConstraints.FILL);

        add(builder.getPanel(), BorderLayout.SOUTH);
    }

    /**
     * Changes tree mode and displays correct component.
     *
     * @param treeMode <code>TRUE</code> to switch to the tree mode.
     */
    public void setTreeMode(boolean treeMode)
    {
        if (this.treeMode != null && this.treeMode.booleanValue() == treeMode) return;

        this.treeMode = Boolean.valueOf(treeMode);

        JComponent removeComponent, setComponent;
        if (treeMode)
        {
            removeComponent = list;
            setComponent = getTreeComponent();
        } else
        {
            removeComponent = tree;
            setComponent = getListComponent();
        }

        if (removeComponent != null) remove(removeComponent);
        add(setComponent, BorderLayout.CENTER);

        validate();
        repaint();
    }

    /**
     * Returns tree component and creates it if it's not there yet.
     *
     * @return tree component.
     */
    private JComponent getTreeComponent()
    {
        if (tree == null)
        {
            CTree cmp = new CTree(collection, readingLists);
            cmp.getSelectionModel().addTreeSelectionListener(itemListener);

            tree = new JScrollPane(cmp);
        }

        return tree;
    }

    /**
     * Returns list component and creates it if it's not there yet.
     *
     * @return list component.
     */
    private synchronized JComponent getListComponent()
    {
        if (list == null)
        {
            listComp = new CList(collection, readingLists);
            listComp.getSelectionModel().addListSelectionListener(itemListener);
            list = new JScrollPane(listComp);
        }
        return list;
    }

    /**
     * Listens to item selections and shows the description in the box.
     */
    private class CItemListener implements ListSelectionListener, TreeSelectionListener
    {
        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        public void valueChanged(ListSelectionEvent e)
        {
            CList.CItemCheckBox box = (CList.CItemCheckBox)listComp.getSelectedValue();
            onNodeSelected(box == null ? null : box.getItem());
        }

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        public void valueChanged(TreeSelectionEvent e)
        {
            TreePath path = e.getNewLeadSelectionPath();
            CollectionNode node = path == null ? null : (CollectionNode)path.getLastPathComponent();
            onNodeSelected(node);
        }

        /**
         * On node selection changes description text.
         *
         * @param node node.
         */
        private void onNodeSelected(CollectionNode node)
        {
            if (node == null)
            {
                taDescription.setText(null);
            } else
            {
                taDescription.setText(node.getDescription());
                UifUtilities.installTextStyle(taDescription, TEXT_STYLE);
            }
        }
    }
}
