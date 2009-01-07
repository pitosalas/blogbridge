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
// $Id: ArticleFilterProtector.java,v 1.2 2008/04/09 04:34:38 spyromus Exp $
//

package com.salas.bb.sentiments;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.Resources;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.views.settings.RenderingManager;
import com.salas.bb.views.settings.RenderingSettingsNames;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

/**
 * When Sentiment Analysis feature is disabled or inaccessible, this
 * object disallows selection of the negative/positive/non-negative
 * article filtering modes. It displays the warning dialog box and
 * reverts the value back to the safe.
 */
public class ArticleFilterProtector implements PropertyChangeListener
{
    /**
     * Hidden singleton constructor.
     */
    private ArticleFilterProtector()
    {
    }

    /**
     * Initializes the protector.
     */
    public static void init()
    {
        RenderingManager.addPropertyChangeListener(
            RenderingSettingsNames.ARTICLE_FILTER, new ArticleFilterProtector());
    }

    /**
     * Invoked when the article filter mode changes.
     *
     * @param evt event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        int newMode = (Integer)evt.getNewValue();

        if (!canSwitchTo(newMode))
        {
            int oldMode = (Integer)evt.getOldValue();
            if (!isSafeMode(oldMode)) oldMode = IFeedDisplayConstants.FILTER_ALL;

            if (needShowWarning())
            {
                JOptionPane.showMessageDialog(GlobalController.SINGLETON.getMainFrame(),
                    MessageFormat.format(Strings.message("sentiment.analysis.cant.switch.filter"),
                    modeToString(newMode)),
                    "BlogBridge", JOptionPane.INFORMATION_MESSAGE,
                    Resources.getLargeApplicationIcon());
            }

            renderingSettings().setArticleFilter(oldMode);
        }
    }

    private static FeedRenderingSettings renderingSettings()
    {
        return GlobalModel.SINGLETON.getGlobalRenderingSettings();
    }

    /**
     * Gets current article filter mode and changes it to ALL if
     * it can't be used.
     */
    public static void switchToSafeIfUnsafe()
    {
        int mode = renderingSettings().getArticleFilter();
        if (!isSafeMode(mode))
        {
           renderingSettings().setArticleFilter(IFeedDisplayConstants.FILTER_ALL);
        }
    }

    /**
     * Returns TRUE if can switch to the given mode.
     *
     * @param mode filter mode.
     * 
     * @return TRUE if can switch.
     */
    public static boolean canSwitchTo(int mode)
    {
        return isSafeMode(mode) || isAllowedUnsafe();
    }

    /**
     * Returns TRUE if unsafe modes are allowed at the moment (feature is enabled).
     *
     * @return TRUE if unsafe is allowed.
     */
    private static boolean isAllowedUnsafe()
    {
        return SentimentsFeature.isEnabled();
    }

    /**
     * Returns TRUE if it's the safe mode and can be switched to
     * at all times.
     *
     * @param mode filter mode.
     *
     * @return TRUE if safe to switch.
     */
    private static boolean isSafeMode(int mode)
    {
        return mode <= IFeedDisplayConstants.FILTER_PINNED; // Safe modes [0; 2], unsafe ...
    }

    /**
     * Returns TRUE when need to show warning.
     *
     * @return TRUE when need to show warning.
     */
    private static boolean needShowWarning()
    {
        return GlobalController.SINGLETON.isInitializationFinished();
    }

    /**
     * Returns the name of the mode by it's number.
     *
     * @param mode filter mode number.
     *
     * @return name or NULL if not Sentiment Analysis filter mode.
     */
    private static String modeToString(int mode)
    {
        String name = null;

        switch (mode)
        {
            case IFeedDisplayConstants.FILTER_POSITIVE:
                name = Strings.message("sentiment.analysis.positive");
                break;
            case IFeedDisplayConstants.FILTER_NEGATIVE:
                name = Strings.message("sentiment.analysis.negative");
                break;
            case IFeedDisplayConstants.FILTER_NON_NEGATIVE:
                name = Strings.message("sentiment.analysis.nonnegative");
                break;
        }

        return name;
    }
}
