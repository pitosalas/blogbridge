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
// $Id: XMLFormatDetector.java,v 1.3 2006/11/28 11:05:27 spyromus Exp $
//

package com.salas.bb.utils.discovery.detector;

import com.salas.bb.utils.net.URLInputStream;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * XML Format Detector.
 */
public class XMLFormatDetector
{
    private static final int STATE_BEGINNING        = 0;
    private static final int STATE_TAG_BEGINNING    = 1;
    private static final int STATE_COMMENT          = 2;
    private static final int STATE_XML_INSTR        = 3;

    private byte[] buf;
    private int bufPos;

    /**
     * Creates detector.
     */
    public XMLFormatDetector()
    {
        buf = new byte[4];
    }

    /**
     * Makes an attempt to read first bytes of the resource to understand its format.
     *
     * @param url url.
     *
     * @return the format or <code>NULL</code>.
     *
     * @throws java.io.IOException in case of I/O error.
     */
    public XMLFormat detect(URL url)
        throws IOException
    {
        return detect(new URLInputStream(url));
    }

    /**
     * Makes an attempt to read first bytes of the resource to understand its format.
     *
     * @param in input stream.
     *
     * @return the format or <code>NULL</code>.
     *
     * @throws java.io.IOException in case of I/O error.
     */
    public XMLFormat detect(InputStream in)
        throws IOException
    {
        int state = STATE_BEGINNING;
        bufPos = 0;
        XMLFormat fmt = null;

        int b;
        boolean finished = false;
        while (!finished && (b = in.read()) != -1)
        {
            switch (state)
            {
                case STATE_BEGINNING:
                    if (isSpace(b)) continue; else
                    if (b == '<') state = STATE_TAG_BEGINNING;
                    break;

                case STATE_TAG_BEGINNING:
                    if (b == '?') state = STATE_XML_INSTR; else
                    if (b == '!')
                    {
                        if (in.read() == '-' && in.read() == '-') state = STATE_COMMENT;
                        else state = STATE_XML_INSTR;
                    } else if ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z')) append(b); else
                    if (b == ':') bufPos = 0; else
                    {
                        fmt = parseFormat();
                        finished = true;
                    }
                    break;

                case STATE_XML_INSTR:
                    if (b == '>') state = STATE_BEGINNING;
                    break;

                case STATE_COMMENT:
                    if (b == '-' && in.read() == '-' && in.read() == '>') state = STATE_BEGINNING;
                    break;

                default:
                    break;
            }
        }

        in.close();

        return fmt;
    }

    /**
     * Returns <code>TRUE</code> if the character is space.
     *
     * @param b character code.
     *
     * @return <code>TRUE</code> if the character is space.
     */
    private static boolean isSpace(int b)
    {
        return ' ' == b || '\n' == b;
    }

    /**
     * Appends a character and increments the pointer.
     *
     * @param b character.
     */
    private void append(int b)
    {
        if (bufPos < buf.length) buf[bufPos++] = (byte)b;
    }

    /**
     * Parses the tag name saved in the buffer and returns the format.
     *
     * @return format or <code>NULL</code> if unknown.
     */
    private XMLFormat parseFormat()
    {
        XMLFormat fmt = null;

        String str = new String(buf, 0, bufPos).toLowerCase();
        if ("rss".equals(str)) fmt = XMLFormat.RSS; else
        if ("rdf".equals(str)) fmt = XMLFormat.RDF; else
        if ("feed".equals(str)) fmt = XMLFormat.ATOM; else
        if ("opml".equals(str)) fmt = XMLFormat.OPML;

        return fmt;
    }

    /**
     * Makes attempt to detect the format, and asks for clarifications if not able.
     *
     * @param url       URL to discover.
     * @param parent    parent frame.
     *
     * @return the format.
     */
    public static XMLFormat detectOrAskFormat(URL url, Component parent)
    {
        XMLFormatDetector detector = new XMLFormatDetector();
        XMLFormat fmt;

        try
        {
            fmt = detector.detect(url);
        } catch (IOException e)
        {
            fmt = chooseFormat(parent);
        }

        return fmt;
    }

    /**
     * Shows the modal dialog with the format selector.
     *
     * @param parent    parent dialog.
     *
     * @return the format.
     */
    public static XMLFormat chooseFormat(Component parent)
    {
        XMLFormat fmt = null;

        String dataFeed = "Data Feed";
        String rl = "Reading List";
        Object[] options = { dataFeed, rl };

        // TODO: Localize
        int res = JOptionPane.showOptionDialog(parent,
            "<html><b>BlogBridge is unable to access the resource.</b>\n\n" +
                "Please suggest the format:", "BlogBridge",
            JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, dataFeed);

        if (res != JOptionPane.CLOSED_OPTION)
        {
            fmt = res == 0 ? XMLFormat.ATOM : XMLFormat.OPML; 
        }

        return fmt;
    }
}
