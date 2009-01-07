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
// $Id: AddGuideAction.java,v 1.34 2007/01/24 19:30:37 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.FeatureManager;
import com.salas.bb.dialogs.guide.AddGuideDialog;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Adds new guide to the list.
 *
 * SHOULD ALWAYS BE EXECUTED FROM EDT!
 */
public final class AddGuideAction extends AbstractAction
{
    private static AddGuideAction instance;

    /**
     * Hidden singleton constructor.
     */
    private AddGuideAction()
    {
        // Enabled back by the ActionsMonitor
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized AddGuideAction getInstance()
    {
        if (instance == null) instance = new AddGuideAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    public void actionPerformed(ActionEvent event)
    {
        GlobalController controller = GlobalController.SINGLETON;
        GlobalModel model = controller.getModel();
        GuidesSet cgs = model.getGuidesSet();

        // Figure out if the publishing limit is already hit
        FeatureManager featureManager = GlobalController.SINGLETON.getFeatureManager();
        int pubLimit = featureManager.getPublicationLimit();
        boolean pubLimitReached = pubLimit > -1 && cgs.countPublishedGuides() >= pubLimit;

        boolean actAvailable = model.getServicePreferences().isAccountInformationEntered();
        AddGuideDialog dialog = new AddGuideDialog(controller.getMainFrame(), actAvailable, pubLimit, pubLimitReached);
        String urls = dialog.open(cgs);

        if (!dialog.hasBeenCanceled())
        {
            final String title = dialog.getGuideTitle();
            final String iconKey = dialog.getIconKey();
            final boolean autoFeedDiscovery = dialog.isAutoFeedDiscovery();

            StandardGuide guide = controller.createStandardGuide(title, iconKey, autoFeedDiscovery);
            if (guide != null)
            {
                // Propagate publishing information
                boolean publishingEnabled = dialog.isPublishingEnabled();
                guide.setPublishingEnabled(publishingEnabled);
                guide.setPublishingTitle(dialog.getPublishingTitle());
                guide.setPublishingTags(dialog.getPublishingTags());
                guide.setPublishingPublic(dialog.isPublishingPublic());
                guide.setPublishingRating(dialog.getPublishingRating() - 1);

                guide.setNotificationsAllowed(dialog.isNotificationsAllowed());

                ReadingList[] readingLists = dialog.getReadingLists();
                if (readingLists != null)
                {
                    for (ReadingList readingList : readingLists) guide.add(readingList);

                    // if there were reading lists -- update them immediately
                    if (readingLists.length > 0) controller.getPoller().update(guide);
                }

                // EDT !!!
                controller.selectGuide(guide, false);
                IFeed feed = controller.createDirectFeed(urls, false);
                if (feed != null) controller.selectFeed(feed);

                if (publishingEnabled) controller.showNewPublishingDialog();
            }
        }
    }
}
