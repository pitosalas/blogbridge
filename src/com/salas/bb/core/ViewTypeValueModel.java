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

package com.salas.bb.core;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.views.settings.IFRS;
import com.salas.bb.views.settings.RenderingSettingsNames;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.FeedAdapter;
import com.salas.bb.domain.FeedType;

import javax.swing.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * Value model for view type.
 */
public class ViewTypeValueModel implements ValueModel
{
    private final PropertyChangeSupport pcs;

    private IFeed   feed;
    private Object  recordedMode;

    /**
     * Creates value model which represents a view type property.
     */
    public ViewTypeValueModel()
    {
        recordedMode = null;
        pcs = new PropertyChangeSupport(this);
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

        feed = aFeed;
    }

    /**
     * Returns the value of view mode property.
     *
     * @return value.
     */
    public Object getValue()
    {
        return new Integer(feed == null ? FeedType.TYPE_TEXT : feed.getType().getType());
    }

    /**
     * Sets the value of view mode property.
     *
     * @param value new value.
     */
    public void setValue(Object value)
    {
        Object oldValue = getValue();

        if (feed != null)
        {
            feed.setType(FeedType.toObject(((Integer)value).intValue()));
            fireChange(oldValue, value);
        }
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
}
