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
// $Id: InsertLinkBoxItemListener.java,v 1.2 2006/12/06 11:10:06 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * Listener for insert-link actions.
 */
class InsertLinkBoxItemListener implements ItemListener
{
    private final JEditorPane editor;

    /**
     * Creates listener.
     *
     * @param editor editor to manipulate.
     */
    public InsertLinkBoxItemListener(JEditorPane editor)
    {
        this.editor = editor;
    }

    /**
     * Invoked when user selects some item from the list.
     */
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.DESELECTED) return;

        JComboBox box = (JComboBox)e.getSource();
        Object item = box.getSelectedItem();

        if (item instanceof Action)
        {
            ((Action)item).actionPerformed(null);
            box.setSelectedIndex(0);
            editor.requestFocus();
        }
    }
}
