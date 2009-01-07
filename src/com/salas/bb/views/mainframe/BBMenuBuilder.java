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
// $Id: BBMenuBuilder.java,v 1.71 2008/04/01 08:35:54 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.action.ToggleAction;
import com.jgoodies.uif.builder.MenuBuilder;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.sentiments.SentimentsFeature;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Manages the Creation of the BlogBridge menus.
 */
public class BBMenuBuilder
{
    /**
     * <code>MENUBAR_MARGIN</code>- margin to be used for menu bar. (according to Karsten :-)
     */
    private static final Insets MENUBAR_MARGIN = new Insets(0, 4, 0, 0);

    /**
     * Build BlogBridge menu bar.
     *
     * @return the resulting menu bar.
     */
    public JMenuBar buildMenuBar()
    {
        JMenuBar myMenu = new JMenuBar();
        myMenu.setMargin(MENUBAR_MARGIN);

        // Apply some special style hints for JGoodies
        myMenu.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        myMenu.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);
        myMenu.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY, BorderStyle.SEPARATOR);

        myMenu.add(buildGuidesMenu());
        myMenu.add(buildFeedsMenu());
        myMenu.add(buildArticlesMenu());
        myMenu.add(buildToolsMenu());
        myMenu.add(buildHelpMenu());
        return myMenu;
    }

    /**
     * Create the Guides menu.
     *
     * @return the created menu.
     */
    private JMenu buildGuidesMenu()
    {
        MenuBuilder bld = createBuilder("guides");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_WHATIS));
        bld.addSeparator();

        menu.add(buildGuideGetLatestSubmenu());
        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_ADD));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_DELETE));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MERGE));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_SUBSCRIBE_READINGLIST));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_POST_TO_BLOG));
        bld.addSeparator();

        menu.add(buildGuideMarkReadSubmenu());
        menu.add(buildGuideMarkUnreadSubmenu());
        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_IMPORT));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_EXPORT));
        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_SORT_BY_TITLE));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_PROPERTIES));

        if (!SystemUtils.IS_OS_MAC)
        {
            bld.addSeparator();
            menu.add(ActionManager.get(ActionsTable.CMD_BB_EXIT));
        }

        return bld.getMenu();
    }

    /**
     * Creates menu builder for a given menu.
     *
     * @param menu  menu name.
     *
     * @return builder.
     */
    private static MenuBuilder createBuilder(String menu)
    {
        String title = Strings.message("mainmenu." + menu);
        char mnemonic = Strings.message("mainmenu." + menu + ".m").charAt(0);

        return new MenuBuilder(title, mnemonic);
    }

    /**
     * Creates the Get Latest guides submenu.
     *
     * @return created menu.
     */
    private JMenu buildGuideGetLatestSubmenu()
    {
        MenuBuilder bld = createBuilder("guides.getlatest");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_RELOAD_ALL_SM));
        menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_RELOAD_SM));

        return bld.getMenu();
    }

    /**
     * Creates the Mark as Read submenu.
     *
     * @return created menu.
     */
    private JMenu buildGuideMarkReadSubmenu()
    {
    	MenuBuilder bld = createBuilder("guides.markread");
    	JMenu menu = bld.getMenu();

    	menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MARK_ALL_READ));
    	menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MARK_READ_SM));

    	return bld.getMenu();
    }

    /**
     * Creates the Mark as Unread submenu.
     *
     * @return created menu.
     */
    private JMenu buildGuideMarkUnreadSubmenu()
    {
    	MenuBuilder bld = createBuilder("guides.markunread");
    	JMenu menu = bld.getMenu();

    	menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MARK_ALL_UNREAD));
    	menu.add(ActionManager.get(ActionsTable.CMD_GUIDE_MARK_UNREAD_SM));

    	return bld.getMenu();

    }

    /**
     * Create the Feeds Menu.
     *
     * @return the created menu.
     */
    private JMenu buildFeedsMenu()
    {
        MenuBuilder bld = createBuilder("feeds");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_FEED_WHATIS));

        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_FEED_RELOAD));

        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_FEED_SUBSCRIBE));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_ADD_SMART_FEED));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_DELETE));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_PROPERTIES));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_TAGS));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_POST_TO_BLOG));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_DISCOVER));

        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_FEED_BROWSE));

        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_FEED_MARK_READ));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_MARK_UNREAD));

        bld.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_FEED_RATING_UP));
        menu.add(ActionManager.get(ActionsTable.CMD_FEED_RATING_DOWN));

        return bld.getMenu();
    }

    /**
     * Create the Articles Menu.
     *
     * @return the created menu.
     */
    private JMenu buildArticlesMenu()
    {
        MenuBuilder bld = createBuilder("articles");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_WHATIS));

        bld.addSeparator();

        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_MARK_READ));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_MARK_UNREAD));

        bld.addSeparator();

        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_BROWSE));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_COPY_LINK));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_SEND_LINK));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_POST_TO_BLOG));

        bld.addSeparator();

        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_PROPERTIES));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_TAGS));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_PIN_UNPIN));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_DISCOVER));

        bld.addSeparator();

        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_GOTO_NEXT_UNREAD));

        bld.addSeparator();

        bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_SHOW_ALL));
        bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_SHOW_UNREAD));
        bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_SHOW_PINNED));

        if (SentimentsFeature.isAvailable())
        {
            bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_SHOW_POSITIVE));
            bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_SHOW_NEGATIVE));
            bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_SHOW_NON_NEGATIVE));
        }

        bld.addSeparator();
        bld.add(buildViewModeSubmenu());
        bld.add(buildTextSizeSubmenu());
        return bld.getMenu();
    }

    /**
     * Builds and creates the ViewMode submenu for the Articles menu.
     *
     * @return created menu.
     */
    private JMenu buildViewModeSubmenu()
    {
        MenuBuilder bld = createBuilder("articles.viewmode");
        bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_VIEWMODE_MINI_TB));
        bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_VIEWMODE_BRIEF_TB));
        bld.addToggle((ToggleAction)ActionManager.get(ActionsTable.CMD_ARTICLE_VIEWMODE_FULL_TB));

        return bld.getMenu();
    }

    /**
     * Create and populate the Text Size submenuu
     * @return the created menu.
     */
    private JMenu buildTextSizeSubmenu()
    {
        MenuBuilder bld = createBuilder("articles.textsize");
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_FONT_BIGGER));
        bld.add(ActionManager.get(ActionsTable.CMD_ARTICLE_FONT_SMALLER));
        return bld.getMenu();
    }

    /**
     * Create the BlogBridge menu.
     *
     * @return the created menu.
     */
    private JMenu buildToolsMenu()
    {
        MenuBuilder bld = createBuilder("tools");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_BB_SEARCH));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_WHATS_HOT));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_STATISTICS));
        menu.addSeparator();

        menu.add(ActionManager.get(ActionsTable.CMD_BB_CLEANUP_WIZARD));
        menu.add(ActionManager.get(ActionsTable.CMD_SYNC_FULL));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_SERVICE));

        menu.addSeparator();
        menu.add(ActionManager.get(ActionsTable.CMD_BB_ACTIVITY));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_ONLINEOFFLINE));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_FORGET_PASSWORDS));
        // We don't do any updates checks when under JWS
        if (ApplicationLauncher.isAutoUpdatesEnabled())
        {
            menu.add(ActionManager.get(ActionsTable.CMD_BB_CHECK_FOR_UPDATES));
        }
        menu.add(buildDatabaseSubmenu());
        menu.add(buildSettingsSubmenu());

        return bld.getMenu();
    }


    /**
     * Creates the Database submenu.
     *
     * @return created menu.
     */
    private JMenu buildDatabaseSubmenu()
    {
        MenuBuilder bld = createBuilder("tools.database");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_BB_DATABASE_COMPACT));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_DATABASE_BACKUP));

        return bld.getMenu();
    }

    /**
     * Creates the Sesttings submenu.
     *
     * @return created menu.
     */
    private JMenu buildSettingsSubmenu()
    {
        MenuBuilder bld = createBuilder("tools.settings");
        JMenu menu = bld.getMenu();

        menu.add(ActionManager.get(ActionsTable.CMD_BB_BLOGSTARZ));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_PLUGIN_MANAGER));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_IMAGE_BLOCKER));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_SENTIMENT_ANALYSIS));

        // On a Mac, the Preferences and Exit commands are on the Application
        // menu not here.
        if (!SystemUtils.IS_OS_MAC)
        {
            menu.add(ActionManager.get(ActionsTable.CMD_BB_PREFERENCES));
        }

        return bld.getMenu();
    }

    /**
     * Create the Help Menu.
     *
     * @return the created menu.
     */
    private JMenu buildHelpMenu()
    {
        MenuBuilder bld = createBuilder("help");
        JMenu menu = bld.getMenu();
/*
        menu.add(ActionManager.get(ActionsTable.CMD_OPEN_HELP));
*/
        menu.add(ActionManager.get(ActionsTable.CMD_BB_FAQ));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_HOME));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_SEND_FEEDBACK));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_TIP_OF_THE_DAY));
        menu.add(ActionManager.get(ActionsTable.CMD_BB_KEYBOARD_SHORTCUTS));

        if (!SystemUtils.IS_OS_MAC)
        {
            menu.add(ActionManager.get(ActionsTable.CMD_BB_ABOUT));
        }

        return bld.getMenu();
    }
}