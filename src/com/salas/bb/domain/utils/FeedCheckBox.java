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
// $Id: FeedCheckBox.java,v 1.5 2006/01/09 08:48:15 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A checkbox with a feed associated.
 */
public class FeedCheckBox extends JCheckBox
{
    private final IFeed feed;

    /**
     * Creates check box.
     *
     * @param aFeed feed to associate.
     */
    public FeedCheckBox(IFeed aFeed)
    {
        IGuide[] guides = aFeed.getParentGuides();
        StringBuffer label = new StringBuffer(aFeed.getTitle());
        if (guides.length > 0)
        {
            label.append(" (");
            label.append(GuidesUtils.getGuidesNames(guides));
            label.append(")");
        }

        setText(label.toString());

        feed = aFeed;
    }

    /**
     * Returns the feed.
     *
     * @return feed.
     */
    public IFeed getFeed()
    {
        return feed;
    }

    /**
     * Wraps each feed on the list of feeds with check box object.
     *
     * @param aFeeds feeds.
     *
     * @return check boxes.
     */
    public static FeedCheckBox[] wrap(List aFeeds)
    {
        FeedCheckBox[] boxes = new FeedCheckBox[aFeeds.size()];
        for (int i = 0; i < aFeeds.size(); i++)
        {
            IFeed feed = (IFeed)aFeeds.get(i);
            boxes[i] = new FeedCheckBox(feed);
            boxes[i].setSelected(true);
        }

        return boxes;
    }

    /**
     * Scans the model for selected feeds and returns them.
     *
     * @param model list model.
     *
     * @return selected feeds.
     */
    public static List getSelected(ListModel model)
    {
        return getSelected(model, true);
    }

    /**
     * Scans the model for deselected feeds and returns them.
     *
     * @param model list model.
     *
     * @return selected feeds.
     */
    public static List getDeselected(ListModel model)
    {
        return getSelected(model, false);
    }

    /**
     * Scans the model for selected feeds and returns them.
     *
     * @param model     list model.
     * @param selected  <code>TRUE</code> to collect selected items.
     *
     * @return selected feeds.
     */
    private static List getSelected(ListModel model, boolean selected)
    {
        List feeds = new ArrayList();
        for (int i = 0; i < model.getSize(); i++)
        {
            Object item = model.getElementAt(i);
            if (item instanceof FeedCheckBox && !(((FeedCheckBox)item).isSelected() ^ selected))
            {
                feeds.add(((FeedCheckBox)item).getFeed());
            }
        }

        return feeds;
    }

    /**
     * Returns the tooltip string that has been set with <code>setToolTipText</code>.
     *
     * @return the text of the tool tip
     *
     * @see #TOOL_TIP_TEXT_KEY
     */
    public String getToolTipText()
    {
        return feed == null ? null : feed.getTitle();
    }
}
