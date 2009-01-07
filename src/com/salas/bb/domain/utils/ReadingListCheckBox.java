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
// $Id: ReadingListCheckBox.java,v 1.3 2006/01/09 08:48:15 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A checkbox with a reading list associated.
 */
public class ReadingListCheckBox extends JCheckBox
{
    private final ReadingList list;

    /**
     * Creates check box.
     *
     * @param aList list to associate.
     */
    public ReadingListCheckBox(ReadingList aList)
    {
        StandardGuide guide = aList.getParentGuide();
        String label = aList.getTitle();
        if (guide != null) label += " (" + guide.getTitle() + ")";
        setText(label);

        list = aList;
    }

    /**
     * Returns the feed.
     *
     * @return feed.
     */
    public ReadingList getList()
    {
        return list;
    }

    /**
     * Wraps each feed on the list of feeds with check box object.
     *
     * @param aLists feeds.
     *
     * @return check boxes.
     */
    public static ReadingListCheckBox[] wrap(List aLists)
    {
        ReadingListCheckBox[] boxes = new ReadingListCheckBox[aLists.size()];
        for (int i = 0; i < aLists.size(); i++)
        {
            ReadingList list = (ReadingList)aLists.get(i);
            boxes[i] = new ReadingListCheckBox(list);
            boxes[i].setSelected(true);
        }

        return boxes;
    }

    /**
     * Scans the model for selected reading lists and returns them.
     *
     * @param model list model.
     *
     * @return selected reading lists.
     */
    public static List getSelected(ListModel model)
    {
        List lists = new ArrayList();
        for (int i = 0; i < model.getSize(); i++)
        {
            Object item = model.getElementAt(i);
            if (item instanceof ReadingListCheckBox && ((ReadingListCheckBox)item).isSelected())
            {
                lists.add(((ReadingListCheckBox)item).getList());
            }
        }

        return lists;
    }

    /**
     * Returns the tooltip string that has been set with <code>setToolTipText</code>.
     *
     * @return the text of the tool tip
     *
     * @see #TOOL_TIP_TEXT_KEY
     */
    public String getToolTipText()
    {
        return list == null ? null : list.getURL().toString();
    }
}
