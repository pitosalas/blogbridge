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
// $Id: SettingsAction.java,v 1.7 2008/02/28 12:36:17 spyromus Exp $
//

package com.salas.bb.sentiments;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Invokes settings dialog.
 */
public class SettingsAction extends AbstractAction
{
    /** Invoked when an action occurs. */
    public void actionPerformed(ActionEvent e)
    {
        SentimentsConfig config = Calculator.getConfig();
        String oldPositive = config.getPositiveExpressions();
        String oldNegative = config.getNegativeExpressions();
        int oldPositiveThreshold = config.getPositiveThreshold();
        int oldNegativeThreshold = config.getNegativeThreshold();
        Color oldPositiveColor = config.getPositiveColor();
        Color oldNegativeColor = config.getNegativeColor();

        // Open dialog
        MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();
        SettingsDialog dialog = new SettingsDialog(mainFrame, config);
        dialog.open();

        // If not cancelled, see what has changed and recalculate if necessary
        if (!dialog.hasBeenCanceled() && SentimentsFeature.isAvailable())
        {
            GlobalModel.touchPreferences();

            String newPositive = config.getPositiveExpressions();
            String newNegative = config.getNegativeExpressions();

            boolean expressionsChanged =
                !StringUtils.equals(oldPositive, newPositive) ||
                !StringUtils.equals(oldNegative, newNegative);

            boolean thresholdsChanged =
                oldPositiveThreshold != config.getPositiveThreshold() ||
                oldNegativeThreshold != config.getNegativeThreshold();

            if (expressionsChanged || thresholdsChanged)
            {
                RecalculateAction.perform(!expressionsChanged);
            } else if (!oldPositiveColor.equals(config.getPositiveColor()) ||
                       !oldNegativeColor.equals(config.getNegativeColor()))
            {
                IFeedDisplay feedDisplay = mainFrame.getArticlesListPanel().getFeedView();
                feedDisplay.repaintSentimentsColorCodes();
            }
        }
    }
}
