// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: DelegatingScrollablePanel.java,v 1.1 2007/07/12 16:32:44 spyromus Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that delegates all Scrollable calls to some other component.
 */
public class DelegatingScrollablePanel extends JPanel implements Scrollable
{
    private final Scrollable scrollable;

    /**
     * Creates new panel.
     *  
     * @param scrollable scrollable to delegate calls to.
     */
    public DelegatingScrollablePanel(Scrollable scrollable)
    {
        this.scrollable = scrollable;
    }

    /**
     * Create a new buffered JPanel with the specified layout manager
     *
     * @param layout the LayoutManager to use
     * @param scrollable scrollable to delegate calls to.
     */
    public DelegatingScrollablePanel(LayoutManager layout, Scrollable scrollable)
    {
        super(layout);
        this.scrollable = scrollable;
    }

    /**
     * Returns the preferred size of the viewport for a view component. For example, the preferred size of a
     * <code>JList</code> component is the size required to accommodate all of the cells in its list. However, the value
     * of <code>preferredScrollableViewportSize</code> is the size required for <code>JList.getVisibleRowCount</code>
     * rows. A component without any properties that would affect the viewport size should just return
     * <code>getPreferredSize</code> here.
     *
     * @return the preferredSize of a <code>JViewport</code> whose view is this <code>Scrollable</code>
     *
     * @see javax.swing.JViewport#getPreferredSize
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        return scrollable.getPreferredScrollableViewportSize();
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment that will completely expose
     * one new row or column, depending on the value of orientation.  Ideally, components should handle a partially
     * exposed row or column by returning the distance required to completely expose the item.
     * <p/>
     * Scrolling containers, like JScrollPane, will use this method each time the user requests a unit scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
     *
     * @return The "unit" increment for scrolling in the specified direction. This value should always be positive.
     *
     * @see javax.swing.JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return scrollable.getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment that will completely expose
     * one block of rows or columns, depending on the value of orientation.
     * <p/>
     * Scrolling containers, like JScrollPane, will use this method each time the user requests a block scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
     *
     * @return The "block" increment for scrolling in the specified direction. This value should always be positive.
     *
     * @see javax.swing.JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return scrollable.getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    /**
     * Return true if a viewport should always force the width of this <code>Scrollable</code> to match the width of the
     * viewport. For example a normal text view that supported line wrapping would return true here, since it would be
     * undesirable for wrapped lines to disappear beyond the right edge of the viewport.  Note that returning true for a
     * Scrollable whose ancestor is a JScrollPane effectively disables horizontal scrolling.
     * <p/>
     * Scrolling containers, like JViewport, will use this method each time they are validated.
     *
     * @return True if a viewport should force the Scrollables width to match its own.
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return scrollable.getScrollableTracksViewportWidth();
    }

    /**
     * Return true if a viewport should always force the height of this Scrollable to match the height of the viewport.
     * For example a columnar text view that flowed text in left to right columns could effectively disable vertical
     * scrolling by returning true here.
     * <p/>
     * Scrolling containers, like JViewport, will use this method each time they are validated.
     *
     * @return True if a viewport should force the Scrollables height to match its own.
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return scrollable.getScrollableTracksViewportHeight();
    }
}
