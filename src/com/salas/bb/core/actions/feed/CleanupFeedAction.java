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
// $Id: CleanupFeedAction.java,v 1.8 2007/07/24 14:04:30 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.CleanupWizardDialog;
import com.salas.bb.domain.*;
import com.salas.bb.utils.ThreadedAction;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Cleans up feeds.
 */
public final class CleanupFeedAction extends ThreadedAction
{
    private static CleanupFeedAction instance;

    private List<IFeed> feeds;
    // keep the selected once values to restore on open
    private int     purgeLimit = -1;
    private List<IArticle> articlesToDelete;

    /**
     * Returns initialized instance.
     *
     * @return instance.
     */
    public static synchronized CleanupFeedAction getInstance()
    {
        if (instance == null) instance = new CleanupFeedAction();
        return instance;
    }
    
    /**
     * <p>Invoked before forking the thread. Override this method to add a
     * go-no-go decision before starting the fork.</p>
     *
     * <p>See for example: {@link DeleteFeedAction#beforeFork}</p>
     *
     * @return <code>TRUE</code> to continue with action.
     */
    protected boolean beforeFork()
    {
        CleanupWizardDialog dialog = new CleanupWizardDialog(
            GlobalController.SINGLETON.getMainFrame(),
            GlobalModel.SINGLETON.getGuidesSet(),
            GlobalModel.SINGLETON.getScoreCalculator());
        
        dialog.open();
        
        final boolean isRunCleanPressed = !dialog.hasBeenCanceled();
        
        if (isRunCleanPressed)
        {
            feeds = dialog.getFeedsToRemove();
            purgeLimit = dialog.getPurgeLimit();
            articlesToDelete = dialog.getArticlesToDelete();
        }
        
        return isRunCleanPressed;
    }
    
    /**
     * @see com.salas.bb.utils.ThreadedAction#doAction(java.awt.event.ActionEvent)
     */
    protected void doAction(ActionEvent event)
    {
        deleteFeeds();

        if (articlesToDelete != null)
        {
            for (IArticle article : articlesToDelete)
            {
                long aid = article.getID();
                IFeed feed = article.getFeed();
                long fid = (feed != null && feed instanceof DataFeed) ? feed.getID() : -1;

                if (aid != -1 && fid != -1)
                {
//                    System.out.println("Remove " + article + " from " + feed);
                    ((DataFeed)feed).removeArticle(article);
                }
            }

            articlesToDelete = null;
        }
    }

    /**
     * Removes feeds selected in wizard.
     */
    private void deleteFeeds()
    {
        Map<IGuide, List<IFeed>> guidesToFeeds = new IdentityHashMap<IGuide, List<IFeed>>();

        for (IFeed feed : feeds)
        {
            if (feed.isDynamic())
            {
                ((DirectFeed)feed).setDisabled(true);
            } else
            {
                IGuide[] targetGuides = feed.getParentGuides();

                for (IGuide targetGuide : targetGuides)
                {
                    List<IFeed> feedsList = guidesToFeeds.get(targetGuide);
                    if (feedsList == null)
                    {
                        feedsList = new ArrayList<IFeed>();
                        guidesToFeeds.put(targetGuide, feedsList);
                    }

                    feedsList.add(feed);
                }
            }
        }

        // Remove feeds in groups
        Set<Map.Entry<IGuide, List<IFeed>>> entries = guidesToFeeds.entrySet();
        for (Map.Entry<IGuide, List<IFeed>> entry : entries)
        {
            IGuide targetGuide = entry.getKey();
            List<IFeed> feedsList = entry.getValue();
            IFeed[] feedsToRemove = feedsList.toArray(new IFeed[feedsList.size()]);

            targetGuide.remove(feedsToRemove);
        }
    }
}