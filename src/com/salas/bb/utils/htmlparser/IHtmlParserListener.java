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
// $Id: IHtmlParserListener.java,v 1.3 2006/12/14 09:31:30 spyromus Exp $
//

package com.salas.bb.utils.htmlparser;

/**
 * Listener of <code>HtmlParser</code> events.
 */
public interface IHtmlParserListener
{
    /**
     * Invoked on document parsing start.
     */
    void onStart();

    /**
     * Invoked on document parsing finish.
     */
    void onFinish();

    /**
     * Invoked when tag detected.
     *
     * @param name      name of tag.
     * @param full      full tag text.
     * @param closeTag  TRUE if tag is closing tag.
     */
    void onTag(String name, String full, boolean closeTag);

    /**
     * Invoked when text block detected.
     *
     * @param text  text.
     */
    void onText(String text);

    /**
     * Invoked when entity detected.
     *
     * @param entity    entity name.
     * @param full      full entity text.
     */
    void onEntity(String entity, String full);

    /**
     * Invoked to learn if some more characters required.
     *
     * @return <code>TRUE</code> to continue parsing.
     */
    boolean needsMore();
}
