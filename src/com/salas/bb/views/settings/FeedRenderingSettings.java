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
// $Id: FeedRenderingSettings.java,v 1.14 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.settings;

import com.jgoodies.binding.beans.Model;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.themes.ITheme;
import com.salas.bb.views.themes.Theme;
import com.salas.bb.views.themes.ThemeKey;
import com.salas.bb.views.themes.ThemeSupport;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Settings for articles and feed rendering. Setting these values doesn't mean that changes
 * become visible immediately. This class is only the storage for them.
 * <p/>
 * The objects of this class hold settings data in maps. Access to settings may be provided through
 * simple <code>get/set</code> methods or through special accessors (which is preferrable).
 * <p/>
 * Unlike previous revision you don't need to fire any events on changes by hand. Instead, every
 * object notifies all his listeners on change in settings immediately.
 * <p/>
 * Each object may have associated parent object which will be the source of values if current
 * object doesn't have one. NOTE: IT'S ASSUMED THAT EACH PROPERTY HAS ITS VALUE IN THIS INSTANCE OR
 * IN PARENT. IF IT DOESN'T THEN YOU WILL PROBABLY GET NULL POINTER EXCEPTION.
 * <p/>
 * Each object is listening for its parent changes as well and passes events upper only if it has no
 * overwritten value for the property changed in parent object and the changed value affects the
 * returned value of this object during the call to it.
 */
public final class FeedRenderingSettings extends Model implements IFRS, PropertyChangeListener
{
    private static final Logger LOG = Logger.getLogger(FeedRenderingSettings.class.getName());

    private static final int RGB_MASK = 0xffffff;
    private static final String RESOURCE_DEFAULT_THEME_NAME = "theme.default.name";

    private HashMap<String, Object> settings = new HashMap<String, Object>();
    private IFRS parent;

    /** Mode when articles are shown completely. */
    public static final Integer VIEW_MODE_FULL = IFeedDisplayConstants.MODE_FULL;
    /** Mode when articles are shown in plain text with limited number of symbols. */
    public static final Integer VIEW_MODE_BRIEF = IFeedDisplayConstants.MODE_BRIEF;
    /** Mode when only titles of the articles are shown. */
    public static final Integer VIEW_MODE_MINIMAL = IFeedDisplayConstants.MODE_MINIMAL;

    // Visual theme
    private ITheme theme;

    /**
     * Registers parent settings object. When we have no value for the specified
     * property to return on <code>get()</code> call we will try to get it from
     * our parent.
     *
     * @param aParent parent settings.
     */
    public void setParent(IFRS aParent)
    {
        if (this.parent == aParent) return;

        // Unregister current parent
        if (this.parent != null) this.removeListener(this);

        // Replace parent wih new one
        IFRS oldParent = this.parent;
        this.parent = aParent;
        if (this.parent != null)
        {
            this.parent.addListener(this);
        }

        // Perform check if some of non-overwritten settings changed
        for (int i = 0; i < RenderingSettingsNames.KEYS.length; i++)
        {
            if (!isOverwritten(RenderingSettingsNames.KEYS[i]))
                firePropertyChange(RenderingSettingsNames.KEYS[i],
                        oldParent == null ? null : oldParent.get(RenderingSettingsNames.KEYS[i]),
                        aParent == null ? null : aParent.get(RenderingSettingsNames.KEYS[i]));
        }
    }

    /**
     * Returns value of a setting under specified key.
     *
     * @param key key name of the setting.
     * @return value of the setting.
     */
    public Object get(final String key)
    {
        Object oval = settings.get(key);
        return (oval == null && parent != null) ? parent.get(key) : oval;
    }

    /**
     * Registers new value of the setting.
     *
     * @param key   key name of the setting.
     * @param value value of the setting or NULL to remove setting value.
     */
    public void set(final String key, final Object value)
    {
        Object oldValue = get(key);
        if (value == null)
        {
            settings.remove(key);
        } else
        {
            settings.put(key, value);
        }

        // fire change of the property
        fireChange(key, oldValue, get(key));
    }

    /**
     * Fires change in property.
     *
     * @param key      name of property.
     * @param oldValue old value.
     * @param newValue new value.
     */
    public void fireChange(final String key, final Object oldValue, final Object newValue)
    {
        firePropertyChange(key, oldValue, newValue);
    }

    /**
     * Returns TRUE if current settings list has overwritten property
     * with the given name.
     *
     * @param property propery name.
     * @return TRUE if has value.
     */
    private boolean isOverwritten(final String property)
    {
        return settings.get(property) != null;
    }

    /**
     * Removes all properties values.
     */
    public void reset()
    {
        final Set<String> set = settings.keySet();
        String[] keys = set.toArray(new String[set.size()]);
        for (String key : keys)
        {
            Object oval = settings.get(key);
            settings.remove(key);
            firePropertyChange(key, oval, get(key));
        }
    }

    /**
     * Restores current settings object from preferences.
     *
     * @param preferences preferences object to get data from.
     */
    public void restoreFrom(Preferences preferences)
    {
        setBoolean(RenderingSettingsNames.IS_GROUPING_ENABLED, preferences);
        setBoolean(RenderingSettingsNames.IS_SHOW_EMPTY_GROUPS, preferences);
        setBoolean(RenderingSettingsNames.IS_ARTICLE_DATE_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_SORTING_ASCENDING, preferences);
        setBoolean(RenderingSettingsNames.IS_SUPPRESSING_OLDER_THAN, preferences);
        setBoolean(RenderingSettingsNames.IS_DISPLAYING_FULL_TITLES, preferences);
        setBoolean(RenderingSettingsNames.IS_STARZ_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_UNREAD_IN_FEEDS_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_ACTIVITY_CHART_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_UNREAD_IN_GUIDES_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING, preferences);
        setBoolean(RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES, preferences);

        setInteger(RenderingSettingsNames.SUPPRESS_OLDER_THAN, preferences);
        setInteger(RenderingSettingsNames.ARTICLE_SIZE_LIMIT, preferences);
        setInteger(RenderingSettingsNames.ARTICLE_VIEW_MODE, preferences);
        setInteger(RenderingSettingsNames.ARTICLE_FILTER, preferences);

        // restore theme
        String defaultThemeName = ResourceUtils.getString(RESOURCE_DEFAULT_THEME_NAME);
        String themeName = preferences.get(RenderingSettingsNames.THEME, defaultThemeName);
        
        ITheme restoredTheme = ThemeSupport.getThemeByName(themeName);

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Default theme name : " + defaultThemeName);
            LOG.fine("Stored theme name  : " + themeName);
            LOG.fine("Loaded theme name  : " + restoredTheme.getName());
        }

        if (restoredTheme == null) restoredTheme = ThemeSupport.getThemeByName(defaultThemeName);
        setTheme(restoredTheme);
        
        setInteger(RenderingSettingsNames.ARTICLE_FONT_BIAS, preferences);

        // Main content font
        String mcfS = preferences.get(RenderingSettingsNames.MAIN_CONTENT_FONT, null);
        Font fnt = mcfS == null ? null : Font.decode(mcfS);
        setMainContentFont(fnt);
    }

    /**
     * Sets boolean property with value taken from preferences stored for the given key.
     *
     * @param key   key of setting.
     * @param prefs preferences object.
     */
    private void setBoolean(final String key, final Preferences prefs)
    {
        if (prefs.get("render." + key, null) != null)
        {
            set(key, Boolean.valueOf(prefs.getBoolean("render." + key, false)));
        } else
        {
            set(key, null);
        }
    }

    /**
     * Sets integer property with value taken from preferences stored for the given key.
     *
     * @param key   key of setting.
     * @param prefs preferences object.
     */
    private void setInteger(final String key, final Preferences prefs)
    {
        if (prefs.get("render." + key, null) != null)
        {
            set(key, new Integer(prefs.getInt("render." + key, 0)));
        } else
        {
            set(key, null);
        }
    }

    /**
     * Saves all the properties into preferences store.
     *
     * @param prefs preferences storage.
     */
    public void storeIn(Preferences prefs)
    {
        String[] keys = settings.keySet().toArray(new String[0]);

        // iterate through all the KEYS and store them
        for (final String key : keys)
        {
            final Object oval = settings.get(key);

            // Skip NULL values
            if (oval == null) continue;

            final String newKey = "render." + key;
            if (oval instanceof Boolean)
            {
                prefs.putBoolean(newKey, (Boolean)oval);
            } else if (oval instanceof Integer)
            {
                prefs.putInt(newKey, (Integer)oval);
            } else if (oval instanceof String)
            {
                prefs.put(newKey, (String)oval);
            } else if (oval instanceof Color)
            {
                final Color cval = (Color)oval;
                prefs.put(newKey, "#" + Integer.toHexString(cval.getRGB() & RGB_MASK));
            } else if (oval instanceof Font)
            {
                final Font fval = (Font)oval;
                putFont(prefs, newKey, fval);
            }
        }

        // save theme
        if (theme != null) prefs.put(RenderingSettingsNames.THEME, theme.getName());
        if (getMainContentFont() != null)
            putFont(prefs, RenderingSettingsNames.MAIN_CONTENT_FONT, getMainContentFont());
    }

    /**
     * Puts font in the preferences.
     *
     * @param prefs preferences.
     * @param key   key.
     * @param font  value.
     */
    private void putFont(Preferences prefs, String key, Font font)
    {
        prefs.put(key, UifUtilities.fontToString(font));
    }

    /**
     * Registers listener of settings changes.
     *
     * @param l new listener object.
     */
    public void addListener(PropertyChangeListener l)
    {
        super.addPropertyChangeListener(l);
    }

    /**
     * Removes listener of settings changes.
     *
     * @param l listener object.
     */
    public void removeListener(PropertyChangeListener l)
    {
        super.removePropertyChangeListener(l);
    }

    /**
     * This method gets called when something changes in parent settings.
     * We are looking if we have overwritten value for that setting and
     * if we don't then we fire this event further as the change is affecting
     * our own value.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        final String property = evt.getPropertyName();
        if (!isOverwritten(property))
        {
            firePropertyChange(property, evt.getOldValue(), evt.getNewValue());
        }
    }

    // --- Implementation of particular properties ----------------------------

    /**
     * Returns view mode of articles.
     *
     * @return view mode.
     *
     * @see FeedRenderingSettings#VIEW_MODE_FULL
     * @see FeedRenderingSettings#VIEW_MODE_BRIEF
     * @see FeedRenderingSettings#VIEW_MODE_MINIMAL
     */
    public int getArticleViewMode()
    {
        return (Integer)get(RenderingSettingsNames.ARTICLE_VIEW_MODE);
    }

    /**
     * Sets new articles view mode.
     *
     * @param mode mode.
     *
     * @see FeedRenderingSettings#VIEW_MODE_FULL
     * @see FeedRenderingSettings#VIEW_MODE_BRIEF
     * @see FeedRenderingSettings#VIEW_MODE_MINIMAL
     */
    public void setArticleViewMode(int mode)
    {
        set(RenderingSettingsNames.ARTICLE_VIEW_MODE, new Integer(mode));
    }

    /**
     * Returns TRUE if grouping of articles is enabled.
     *
     * @return TRUE if enabled.
     */
    public boolean isGroupingEnabled()
    {
        return (Boolean)get(RenderingSettingsNames.IS_GROUPING_ENABLED);
    }

    /**
     * Sets new value for grouping of articles.
     *
     * @param groupingEnabled TRUE to enable.
     */
    public void setGroupingEnabled(boolean groupingEnabled)
    {
        set(RenderingSettingsNames.IS_GROUPING_ENABLED, Boolean.valueOf(groupingEnabled));
    }

    /**
     * Returns TRUE if showing of empty groups is allowed.
     *
     * @return TRUE if showing is allowed.
     */
    public boolean isShowEmptyGroups()
    {
        return (Boolean)get(RenderingSettingsNames.IS_SHOW_EMPTY_GROUPS);
    }

    /**
     * Sets new state of showing empty groups.
     *
     * @param showEmptyGroups TRUE to allow showing.
     */
    public void setShowEmptyGroups(boolean showEmptyGroups)
    {
        set(RenderingSettingsNames.IS_SHOW_EMPTY_GROUPS, Boolean.valueOf(showEmptyGroups));
    }

    /**
     * Returns TRUE if date of the article creation should be showed.
     *
     * @return TRUE if date showing is enabled.
     */
    public boolean isArticleDateShowing()
    {
        return (Boolean)get(RenderingSettingsNames.IS_ARTICLE_DATE_SHOWING);
    }

    /**
     * Sets the state of showing article date.
     *
     * @param articleDateShowing TRUE to enable showing of dates.
     */
    public void setArticleDateShowing(boolean articleDateShowing)
    {
        set(RenderingSettingsNames.IS_ARTICLE_DATE_SHOWING, Boolean.valueOf(articleDateShowing));
    }

    /**
     * Returns TRUE if Starz should be shown.
     *
     * @return TRUE if showing Starz is enabled.
     */
    public boolean isShowStarz()
    {
        return (Boolean)get(RenderingSettingsNames.IS_STARZ_SHOWING);
    }

    /**
     * Sets the state of showing Starz.
     *
     * @param showStarz TRUE to enable showing of starz.
     */
    public void setShowStarz(boolean showStarz)
    {
        set(RenderingSettingsNames.IS_STARZ_SHOWING, Boolean.valueOf(showStarz));
    }


    /**
     * Sets the state of showing unread counts in feeds.
     *
     * @param showUnreadInFeeds TRUE to enable showing of unread counts in feeds.
     */
    public void setShowUnreadInFeeds(boolean showUnreadInFeeds)
    {
        set(RenderingSettingsNames.IS_UNREAD_IN_FEEDS_SHOWING, Boolean.valueOf(showUnreadInFeeds));
    }

    /**
     * Returns TRUE if unread counts in feeds should be shown.
     *
     * @return TRUE if showing unread counts in feeds is enabled.
     */
    public boolean isShowUnreadInFeeds()
    {
        return (Boolean)get(RenderingSettingsNames.IS_UNREAD_IN_FEEDS_SHOWING);
    }

    /**
     * Returns TRUE if article activity chart should be shown in feed area.
     *
     * @return TRUE if showing article activity chart is enabled.
     */
    public boolean isShowActivityChart()
    {
        return (Boolean)get(RenderingSettingsNames.IS_ACTIVITY_CHART_SHOWING);
    }

    /**
     * Sets the state of showing the article activity chart.
     *
     * @param showActivityChart TRUE to enable showing of activity chart.
     */
    public void setShowActivityChart(boolean showActivityChart)
    {
        set(RenderingSettingsNames.IS_ACTIVITY_CHART_SHOWING, Boolean.valueOf(showActivityChart));
    }

    /**
     * Sets the state of showing unread counts in guides.
     *
     * @param show TRUE to enable showing of unread counts in guides.
     */
    public void setShowUnreadInGuides(boolean show)
    {
        set(RenderingSettingsNames.IS_UNREAD_IN_GUIDES_SHOWING, Boolean.valueOf(show));
    }

    /**
     * Returns <code>TRUE</code> if unread counts in guides should be shown.
     *
     * @return <code>TRUE</code> if showing unread counts in guides is enabled.
     */
    public boolean isShowUnreadInGuides()
    {
        return (Boolean)get(RenderingSettingsNames.IS_UNREAD_IN_GUIDES_SHOWING);
    }

    /**
     * Sets the state of showing icons in guides.
     *
     * @param show TRUE to enable showing of icons in guides.
     */
    public void setShowIconInGuides(boolean show)
    {
        set(RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING, Boolean.valueOf(show));
    }

    /**
     * Returns <code>TRUE</code> if icons in guides should be shown.
     *
     * @return <code>TRUE</code> if showing icons in guides is enabled.
     */
    public boolean isShowIconInGuides()
    {
        return (Boolean)get(RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING);
    }

    /**
     * Sets the state of showing text in guides.
     *
     * @param show TRUE to enable showing of text in guides.
     */
    public void setShowTextInGuides(boolean show)
    {
        set(RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING, Boolean.valueOf(show));
    }

    /**
     * Returns <code>TRUE</code> if text in guides should be shown.
     *
     * @return <code>TRUE</code> if showing text in guides is enabled.
     */
    public boolean isShowTextInGuides()
    {
        return (Boolean)get(RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING);
    }

    /**
     * Sets the size of icon in guides.
     *
     * @param big <code>TRUE</code> for a big icon.
     */
    public void setBigIconInGuides(boolean big)
    {
        set(RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES, Boolean.valueOf(big));
    }

    /**
     * Returns <code>TRUE</code> if big icon should be shown in guides.
     *
     * @return <code>TRUE</code> if big icon should be shown in guides.
     */
    public boolean isBigIconInGuides()
    {
        return (Boolean)get(RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES);
    }

    // Theming -------------------------------------------------------------------------------------

    /**
     * Returns font for rendering of article body.
     *
     * @return font for article body text.
     */
    public Font getArticleBodyFont()
    {
        return getTheme().getFontWithBias(ThemeKey.Font.ARTICLE_TEXT, getArticleFontBias());
    }

    /**
     * Returns font for rendering of article title.
     *
     * @return font for rendering of article title.
     */
    public Font getArticleTitleFont()
    {
        return getTheme().getFontWithBias(ThemeKey.Font.ARTICLE_TITLE, getArticleFontBias());
    }

    /**
     * Returns font for rendering of article date.
     *
     * @return font.
     */
    public Font getArticleDateFont()
    {
        return getTheme().getFontWithBias(ThemeKey.Font.ARTICLE_DATE, getArticleFontBias());
    }

    /**
     * Returns font for rendering of groups divider title text.
     *
     * @return font.
     */
    public Font getDividerTextFont()
    {
        return getTheme().getFontWithBias(ThemeKey.Font.ARTICLEGROUP, getArticleFontBias());
    }

    /**
     * Returns color for selected article background.
     *
     * @return color of background.
     */
    public Color getSelectedArticleBackground()
    {
        return getTheme().getColor(ThemeKey.Color.ARTICLE_SEL_BG);
    }

    /**
     * Returns color for normal article background.
     *
     * @return color of background.
     */
    public Color getArticleBackground()
    {
        return getTheme().getColor(ThemeKey.Color.ARTICLE_UNSEL_BG);
    }

    /**
     * Returns color of article body background.
     *
     * @return color of background.
     */
    public Color getArticleBodyBackground()
    {
        // TODO what this for?
        return getTheme().getColor(ThemeKey.Color.ARTICLE_UNSEL_BG);
    }

    /**
     * Returns color for article title background.
     *
     * @return color of background.
     */
    public Color getArticleTitleBackground()
    {
        return getTheme().getColor(ThemeKey.Color.ARTICLE_UNSEL_BG);
    }

    /**
     * Returns color for feed name in the header of articles list panel.
     *
     * @return color for the feed name.
     */
    public Color getArticleListFeedNameForeground()
    {
        return getTheme().getColor(ThemeKey.Color.ARTICLELIST_FEEDNAME_FG);
    }

    /**
     * Returns font for feed name in the header of articles list panel.
     *
     * @return font for the feed name.
     */
    public Font getArticleListFeedNameFont()
    {
        return getTheme().getFont(ThemeKey.Font.ARTICLELIST_FEEDNAME);
    }

    /**
     * Returns grouping divider background color.
     *
     * @return color of background.
     */
    public Color getGroupingDividerColor()
    {
        return getTheme().getColor(ThemeKey.Color.ARTICLEGROUP_BG);
    }

    /**
     * Returns TRUE if we should suppress rendering of old articles.
     *
     * @return TRUE to suppress.
     */
    public boolean isSuppressingOlderThan()
    {
        return (Boolean)get(RenderingSettingsNames.IS_SUPPRESSING_OLDER_THAN);
    }

    /**
     * Sets the flag whether we should suppress old articles or not.
     *
     * @param suppressingOlderThan TRUE to suppress old articles.
     */
    public void setSuppressingOlderThan(boolean suppressingOlderThan)
    {
        set(RenderingSettingsNames.IS_SUPPRESSING_OLDER_THAN,
                Boolean.valueOf(suppressingOlderThan));
    }

    /**
     * Returns number of days starting from which artiles should not be displayed.
     *
     * @return number of days.
     */
    public int getSuppressOlderThan()
    {
        return (Integer)get(RenderingSettingsNames.SUPPRESS_OLDER_THAN);
    }

    /**
     * Sets number of days to limit the age of the articles to be displayed.
     *
     * @param suppressOlderThan number of days.
     */
    public void setSuppressOlderThan(int suppressOlderThan)
    {
        set(RenderingSettingsNames.SUPPRESS_OLDER_THAN, new Integer(suppressOlderThan));
    }

    /**
     * Sets FROM STRING number of days to limit the age of the articles to be displayed.
     *
     * @param valueAsString number of days as a String
     */
    public void setSuppressOlderThanString(String valueAsString)
    {
        Integer value = Integer.valueOf(valueAsString);
        setSuppressOlderThan(value);
    }

    /**
     * Returns AS STRING the number of days starting from which artiles should not be displayed.
     *
     * @return value as a String
     */
    public String getSuppressOlderThanString()
    {
        return String.valueOf(getSuppressOlderThan());
    }

    /**
     * Returns TRUE if ascending sorting is enabled.
     *
     * @return TRUE to sort articles ascending.
     */
    public boolean isSortingAscending()
    {
        return (Boolean)get(RenderingSettingsNames.IS_SORTING_ASCENDING);
    }

    /**
     * Sets the order of sorting.
     *
     * @param sortingAscending TRUE to sort ascending.
     */
    public void setSortingAscending(boolean sortingAscending)
    {
        set(RenderingSettingsNames.IS_SORTING_ASCENDING, Boolean.valueOf(sortingAscending));
    }

    /**
     * Returns number of characters for limiting of articles size while in brief mode.
     *
     * @return number of chars.
     */
    public int getArticleSizeLimit()
    {
        return (Integer)get(RenderingSettingsNames.ARTICLE_SIZE_LIMIT);
    }

    /**
     * Sets new maximum number of chars for limiting of article size for brief mode.
     *
     * @param articleSizeLimit new number of chars.
     */
    public void setArticleSizeLimit(int articleSizeLimit)
    {
        set(RenderingSettingsNames.ARTICLE_SIZE_LIMIT, new Integer(articleSizeLimit));
    }

    /**
     * Returns current article font bias.
     *
     * @return bias.
     */
    public int getArticleFontBias()
    {
        return (Integer)get(RenderingSettingsNames.ARTICLE_FONT_BIAS);
    }
    
    /**
     * Changes current value for the articleFontBias.
     * 
     * @param articleFontBiasChangeAmount 0 means reset to zero. Non-zero gets added to font bias.
     */
    public void setArticleFontBias(int articleFontBiasChangeAmount)
    {
        int oldFontBias = getArticleFontBias();
        int newFontBias;
        if (articleFontBiasChangeAmount == 0)
        {
            // means the user wants no bias
            newFontBias = 0;
            set(RenderingSettingsNames.ARTICLE_FONT_BIAS, new Integer(newFontBias));
        } else
        {
            // otherwise apply the change amount to the existing bias
            newFontBias = oldFontBias + articleFontBiasChangeAmount; 
            set(RenderingSettingsNames.ARTICLE_FONT_BIAS, new Integer(newFontBias));
        }

        fireChange(RenderingSettingsNames.ARTICLE_FONT_BIAS, oldFontBias, newFontBias);
    }
    
    /**
     * Returns a code to indicate which articles are filtered out.
     * <ul>
     * <li>0 = Show all articles</li>
     * <li>1 = Show Unread articles only</li>
     * <li>2 = Show Unread, recommended articles only</li>
     * </ul>
     *
     * @return code.
     */
    public int getArticleFilter()
    {
        return (Integer)get(RenderingSettingsNames.ARTICLE_FILTER);
    }

    /**
     * Sets a code to indicate how much detail we show for each article.
     * <ul>
     * <li>0 = Show all articles</li>
     * <li>1 = Show Unread articles only</li>
     * <li>2 = Show Unread, recommended articles only</li>
     * </ul>
     *
     * @param artFilter set article filter code.
     */
    public void setArticleFilter(int artFilter)
    {
        set(RenderingSettingsNames.ARTICLE_FILTER, new Integer(artFilter));
    }

    /**
     * Returns the color of registered BlogLinks.
     *
     * @return color.
     */
    public Color getRegisteredBlogLinkColor()
    {
        return getTheme().getColor(ThemeKey.Color.BLOGLINK_DISC_BG);
    }

    /**
     * Returns the color of unregistered BlogLinks.
     *
     * @return color.
     */
    public Color getUnregisteredBlogLinkColor()
    {
        return getTheme().getColor(ThemeKey.Color.BLOGLINK_UNDISC_BG);
    }

    /**
     * Returns value of flag for displaying of full article titles.
     *
     * @return <code>TRUE</code> for on.
     */
    public boolean isDisplayingFullTitles()
    {
        return (Boolean)get(RenderingSettingsNames.IS_DISPLAYING_FULL_TITLES);
    }

    /**
     * Turns on / off displaying of full titles.
     *
     * @param value new value.
     */
    public void setDisplayingFullTitles(boolean value)
    {
        set(RenderingSettingsNames.IS_DISPLAYING_FULL_TITLES, Boolean.valueOf(value));
    }

    /**
     * Returns current theme.
     *
     * @return theme.
     */
    public ITheme getTheme()
    {
        ITheme theTheme = theme;

        if (theTheme == null && parent != null) theTheme = parent.getTheme();

        return theTheme;
    }

    /**
     * Sets new current theme.
     *
     * @param aTheme theme.
     */
    public void setTheme(ITheme aTheme)
    {
        if (aTheme == null) throw new IllegalArgumentException(Strings.error("unspecified.theme"));

        ITheme oldTheme = theme;
        theme = aTheme;
        firePropertyChange(RenderingSettingsNames.THEME, oldTheme, theme);
    }

    /**
     * Returns color of the article title.
     *
     * @param aSelected if article is selected.
     *
     * @return color of title.
     */
    public Color getArticleTitleColor(boolean aSelected)
    {
        ThemeKey.Color key = aSelected
            ? ThemeKey.Color.ARTICLE_TITLE_SEL_FG
            : ThemeKey.Color.ARTICLE_TITLE_UNSEL_FG;

        return getTheme().getColor(key);
    }

    /**
     * Returns color of the article text.
     *
     * @param aSelected if article is selected.
     *
     * @return color of text.
     */
    public Color getArticleTextColor(boolean aSelected)
    {
        ThemeKey.Color key = aSelected
            ? ThemeKey.Color.ARTICLE_TEXT_SEL_FG
            : ThemeKey.Color.ARTICLE_TEXT_UNSEL_FG;

        return getTheme().getColor(key);
    }

    /**
     * Returns color of the article date.
     *
     * @param aSelected if article is selected.
     *
     * @return color of date.
     */
    public Color getArticleDateColor(boolean aSelected)
    {
        ThemeKey.Color key = aSelected
            ? ThemeKey.Color.ARTICLE_DATE_SEL_FG
            : ThemeKey.Color.ARTICLE_DATE_UNSEL_FG;

        return getTheme().getColor(key);
    }

    /**
     * Returns selected background color for feeds list.
     *
     * @return color.
     */
    public Color getFeedsListSelectedBackground()
    {
        return getTheme().getColor(ThemeKey.Color.FEEDSLIST_SEL_BG);
    }

    /**
     * Returns background color for feeds list.
     *
     * @param alternating TRUE if alternating color required.
     *
     * @return color.
     */
    public Color getFeedsListBackground(boolean alternating)
    {
        ThemeKey.Color key = alternating
            ? ThemeKey.Color.FEEDSLIST_ALT_BG
            : ThemeKey.Color.FEEDSLIST_BG;

        return getTheme().getColor(key);
    }

    /**
     * Returns foreground color for feeds list.
     *
     * @param selected TRUE if selected color required.
     *
     * @return color.
     */
    public Color getFeedsListForeground(boolean selected)
    {
        ThemeKey.Color key = selected
            ? ThemeKey.Color.FEEDSLIST_SEL_FG
            : ThemeKey.Color.FEEDSLIST_FG;

        return getTheme().getColor(key);
    }

    /**
     * Returns current main content font taking in account overrides.
     *
     * @return font.
     */
    public Font getMainContentFont()
    {
        return getTheme().getFont(ThemeKey.Font.MAIN);
    }

    /**
     * Sets new content font as override and fires theme change event.
     *
     * @param font new font.
     */
    public void setMainContentFont(Font font)
    {
        Font oldFont = getMainContentFont();
        if (!oldFont.equals(font))
        {
            Theme.setMainFontOverride(font);
            firePropertyChange(RenderingSettingsNames.THEME, null, theme);
        }
    }
}
