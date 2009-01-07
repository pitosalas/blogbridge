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
// $Id: ListSelectionManager.java,v 1.2 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;

/**
 * Handling multiple selections in the list in human-intuitive way.
 */
public final class ListSelectionManager
{
    /**
     * Takes the parameters of list selections and old selection index and
     * returns the most appropriate selection index. This method is usually called after
     * selection changes events to get the most appropriate indexes for further selection
     * as native Swing selection model produces strange effects when adjusting multiple
     * selections in the list with CTRL and SHIFT modifiers and clicks.
     *
     * @param list              list to analyze.
     * @param oldSelectionIndex last selection index.
     *
     * @return new selection index (can be <code>-1</code>).
     */
    public static int evaluateSelectionIndex(JList list, int oldSelectionIndex)
    {
        int leadIndex = list.getLeadSelectionIndex();

        if (!list.isSelectedIndex(leadIndex))
        {
            int selectedIndex = list.getSelectedIndex();
            leadIndex = list.isSelectedIndex(oldSelectionIndex)
                ? oldSelectionIndex : selectedIndex;
        }

        return leadIndex;
    }
}
