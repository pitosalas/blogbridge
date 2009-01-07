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

package com.salas.bb.views;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.FeedType;

import javax.swing.*;
import java.awt.*;

/**
 * View type selector component.
 */
public class ViewTypeSelector extends AbstractSelectorComponent
{
    public static final int TYPE_TEXT   = FeedType.TYPE_TEXT;
    public static final int TYPE_IMAGE  = FeedType.TYPE_IMAGE;

    private static final String[] TYPES = { "text", "image" };
    private static final Icon[][] ICONS = prepareIcons(TYPES);

    private int xSep1;
    private int xText;

    /**
     * Creates selector component.
     *
     * @param aModeModel integer value model.
     */
    public ViewTypeSelector(ValueModel aModeModel)
    {
        super(aModeModel);
    }

    /**
     * Returns icon for a given mode and state.
     *
     * @param mode  mode.
     * @param state state.
     *
     * @return icon.
     */
    protected Icon getIcon(int mode, State state)
    {
        return ICONS[mode][state.ordinal()];
    }

    @Override
    protected Dimension getPreferredDimensions()
    {
        Dimension dimBtnText = getIconDimension(TYPE_TEXT, State.ON);
        Dimension dimBtnImage = getIconDimension(TYPE_IMAGE, State.ON);
        Dimension dimSep = getIconDimensions(getSeparatorIcon(State.ON));

        xSep1 = dimBtnText.width;
        xText = xSep1 + dimSep.width;
        int xSep2 = xText + dimBtnImage.width;
        int xImage = xSep2 + dimSep.width;

        return new Dimension(xImage, dimBtnText.height);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        State textState = getCurrentState(TYPE_TEXT);
        State imageState = getCurrentState(TYPE_IMAGE);

        State text2imageState = getSeparatorState(textState, imageState);

        getIcon(TYPE_TEXT, textState).paintIcon(this, g, 0, 0);
        getSeparatorIcon(text2imageState).paintIcon(this, g, xSep1, 0);
        getIcon(TYPE_IMAGE, imageState).paintIcon(this, g, xText, 0);
    }

    @Override
    protected int locationToMode(Point aPoint)
    {
        int md;
        int x = aPoint.x;

        if (x >= xText)
        {
            md = TYPE_IMAGE;
        } else
        {
            md = TYPE_TEXT;
        }

        return md;
    }
}
