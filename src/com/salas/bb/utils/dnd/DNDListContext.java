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
// $Id: DNDListContext.java,v 1.7 2006/05/31 11:28:31 spyromus Exp $
//

package com.salas.bb.utils.dnd;

import com.salas.bb.utils.i18n.Strings;

import java.util.logging.Logger;

/**
 * Context of all dragging operations with {@link DNDList}'s.
 */
public class DNDListContext
{
    private static final Logger LOG = Logger.getLogger(DNDListContext.class.getName());

    /** <code>TRUE</code> when dragging is in progress. */
    private static boolean      dragging;

    /** Source list of dragging operation. */
    private static DNDList      source;

    /** What is being dragged. */
    private static IDNDObject   object;

    /**
     * If some object recognizes itself as destination it can register here to let the
     * processor of dragging finish even know where to deliver items.
     */
    private static Object       destination;

    /** <code>TRUE</code> when copying item, <code>FALSE</code> when moving. */
    private static boolean      copying;
    /** <code>TRUE</code> when was copying when dragging finished. */
    private static boolean      finishedCopying;

    /**
     * Registers objects corresponding to dragging start.
     *
     * @param aSource    source of event.
     * @param dndObject object being dragged somewhere.
     */
    public static synchronized void startDragging(DNDList aSource, IDNDObject dndObject)
    {
        if (dragging) LOG.severe(Strings.error("dnd.already.dragging"));

        source = aSource;
        object = dndObject;
        dragging = true;
    }

    /**
     * Places mark that dragging is no longer in progress but leaves all information
     * intouched so that everyone can take it and process.
     */
    public static synchronized void finishDragging()
    {
        dragging = false;
        finishedCopying = source != null && source.isCopyingAllowed() && copying;
    }

    /**
     * Returns <code>TRUE</code> when dragging is currently happening.
     *
     * @return <code>TRUE</code> when dragging is currently happening.
     */
    public static boolean isDragging()
    {
        return dragging;
    }

    /**
     * Returns the source of dragging.
     *
     * @return the source of dragging.
     */
    public static DNDList getSource()
    {
        return source;
    }

    /**
     * Returns the object being dragged from the source somewhere.
     *
     * @return theobject being dragged.
     */
    public static IDNDObject getObject()
    {
        return object;
    }

    /**
     * Returns destination object.
     *
     * @return destination object.
     */
    public static Object getDestination()
    {
        return destination;
    }

    /**
     * Registers destination object.
     *
     * @param aDestination destination.
     */
    public static void setDestination(Object aDestination)
    {
        destination = aDestination;
    }

    /**
     * Sets the flag of moving / copying.
     *
     * @param flag flag.
     */
    public static void setCopying(boolean flag)
    {
        copying = flag;

        if (source != null && source.isCopyingAllowed())
        {
            source.copyModeStateChanged(flag);
        }
    }

    /**
     * Returns <code>TRUE</code> if copying.
     *
     * @return copying.
     */
    public static boolean isCopying()
    {
        return copying;
    }

    /**
     * Returns <code>TRUE</code> if was copying when dragging finished.
     *
     * @return <code>TRUE</code> if was copying when dragging finished.
     */
    public static boolean isFinishedCopying()
    {
        return finishedCopying;
    }
}
