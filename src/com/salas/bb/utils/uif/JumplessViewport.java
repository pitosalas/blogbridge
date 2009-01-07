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
// $Id: JumplessViewport.java,v 1.5 2007/07/19 11:02:30 spyromus Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;

/**
 * A viewport component that saves the position relative to items of the list
 * in the view.
 */
public class JumplessViewport extends JViewport
{
    private JumplessScrollPosition sp;

    /**
     * Creates a viewport.
     */
    public JumplessViewport()
    {
        // Either BACKINGSTORE_SCROLL_MODE or SIMPLE_SCROLL_MODE is
        // suitable for flicker-less scrolling
        setScrollMode(BACKINGSTORE_SCROLL_MODE);
    }

    @Override
    protected ViewListener createViewListener()
    {
        return new CustomViewListener();
    }

    @Override
    protected LayoutManager createLayoutManager()
    {
        return new CustomViewportLayout();
    }

    @Override
    public void setViewPosition(Point p)
    {
        // Calculate current position in relative to objects in the list
        // Get the list component
        Point pos = getViewPosition();
        Component view = getView();
        if (view != null)
        {
            Component list = view.getComponentAt(pos);
            if (list != null)
            {
                // Get the list item under the given coordinates
                Component panel = list.getComponentAt(pos);

                // Convert coordinates into the item space
                Point panelPos = SwingUtilities.convertPoint(list, pos, panel);
                int panelHeight = panel.getHeight();

                // Calculate the offset relative to the head or the tail of the item
                boolean head = true;
                int offset = panelPos.y;
                if (offset > panelHeight / 2)
                {
                    head = false;
                    offset = panelHeight - offset;
                }

                // Record it for the future reference
                setStoredPosition(panel, head, offset);
            }
        }
        super.setViewPosition(p);
    }

    /**
     * Sets the stored position.
     *
     * @param panel     panel.
     * @param head      <code>TRUE</code> to count offset from the head.
     * @param offset    number of pixels to offset from the top or bottom.
     */
    public void setStoredPosition(Component panel, boolean head, int offset)
    {
        sp = new JumplessScrollPosition(panel, head, offset);
    }

    /**
     * Forgets the stored position. Useful when changing the content at once.
     */
    public void resetStoredPosition()
    {
        sp = null;
    }

    /**
     * Resize listener that updates the viewing position so that
     * no jumps are visible.
     */
    private class CustomViewListener extends ViewListener
    {
        @Override
        public void componentResized(ComponentEvent e)
        {
            // There's nothing here intentionally
        }
    }

    /**
     * View port layout that omits recalculation of y-coordinate of the view window.
     */
    private class CustomViewportLayout extends ViewportLayout
    {
        private int oldHeight = 0;

        @Override
        public void layoutContainer(Container parent)
        {
            JViewport vp = (JViewport)parent;
            Component view = vp.getView();
            Scrollable scrollableView = null;

            if (view == null) return;
            if (view instanceof Scrollable) scrollableView = (Scrollable)view;

            view.validate();

            Dimension viewPrefSize = view.getPreferredSize();
            Dimension vpSize = vp.getSize();
            Dimension extentSize = vp.toViewCoordinates(vpSize);
            Dimension viewSize = new Dimension(viewPrefSize);

            if (scrollableView != null)
            {
                if (scrollableView.getScrollableTracksViewportWidth())
                {
                    viewSize.width = vpSize.width;
                }
                if (scrollableView.getScrollableTracksViewportHeight())
                {
                    viewSize.height = vpSize.height;
                }
            }

            Point viewPosition = vp.getViewPosition();

            if (scrollableView == null ||
                vp.getParent() == null ||
                vp.getParent().getComponentOrientation().isLeftToRight())
            {
                if ((viewPosition.x + extentSize.width) > viewSize.width)
                {
                    viewPosition.x = Math.max(0, viewSize.width - extentSize.width);
                }
            } else
            {
                if (extentSize.width > viewSize.width)
                {
                    viewPosition.x = viewSize.width - extentSize.width;
                } else
                {
                    viewPosition.x = Math.max(0, Math.min(viewSize.width - extentSize.width, viewPosition.x));
                }
            }

            // Update y-coord
            if (sp != null && viewPrefSize.height != oldHeight)
            {
                // Find the coordinate of the saved position in the new layout
                Component list = view.getComponentAt(0, 0);
                if (list != null)
                {
                    Component panel = sp.getAnchor();
                    int offset = sp.isHead() ? sp.getYOffset() : panel.getHeight() - sp.getYOffset();
                    Point point = SwingUtilities.convertPoint(panel, 0, offset, list);

                    // See if there's a blank space going to be visible after the last
                    // panel, and make so that it isn't
                    if ((point.y + extentSize.height) > viewSize.height)
                    {
                        point.y = Math.max(0, viewSize.height - extentSize.height);
                    }

                    // Change the position
                    viewPosition.y = point.y;
                }
            }

            if (scrollableView == null)
            {
                if ((viewPosition.x == 0) && (vpSize.width > viewPrefSize.width))
                {
                    viewSize.width = vpSize.width;
                }
                if ((viewPosition.y == 0) && (vpSize.height > viewPrefSize.height))
                {
                    viewSize.height = vpSize.height;
                }
            }

            oldHeight = viewPrefSize.height;

            vp.setViewPosition(viewPosition);
            vp.setViewSize(viewSize);
        }
    }
}
