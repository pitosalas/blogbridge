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
// $Id: URLDropTarget.java,v 1.7 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.utils.StringUtils;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.*;
import java.net.URL;
import java.io.IOException;

/**
 * Drop target for URL D'n'D operations.
 */
public class URLDropTarget extends DropTarget
{
    private IURLDropTargetListener listener;

    /**
     * Creates a <code>DropTarget</code>.
     *
     * @param aListener listener for dropped URL's.
     */
    public URLDropTarget(IURLDropTargetListener aListener)
    {
        listener = aListener;
    }

    /**
     * The <code>DropTarget</code> intercepts dragEnter() notifications before the registered
     * <code>DropTargetListener</code> gets them.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public synchronized void dragEnter(DropTargetDragEvent dtde)
    {
        if (isDragOK(dtde))
        {
            dtde.acceptDrag(dtde.getDropAction());
        } else
        {
            dtde.rejectDrag();
        }
    }

    // Returns true if the dragging operation is OK
    private boolean isDragOK(DropTargetDragEvent dtde)
    {
        int action = dtde.getDropAction();
        return (action == DnDConstants.ACTION_COPY || action == DnDConstants.ACTION_MOVE ||
                action == DnDConstants.ACTION_LINK) &&
            dtde.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    /**
     * The <code>DropTarget</code> intercepts dragOver() notifications before the registered
     * <code>DropTargetListener</code> gets them.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public synchronized void dragOver(DropTargetDragEvent dtde)
    {
        dragEnter(dtde);
    }

    /**
     * The <code>DropTarget</code> intercepts dropActionChanged() notifications before the
     * registered <code>DropTargetListener</code> gets them. <P>
     *
     * @param dtde the DropTargetDragEvent
     */
    public synchronized void dropActionChanged(DropTargetDragEvent dtde)
    {
        dragEnter(dtde);
    }

    /**
     * The <code>DropTarget</code> intercepts drop() notifications before the registered
     * <code>DropTargetListener</code> gets them.
     *
     * @param evt the <code>DropTargetDropEvent</code>
     */
    public synchronized void drop(DropTargetDropEvent evt)
    {
        try
        {
            Transferable t = evt.getTransferable();

            if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
                evt.acceptDrop(evt.getDropAction());
                URL url = null;
                boolean success = true;
                try
                {
                    String s = (String)t.getTransferData(DataFlavor.stringFlavor);
                    s = StringUtils.cleanDraggedURL(s);
                    url = new URL(s);
                } catch (IOException e)
                {
                    success = false;
                }
                evt.getDropTargetContext().dropComplete(success);
                if (success) fireURLDropped(url, evt.getLocation());
            } else
            {
                evt.rejectDrop();
            }
        } catch (UnsupportedFlavorException e)
        {
            evt.rejectDrop();
        }
    }

    private void fireURLDropped(URL url, Point location)
    {
        if (listener != null) listener.urlDropped(url, location);
    }
}
