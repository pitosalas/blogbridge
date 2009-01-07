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
// $Id: DelegatingMouseListener.java,v 1.4 2007/06/12 10:35:33 spyromus Exp $
//

package com.salas.bb.utils.uif;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Mouse listener which is delegating the processing of all events to the parent.
 */
public class DelegatingMouseListener extends MouseAdapter
{
    private final Component component;
    private final boolean   directDelegation;
    private final int       parentCount;

    /**
     * Creates listener.
     *
     * @param aComponent component listener should be created for.
     */
    public DelegatingMouseListener(Component aComponent)
    {
        this(aComponent, false);
    }

    /**
     * Creates listener.
     *
     * @param aComponent component listener should be created for.
     * @param aDirectDelegation <code>TRUE</code> to not convert event to parent's coordinate space.
     */
    public DelegatingMouseListener(Component aComponent, boolean aDirectDelegation)
    {
        this(aComponent, aDirectDelegation, 1);
    }

    /**
     * Creates listener.
     *
     * @param aComponent component listener should be created for.
     * @param aDirectDelegation <code>TRUE</code> to not convert event to parent's coordinate space.
     * @param aParentCount how many parents up hierarchy to look for.
     */
    public DelegatingMouseListener(Component aComponent, boolean aDirectDelegation, int aParentCount)
    {
        component = aComponent;
        directDelegation = aDirectDelegation;
        parentCount = aParentCount;
    }

    /** Invoked when a mouse button has been pressed on a component. */
    public void mousePressed(MouseEvent e)
    {
        delegateToParent(e);
    }

    /** Invoked when a mouse button has been released on a component. */
    public void mouseReleased(MouseEvent e)
    {
        delegateToParent(e);
    }

    /** Invoked when the mouse has been clicked on a component. */
    public void mouseClicked(MouseEvent e)
    {
        delegateToParent(e);
    }

    /**
     * Delegating the event to the parent.
     *
     * @param e event.
     */
    private void delegateToParent(MouseEvent e)
    {
        Component parent = findParent();
        if (parent != null) UifUtilities.delegateEventToParent(component, parent, e, directDelegation);
    }

    /**
     * Find a parent of the component this listener was created for. This
     * method takes the <code>parentCount</code>'th parent and reports.
     *
     * @return parent.
     */
    private Component findParent()
    {
        if (component == null) return null;

        Container parent = null;
        Component comp = component;

        // Look for the parent
        for (int i = 0; i < parentCount; i++)
        {
            // Get the parent of the component and return if it's NULL
            parent = comp.getParent();
            if (parent == null) return null;

            // Prepare for the next loop
            comp = parent;
        }

        return parent;
    }
}
