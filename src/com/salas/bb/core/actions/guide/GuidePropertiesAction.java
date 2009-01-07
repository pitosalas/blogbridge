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
// $Id: GuidePropertiesAction.java,v 1.28 2007/01/24 19:30:37 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.FeatureManager;
import com.salas.bb.dialogs.guide.EditGuideDialog;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.views.GuidesPanel;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Edits guide properties.
 *
 * SHOULD ALWAYS BE EXECUTED FROM EDT!
 */
public final class GuidePropertiesAction extends AbstractAction
{
    private static GuidePropertiesAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private GuidePropertiesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized GuidePropertiesAction getInstance()
    {
        if (instance == null) instance = new GuidePropertiesAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    public void actionPerformed(ActionEvent event)
    {
        GlobalModel model = GlobalModel.SINGLETON;
        final IGuide guide = model.getSelectedGuide();
        if (guide != null)
        {
            boolean oldPublishingEnabled = guide.isPublishingEnabled();

            GuidesSet set = model.getGuidesSet();
            MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();

            // Figure out if the publishing limit is already hit
            FeatureManager featureManager = GlobalController.SINGLETON.getFeatureManager();
            int pubLimit = featureManager.getPublicationLimit();
            boolean pubLimitReached = pubLimit > -1 && set.countPublishedGuides() >= pubLimit;

            boolean actAvailable = model.getServicePreferences().isAccountInformationEntered();
            EditGuideDialog dialog = new EditGuideDialog(mainFrame, actAvailable, pubLimit, pubLimitReached);
            dialog.open(set, guide);

            if (!dialog.hasBeenCanceled())
            {
                performUpdates(guide, dialog, set.indexOf(guide));

                boolean newPublishingEnabled = guide.isPublishingEnabled();
                if (newPublishingEnabled && !oldPublishingEnabled)
                {
                    GlobalController.SINGLETON.showNewPublishingDialog();
                }
            }
        }
    }

    /**
     * Update guide information.
     *
     * @param guide         guide to update.
     * @param dialog        dialog with information.
     * @param position      old guide position.
     */
    private void performUpdates(IGuide guide, EditGuideDialog dialog, int position)
    {
        guide.setTitle(dialog.getGuideTitle());
        guide.setIconKey(dialog.getIconKey());
        guide.setAutoFeedsDiscovery(dialog.isAutoFeedDiscovery());
        guide.setNotificationsAllowed(dialog.isNotificationsAllowed());

        // Publishing
        guide.setPublishingEnabled(dialog.isPublishingEnabled());
        guide.setPublishingTitle(dialog.getPublishingTitle());
        guide.setPublishingTags(dialog.getPublishingTags());
        guide.setPublishingPublic(dialog.isPublishingPublic());
        guide.setPublishingRating(dialog.getPublishingRating() - 1);
        
        // Repositioning
        int newPosition = dialog.getPosition();
        if (position != newPosition) updatePosition(guide, newPosition);

        // If guide is standard and reading lists changed
        if (guide instanceof StandardGuide)
        {
            StandardGuide sguide = (StandardGuide)guide;
            ReadingList[] aReadingLists = sguide.getReadingLists();

            List<ReadingList> oldLists = Arrays.asList(aReadingLists);
            List<ReadingList> newLists = Arrays.asList(dialog.getReadingLists());

            boolean addedReadingList = false;

            // Add missing
            for (ReadingList list : newLists)
            {
                if (!oldLists.contains(list))
                {
                    sguide.add(list);
                    addedReadingList = true;
                }
            }

            // Remove deleted
            for (ReadingList list : oldLists)
            {
                if (!newLists.contains(list)) sguide.remove(list, true);
            }

            if (addedReadingList) GlobalController.SINGLETON.getPoller().update(sguide);
        }
    }

    /**
     * Update position of guide and select it again in view.
     *
     * @param guide       guide to move.
     * @param newPosition new position in list.
     */
    private void updatePosition(final IGuide guide, final int newPosition)
    {
        final GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();

        // SHOULD BE IN EDT HERE
        cgs.relocateGuide(guide, newPosition);

        // Select guide in view
        final MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();
        final GuidesPanel cgp = mainFrame.getGudiesPanel();
        cgp.selectGuide(newPosition);
    }
}
