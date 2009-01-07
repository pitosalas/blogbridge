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
// $Id: ExtendedLabel.java,v 1.6 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;

/**
 * Extended label supports fast HTML rendering and is capable of doing that in multiple lines.
 *
 * TODO use single view for rendering and copy font and colors to attributes instead.
 */
public class ExtendedLabel extends JTextArea
{
    /**
     * Creates extended label.
     */
    public ExtendedLabel()
    {
        this(null);
    }

    /**
     * Creates extended label.
     *
     * @param text text to set.
     */
    public ExtendedLabel(String text)
    {
        super(text);
        setEditable(false);
        setBorder(null);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    /**
     * Sets the text.
     *
     * @param t text.
     */
    public void setText(String t)
    {
        super.setText(t);
        setCaretPosition(0);
    }
}
