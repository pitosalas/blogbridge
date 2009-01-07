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
// $Id: GuideCheckBox.java,v 1.1 2006/01/09 16:07:01 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A checkbox with a guide associated.
 */
public class GuideCheckBox extends JCheckBox
{
    private final IGuide guide;

    /**
     * Creates check box.
     *
     * @param aGuide guide to associate.
     */
    public GuideCheckBox(IGuide aGuide)
    {
        setText(aGuide.getTitle());

        guide = aGuide;
    }

    /**
     * Returns the guide.
     *
     * @return guide.
     */
    public IGuide getGuide()
    {
        return guide;
    }

    /**
     * Wraps each guide on the list of guides with check box object.
     *
     * @param aGuides guides.
     *
     * @return check boxes.
     */
    public static GuideCheckBox[] wrap(IGuide[] aGuides)
    {
        GuideCheckBox[] boxes = new GuideCheckBox[aGuides.length];
        for (int i = 0; i < aGuides.length; i++)
        {
            boxes[i] = new GuideCheckBox(aGuides[i]);
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
        return GuideCheckBox.getSelected(model, true);
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
        return GuideCheckBox.getSelected(model, false);
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
            if (item instanceof GuideCheckBox && !(((GuideCheckBox)item).isSelected() ^ selected))
            {
                feeds.add(((GuideCheckBox)item).getGuide());
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
        return guide == null ? null : guide.getTitle();
    }
}
