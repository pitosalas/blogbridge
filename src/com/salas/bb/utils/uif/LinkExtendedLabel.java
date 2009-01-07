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
// $Id: LinkExtendedLabel.java,v 1.14 2007/04/10 13:51:02 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Multi-line label which holds the link to somewhere.
 */
public class LinkExtendedLabel extends ExtendedLabel
{
    private URL link;
    private boolean linkPresent = false;

    private Color originalForeground;

    /**
     * Creates pure link label without link.
     */
    public LinkExtendedLabel()
    {
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        originalForeground = getForeground();

        setDragEnabled(true);
        setTransferHandler(new URLTransferHandler());

        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DragGestureListener()
        {
            public void dragGestureRecognized(final DragGestureEvent dge)
            {
                TransferHandler th = getTransferHandler();
                th.exportAsDrag(LinkExtendedLabel.this, dge.getTriggerEvent(), DnDConstants.ACTION_COPY);
            }
        });
    }

    /**
     * Intercepts the call and saves foreground.
     *
     * @param fg color.
     */
    public void setForeground(Color fg)
    {
        super.setForeground(fg);
        originalForeground = fg;
    }

    /**
     * Returns link of current label.
     *
     * @return URL
     */
    public URL getLink()
    {
        return link;
    }

    /**
     * Binds this label to new link.
     *
     * @param aLink new link URL.
     */
    public void setLink(final URL aLink)
    {
        this.link = aLink;
        linkPresent = ((aLink != null) &&
            !(Constants.EMPTY_STRING.equals(aLink.toString().trim())));
        
        String fullLink = aLink != null ? aLink.toString() : null;
        setToolTipText(fullLink);
    }

    /**
     * Processes mouse events.
     *
     * @param e event.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_CLICKED:
                if (linkPresent && (e.getClickCount() == getTriggerClickCount()) &&
                        (e.getButton() == MouseEvent.BUTTON1))
                {
                    // If user clicked twice then open the link in browser
                    doAction();
                }
                break;

            case MouseEvent.MOUSE_ENTERED:
                // Change color only if link is present
                if (linkPresent)
                {
                    super.setForeground(Color.BLUE);
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    GlobalController.SINGLETON.setStatus(getLink().toString());
                }
                break;

            case MouseEvent.MOUSE_EXITED:
                // Change color only if link is present
                if (linkPresent)
                {
                    super.setForeground(originalForeground);
                    this.setCursor(Cursor.getDefaultCursor());
                    GlobalController.SINGLETON.setStatus(null);
                }
                break;
        }
    }

    /**
     * Performs an action when triggered.
     */
    protected void doAction()
    {
        BrowserLauncher.showDocument(getLink(), GlobalModel.SINGLETON.getUserPreferences().getInternetBrowser());
    }

    /**
     * Returns number of clicks triggering opening the link in bowser.
     *
     * @return number of clicks.
     */
    protected int getTriggerClickCount()
    {
        return 1;
    }

    private class URLTransferHandler extends TransferHandler
    {
        private final DataFlavor FLAVOR_URL = createFlavor("application/x-java-url;class=java.net.URL");
        private final DataFlavor FLAVOR_URI_LIST = createFlavor("text/uri-list");
        private final DataFlavor FLAVOR_PLAIN = createFlavor("text/plain");
        private final java.util.List<DataFlavor> FLAVORS;


        public URLTransferHandler()
        {
            FLAVORS = new ArrayList<DataFlavor>();
            if (FLAVOR_URL != null) FLAVORS.add(FLAVOR_URL);
            if (FLAVOR_URI_LIST != null) FLAVORS.add(FLAVOR_URI_LIST);
            if (FLAVOR_PLAIN != null) FLAVORS.add(FLAVOR_PLAIN);
            FLAVORS.add(DataFlavor.stringFlavor);
        }

        private DataFlavor createFlavor(String mime)
        {
            DataFlavor f;

            try
            {
                f = new DataFlavor(mime);
            } catch (ClassNotFoundException e)
            {
                f = null;
            }

            return f;
        }

        @Override
        public int getSourceActions(JComponent c)
        {
            return DnDConstants.ACTION_COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            return new Transferable()
            {
                public DataFlavor[] getTransferDataFlavors()
                {
                    return FLAVORS.toArray(new DataFlavor[FLAVORS.size()]);
                }

                public boolean isDataFlavorSupported(DataFlavor flavor)
                {
                    return FLAVORS.contains(flavor);
                }

                public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException
                {
                    Object data;

                    if (flavor.equals(FLAVOR_URL))
                    {
                        data = link;
                    } else
                    {
                        data = new ByteArrayInputStream(link.toString().getBytes());
                    }

                    return data;
                }
            };
        }
    }
}