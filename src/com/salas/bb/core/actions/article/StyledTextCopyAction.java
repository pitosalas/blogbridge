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
// $Id: StyledTextCopyAction.java,v 1.2 2006/12/06 16:52:12 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Copies the HTML text from an article to the clipboard.
 */
public final class StyledTextCopyAction extends AbstractAction
{
    private static StyledTextCopyAction instance;

    /**
     * Hidden singleton constructor.
     */
    private StyledTextCopyAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized StyledTextCopyAction getInstance()
    {
        if (instance == null) instance = new StyledTextCopyAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        IArticle art = GlobalModel.SINGLETON.getSelectedArticle();
        if (art != null)
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new HtmlSelection(art.getHtmlText()), null);
        }
    }

    /**
     * HTML selection.
     */
    private static class HtmlSelection implements Transferable
    {
        private static ArrayList htmlFlavors = new ArrayList();

        static
        {
            try
            {
                htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
                htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
                htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
            } catch (ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }

        private String html;

        /**
         * Creates selection.
         *
         * @param html HTML text.
         */
        public HtmlSelection(String html)
        {
            this.html = html;
        }

        /**
         * Reports transferrable data flavors.
         *
         * @return flavors.
         */
        public DataFlavor[] getTransferDataFlavors()
        {
            return (DataFlavor[])htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
        }

        /**
         * Returns <code>TRUE</code> if flavor is supported.
         *
         * @param flavor flavor.
         *
         * @return <code>TRUE</code> if flavor is supported.
         */
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return htmlFlavors.contains(flavor);
        }

        /**
         * Returns data being transferred.
         *
         * @param flavor flavor.
         *
         * @return data.
         *
         * @throws UnsupportedFlavorException if flavor is not supported.
         */
        public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException
        {
            if (String.class.equals(flavor.getRepresentationClass()))
            {
                return html;
            } else if (Reader.class.equals(flavor.getRepresentationClass()))
            {
                return new StringReader(html);
            } else if (InputStream.class.equals(flavor.getRepresentationClass()))
            {
                return new StringBufferInputStream(html);
            }

            throw new UnsupportedFlavorException(flavor);
        }
    }
}
