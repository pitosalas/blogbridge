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
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.views.stylesheets.IStylesheet;
import com.salas.bb.views.stylesheets.StylesheetManager;
import com.salas.bb.views.stylesheets.domain.IRule;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Collections tree component.
 */
class CTree extends JTree
{
    /**
     * Creates component.
     *
     * @param collection  collection.
     * @param leafFolders <code>TRUE</code> if leaves should be considered as folders.
     */
    public CTree(Collection collection, boolean leafFolders)
    {
        super(collection);

        setCellRenderer(new CTreeRenderer(StylesheetManager.getSuggestionsStylesheet(), leafFolders));
        addMouseListener(new CTreeMouseListener());

        setRootVisible(false);
        setShowsRootHandles(true);
    }

    /**
     * Check boxed list renderer.
     */
    private static class CTreeRenderer extends DefaultTreeCellRenderer
    {
        private final JCheckBox         box = new JCheckBox();
        private final JPanel            panel = new JPanel();
        private final IStylesheet       stylesheet;
        private final CellConstraints   cc;
        private final boolean           leafFolders;

        /**
         * Creates renderer.
         *
         * @param stylesheet    the stylesheet.
         * @param leafFolders   <code>TRUE</code> if leaves should be considered as folders.
         */
        public CTreeRenderer(IStylesheet stylesheet, boolean leafFolders)
        {
            this.leafFolders = leafFolders;
            this.stylesheet = stylesheet;

            cc = new CellConstraints();
            FormLayout layout = new FormLayout("p, 1dlu, p", "p");
            panel.setLayout(layout);
            panel.add(this, cc.xy(3, 1));

            box.setOpaque(false);
            panel.setOpaque(false);

            setLeafIcon(null);
            setClosedIcon(null);
            setOpenIcon(null);
            setIcon(null);
        }

        /**
         * Configures the renderer based on the passed in components.
         * The value is set from messaging the tree with
         * <code>convertValueToText</code>, which ultimately invokes
         * <code>toString</code> on <code>value</code>.
         * The foreground color is set based on the selection and the icon
         * is set based on on leaf and expanded.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            CollectionNode node = (CollectionNode)value;

            String el = "folder";
            String[] tags = node.getTags();

            // Show box for items
            panel.remove(box);
            if (value instanceof CollectionItem)
            {
                panel.add(box, cc.xy(1, 1));
                box.setSelected(node.isSelected());
                if (!leafFolders) el = "item";
            }

            IRule rule = getRule(el, tags);
            if (rule != null)
            {
                // Update font
                Font fnt = rule.getFont();
                boolean bold = fnt != null && fnt.isBold();

                Font curFont = getFont();
                if (curFont.isBold() != bold) setFont(curFont.deriveFont(bold ? Font.BOLD : Font.PLAIN));

                // Update color
                if (!sel)
                {
                    Color color = rule.getColor();
                    if (color == null) color = Color.BLACK;
                    setForeground(color);
                }

                // Assign icon
                Icon icon = rule.getIcon();
                setIcon(icon);
                setClosedIcon(icon);
                setOpenIcon(icon);
                setLeafIcon(icon);
            }

            panel.invalidate();

            return panel;
        }

        /**
         * Returns the rule from the stylesheet corresponding to the given element and tags.
         *
         * @param el    element.
         * @param tags  tags.
         *
         * @return rule.
         */
        private IRule getRule(String el, String[] tags)
        {
            // NOTE: to further improve the performance (?) we can save the rules we get
            //       in some cache
            return stylesheet.getRule(el, tags);
        }
    }

    private static class CTreeMouseListener extends MouseAdapter
    {
        private static int checkboxWidth = new JCheckBox().getPreferredSize().width;

        /**
         * Invoked when a mouse button has been pressed on a component.
         */
        public void mousePressed(MouseEvent e)
        {
            JTree tree = (JTree)e.getSource();
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path != null)
            {
                CollectionNode node = (CollectionNode)path.getLastPathComponent();
                if (node instanceof CollectionItem && (e.getX() < tree.getPathBounds(path).x + checkboxWidth))
                {
                    node.setSelected(!node.isSelected());
                    tree.treeDidChange();
                }
            }
        }
    }
}
