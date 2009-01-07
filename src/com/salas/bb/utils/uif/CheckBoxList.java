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
// $Id: CheckBoxList.java,v 1.6 2007/03/02 15:24:34 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Simple list accepting list of checkboxes.
 */
public class CheckBoxList extends JList
{
    private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    /**
     * Creates list.
     */
    public CheckBoxList()
    {
        setCellRenderer(new CellRenderer());

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        if (e.getID() == MouseEvent.MOUSE_PRESSED)
        {
            int index = locationToIndex(e.getPoint());

            if (index != -1 && e.getPoint().getX() < 16)
            {
                JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                toggleItem(checkbox);
            }
        }
    }

    @Override
    protected void processKeyEvent(KeyEvent e)
    {
        super.processKeyEvent(e);
        if (e.getKeyCode() == 32 && e.getID() == KeyEvent.KEY_PRESSED)
        {
            JCheckBox checkbox = (JCheckBox)getSelectedValue();
            toggleItem(checkbox);
        }
    }

    private void toggleItem(JCheckBox checkbox)
    {
        if (checkbox == null) return;
        
        checkbox.setSelected(!checkbox.isSelected());
        repaint();
    }

    /**
     * Marks all items as (un)selected.
     *
     * @param selected <code>TRUE</code> to select.
     */
    public void selectAll(boolean selected)
    {
        ListModel model = getModel();
        for (int i = 0; i < model.getSize(); i++)
        {
            ((JCheckBox)model.getElementAt(i)).setSelected(selected);
        }
    }

    /**
     * Custom renderer of check-box list cell.
     */
    private class CellRenderer implements ListCellRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            JCheckBox checkbox = (JCheckBox)value;
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected
                ? UIManager.getBorder("List.focusCellHighlightBorder") : NO_FOCUS_BORDER);

            return checkbox;
        }
    }

    /**
     * Creates a simple panel with two buttons: "All" and "None".
     *
     * @param list  list to operate.
     *
     * @return panel.
     */
    public static JPanel createAllNonePanel(CheckBoxList list)
    {
        JButton btnAll = new JButton(new SelectAllAction(list, true,
            Strings.message("ui.checkbox.list.all")));
        JButton btnNone = new JButton(new SelectAllAction(list, false,
            Strings.message("ui.checkbox.list.none")));

        JPanel panel = new JPanel();
        panel.add(btnAll);
        panel.add(btnNone);

        return panel;
    }

    /**
     * Action for selecting and unselecting items of the list.
     */
    public static class SelectAllAction extends AbstractAction
    {
        private final CheckBoxList list;
        private final boolean select;

        /**
         * Creates action,
         *
         * @param aList     list to operate.
         * @param aSelect   <code>TRUE</code> to select items.
         * @param aLabel    action label.
         */
        public SelectAllAction(CheckBoxList aList, boolean aSelect, String aLabel)
        {
            super(aLabel);

            list = aList;
            select = aSelect;
        }

        /**
         * Invoked when action happens.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            list.selectAll(select);
            list.repaint();
        }
    }
}