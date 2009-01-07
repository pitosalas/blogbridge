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
// $Id: GuidesUtils.java,v 1.3 2006/01/08 04:48:16 kyank Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.IGuide;

import java.util.List;
import java.util.ArrayList;

/**
 * Set of guides utilities.
 */
public final class GuidesUtils
{
    /**
     * Hidden utility class constructor.
     */
    private GuidesUtils()
    {
    }

    /**
     * Removes the guides we mention from the guides list.
     *
     * @param allGuides         all guides we have.
     * @param guidesToFilter    guides we need not have in the list.
     *
     * @return list of all guides without those we mentioned.
     */
    public static StandardGuide[] filterGuides(StandardGuide[] allGuides,
        IGuide[] guidesToFilter)
    {
        List guidesLeft = new ArrayList(allGuides.length - guidesToFilter.length);

        for (int i = 0; i < allGuides.length; i++)
        {
            StandardGuide guide = allGuides[i];
            if (!contains(guide, guidesToFilter)) guidesLeft.add(guide);
        }

        return (StandardGuide[])guidesLeft.toArray(new StandardGuide[guidesLeft.size()]);
    }

    /**
     * Returns <code>TRUE</code> if guides list contains the given guide.
     *
     * @param aGuide    guide to look for.
     * @param aGuides   list of guides to look at.
     *
     * @return <code>TRUE</code> if guides list contains the given guide.
     */
    public static boolean contains(IGuide aGuide, IGuide[] aGuides)
    {
        boolean contains = false;

        for (int i = 0; !contains && i < aGuides.length; i++)
        {
            contains = aGuide == aGuides[i];
        }

        return contains;
    }

    /**
     * Returns the concatenated list of guides names.
     *
     * @param aGuides   guides.
     *
     * @return comma-separated list of names.
     */
    public static String getGuidesNames(IGuide[] aGuides)
    {
        String names = null;

        if (aGuides != null && aGuides.length > 0)
        {
            StringBuffer namesBuf = new StringBuffer();
            namesBuf.append(aGuides[0].getTitle());
            for (int i = 1; i < aGuides.length; i++)
            {
                namesBuf.append(",").append(aGuides[i].getTitle());
            }

            names = namesBuf.toString();
        }

        return names;
    }
}
