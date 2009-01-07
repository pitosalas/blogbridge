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
// $Id: ViewModeSelector.java,v 1.8 2007/06/13 10:31:59 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.binding.value.ValueModel;

import javax.swing.*;
import java.awt.*;

/**
 * View mode selector component.
 */
public class ViewModeSelector extends AbstractSelectorComponent
{
    public static final int MODE_MINI   = 0;
    public static final int MODE_BRIEF  = 1;
    public static final int MODE_FULL   = 2;

    private static final String[] MODES = { "mini", "brief", "full" };
    private static final Icon[][] ICONS = prepareIcons(MODES);

    private int xSep1;
    private int xBrief;
    private int xSep2;
    private int xFull;

    /**
     * Creates selector component.
     *
     * @param aModeModel integer value model.
     */
    public ViewModeSelector(ValueModel aModeModel)
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
        Dimension dimBtnMini = getIconDimension(MODE_MINI, State.ON);
        Dimension dimBtnBrief = getIconDimension(MODE_BRIEF, State.ON);
        Dimension dimBtnFull = getIconDimension(MODE_FULL, State.ON);
        Dimension dimSep = getIconDimensions(getSeparatorIcon(State.ON));

        xSep1 = dimBtnMini.width;
        xBrief = xSep1 + dimSep.width;
        xSep2 = xBrief + dimBtnBrief.width;
        xFull = xSep2 + dimSep.width;

        return new Dimension(xFull + dimBtnFull.width, dimBtnMini.height);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        State miniState = getCurrentState(MODE_MINI);
        State briefState = getCurrentState(MODE_BRIEF);
        State fullState = getCurrentState(MODE_FULL);

        State mini2briefState = getSeparatorState(miniState, briefState);
        State brief2fullState = getSeparatorState(briefState, fullState);

        getIcon(MODE_MINI, miniState).paintIcon(this, g, 0, 0);
        getSeparatorIcon(mini2briefState).paintIcon(this, g, xSep1, 0);
        getIcon(MODE_BRIEF, briefState).paintIcon(this, g, xBrief, 0);
        getSeparatorIcon(brief2fullState).paintIcon(this, g, xSep2, 0);
        getIcon(MODE_FULL, fullState).paintIcon(this, g, xFull, 0);
    }

    @Override
    protected int locationToMode(Point aPoint)
    {
        int md;
        int x = aPoint.x;

        if (x >= xFull)
        {
            md = MODE_FULL;
        } else if (x >= xBrief)
        {
            md = MODE_BRIEF;
        } else
        {
            md = MODE_MINI;
        }

        return md;
    }
}
