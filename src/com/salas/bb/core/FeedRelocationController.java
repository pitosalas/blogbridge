// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: FeedRelocationController.java,v 1.1 2007/06/29 10:15:56 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Relocation controller is used to copy / move feeds.
 * The implementation is NOT thread-safe.
 * The implementation maintains no references to feeds or guides involved in copy operations.
 * It refers to the by their IDs. If either source guide or feeds involved are no longer
 * exist, the operation is aborted.
 */
public class FeedRelocationController
{
    /** Action that starts copy operation over the selected feeds. */
    public static final Action COPY_OPERATION = new StartOperationAction(true);
    /** Action that starts cut (move) operation over the selected feeds. */
    public static final Action CUT_OPERATION = new StartOperationAction(false);
    /** Action that commits copy / cut operation over the selected feeds. */
    public static final Action PASTE_OPERATION = new FinishOperationAction();
    /** Action to abort copy / cut operation. */
    public static final Action ABORT_OPERATION = new AbortOperationAction();

    private static boolean  copy;
    private static Long     sourceGuideId;
    private static Long[]   feedIds;

    /**
     * Starts copy / cut operation.
     *
     * @param sourceGuide   source guide.
     * @param feeds         feeds to copy / move upon commit.
     * @param isCopy        <code>TRUE</code> top copy; otherwise -- move.
     */
    private static void startOperation(StandardGuide sourceGuide, IFeed[] feeds, boolean isCopy)
    {
        sourceGuideId = sourceGuide.getID();

        feedIds = new Long[feeds.length];
        for (int i = 0; i < feeds.length; i++) feedIds[i] = feeds[i].getID();
        copy = isCopy;
    }

    /**
     * Aborts copy / cut operation.
     */
    private static void abortOperation()
    {
        sourceGuideId = null;
        feedIds = null;
    }

    /**
     * Commits copy / cut operation. Never adds feeds to the same guide.
     *
     * @param destinationGuides list of destinations.
     */
    private static void commitOperation(List<StandardGuide> destinationGuides)
    {
        StandardGuide sourceGuide = getSourceGuide();
        if (sourceGuide != null && destinationGuides != null && destinationGuides.size() > 0)
        {
            List<IFeed> feeds = getFeeds();
            for (IFeed feed : feeds)
            {
                for (StandardGuide destinationGuide : destinationGuides)
                {
                    if (destinationGuide == sourceGuide) continue;

                    if (copy)
                    {
                        destinationGuide.add(feed);
                    } else
                    {
                        GlobalController.SINGLETON.moveFeed(feed, sourceGuide,
                            destinationGuide, destinationGuide.getFeedsCount());
                    }
                }
            }
        }
        
        abortOperation();
    }

    /**
     * Returns the list of feeds by the recorded IDs.
     *
     * @return feeds.
     */
    private static List<IFeed> getFeeds()
    {
        List<IFeed> feeds = new LinkedList<IFeed>();

        if (feedIds != null)
        {
            List<Long> ids = Arrays.asList(feedIds);
            GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();
            FeedsList feedsList = set.getFeedsList();

            for (int i = 0; i < feedsList.getFeedsCount(); i++)
            {
                IFeed feed = feedsList.getFeedAt(i);
                if (ids.contains(feed.getID())) feeds.add(feed);
            }
        }

        return feeds;
    }

    /**
     * Finds a guide with the recorded ID.
     *
     * @return guide or <code>NULL</code> if not found.
     */
    private static StandardGuide getSourceGuide()
    {
        GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();
        return set.findGuideByID(sourceGuideId);
    }

    /** The action to start copy / cut operation. */
    private static class StartOperationAction extends AbstractAction
    {
        private final boolean copy;

        /**
         * Constructs the action.
         *
         * @param copy <code>TRUE</code> if it's copy-operation, otherwise -- cut.
         */
        public StartOperationAction(boolean copy)
        {
            this.copy = copy;
        }

        /**
         * Invoked when event occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            GlobalModel model = GlobalModel.SINGLETON;
            IGuide guide = model.getSelectedGuide();
            if (guide != null && guide instanceof StandardGuide)
            {
                IFeed[] feeds = GlobalController.SINGLETON.getSelectedFeeds();
                if (feeds != null && feeds.length > 0)
                {
                    startOperation((StandardGuide)guide, feeds, copy);
                }
            }
        }
    }

    /** Commits the copy / cut command. */
    private static class FinishOperationAction extends AbstractAction
    {
        /**
         * Invoked when event occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            IGuide[] guides = GlobalController.SINGLETON.getSelectedGuides();
            List<StandardGuide> selectedGuides = new LinkedList<StandardGuide>();
            for (IGuide guide : guides)
            {
                if (guide instanceof StandardGuide) selectedGuides.add((StandardGuide)guide);
            }

            commitOperation(selectedGuides);
        }
    }

    /** Aborts the copy / cut command. */
    private static class AbortOperationAction extends AbstractAction
    {
        /**
         * Invoked when event occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            abortOperation();
        }
    }
}
