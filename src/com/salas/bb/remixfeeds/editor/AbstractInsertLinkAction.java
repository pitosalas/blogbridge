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
// $Id: AbstractInsertLinkAction.java,v 1.3 2007/02/21 13:12:25 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of insert link action taking care of inserting the link.
 */
abstract class AbstractInsertLinkAction extends AbstractAction
{
    private static final Logger LOG = Logger.getLogger(AbstractInsertLinkAction.class.getName());

    private final JEditorPane editor;
    private final String itemTitle;

    /**
     * Creates action.
     *
     * @param editor editor.
     * @param itemTitle title.
     */
    public AbstractInsertLinkAction(JEditorPane editor, String itemTitle)
    {
        super(itemTitle);
        this.editor = editor;
        this.itemTitle = itemTitle;
    }

    /**
     * Inserts the link from the selected into the text at current position.
     *
     * @param link  link to insert.
     * @param title title of the link.
     */
    protected void insertLink(String link, String title)
    {
        String txt = MessageFormat.format("<a href=\"{0}\">{1}</a>", link, title);

        Document document = editor.getDocument();
        if (document instanceof HTMLDocument)
        {
            HTMLDocument htmlDocument = (HTMLDocument)document;
            int offset = editor.getSelectionStart();

            HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
            try
            {
                htmlDocument.remove(offset, editor.getSelectionEnd() - offset);
                kit.insertHTML(htmlDocument, offset, txt, 0, 0, HTML.Tag.A);
            } catch (Throwable e)
            {
                LOG.log(Level.WARNING, "Failed to insert link", e);
            }
        } else
        {
            editor.replaceSelection(txt);
        }
    }

    /**
     * Returns the text which is currently selected.
     *
     * @return text.
     */
    protected String getSelectedText()
    {
        return editor.getSelectedText();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return itemTitle;
    }
}
