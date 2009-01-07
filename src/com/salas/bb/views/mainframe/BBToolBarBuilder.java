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
// $Id: BBToolBarBuilder.java,v 1.46 2007/09/18 12:54:23 spyromus Exp $
//
package com.salas.bb.views.mainframe;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.util.Modes;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.actions.ActionsTable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Builder of application tool-bar.
 */
public class BBToolBarBuilder
{
    private static ToolbarLayout layout;

    /**
     * Create the builder.
     */
    static
    {
        layout = new ToolbarLayout();

        // Default layout
        layout.appendAction(ActionsTable.CMD_GUIDE_RELOAD_ALL_TB);
        layout.appendAction(ActionsTable.CMD_SYNC_FULL_TB);
        layout.appendLargeGap();

        layout.appendAction(ActionsTable.CMD_ARTICLE_GOTO_NEXT_UNREAD_TB);
        layout.appendLargeGap();

        layout.appendAction(ActionsTable.CMD_ARTICLE_BROWSE_TB);
        layout.appendLargeGap();

        layout.appendAction(ActionsTable.CMD_GUIDE_ADD_TB);
        layout.appendAction(ActionsTable.CMD_FEED_SUBSCRIBE_TB);
        layout.appendLargeGap();

        layout.appendAction(ActionsTable.CMD_FEED_ADD_SMART_FEED_TB);
        layout.appendLargeGap();

        layout.appendAction(ActionsTable.CMD_BB_TAGS_TB);
        layout.appendLargeGap();

        layout.appendAction(ActionsTable.CMD_BB_SEARCH_TB);
        layout.appendAction(ActionsTable.CMD_BB_WHATS_HOT_TB);
        layout.appendAction(ActionsTable.CMD_BB_STATISTICS_TB);
    }

    /**
     * Returns the toolbar layout.
     *
     * @return layout.
     */
    public static ToolbarLayout getLayout()
    {
        return layout;
    }

    /**
     * Creates, configures, and composes the tool bar.
     *
     * @return toolbar component.
     */
    public JToolBar buildToolBar()
    {
        ToolBarBuilder tbldr = new ToolBarBuilder("ToolBar");

        setToolBarSettings(tbldr);
        setToolBarJGoodiesSettings(tbldr);
        addLeftButtonSet(tbldr);

        return tbldr.getToolBar();
    }

    /**
     * Add Toolbar buttons on the left part of the ToolBar.
     *
     * @param bldr          builder.
     */
    private void addLeftButtonSet(ToolBarBuilder bldr)
    {
        bldr.addGap();

        layout.build(bldr);

        bldr.addGap();
    }

    /**
     * Creates and answers a button which is suitable for use in a tool bar.
     *
     * @param action action.
     *
     * @return button.
     */
    private static BBToolBarButton createToolbarButton(Action action)
    {
        BBToolBarButton toolBarBtn = new BBToolBarButton(action);
        toolBarBtn.setWideMarginMode(Modes.ALWAYS);
        return toolBarBtn;
    }

    /**
     * JGoodies specific setup.
     *
     * @param bldr builder.
     */
    private void setToolBarJGoodiesSettings(ToolBarBuilder bldr)
    {
        JToolBar toolBar = bldr.getToolBar();

        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.putClientProperty(PlasticLookAndFeel.IS_3D_KEY, Boolean.TRUE);
        toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);

        // Set a hint so that JGoodies Looks will detect it as being in the header.
//        toolBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);

        // Unlike the default, use a separator border.
        toolBar.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY, BorderStyle.EMPTY);
        toolBar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY, BorderStyle.EMPTY);
    }

    /**
     * Placement and other specific configuration.
     *
     * @param bldr builder.
     */
    private void setToolBarSettings(ToolBarBuilder bldr)
    {
        // Give the toolbar a little breathing space on non-mac
        if (!SystemUtils.IS_OS_MAC)
        {
            bldr.getToolBar().setMargin(new Insets(2, 0, 1, 0));
        }
    }

    /**
     * Toolbar layout.
     */
    public static class ToolbarLayout
    {
        private static final String LARGE_GAP = "large-gap";

        private java.util.List<Object> layout = new LinkedList<Object>();

        /**
         * Clears the layout.
         */
        public void clear()
        {
            layout.clear();
        }

        /**
         * Appends a large gap to the end of the layout.
         */
        public void appendLargeGap()
        {
            layout.add(LARGE_GAP);
        }

        /**
         * Appends the action to the end of the layout.
         *
         * @param action action.
         */
        public void appendAction(Action action)
        {
            layout.add(action);
        }

        /**
         * Appends the action to the end of the layout.
         *
         * @param action action.
         */
        public void appendAction(String action)
        {
            layout.add(action);
        }

        /**
         * Builds the toolbar from the layout.
         *
         * @param builder builder.
         */
        void build(ToolBarBuilder builder)
        {
            for (Object el : layout)
            {
                if (el instanceof Action)
                {
                    builder.add(createToolbarButton((Action)el));
                } else if (el == LARGE_GAP)
                {
                    builder.addLargeGap();
                } else if (el instanceof String)
                {
                    builder.add(createToolbarButton(ActionManager.get((String)el)));
                }
            }
        }
    }
}
