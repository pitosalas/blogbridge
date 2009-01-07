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
// $Id $
//

package com.salas.bb.utils.feedscollections;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Collection tab which showing progress page when everything is only loading and switches to tree/list
 * when loading is complete.
 */
class CTab extends JPanel implements IProgressListener
{
    public static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    private CLoadingProgressPage loadingProgressPage = new CLoadingProgressPage();
    private CListTree listTree;

    private final Collection collection;
    private final boolean readingLists;
    private final boolean treeMode;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     *
     * @param collection    the collection of nodes.
     * @param treeMode      <code>TRUE</code> to init in the tree mode.
     * @param readingLists  <code>TRUE</code> if showing reading lists.
     */
    public CTab(Collection collection, boolean treeMode, boolean readingLists)
    {
        this.treeMode = treeMode;
        this.collection = collection;
        this.readingLists = readingLists;

        setLayout(new BorderLayout());
        add(loadingProgressPage, BorderLayout.CENTER);
        setBorder(EMPTY_BORDER);
    }

    /**
     * Invoked when loading of collection started.
     */
    public void started()
    {
        loadingProgressPage.started();

        showProgressPage(true);
    }

    /**
     * Invoked when progress changed.
     *
     * @param percentage progress percentage [0;100].
     */
    public void progress(int percentage)
    {
        loadingProgressPage.progress(percentage);
    }

    /**
     * Sets explicit status of loader.
     *
     * @param status status message.
     */
    public void status(String status)
    {
        loadingProgressPage.status(status);
    }

    /**
     * Invoked when loading finished.
     *
     * @param error NULL if no error.
     */
    public void finished(String error)
    {
        loadingProgressPage.finished(error);

        if (error == null) showProgressPage(false);
    }

    /**
     * Shows/hides progress page.
     *
     * @param show <code>TRUE</code> to show.
     */
    private void showProgressPage(boolean show)
    {
        JComponent remove, add;

        if (show)
        {
            remove = listTree;
            add = loadingProgressPage;
        } else
        {
            remove = loadingProgressPage;
            add = getListTree();
        }

        if (remove != null) remove(remove);
        if (add != null) add(add, BorderLayout.CENTER);

        validate();
        repaint();
    }

    /**
     * Returns list/tree component. Creates it when called for the first time.
     *
     * @return component.
     */
    private synchronized JComponent getListTree()
    {
        if (listTree == null) listTree = new CListTree(collection, treeMode, readingLists);
        return listTree;
    }
}
