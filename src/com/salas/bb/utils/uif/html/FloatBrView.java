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
// $Id: FloatBrView.java,v 1.4 2006/01/08 05:10:10 kyank Exp $
//

package com.salas.bb.utils.uif.html;

import javax.swing.text.html.InlineView;
import javax.swing.text.html.HTML;
import javax.swing.text.Element;

/**
 * Customized version of break view. Enhanced to support clear attribute and
 * respond appropriately -- clearing any floated elements in containing/surrounding
 * paragraphs.
 *
 * Based on <code>javax.swing.text.html.BRView</code>, which is package-private, and
 * therefore cannot be extended.
 *
 * @see javax.swing.text.html.BRView
 */
public class FloatBrView extends InlineView
{
    /**
     * Creates a new view that represents a &lt;BR&gt; element.
     *
     * @param elem the element to create a view for
     */
    public FloatBrView(Element elem)
    {
        super(elem);
    }

    /**
     * Forces a line break.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @param pos the potential location of the start of the broken view >= 0. This may be useful
     * for calculating tab positions.
     * @param len specifies the relative length from pos where a potential break is desired >= 0.
     *
     * @return View.ForcedBreakWeight for X_AXIS.
     */
    public int getBreakWeight(int axis, float pos, float len)
    {
        if (axis == X_AXIS)
        {
            return ForcedBreakWeight;
        } else
        {
            return super.getBreakWeight(axis, pos, len);
        }
    }

    /**
     * Determines if the break should clear floats in the left margin.
     *
     * @return True if this line break clears the left margin of floats.
     */
    public boolean isClearLeft()
    {
        Object clear = getElement().getAttributes().getAttribute(HTML.Attribute.CLEAR);
        return clear != null && (clear.equals("left") || clear.equals("all"));
    }

    /**
     * Determines if the break should clear floats in the right margin.
     *
     * @return True if this line break clears the right margin of floats.
     */
    public boolean isClearRight()
    {
        Object clear = getElement().getAttributes().getAttribute(HTML.Attribute.CLEAR);
        return clear != null && (clear.equals("right") || clear.equals("all"));
    }
}
