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
// $Id: GuidesSetAdapter.java,v 1.4 2007/10/04 09:55:08 spyromus Exp $
//

package com.salas.bb.domain;

/**
 * Default empty implementation of all listener methods.
 */
public class GuidesSetAdapter implements IGuidesSetListener
{
    /**
     * Invoked when new guide has been added to the set.
     *
     * @param set           guides set.
     * @param guide         added guide.
     * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
     */
    public void guideAdded(GuidesSet set, IGuide guide, boolean lastInBatch)
    {
    }

    /**
     * Invoked when the guide has been moved to a new location in list.
     *
     * @param set      guides set.
     * @param guide    guide which has been removed.
     * @param oldIndex old guide index.
     * @param newIndex new guide index.
     */
    public void guideMoved(GuidesSet set, IGuide guide, int oldIndex, int newIndex)
    {
    }

    /**
     * Invoked when the guide has been removed from the set.
     *
     * @param set   guides set.
     * @param guide removed guide.
     * @param index old guide index.
     */
    public void guideRemoved(GuidesSet set, IGuide guide, int index)
    {
    }
}
