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
// $Id: Outputer.java,v 1.3 2006/05/29 12:48:37 spyromus Exp $
//

package com.salas.bb.utils.opml;

import org.jdom.output.XMLOutputter;
import org.jdom.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.salas.bb.utils.i18n.Strings;

/**
 * Outputs XML to different destinations.
 */
public final class Outputer
{
    private static final Logger LOG = Logger.getLogger(Outputer.class.getName());

    /**
     * Hidden utility class constructor.
     */
    private Outputer()
    {
    }

    /**
     * Writes XML document to file.
     *
     * @param doc       document.
     * @param filename  name of target file.
     */
    public static void writeDocumentToFile(Document doc, String filename)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            XMLOutputter xo = new XMLOutputter();
            xo.output(doc, fos);
            fos.close();
        } catch (IOException e)
        {
            LOG.log(Level.SEVERE, Strings.error("failed.to.export.guide.data"), e);
        }
    }
}
