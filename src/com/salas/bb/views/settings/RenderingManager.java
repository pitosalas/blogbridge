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
// $Id: RenderingManager.java,v 1.8 2008/04/08 08:06:18 spyromus Exp $
//

package com.salas.bb.views.settings;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Manages rendering settings and their changes. <p/>Manager can give to everyone interested current rendering settings
 * at any given moment of time. Also if someone wants to listen for updates of current settings it can register as
 * listener of all or particular properties and be updated.
 */
public class RenderingManager implements PropertyChangeListener
{
    private static final RenderingManager INSTANCE = new RenderingManager();
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private FeedRenderingSettings globalSettings;
    private FeedRenderingSettings localSettings;

    // Biased Font cache. Simply to avoid having to instantiate lots and lots of Font objects
    private static Font cacheArticleBodyFont;
    private static Font cacheArticleDateFont;
    private static Font cacheDividerTextFont;
    private static Font cacheUnreadTitleFont;
    private static Font cacheReadTitleFont;
    private static Font cacheArticleTitleFont;

    // Highlight Colors
    private Color lastSearchKeywordHC;
    private Color lastUnregBlogLinkHC;
    private Color lastRegBlogLinksHC;

    // Highlights Painters
    private Highlighter.HighlightPainter searchKeywordsHP;
    private Highlighter.HighlightPainter unregBlogLinksHP;
    private Highlighter.HighlightPainter regBlogLinksHP;

    /**
     * Returns pre-initialized object.
     * 
     * @return instance.
     */
    public static RenderingManager getInstance()
    {
        return INSTANCE;
    }

    private RenderingManager()
    {
        resetFontCache();
    }
 
    /**
     * Clear the saved Fonts from the private Font cache.
     */
    private void resetFontCache()
    {
        cacheArticleBodyFont = null;
        cacheDividerTextFont = null;
        cacheArticleDateFont = null;
        cacheReadTitleFont = null;
        cacheUnreadTitleFont = null;
        cacheArticleTitleFont = null;
    }
    
    // --- Events -------------------------------------------------------------

    /**
     * Registers settings property change listener.
     * 
     * @param l
     *        listener.
     */
    public static void addPropertyChangeListener(PropertyChangeListener l)
    {
        INSTANCE.pcs.addPropertyChangeListener(l);
    }

    /**
     * Register settings property change listener for specific property.
     * 
     * @param property
     *        property.
     * @param l
     *        listener.
     */
    public static void addPropertyChangeListener(String property, PropertyChangeListener l)
    {
        INSTANCE.pcs.addPropertyChangeListener(property, l);
    }

    /**
     * Removes settings property change listener.
     * 
     * @param l
     *        listener.
     */
    public static void removePropertyChangeListener(PropertyChangeListener l)
    {
        INSTANCE.pcs.removePropertyChangeListener(l);
    }

    /**
     * Removes setting property change listener registered for specific property.
     * 
     * @param property
     *        property.
     * @param l
     *        listener.
     */
    public void removePropertyChangeListener(String property, PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(property, l);
    }

    /**
     * This method is called when global or local settings are changed.
     * 
     * @param evt
     *        A PropertyChangeEvent object describing the event source and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        final Object source = evt.getSource();
        final boolean equalToGlobalSettings;

        // Reset the font Cache when theme or font bias changes
        final String prop = evt.getPropertyName();
        if (RenderingSettingsNames.THEME.equals(prop)|| RenderingSettingsNames.ARTICLE_FONT_BIAS.equals(prop))
        {
            resetFontCache();
        }

        
        // Dispatch the property change to listeners
        synchronized (this)
        {
            equalToGlobalSettings = source == globalSettings;
        }

        if ((equalToGlobalSettings && localSettings == null) || (source == localSettings))
        {
            // Respond to global settings changes only when there's not local
            // settings installed.
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    /**
     * Fires differences in properties between the settings.
     * 
     * @param oldSettings
     *        old settings.
     * @param newSettings
     *        new settings.
     */
    private void fireDifference(final FeedRenderingSettings oldSettings, final FeedRenderingSettings newSettings)
    {
        for (int i = 0; i < RenderingSettingsNames.KEYS.length; i++)
        {
            final String key = RenderingSettingsNames.KEYS[i];
            if (newSettings != null)
            {
                newSettings.fireChange(key, oldSettings == null ? null : oldSettings.get(key), newSettings.get(key));
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Registers global setting object. If we don't have local settings object yet then global settings will be
     * registered as local too.
     * 
     * @param settings
     *        new global settings.
     */
    public static void setGlobalSettings(FeedRenderingSettings settings)
    {
        INSTANCE.setGlobalSettings0(settings);
    }

    /**
     * Registers global setting object. If we don't have local settings object yet then global settings will be
     * registered as local too.
     * 
     * @param settings
     *        new global settings.
     */
    private synchronized void setGlobalSettings0(FeedRenderingSettings settings)
    {
        // We don't need to deal with exact copy
        if (this.globalSettings == settings) return;

        // If we have already global settings defined
        // then unregister from them
        if (this.globalSettings != null) this.globalSettings.removeListener(this);

        final FeedRenderingSettings oldSettings = this.globalSettings;
        this.globalSettings = settings;

        if (this.globalSettings != null) this.globalSettings.addListener(this);

        // Check the existence of local settings
        if (this.localSettings == null)
        {
            // no local settings - register global as local
            this.localSettings = this.globalSettings;
        } else
        {
            // present - register global as parent
            this.localSettings.setParent(this.globalSettings);
        }

        fireDifference(oldSettings, settings);
    }

    // ------------------------------------------------------------------------

    /**
     * Returns TRUE if grouping of articles is enabled.
     * 
     * @return TRUE if enabled.
     */
    public static boolean isGroupingEnabled()
    {
        return INSTANCE.localSettings.isGroupingEnabled();
    }

    /**
     * Returns TRUE if showing of empty groups is allowed.
     * 
     * @return TRUE if showing is allowed.
     */
    public static boolean isShowEmptyGroups()
    {
        return INSTANCE.localSettings.isShowEmptyGroups();
    }

    /**
     * Returns TRUE if date of the article creation should be showed.
     * 
     * @return TRUE if date showing is enabled.
     */
    public static boolean isArticleDateShowing()
    {
        return INSTANCE.localSettings.isArticleDateShowing();
    }

    /**
     * Returns TRUE if browsing in brief mode.
     * 
     * @return TRUE for brief mode.
     */
    public static boolean isBriefMode()
    {
        return INSTANCE.localSettings.getArticleViewMode() == FeedRenderingSettings.VIEW_MODE_BRIEF;
    }

    /**
     * Returns view mode of articles.
     * 
     * @return view mode.
     * 
     * @see FeedRenderingSettings#VIEW_MODE_FULL
     * @see FeedRenderingSettings#VIEW_MODE_BRIEF
     * @see FeedRenderingSettings#VIEW_MODE_MINIMAL
     */
    public static int getArticleViewMode()
    {
        return INSTANCE.localSettings.getArticleViewMode();
    }

    /**
     * Returns font for rendering of article body. Use value in cache if something is there
     * 
     * @return font for article body text.
     */
    public static Font getArticleBodyFont()
    {
        if (cacheArticleBodyFont == null) {
            cacheArticleBodyFont = INSTANCE.localSettings.getArticleBodyFont();
        }
        return cacheArticleBodyFont;
    }

    /**
     * Returns font for rendering of article title.
     * 
     * @return font for rendering of article title.
     */
    public static Font getArticleTitleFont()
    {
        if (cacheArticleTitleFont == null)
        {
            cacheArticleTitleFont = INSTANCE.localSettings.getArticleTitleFont();
        }
        return cacheArticleTitleFont;
    }

    /**
     * Returns font for rendering of article date.
     * 
     * @return font.
     */
    public static Font getArticleDateFont()
    {
        if (cacheArticleDateFont == null)
        {
            cacheArticleDateFont = INSTANCE.localSettings.getArticleDateFont();
        }
        return cacheArticleDateFont;
    }

    /**
     * Returns font for rendering of groups divider title text. Use cache
     * 
     * @return font.
     */
    public static Font getDividerTextFont()
    {
        if (cacheDividerTextFont == null)
        {
            cacheDividerTextFont = INSTANCE.localSettings.getDividerTextFont();
        }
        return cacheDividerTextFont;
    }

    /**
     * Returns color for selected article background.
     * 
     * @return color of background.
     */
    public static Color getSelectedArticleBackground()
    {
        return INSTANCE.localSettings.getSelectedArticleBackground();
    }

    /**
     * Returns color for normal article background.
     * 
     * @return color of background.
     */
    public static Color getArticleBackground()
    {
        return INSTANCE.localSettings.getArticleBackground();
    }

    /**
     * Returns color of article body background.
     * 
     * @return color of background.
     */
    public static Color getArticleBodyBackground()
    {
        return INSTANCE.localSettings.getArticleBodyBackground();
    }

    /**
     * Returns color for article title background.
     * 
     * @return color of background.
     */
    public static Color getArticleTitleBackground()
    {
        return INSTANCE.localSettings.getArticleTitleBackground();
    }

    /**
     * Returns grouping divider background color.
     * 
     * @return color of background.
     */
    public static Color getGroupingDividerColor()
    {
        return INSTANCE.localSettings.getGroupingDividerColor();
    }

    /**
     * Returns TRUE if we should suppress rendering of old articles.
     * 
     * @return TRUE to suppress.
     */
    public static boolean isSuppressingOlderThan()
    {
        return INSTANCE.localSettings.isSuppressingOlderThan();
    }

    /**
     * Returns number of days starting from which artiles should not be displayed.
     * 
     * @return number of days.
     */
    public static int getSuppressOlderThan()
    {
        return INSTANCE.localSettings.getSuppressOlderThan();
    }

    /**
     * Returns TRUE if ascending sorting is enabled.
     * 
     * @return TRUE to sort articles ascending.
     */
    public static boolean isSortingAscending()
    {
        return INSTANCE.localSettings.isSortingAscending();
    }

    /**
     * Returns number of characters for limiting of articles size while in brief mode.
     * 
     * @return number of chars.
     */
    public static int getArticleSizeLimit()
    {
        return INSTANCE.localSettings.getArticleSizeLimit();
    }

    /**
     * Returns Article Filtering code.
     * 
     * @return filtering code.
     */
    public static int getArticleFilter()
    {
        return INSTANCE.localSettings.getArticleFilter();
    }

    /**
     * Returns current font for titles of unread articles.
     * 
     * @return current unread article title font.
     */
    public static Font getUnreadArticleTitleFont()
    {
        if (cacheUnreadTitleFont == null)
        {
            cacheUnreadTitleFont = getArticleTitleFont().deriveFont(Font.BOLD);
        }
        return cacheUnreadTitleFont;
    }

    /**
     * Returns current font for titles of read articles.
     * 
     * @return current read article title font.
     */
    public static Font getReadArticleTitleFont()
    {
        if (cacheReadTitleFont == null)
        {
            cacheReadTitleFont = getArticleTitleFont().deriveFont(Font.PLAIN);
        }
        return cacheReadTitleFont;
    }

    /**
     * Returns painter for search keyword highlights.
     * 
     * @return painter.
     */
    public static synchronized Highlighter.HighlightPainter getSearchKeywordsHighlightPainter()
    {
        // Create new highlighter of search keywords only of it was not created yet or
        // the color changed.
        Color color = getSerachKeywordHighlightBackground();

        if (INSTANCE.searchKeywordsHP == null || !INSTANCE.lastSearchKeywordHC.equals(color))
        {
            INSTANCE.lastSearchKeywordHC = color;
            INSTANCE.searchKeywordsHP = new DefaultHighlighter.DefaultHighlightPainter(color);
        }

        return INSTANCE.searchKeywordsHP;
    }

    /**
     * Returns the background color for the search highlight.
     *
     * @return color.
     */
    public static Color getSerachKeywordHighlightBackground()
    {
        return Color.YELLOW;
    }

    /**
     * Returns color of unregistered blog links.
     *
     * @return color.
     */
    public static synchronized Color getUnregisteredBlogLinkColor()
    {
        return INSTANCE.localSettings.getUnregisteredBlogLinkColor();
    }

    /**
     * Returns color of registered blog links.
     *
     * @return color.
     */
    public static synchronized Color getRegisteredBlogLinkColor()
    {
        return INSTANCE.localSettings.getRegisteredBlogLinkColor();
    }

    /**
     * Returns painter for discovered and unregistered URL's.
     * 
     * @return painter.
     */
    public static synchronized Highlighter.HighlightPainter getUnregisteredUrlsHighlightPainter()
    {
        // Create new highlighter of unrgistered BlogLinks only of it was not created yet or
        // the color changed.
        Color currentColor = INSTANCE.localSettings.getUnregisteredBlogLinkColor();
        if (INSTANCE.unregBlogLinksHP == null || !INSTANCE.lastUnregBlogLinkHC.equals(currentColor))
        {
            INSTANCE.lastUnregBlogLinkHC = currentColor;
            INSTANCE.unregBlogLinksHP = new DefaultHighlighter.DefaultHighlightPainter(INSTANCE.lastUnregBlogLinkHC);
        }

        return INSTANCE.unregBlogLinksHP;
    }

    /**
     * Returns painter for discovered and registered URL's.
     * 
     * @return painter.
     */
    public static synchronized Highlighter.HighlightPainter getRegisteredUrlsHighlightPainter()
    {
        // Create new highlighter of unrgistered BlogLinks only of it was not created yet or
        // the color changed.
        Color currentColor = INSTANCE.localSettings.getRegisteredBlogLinkColor();
        if (INSTANCE.regBlogLinksHP == null || !INSTANCE.lastRegBlogLinksHC.equals(currentColor))
        {
            INSTANCE.lastRegBlogLinksHC = currentColor;
            INSTANCE.regBlogLinksHP = new DefaultHighlighter.DefaultHighlightPainter(INSTANCE.lastRegBlogLinksHC);
        }

        return INSTANCE.regBlogLinksHP;
    }

    /**
     * Returns value of flag for displaying of full article titles.
     * 
     * @return <code>TRUE</code> for on.
     */
    public static boolean isDisplayingFullTitles()
    {
        return INSTANCE.localSettings.isDisplayingFullTitles();
    }

    /**
     * Returns color of article title.
     * 
     * @param aSelected
     *        TRUE if color of selected article title needed.
     * 
     * @return color.
     */
    public static Color getArticleTitleColor(boolean aSelected)
    {
        return INSTANCE.localSettings.getArticleTitleColor(aSelected);
    }

    /**
     * Returns color of article text.
     * 
     * @param aSelected
     *        TRUE if color of selected article text needed.
     * 
     * @return color.
     */
    public static Color getArticleTextColor(boolean aSelected)
    {
        return INSTANCE.localSettings.getArticleTextColor(aSelected);
    }

    /**
     * Returns color of article date.
     * 
     * @param aSelected
     *        TRUE if color of selected article date needed.
     * 
     * @return color.
     */
    public static Color getArticleDateColor(boolean aSelected)
    {
        return INSTANCE.localSettings.getArticleDateColor(aSelected);
    }

    /**
     * Returns color for feed name in the header of articles list panel.
     * 
     * @return color for the feed name.
     */
    public static Color getArticleListFeedNameForeground()
    {
        return INSTANCE.localSettings.getArticleListFeedNameForeground();
    }

    /**
     * Returns font for feed name in the header of articles list panel.
     * 
     * @return font for the feed name.
     */
    public static Font getArticleListFeedNameFont()
    {
        return INSTANCE.localSettings.getArticleListFeedNameFont();
    }

    /**
     * Returns background color for feeds list.
     * 
     * @param alternating
     *        TRUE if alternating color required.
     * 
     * @return color.
     */
    public static Color getFeedsListBackground(boolean alternating)
    {
        return INSTANCE.localSettings.getFeedsListBackground(alternating);
    }

    /**
     * Returns selected background color for feeds list.
     * 
     * @return color.
     */
    public static Color getFeedsListSelectedBackground()
    {
        return INSTANCE.localSettings.getFeedsListSelectedBackground();
    }

    /**
     * Returns foreground color for feeds list.
     * 
     * @param selected
     *        TRUE if selected color required.
     * 
     * @return color.
     */
    public static Color getFeedsListForeground(boolean selected)
    {
        return INSTANCE.localSettings.getFeedsListForeground(selected);
    }
    
    /**
     * Returns whether to show starz in feeds area.
     * 
     * @return TRUE if starz should be shown in feeds.
     */
    public static boolean isShowStarz()
    {
        return INSTANCE.localSettings.isShowStarz();
    }
    
    /**
     * Returns whether to show unread count in feeds area.
     * 
     * @return TRUE if unread count should be shown in feeds.
     */
    public static boolean isShowUnreadInFeeds()
    {
        return INSTANCE.localSettings.isShowUnreadInFeeds();
    }
    
    /**
     * Returns whether to activity chart in feeds area.
     * 
     * @return TRUE if activity chart should be be shown in feeds.
     */
    public static boolean isShowActivityChart()
    {
        return INSTANCE.localSettings.isShowActivityChart();
    }
    
    /**
     * Returns whether to show icons in guides area.
     * 
     * @return TRUE if icons should be shown in guides.
     */
    public static boolean isShowIconInGuides()
    {
        return INSTANCE.localSettings.isShowIconInGuides();
    }

    /**
     * Returns whether to show text in guides area.
     *
     * @return TRUE if text should be shown in guides.
     */
    public static boolean isShowTextInGuides()
    {
        return INSTANCE.localSettings.isShowTextInGuides();
    }

    /**
     * Returns whether to show unread count in guides area.
     *
     * @return TRUE if unread count should be shown in guides.
     */
    public static boolean isShowUnreadInGuides()
    {
        return INSTANCE.localSettings.isShowUnreadInGuides();
    }

    /**
     * Returns whether to show big or small icon in guides area.
     *
     * @return TRUE to show big icons.
     */
    public static boolean isBigIconInGuides()
    {
        return INSTANCE.localSettings.isBigIconInGuides();
    }
}