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
// $Id: FeedDisplayModeManager.java,v 1.19 2008/03/18 06:56:46 spyromus Exp $
//

package com.salas.bb.core;

import static com.salas.bb.domain.FeedClass.*;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;

import java.awt.*;

/**
 * <p>Manages information about how the feed should be displayed.
 * For now it manages only feed list cell properties.</p>
 *
 * <p>When application needs to paint feed cell it can ask if the cell
 * should be visible by calling <code>isVisible()</code> method and
 * if it should then the <code>getColor</code> will return the main color
 * for painting the text of unselected cell.</p>
 *
 * <p>In its initial state manager always returns default color and
 * says that cells of feeds of any class are visible. The behavior can
 * be changed by changing the colors for the classes by using
 * <code>setColor()</code> method call. When the application sets
 * NULL instead of the color using the above method, its understood by
 * this component as an order to report cells of this class to be
 * invisible.</p>
 *
 * <p>The component is capable of storing and restoring of its internal
 * state in preferences object. In order to do that the application
 * is required to pass preferences object to <code>storePreferences</code>
 * and <code>restorePreferences</code> as appropriate.</p>
 */
public class FeedDisplayModeManager extends AbstractDisplayModeManager
{
    private static FeedDisplayModeManager instance;

    /**
     * Priority of classes in ascending order.
     */
    private static final int[] CLASSES_PRIO = new int[]
    {
        UNDISCOVERED, READ, LOW_RATED,
        INVALID, DISABLED
    };

    /**
     * Default color for missing mappings.
     */
    static final Color DEFAULT_COLOR = Color.BLACK;

    /**
     * Creates manager.
     */
    FeedDisplayModeManager()
    {
        super(DEFAULT_COLOR, CLASSES_PRIO, "cdmm.");

        // Setting proper defaults
        setColor(INVALID, Color.decode("#9E9E9E"));
        setColor(LOW_RATED, null);
        setColor(DISABLED, null);
    }

    /**
     * Returns instance of the manager.
     *
     * @return manager.
     */
    public static synchronized FeedDisplayModeManager getInstance()
    {
        if (instance == null) instance = new FeedDisplayModeManager();
        return instance;
    }

    /**
     * Returns TRUE if the feed is visible according to user preferences.
     *
     * @param feed feed to test.
     *
     * @return TRUE if visible.
     *
     * @throws IllegalArgumentException if feed isn't specified.
     */
    public boolean isVisible(IFeed feed)
    {
        return getColor(feed) != null;
    }

    /**
     * Returns foreground color to paint the feed cell with.
     *
     * @param feed feed to get color for.
     *
     * @return the color.
     *
     * @throws IllegalArgumentException if feed isn't specified.
     */
    public Color getColor(IFeed feed)
    {
        if (feed == null)
            throw new IllegalArgumentException(Strings.error("unspecified.feed"));

        return getColor(feed.getClassesMask());
    }
}
