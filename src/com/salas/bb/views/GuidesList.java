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
// $Id: GuidesList.java,v 1.17 2007/09/07 13:15:19 spyromus Exp $
//

package com.salas.bb.views;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.dnd.DNDList;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

/**
 * List of channel guides providing tool tips with full guide names for each guide.
 */
public class GuidesList extends DNDList implements PropertyChangeListener
{
    /**
     * Creates list with specified data model.
     *
     * @param dataModel model to use.
     */
    public GuidesList(ListModel dataModel)
    {
        super(dataModel, false);
    }

    /**
     * Returns full guide name when mouse hovers the cell.
     *
     * @param event event object.
     *
     * @return the name of guide.
     */
    public String getToolTipText(MouseEvent event)
    {
        String tooltip = null;

        int index = locationToIndex(event.getPoint());
        if (index > -1 && getCellBounds(index, index).contains(event.getPoint()))
        {
            IGuide guide = (IGuide)getModel().getElementAt(index);
            tooltip = "<html><b>" + guide.getTitle();
            int count = GlobalModel.SINGLETON.getUnreadArticlesCount(guide);
            if (count > 0) tooltip += "</b><br>" +
                MessageFormat.format(Strings.message("panel.guides.unread.0.articles"), count);
        }

        return tooltip;
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source and the property that
     *            has changed.
     */
    public void propertyChange(final PropertyChangeEvent evt)
    {
        // This is the receiver of dragging events from other lists (feeds list)

        // When we receive this event we understand that mouse is being moved and
        // it is probably over this component. On condition that it is, we should
        // find a point and highlight the hovered cell.

        MouseEvent e = (MouseEvent)evt.getNewValue();
        Point dragPoint = (Point)e.getPoint().clone();
        SwingUtilities.convertPointToScreen(dragPoint, e.getComponent());
        SwingUtilities.convertPointFromScreen(dragPoint, this);
        if (contains(dragPoint))
        {
            int row = locationToIndex(dragPoint);
            if (row != -1) setSelectedIndex(row);
        } else clearSelection();
    }
}