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
// $Id: GuideDisplayModeManager.java,v 1.1 2007/04/17 07:57:18 spyromus Exp $
//

package com.salas.bb.core;

import static com.salas.bb.domain.GuideClass.READ;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.settings.FeedRenderingSettings;

import java.awt.*;

/**
 * <p>Manages information about how the feed should be displayed.
 * For now it manages only feed list cell properties.</p>
 *
 * @see com.salas.bb.core.FeedDisplayModeManager
 */
public class GuideDisplayModeManager extends AbstractDisplayModeManager
{
    private static GuideDisplayModeManager instance;

    /**
     * Priority of classes in ascending order.
     */
    private static final int[] CLASSES_PRIO = new int[] { READ };

    /**
     * Creates manager.
     */
    GuideDisplayModeManager()
    {
        super(CLASSES_PRIO, "gdmm.");
    }

    @Override
    protected Color getDefaultColor(boolean selected)
    {
        Color color = null;

        GlobalModel gm = GlobalModel.SINGLETON;
        if (gm != null) {
            FeedRenderingSettings renderingSettings = gm.getGlobalRenderingSettings();
            if (renderingSettings != null) color = renderingSettings.getFeedsListForeground(selected);
        }

        return color != null ? color : super.getDefaultColor(selected);
    }

    /**
     * Returns instance of the manager.
     *
     * @return manager.
     */
    public static synchronized GuideDisplayModeManager getInstance()
    {
        if (instance == null) instance = new GuideDisplayModeManager();
        return instance;
    }

    /**
     * Returns <code>TRUE</code> if the guide is visible according to user preferences.
     *
     * @param guide guide to test.
     *
     * @return <code>TRUE</code> if visible.
     *
     * @throws IllegalArgumentException if guide isn't specified.
     */
    public boolean isVisible(IGuide guide)
    {
        return getColor(guide, false) != null;
    }

    /**
     * Returns foreground color to paint the guide cell with.
     *
     * @param guide guide to get color for.
     * @param selected TRUE when selected.
     *
     * @return the color.
     *
     * @throws IllegalArgumentException if guide isn't specified.
     */
    public Color getColor(IGuide guide, boolean selected)
    {
        if (guide == null)
            throw new IllegalArgumentException(Strings.error("unspecified.guide"));

        return getColor(guide.getClassesMask(), selected);
    }
}
