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

import com.salas.bb.utils.uif.CheckBoxList;
import com.salas.bb.views.stylesheets.IStylesheet;
import com.salas.bb.views.stylesheets.StylesheetManager;
import com.salas.bb.views.stylesheets.domain.IRule;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Vector;
import java.awt.*;

/**
 * Collections list component.
 */
class CList extends CheckBoxList
{
    /**
     * Creates component.
     *
     * @param collection collection.
     * @param readingLists  <code>TRUE</code> if showing reading lists.
     */
    public CList(Collection collection, boolean readingLists)
    {
        setCellRenderer(new CellRenderer(StylesheetManager.getSuggestionsStylesheet(), readingLists));
        setListData(CItemCheckBox.wrap(collection));
    }

    /**
     * Collection check box.
     */
    static class CItemCheckBox extends JCheckBox implements PropertyChangeListener
    {
        private CollectionItem item;

        /**
         * Creates an initially unselected check box button with no text, no icon.
         *
         * @param item item to wrap with this component.
         */
        public CItemCheckBox(CollectionItem item)
        {
            setSelected(item.isSelected());
            setText(item.getTitle());

            this.item = item;
            item.addListener(this);
        }

        /**
         * Returns the item.
         *
         * @return the item.
         */
        public CollectionItem getItem()
        {
            return item;
        }

        /**
         * Notifies all listeners that have registered interest for
         * notification on this event type.  The event instance
         * is lazily created.
         *
         * @see javax.swing.event.EventListenerList
         */
        protected void fireStateChanged()
        {
            super.fireStateChanged();
            if (item != null) item.setSelected(isSelected());
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source
         *            and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            setSelected(item.isSelected());
        }

        /**
         * Wraps each node and all or it's leaf-sub-nodes with JCheckBox.
         *
         * @param node node to convert to list of checkboxes.
         *
         * @return check boxes.
         */
        public static Vector wrap(CollectionNode node)
        {
            Vector checkboxes = new Vector();

            if (node instanceof CollectionItem)
            {
                checkboxes.add(new CItemCheckBox((CollectionItem)node));
            } else if (node instanceof CollectionFolder)
            {
                CollectionFolder folder = (CollectionFolder)node;
                int count = folder.getChildCount();
                for (int i = 0; i < count; i++)
                {
                    checkboxes.addAll(wrap((CollectionNode)folder.getChildAt(i)));
                }
            }

            return checkboxes;
        }
    }

    /**
     * Custom renderer of check-box list cell.
     */
    private class CellRenderer extends DefaultListCellRenderer implements ListCellRenderer
    {
        private final Font defFont;
        private final IStylesheet stylesheet;
        private final boolean readingLists;

        private final JCheckBox box = new JCheckBox();
        private final JPanel panel = new JPanel();

        public CellRenderer(IStylesheet stylesheet, boolean readingLists)
        {
            this.stylesheet = stylesheet;
            this.readingLists = readingLists;

            defFont = getFont();

            CellConstraints cc = new CellConstraints();
            FormLayout layout = new FormLayout("1dlu, p, 1dlu, p:grow", "p");

            panel.setLayout(layout);
            panel.add(box, cc.xy(2, 1));
            panel.add(this, cc.xy(4, 1));

            box.setOpaque(false);
            panel.setOpaque(false);

            setIcon(null);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            CItemCheckBox cb = (CItemCheckBox)value;

            super.getListCellRendererComponent(list, cb.getText(), index, isSelected, cellHasFocus);

            box.setSelected(cb.isSelected());

            CollectionItem item = cb.getItem();

            String el = readingLists ? "folder" : "item";
            String[] tags = new String[0]; // TODO fetch tags

            panel.setBackground(isSelected ? getSelectionBackground() : getBackground());
            box.setBackground(isSelected ? getSelectionBackground() : getBackground());

            IRule rule = stylesheet.getRule(el, tags);
            if (rule != null)
            {
                // Update font
                Font fnt = rule.getFont();
                boolean bold = fnt != null && fnt.isBold();
                if (defFont.isBold() != bold) setFont(defFont.deriveFont(bold ? Font.BOLD : Font.PLAIN));

                // Update color
                if (!isSelected)
                {
                    Color color = rule.getColor();
                    if (color == null) color = Color.BLACK;
                    setForeground(color);
                }

                // Assign icon
                Icon icon = rule.getIcon();
                setIcon(icon);
            }

//            checkbox.setFocusPainted(false);
//            checkbox.setBorderPainted(true);

            return panel;
        }
    }
}
