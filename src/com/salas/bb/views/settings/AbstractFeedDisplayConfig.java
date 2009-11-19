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
// $Id: AbstractFeedDisplayConfig.java,v 1.12 2008/04/08 08:06:19 spyromus Exp $
//

package com.salas.bb.views.settings;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.ViewModeValueModel;
import com.salas.bb.domain.FeedMetaDataHolder;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.domain.prefs.IViewModePreferencesChangeListener;
import com.salas.bb.domain.prefs.ViewModePreferences;
import com.salas.bb.domain.utils.TextRange;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.feeds.IFeedDisplayConfig;
import com.salas.bb.views.feeds.IHighlightsAdvisor;
import com.salas.bb.views.feeds.html.IArticleDisplayConfig;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Abstract BlogBridge feed view configuration.
 */
abstract class AbstractFeedDisplayConfig implements IFeedDisplayConfig
{
    private final ViewModeValueModel        viewModeValueModel;
    private final IHighlightsAdvisor        highlightsAdvisor;
    private final PropertyChangeListener    renderingManagerListener;
    private PropertyChangeListener          listener;
    private ArticleDisplayConfig            articleViewConfig = new ArticleDisplayConfig();

    /**
     * Creates config object.
     */
    public AbstractFeedDisplayConfig()
    {
        highlightsAdvisor = new HighlightsAdvisor();
        renderingManagerListener = new RenderingManagerListener();

        viewModeValueModel = GlobalModel.SINGLETON.getViewModeValueModel();
        viewModeValueModel.addValueChangeListener(new ViewModeValueModelListener());
    }

    /**
     * Returns adapter listener.
     *
     * @return listener.
     */
    public PropertyChangeListener getRenderingManagerListener()
    {
        return renderingManagerListener;
    }

    /**
     * Set configuration properties change listener.
     *
     * @param l listener.
     */
    public void setListener(PropertyChangeListener l)
    {
        listener = l;
    }

    /**
     * Returns key adapter which is reported of key events happening when component has focus.
     *
     * @return adapter.
     */
    public KeyListener getKeyAdapter()
    {
        return null;
    }

    /**
     * Returns <code>TRUE</code> if it's required to show empty groups.
     *
     * @return <code>TRUE</code> if it's required to show empty groups.
     */
    public boolean showEmptyGroups()
    {
        return RenderingManager.isShowEmptyGroups();
    }

    /**
     * Returns <code>TRUE</code> if it's required to show groups.
     *
     * @return <code>TRUE</code> if it's required to show groups.
     */
    public boolean showGroups()
    {
        return RenderingManager.isGroupingEnabled();
    }

    /**
     * Returns the advisor object to use for keywords highlighting.
     *
     * @return advisor.
     */
    public IHighlightsAdvisor getHighlightsAdvisor()
    {
        return highlightsAdvisor;
    }

    /**
     * Filter to use in order to hide articles.
     *
     * @return filter.
     *
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_ALL
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_UNREAD
     */
    public int getFilter()
    {
        return RenderingManager.getArticleFilter();
    }

    /**
     * Returns <code>TRUE</code> if ascending sorting selected.
     *
     * @return <code>TRUE</code> if ascending sorting selected.
     */
    public boolean isAscendingSorting()
    {
        return RenderingManager.isSortingAscending();
    }

    /**
     * Returns the view mode.
     *
     * @return view mode.
     *
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_BRIEF
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_FULL
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_MINIMAL
     */
    public int getViewMode()
    {
        return (Integer)viewModeValueModel.getValue();
    }

    /**
     * Returns background color of the feed display.
     *
     * @return background color.
     */
    public Color getDisplayBGColor()
    {
        return RenderingManager.getArticleBodyBackground();
    }

    /**
     * Returns font of groups divider component.
     *
     * @return font.
     */
    public Font getGroupDividerFont()
    {
        return RenderingManager.getDividerTextFont();
    }

    /**
     * Fires property change event.
     *
     * @param evt event.
     */
    protected void firePropertyChangeEvent(PropertyChangeEvent evt)
    {
        if (listener != null) listener.propertyChange(evt);
    }

    /**
     * Returns the adapter for the article groups.
     *
     * @return popup adapter.
     */
    public MouseListener getGroupPopupAdapter()
    {
        return GlobalController.SINGLETON.getMainFrame().getArticleGroupPopupAdapter();
    }

    /**
     * Returns configuration of articles views.
     *
     * @return configuration of articles views.
     */
    public IArticleDisplayConfig getArticleViewConfig()
    {
        return articleViewConfig;
    }

    /**
     * Highlights advisor.
     */
    private static class HighlightsAdvisor implements IHighlightsAdvisor
    {
        /**
         * Returns the ranges to highlight in text as search-words.
         *
         * @param aText text.
         *
         * @return ranges.
         */
        public TextRange[] getSearchwordsRanges(String aText)
        {
            return GlobalController.SINGLETON.getSearchHighlightsCalculator().getHighlights(aText);
        }
    }

    /**
     * Listener to rendering manager changes.
     */
    private class RenderingManagerListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property
         *              that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            firePropertyChangeEvent(evt);
        }
    }

    /**
     * Monitors the view mode changes.
     */
    private class ViewModeValueModelListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property
         *              that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (listener != null)
            {
                PropertyChangeEvent event = new PropertyChangeEvent(evt.getSource(),
                    RenderingSettingsNames.ARTICLE_VIEW_MODE, evt.getOldValue(), evt.getNewValue());

                firePropertyChangeEvent(event);
            }
        }
    }
    /**
     * Article view configuration.
     */
    private class ArticleDisplayConfig implements IArticleDisplayConfig
    {
        private final ViewModePreferences viewModePrefs;

        /**
         * Creates article display config.
         */
        public ArticleDisplayConfig()
        {
            viewModePrefs = GlobalModel.SINGLETON.getUserPreferences().getViewModePreferences();
            viewModePrefs.addListener(new IViewModePreferencesChangeListener()
            {
                public void viewModeChanged(int mode)
                {
                    PropertyChangeEvent event = new PropertyChangeEvent(this,
                        VIEW_MODE_LAYOUT, null, mode);

                    firePropertyChangeEvent(event);
                }
            });
        }

        /**
         * Returns <code>TRUE</code> if article should be rendered with date.
         *
         * @return <code>TRUE</code> if article should be rendered with date.
         */
        public boolean isShowingDate()
        {
            return RenderingManager.isArticleDateShowing();
        }

        /**
         * Returns the length of text excerpt in brief mode.
         *
         * @return number of characters in excerpt.
         */
        public int getBriefModeTextLength()
        {
            return RenderingManager.getArticleSizeLimit();
        }

        /**
         * Returns foreground color of view date.
         *
         * @param aSelected <code>TRUE</code> if article view is selected.
         *
         * @return color.
         */
        public Color getDateFGColor(boolean aSelected)
        {
            return RenderingManager.getArticleDateColor(aSelected);
        }

        /**
         * Returns the font to be used for painting date.
         *
         * @return font.
         */
        public Font getDateFont()
        {
            return RenderingManager.getArticleDateFont();
        }

        /**
         * Returns global background color for the view.
         *
         * @param aSelected <code>TRUE</code> if article view is selected.
         *
         * @return color.
         */
        public Color getGlobalBGColor(boolean aSelected)
        {
            return aSelected
                ? RenderingManager.getSelectedArticleBackground()
                : RenderingManager.getArticleBodyBackground();
        }

        /**
         * Returns color of background for the link.
         *
         * @param type type of the link.
         *
         * @return color.
         */
        public Color getLinkBGColor(LinkType type)
        {
            Color color;

            switch (type)
            {
                case REGISTERED:
                    color = RenderingManager.getRegisteredBlogLinkColor();
                    break;
                case UNREGISTERED:
                    color = RenderingManager.getUnregisteredBlogLinkColor();
                    break;
                case SEARCH:
                    color = RenderingManager.getSerachKeywordHighlightBackground();
                    break;
                default:
                    color = null;
                    break;
            }

            return color;
        }

        /**
         * Returns tooltip to use when mouse over the link.
         *
         * @param link link.
         *
         * @return tool-tip text.
         */
        public String getLinkTooltip(URL link)
        {
            String tooltip = null;

            if (link != null)
            {
                StringBuffer buf = new StringBuffer("<html><body>").append(link.toString());

                GlobalController controller = GlobalController.SINGLETON;
                FeedMetaDataHolder metaData = controller.discoverLinkFromArticle(link);
                if (metaData != null)
                {
                    URL xmlURL = metaData.getXmlURL();
                    NetworkFeed feed = controller.getModel().getGuidesSet().findDirectFeed(xmlURL);

                    if (feed == null)
                    {
                        if (metaData.isComplete() && metaData.isDiscoveredValid())
                        {
                            String msg = metaData.getTextualInboundLinks();
                            String title = metaData.getTitle();
                            if (title == null) title = xmlURL.toString();
                            URL siteUrl = metaData.getHtmlURL();
                            String author = metaData.getAuthor();

                            buf.append("<br><b>").append(Strings.message("articledisplay.config.new.blog"));
                            buf.append("</b> ").append(title);
                            if (author != null)
                            {
                                buf.append("<br><b>").append(Strings.message("articledisplay.config.author"));
                                buf.append("</b> ").append(author);
                            }
                            if (siteUrl != null)
                            {
                                buf.append("<br><b>").append(Strings.message("articledisplay.config.siteurl"));
                                buf.append("</b> ").append(siteUrl);
                            }
                            buf.append("<br><b>").append(Strings.message("articledisplay.config.feedurl"));
                            buf.append("</b> ").append(xmlURL);
                            buf.append("<br><b>").append(Strings.message("articledisplay.config.inbound.links"));
                            buf.append("</b> ").append(msg);
                        }
                    } else
                    {
                        buf.append("<br><b>").append(Strings.message("articledisplay.config.existing.blog"));
                        buf.append("</b> ").append(feed.getTitle());
                    }
                }
                buf.append("</body></html>");
                tooltip = buf.toString();
            }

            return tooltip;
        }

        /**
         * Returns the type of the link.
         *
         * @param link link.
         *
         * @return type.
         */
        public LinkType getLinkType(String link)
        {
            LinkType type;

            try
            {
                GlobalController controller = GlobalController.SINGLETON;
                String highlightedArticleLink = controller.getHighlightedArticleLink();
                if (highlightedArticleLink != null && highlightedArticleLink.equalsIgnoreCase(link))
                {
                    type = LinkType.SEARCH;
                } else
                {
                    FeedMetaDataHolder metaData = controller.discoverLinkFromArticle(new URL(link));
                    if (metaData != null && metaData.isDiscoveredValid())
                    {
                        type = LinkType.UNREGISTERED;

                        URL xmlURL = metaData.getXmlURL();
                        NetworkFeed feed = controller.getModel().getGuidesSet().findDirectFeed(xmlURL);

                        if (feed != null) type = LinkType.REGISTERED;
                    } else
                    {
                        type = LinkType.NORMAL;
                    }
                }
            } catch (MalformedURLException e)
            {
                type = LinkType.NORMAL;
            }

            return type;
        }

        /**
         * Returns maximum length of single-line title.
         *
         * @return maximum length of single-line title.
         */
        public int getMaxSingleLineTitleLength()
        {
            return 50;
        }

        /**
         * Returns the background color of search-words.
         *
         * @return color.
         */
        public Color getSearchwordBGColor()
        {
            return Color.YELLOW;
        }
        /**
         * Returns the color of the text.
         *
         * @param aSelected <code>TRUE</code> if selected.
         *
         * @return color.
         */
        public Color getTextColor(boolean aSelected) {
            return RenderingManager.getArticleTextColor(aSelected);
        }

        /**
         * Returns the background color of the text area.
         *
         * @param aSelected <code>TRUE</code> if article view is selected.
         *
         * @return color.
         */
        public Color getTextBGColor(boolean aSelected)
        {
            return getGlobalBGColor(aSelected);
        }

        /**
         * Returns the font to be used for painting text area.
         *
         * @return font.
         */
        public Font getTextFont()
        {
            return RenderingManager.getArticleBodyFont();
        }

        /**
         * Returns the background color of the title.
         *
         * @param aSelected <code>TRUE</code> if article view is selected.
         *
         * @return color.
         */
        public Color getTitleBGColor(boolean aSelected)
        {
            return getGlobalBGColor(aSelected);
        }

        /**
         * Returns foreground color of view title.
         *
         * @param aSelected <code>TRUE</code> if article view is selected.
         *
         * @return color.
         */
        public Color getTitleFGColor(boolean aSelected)
        {
            return RenderingManager.getArticleTitleColor(aSelected);
        }

        /**
         * Returns the font to be used for painting title.
         *
         * @param aRead <code>TRUE</code> if font for read state is required.
         *
         * @return font.
         */
        public Font getTitleFont(boolean aRead)
        {
            Font font = RenderingManager.getArticleTitleFont();
            return aRead ? font : font.deriveFont(Font.BOLD);
        }

        /**
         * Returns <code>TRUE</code> when single-line titles are enabled.
         *
         * @return <code>TRUE</code> when single-line titles are enabled.
         */
        public boolean isSingleLineTitles()
        {
            return !RenderingManager.isDisplayingFullTitles();
        }

        /**
         * Returns the advisor object to use for keywords highlighting.
         *
         * @return advisor.
         */
        public IHighlightsAdvisor getHighlightsAdvisor()
        {
            return AbstractFeedDisplayConfig.this.getHighlightsAdvisor();
        }

        /**
         * Returns border which should be displayed around the article view.
         *
         * @param aSelected <code>TRUE</code> if article view is selected.
         * @param aFocused  <code>TRUE</code> if article view is focused.
         *
         * @return border.
         */
        public Border getBorder(boolean aSelected, boolean aFocused)
        {
            return BorderFactory.createEmptyBorder();
        }

        /**
         * Returns the view mode.
         *
         * @return view mode.
         *
         * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_BRIEF
         * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_FULL
         * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_MINIMAL
         */
        public int getViewMode()
        {
            return AbstractFeedDisplayConfig.this.getViewMode();
        }

        /**
         * Returns view mode preferences.
         *
         * @return view mode preferences.
         */
        public ViewModePreferences getViewModePreferences()
        {
            return viewModePrefs;
        }

        /**
         * Returns <code>TRUE</code> if browser should launch on dbl-click over the title.
         *
         * @return <code>TRUE</code> to open browser on double click over the article title.
         */
        public boolean isBrowseOnTitleDoubleClick()
        {
            return GlobalModel.SINGLETON.getUserPreferences().isBrowseOnDblClick();
        }
    }
}
