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
// $Id: AmazonImageDetails.java,v 1.2 2006/01/08 05:00:08 kyank Exp $
//

package com.salas.bb.utils.amazon;

import java.net.URL;

/**
 * This object holds all image details including link to image and its dimensions.
 */
public class AmazonImageDetails
{
    private URL     url;
    private int     width;
    private int     height;

    /**
     * Creates details object.
     *
     * @param url       link to image on the web.
     * @param width     width of image.
     * @param height    height of image.
     */
    public AmazonImageDetails(URL url, int width, int height)
    {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns link to image.
     *
     * @return link.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Returns image width.
     *
     * @return width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Returns image height.
     *
     * @return height.
     */
    public int getHeight()
    {
        return height;
    }
}
