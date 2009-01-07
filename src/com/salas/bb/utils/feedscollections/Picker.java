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

import EDU.oswego.cs.dl.util.concurrent.Executor;
import com.salas.bb.utils.concurrency.ExecutorFactory;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;

/**
 * Picker component for selecting items from collections.
 */
public class Picker extends JPanel
{
    public static final int ITEM_TYPE_RL    = 1;
    public static final int ITEM_TYPE_FEED  = 2;

    private static Executor execLoader;
    private JTabbedPane tabbedPane;

    private List collections;

    static {
        execLoader = ExecutorFactory.createPooledExecutor("Collections Loader", 2, 100);
    }

    /**
     * Creates picker component.
     */
    public Picker()
    {
        collections = new ArrayList();

        setLayout(new BorderLayout());
        add(new JLabel(Strings.message("collections.no.collections.available"), JLabel.CENTER), BorderLayout.CENTER);
    }

    /**
     * Adds picker collection to show.
     *
     * @param indexURL  URL of OPML index.
     * @param title     title of the list.
     * @param treeMode  <code>TRUE</code> to show in tree mode.
     * @param itemType  type of items to focus on.
     * @param skipLevel <code>TRUE</code> to skip first level of folders and treat them as organization
     *                  level fore reading list.
     */
    public void addCollection(final URL indexURL, String title, boolean treeMode, final int itemType,
                              boolean skipLevel)
    {
        if (indexURL == null) return;
        if (title == null) throw new NullPointerException(Strings.error("unspecified.title"));
        if (itemType != ITEM_TYPE_RL && itemType != ITEM_TYPE_FEED)
            throw new IllegalArgumentException(Strings.error("invalid.item.type.given"));

        final Collection collection = new Collection(title);
        final CTab tab = new CTab(collection, treeMode, itemType == ITEM_TYPE_RL);
        addTab(title, tab);

        // Collections loading task
        Runnable task = new LoadCollectionTask(collection, indexURL, itemType, tab, skipLevel);

        try
        {
            execLoader.execute(task);
        } catch (InterruptedException e)
        {
            task.run();
        }
    }

    /**
     * Adds tab.
     *
     * @param title title.
     * @param tab   tab.
     */
    private synchronized void addTab(String title, CTab tab)
    {
        if (tabbedPane == null)
        {
            tabbedPane = new JTabbedPane();

            // We show first tab without tabbed pane
            removeAll();
            add(tabbedPane, BorderLayout.CENTER);

            validate();
            repaint();
        }

        tabbedPane.addTab(title, tab);
    }

    /**
     * Returns the list of all currently selected collections items.
     *
     * @return collections items.
     */
    public CollectionItem[] getSelectedCollectionItems()
    {
        ArrayList selected = new ArrayList();

        for (int i = 0; i < collections.size(); i++)
        {
            Collection col = (Collection)collections.get(i);
            selected.addAll(col.getSelectedItems());
        }

        return (CollectionItem[])selected.toArray(new CollectionItem[selected.size()]);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Collections model
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Loader of collection.
     */
    private class LoadCollectionTask implements Runnable
    {
        private final Collection collection;
        private final URL indexURL;
        private final int itemType;
        private final IProgressListener progressListener;
        private final boolean skipLevel;

        /**
         * Creates collection loader task for thread execution.
         *
         * @param collection        collection loader.
         * @param indexURL          index URL.
         * @param itemType          type of items to load.
         * @param progressListener  progress listener.
         * @param skipLevel         <code>TRUE</code> to skip first level of folders and treat them as organization
         *                          level fore reading list.
         */
        public LoadCollectionTask(Collection collection, URL indexURL, int itemType,
                                  IProgressListener progressListener, boolean skipLevel)
        {
            this.collection = collection;
            this.indexURL = indexURL;
            this.itemType = itemType;
            this.progressListener = progressListener;
            this.skipLevel = skipLevel;
        }

        /**
         * Invoked when task execution starts.
         */
        public void run()
        {
            CollectionLoader.load(collection, indexURL, itemType == ITEM_TYPE_RL, skipLevel, progressListener);
            collections.add(collection);
        }
    }
}
