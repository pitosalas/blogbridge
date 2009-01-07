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
// $Id: DirectLoader.java,v 1.2 2006/10/16 10:12:40 spyromus Exp $
//

package com.salas.bb.views.stylesheets.loader;

import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.utils.net.URLInputStream;
import com.salas.bb.utils.net.ClientErrorException;

import javax.swing.*;
import java.net.URL;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;

/** The direct loader of icons. */
public class DirectLoader implements ILoader
{

    /**
     * Loads a stylesheet by the URL.
     *
     * @param base          base URL.
     * @param stylesheetURL stylesheet path.
     *
     * @return stylesheet.
     *
     * @throws IOException if failed loading.
     */
    public String loadStylesheet(URL base, String stylesheetURL)
        throws IOException
    {
        StringBuffer sb;

        URL url = base != null ? new URL(base, stylesheetURL) : new URL(stylesheetURL);
        URLInputStream uis = new URLInputStream(url);
        BufferedInputStream bis = new BufferedInputStream(uis);

        try
        {
            sb = new StringBuffer();
            byte[] buf = new byte[1024];
            int read;
            while ((read = bis.read(buf)) != -1)
            {
                sb.append(new String(buf, 0, read));
            }
        } catch (ClientErrorException e)
        {
            if (e.getCode() == 404)
            {
                sb = null;
            } else throw e;
        } finally
        {
            bis.close();
        }

        return sb == null ? null : sb.toString();
    }

    /**
     * Load an icon by the URL.
     *
     * @param base    base URL.
     * @param iconURL icon URL to load.
     *
     * @return icon or <code>NULL</code> if URL is <code>NULL</code>.
     *
     * @throws java.io.IOException if failed loading.
     */
    public Icon loadIcon(URL base, String iconURL)
        throws IOException
    {
        URL url = base != null ? new URL(base, iconURL) : new URL(iconURL);
        return IconSource.loadIcon(url);
    }
}
