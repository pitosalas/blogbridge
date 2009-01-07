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
// $Id: DefaultFilter.java,v 1.3 2006/12/14 09:31:30 spyromus Exp $
//

package com.salas.bb.utils.htmlparser.utils;

import com.salas.bb.utils.htmlparser.IHtmlParserListener;

/**
 * Default filter which is doing nothing except sending events to the given sub-listener.
 */
public class DefaultFilter implements IHtmlParserListener
{
    private IHtmlParserListener listener;

    /**
     * Creates filter-wrapper for give listener.
     *
     * @param listener listener to wrap.
     */
    public DefaultFilter(IHtmlParserListener listener)
    {
        this.listener = listener;
    }

    /**
     * Invoked when entity detected.
     *
     * @param entity entity name.
     * @param full   full entity text.
     */
    public void onEntity(String entity, String full)
    {
        listener.onEntity(entity, full);
    }

    /**
     * Invoked on document parsing finish.
     */
    public void onFinish()
    {
        listener.onFinish();
    }

    /**
     * Invoked on document parsing start.
     */
    public void onStart()
    {
        listener.onStart();
    }

    /**
     * Invoked when tag detected.
     *
     * @param name     name of tag.
     * @param full     full tag text.
     * @param closeTag TRUE if tag is closing tag.
     */
    public void onTag(String name, String full, boolean closeTag)
    {
        listener.onTag(name, full, closeTag);
    }

    /**
     * Invoked when text block detected.
     *
     * @param text text.
     */
    public void onText(String text)
    {
        listener.onText(text);
    }


    /**
     * Invoked to learn if some more characters required.
     *
     * @return <code>TRUE</code> to continue parsing.
     */
    public boolean needsMore()
    {
        return listener.needsMore();
    }
}