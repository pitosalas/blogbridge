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
// $Id: ViewModeValueModel.java,v 1.8 2006/01/31 16:50:16 spyromus Exp $
//

package com.salas.bb.core;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.FeedAdapter;
import com.salas.bb.domain.IFeed;
import com.salas.bb.views.settings.IFRS;
import com.salas.bb.views.settings.RenderingSettingsNames;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Value model for view mode.
 */
public class ViewModeValueModel implements ValueModel
{
    private final PropertyChangeSupport pcs;
    private final FeedListener feedListener;

    private IFRS    feedRenderingSettings;
    private IFeed   feed;
    private Object  recordedMode;

    /**
     * Creates value model which represents a view mode property.
     *
     * @param aFeedRenderingSettings rendering settings to take/put values from/to.
     */
    public ViewModeValueModel(IFRS aFeedRenderingSettings)
    {
        recordedMode = null;
        pcs = new PropertyChangeSupport(this);
        feedListener = new FeedListener();

        feedRenderingSettings = aFeedRenderingSettings;
    }

    /**
     * Sets the feed to grab settings from.
     *
     * @param aFeed feed.
     */
    public void setFeed(IFeed aFeed)
    {
        // We intentionally not firing the mode change events here even though
        // it's possible that the mode changes as result of the feed object
        // substitution. The reason is that the mode change will happen while
        // the previous feed is still displayed in the articles list. Having
        // done that, we will force to change the mode of all these articles
        // first and only then switch to the next feed which will slow down
        // the perceptional selection of the feed.

        if (feed != null) feed.removeListener(feedListener);

        feed = aFeed;

        if (feed != null) feed.addListener(feedListener);
    }

    /**
     * Returns the value of view mode property.
     *
     * @return value.
     */
    public Object getValue()
    {
        Object viewMode;

        if (feed != null && feed.isCustomViewModeEnabled() && feed.getCustomViewMode() != -1)
        {
            viewMode = new Integer(feed.getCustomViewMode());
        } else
        {
            viewMode = getGlobalValue();
        }

        return viewMode;
    }

    /**
     * Gets global value of the view mode.
     *
     * @return global view mode.
     */
    private Integer getGlobalValue()
    {
        return feedRenderingSettings == null ? null
            : (Integer)feedRenderingSettings.get(RenderingSettingsNames.ARTICLE_VIEW_MODE);
    }

    /**
     * Sets the value of view mode property.
     *
     * @param value new value.
     */
    public void setValue(Object value)
    {
        Object oldValue = getValue();

        if (feed != null && feed.isCustomViewModeEnabled())
        {
            feed.setCustomViewMode(((Integer)value).intValue());
        } else if (feedRenderingSettings != null)
        {
            feedRenderingSettings.set(RenderingSettingsNames.ARTICLE_VIEW_MODE, value);
        }

        fireChange(oldValue, value);
    }

    /**
     * Fires the change of the property value.
     *
     * @param oldValue  old value.
     * @param newValue  new value.
     */
    private void fireChange(Object oldValue, Object newValue)
    {
        pcs.firePropertyChange("value", oldValue, newValue);
    }

    /**
     * Adds listener.
     *
     * @param listener listener.
     */
    public void addValueChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes listener.
     *
     * @param listener listener.
     */
    public void removeValueChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }

    // ---------------------------------------------------------------------------------------------
    // The pair of methods below is used for two-step feed changing. The problem is described in
    // setFeed() method. To overcome this hurdle we record the value before changing feed and
    // after the feed is changed and displayed we compare new value with the recorded and fire
    // the change event to update all the controls depending on this value (for instance, the
    // view mode selector component).
    // ---------------------------------------------------------------------------------------------

    /**
     * Records current mode for future comparison.
     */
    public void recordValue()
    {
        recordedMode = getValue();
    }

    /**
     * Compares recorded mode with current and fires the event.
     */
    public void compareRecordedWithCurrent()
    {
        if (recordedMode != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    final Object oldMode = recordedMode;
                    fireChange(oldMode, getValue());
                }
            });

            recordedMode = null;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Monitors feed custom view mode enableness flag.
     */
    private class FeedListener extends FeedAdapter
    {
        /**
         * Called when information in feed changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (property.equals(IFeed.PROP_CUSTOM_VIEW_MODE_ENABLED))
            {
                boolean enabled = ((Boolean)newValue).booleanValue();

                if (enabled)
                {
                    int globalViewMode = getGlobalValue().intValue();
                    if (feed.getCustomViewMode() == -1)
                    {
                        feed.setCustomViewMode(globalViewMode);
                    } else
                    {
                        fireChange(new Integer(globalViewMode),
                            new Integer(feed.getCustomViewMode()));
                    }
                } else
                {
                    Integer oldMode = new Integer(feed.getCustomViewMode());
                    fireChange(oldMode, getValue());
                }
            }
        }
    }
}
