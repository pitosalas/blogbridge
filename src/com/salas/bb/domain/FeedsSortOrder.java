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
// $Id: FeedsSortOrder.java,v 1.5 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.i18n.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * Order of feeds sorting.
 */
public interface FeedsSortOrder
{
    /**
     * Rating based sorting.
     */
    int RATING = 1;

    /**
     * Read status based sorting. Unread feeds go first.
     */
    int READ = 2;

    /**
     * Alphabet based sorting.
     */
    int ALPHABETICAL = 3;

    /**
     * Invalid feeds. Valid feeds go first.
     */
    int INVALIDNESS = 12;

    /**
     * Vists count based sorting. Most visited go first.
     */
    int VISITS = 17;

    /**
     * The class ID to its name
     */
    Map SORTING_CLASS_NAMES = new HashMap()
    {
        {
            put(new Integer(RATING), Strings.message("userprefs.tab.feeds.sort.high.low.starz"));
            put(new Integer(ALPHABETICAL), Strings.message("userprefs.tab.feeds.sort.alphabetical.order"));
            put(new Integer(READ), Strings.message("userprefs.tab.feeds.sort.unread.read"));
            put(new Integer(INVALIDNESS), Strings.message("userprefs.tab.feeds.sort.valid.invalid"));
            put(new Integer(VISITS), Strings.message("userprefs.tab.feeds.sort.more.less.visits"));
        }
    };
}
