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
// $Id$
//

package com.salas.bb.twitter;

import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import java.awt.*;

/**
 * The document that maintains the correct size.
 */
class TwitterMessage extends PlainDocument
{
    private static final int MAX_LENGTH = 160;

    public TwitterMessage()
    {
    }

    public void insertString(int offset, String str, AttributeSet a)
        throws BadLocationException
    {
        boolean beep = true;
        int messageLength = super.getLength();
        if (messageLength < MAX_LENGTH)
        {
            // Remove carriage returns
            str = str.replaceAll("\n", " ");

            int length = str.length();
            if (length > MAX_LENGTH - messageLength) length -= MAX_LENGTH - messageLength; else beep = false;

            super.insertString(offset, str.substring(0, length), a);
        }

        if (beep) Toolkit.getDefaultToolkit().beep();
    }
}