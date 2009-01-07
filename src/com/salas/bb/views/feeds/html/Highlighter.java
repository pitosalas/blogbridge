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
// $Id: Highlighter.java,v 1.6 2006/01/08 05:12:59 kyank Exp $
//

package com.salas.bb.views.feeds.html;

import com.salas.bb.domain.utils.TextRange;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;

/**
 * Utility class which takes the list of keywords and instance of text area and
 * highlights these keywords with a given colors.
 */
final class Highlighter
{
    /**
     * Highlight ranges in the pane with given color.
     *
     * @param aPane         pane.
     * @param aRanges       ranges.
     * @param aBackground   background color.
     */
    public static void highlight(JEditorPane aPane, TextRange[] aRanges, Color aBackground)
    {
        javax.swing.text.Highlighter highlighter = aPane.getHighlighter();

        DefaultHighlighter.DefaultHighlightPainter painter =
            new DefaultHighlighter.DefaultHighlightPainter(aBackground);

        for (int i = 0; i < aRanges.length; i++)
        {
            TextRange range = aRanges[i];

            try
            {
                highlighter.addHighlight(range.getStart(), range.getEnd(), painter);
            } catch (BadLocationException e)
            {
                // Failed to put highligh marks
            }
        }
    }

    /**
     * Highlight range in the pane with given color.
     *
     * @param aPane         pane.
     * @param aRange        range.
     * @param aBackground   background color.
     */
    public static void highlight(JEditorPane aPane, TextRange aRange, Color aBackground)
    {
        javax.swing.text.Highlighter highlighter = aPane.getHighlighter();

        DefaultHighlighter.DefaultHighlightPainter painter =
            new DefaultHighlighter.DefaultHighlightPainter(aBackground);

        try
        {
            highlighter.addHighlight(aRange.getStart(), aRange.getEnd(), painter);
        } catch (BadLocationException e)
        {
            // Failed to put highligh marks
        }
    }

    /**
     * Removes all highlights.
     *
     * @param aPane pane.
     */
    public static void removeHighlights(JEditorPane aPane)
    {
        javax.swing.text.Highlighter aHighlighter = aPane.getHighlighter();
        aHighlighter.removeAllHighlights();
    }
}
