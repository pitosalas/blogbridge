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
// $Id$
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.StringUtils;

import java.io.IOException;

/**
 * Utility class that takes a link and returns the shortened version of it.
 */
public class LinkShortener
{
    /**
     * Shortens the link.
     *
     * @param link link to shorten.
     *
     * @return shortened version.
     *
     * @throws LinkShorteningException if there was a problem.
     */
    public static String process(String link)
        throws LinkShorteningException
    {
        // Shortcut
        if (StringUtils.isEmpty(link)) return link;

        try
        {
            String result = HttpClient.get("http://is.gd/api.php?longurl=" + StringUtils.escape(link));
            if (result.length() < link.length()) link = result;
        } catch (IOException e)
        {
            throw new LinkShorteningException("Link is not valid or cannot be shortened");
        }

        return link;
    }
}
