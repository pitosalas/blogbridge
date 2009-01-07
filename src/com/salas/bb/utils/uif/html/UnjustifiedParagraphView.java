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
// $Id: UnjustifiedParagraphView.java,v 1.5 2006/01/08 05:10:10 kyank Exp $
//

package com.salas.bb.utils.uif.html;

import javax.swing.text.html.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.Element;

/**
 * The usual paragraph converting full justification into left aligning.
 */
public class UnjustifiedParagraphView extends ParagraphView
{
    /**
     * Creates paragraph.
     *
     * @param elem element.
     */
    public UnjustifiedParagraphView(Element elem)
    {
        super(elem);
    }

    /**
     * Set justification. It converts full jutification into left aligning.
     *
     * @param j justification.
     */
    protected void setJustification(int j)
    {
        super.setJustification(j == StyleConstants.ALIGN_JUSTIFIED ? StyleConstants.ALIGN_LEFT : j);
    }
}
