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
// $Id: CustomHTMLFactory.java,v 1.6 2006/01/08 05:10:10 kyank Exp $
//

package com.salas.bb.utils.uif.html;

import javax.swing.text.html.*;
import javax.swing.text.View;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;

/**
 * Custom HTML view factory. We use it to override views for some tags.
 */
public class CustomHTMLFactory extends HTMLEditorKit.HTMLFactory
{
    private static final int TYPE_UNWANTED  = -1;
    private static final int TYPE_IMAGE     = 0;
    private static final int TYPE_PARAGRAPH = 1;
    private static final int TYPE_BREAK     = 2;

    /**
     * Creates a view from an element.
     *
     * @param elem the element
     * @return the view
     */
    public View create(Element elem)
    {
        int type = getElementType(elem);

        View view = null;
        switch (type)
        {
            case TYPE_IMAGE:
                view = new FloatableImageView(elem);
                break;
            case TYPE_PARAGRAPH:
                view = new FloatParagraphView(elem);
                break;
            case TYPE_BREAK:
                view = new FloatBrView(elem);
                break;
            default:
                view = super.create(elem);
        }

        return view;
    }

    /**
     * Looks if the element is one we wish to intercept.
     *
     * @param elem  element to create view for.
     *
     * @return type of element.
     */
    private int getElementType(Element elem)
    {
        int type = TYPE_UNWANTED;

        Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag)
        {
            HTML.Tag kind = (HTML.Tag)o;

            if (kind == HTML.Tag.IMG)
            {
                type = TYPE_IMAGE;
            } else if (kind == HTML.Tag.IMPLIED)
            {
                String ws = (String) elem.getAttributes().getAttribute(CSS.Attribute.WHITE_SPACE);
                if (ws == null || !ws.equals("pre"))
                {
                    type = TYPE_PARAGRAPH;
                }
            } else if ((kind == HTML.Tag.P) ||
                       (kind == HTML.Tag.H1) ||
                       (kind == HTML.Tag.H2) ||
                       (kind == HTML.Tag.H3) ||
                       (kind == HTML.Tag.H4) ||
                       (kind == HTML.Tag.H5) ||
                       (kind == HTML.Tag.H6) ||
                       (kind == HTML.Tag.DT))
            {
                type = TYPE_PARAGRAPH;
            } else if (kind == HTML.Tag.BR)
            {
                type = TYPE_BREAK;
            }
        }

        return type;
    }
}
