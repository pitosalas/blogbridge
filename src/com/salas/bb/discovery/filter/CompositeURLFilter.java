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
// $Id $
//

package com.salas.bb.discovery.filter;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * URL filter that is composed from several other. It returns the match
 * if any of the child filters report the match.
 */
public class CompositeURLFilter implements IURLFilter
{
    private List filters = new ArrayList();

    /**
     * Adds another filter to the list.
     *
     * @param filter filter.
     */
    public void addFilter(IURLFilter filter)
    {
        if (filter != null && !filters.contains(filter)) filters.add(filter);
    }

    /**
     * Checks if the URL matches the filter.
     *
     * @param url URL to check.
     *
     * @return <code>TRUE</code> if the url matches filter.
     */
    public boolean matches(URL url)
    {
        boolean matches = false;

        for (int i = 0; !matches && i < filters.size(); i++)
        {
            IURLFilter filter = (IURLFilter)filters.get(i);
            matches = filter.matches(url);
        }

        return matches;
    }
}
