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
// $Id: StringBuilderListener.java,v 1.4 2006/12/14 09:31:30 spyromus Exp $
//

package com.salas.bb.utils.htmlparser.utils;

import com.salas.bb.utils.htmlparser.IHtmlParserListener;

/**
 * Listener which is building a string from the pieces provided by <code>HtmlParser</code> events.
 */
public class StringBuilderListener implements IHtmlParserListener
{
    private StringBuffer buffer;
    private int sizeLimit;

    /**
     * Creates builder with default size and output limit.
     *
     * @param initialSize   initial size of buffer.
     * @param sizeLimit     max number of chars - 3 to get in output or -1.
     */
    public StringBuilderListener(int initialSize, int sizeLimit)
    {
        buffer = new StringBuffer(initialSize);
        this.sizeLimit = sizeLimit;
    }

    /**
     * Invoked on document parsing start.
     */
    public void onStart()
    {
    }

    /**
     * Invoked on document parsing finish.
     */
    public void onFinish()
    {
    }

    /**
     * Invoked when text block detected.
     *
     * @param text text.
     */
    public void onText(String text)
    {
        if (text != null) buffer.append(text);
    }

    /**
     * Invoked when tag detected.
     *
     * @param name      name of tag.
     * @param full      full tag text.
     * @param closeTag  TRUE if tag is closing tag.
     */
    public void onTag(String name, String full, boolean closeTag)
    {
        if (full != null) buffer.append(full);
    }

    /**
     * Invoked when entity detected.
     *
     * @param entity entity name.
     * @param full   full entity text.
     */
    public void onEntity(String entity, String full)
    {
        if (full != null) buffer.append(full);
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        String result = null;

        if (buffer != null)
        {
            result = buffer.toString().trim();
            if (sizeLimit > -1 && result.length() > sizeLimit)
            {
                result = result.substring(0, sizeLimit) + "...";
            }
        }

        return result;
    }


    /**
     * Invoked to learn if some more characters required.
     *
     * @return <code>TRUE</code> to continue parsing.
     */
    public boolean needsMore()
    {
        return sizeLimit < 0 || buffer.length() < sizeLimit;
    }
}
