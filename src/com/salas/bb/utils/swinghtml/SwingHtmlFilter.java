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
// $Id: SwingHtmlFilter.java,v 1.5 2006/12/14 09:31:30 spyromus Exp $
//

package com.salas.bb.utils.swinghtml;

import com.salas.bb.utils.htmlparser.IHtmlParserListener;

import java.util.List;
import java.util.Arrays;

/**
 * Filter for leaving only allowed HTML-like markup specific to Swing rendering.
 */
public class SwingHtmlFilter extends AbstractSwingFilter
{
    private static final List ALLOWED_TAGS = Arrays.asList(new String[] {
        "a", "b", "blockquote", "br", "em", "ol", "u", "ul",
        "i", "img", "li", "p", "pre", "table", "tr", "td", "th", "strong",
        "dl", "dt", "dd", "h1", "h2", "h3", "h4", "h5", "h6" });

    /**
     * Creates filter for a given listener.
     *
     * @param listener listener.
     */
    public SwingHtmlFilter(IHtmlParserListener listener)
    {
        super(listener);
    }

    /**
     * Returns the list of allowed tags.
     *
     * @return allowed tags.
     */
    protected List getAllowedTags()
    {
        return ALLOWED_TAGS;
    }
}
