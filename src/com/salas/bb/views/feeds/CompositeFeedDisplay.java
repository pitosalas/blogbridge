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
// $Id: CompositeFeedDisplay.java,v 1.32 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.*;
import com.salas.bb.utils.uif.VertialScrollablePanel;
import com.salas.bb.views.feeds.html.HTMLFeedDisplay;
import com.salas.bb.views.feeds.html.IHTMLFeedDisplayConfig;
import com.salas.bb.views.feeds.image.IImageFeedDisplayConfig;
import com.salas.bb.views.feeds.image.ImageFeedDisplay;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Composite feed display analyzes feed type and shows appropriate feed display.
 */
public class CompositeFeedDisplay extends AbstractFeedDisplay
{
    private final IHTMLFeedDisplayConfig htmlDisplayConfig;
    private final IImageFeedDisplayConfig imageDisplayConfig;

    private IFeedDisplayListener displayListener;
    private IFeedListener       feedListener;

    private AbstractFeedDisplay currentDisplay;
    private IFeed               currentFeed;
    private FeedType            currentFeedType;
    private ScrollablePanel     scrollableView;

    private boolean             initializedDisplay;

    /** Page model with the number of pages. Updated by the FeedDisplayModel. */
    private final ValueModel   pageCountModel;
    /** Page model to update when the page changes. */
    private final ValueModel pageModel;
    private int pageSize;

    /**
     * Creates composite feed display.
     *
     * @param aHtmlConfig       HTML configuration.
     * @param aImageConfig      Image configuration.
     * @param pageModel         page model to update when the page changes.
     * @param pageCountModel    page model to update with the number of pages.
     */
    public CompositeFeedDisplay(IHTMLFeedDisplayConfig aHtmlConfig, IImageFeedDisplayConfig aImageConfig,
                                ValueModel pageModel, ValueModel pageCountModel)
    {
        super(null, null, pageCountModel);

        this.pageCountModel = pageCountModel;
        this.pageModel = pageModel;

        initializedDisplay = false;

        htmlDisplayConfig = aHtmlConfig;
        imageDisplayConfig = aImageConfig;

        displayListener = new DelegatingDisplayListener();
        feedListener = new FeedListener();

        enableEvents(AWTEvent.KEY_EVENT_MASK);
        scrollableView = new ScrollablePanel(new BorderLayout());
    }

    /**
     * Returns <code>TRUE</code> during firing the article(s) selection event, so that it's possible to learn if it's
     * the source of this event. This is particularily useful when it's necessary to skip sending the event back to the
     * display.
     *
     * @return <code>TRUE</code> if current event has come from this component.
     */
    @Override
    public boolean isArticleSelectionSource()
    {
        return currentDisplay != null && currentDisplay.isArticleSelectionSource();
    }

    /**
     * Sets the viewport which will be used for showing this component.
     *
     * @param aViewport viewport.
     */
    @Override
    public void setViewport(JViewport aViewport)
    {
        super.setViewport(aViewport);
        scrollableView.setViewport(aViewport);
    }

    /**
     * Returns displayable feed view component.
     *
     * @return displayable feed view component.
     */
    public JComponent getComponent()
    {
        return scrollableView;
    }

    /** Requests focus for this display. */
    public void focus()
    {
        if (currentDisplay == null) super.requestFocus(); else currentDisplay.focus();
    }


    /**
     * Returns currently selected text in currently selected article.
     *
     * @return text.
     */
    public String getSelectedText()
    {
        return currentDisplay == null ? super.getSelectedText() : currentDisplay.getSelectedText();
    }

    /**
     * Repaints all highlights in all visible articles.
     */
    public void repaintHighlights()
    {
        if (currentDisplay != null) currentDisplay.repaintHighlights();
    }

    /**
     * Repaints all sentiments color codes.
     */
    public void repaintSentimentsColorCodes()
    {
        if (currentDisplay != null) currentDisplay.repaintSentimentsColorCodes();
    }

    /**
     * Orders view to select and show article if it can be visible.
     *
     * @param article article to select.
     */
    public void selectArticle(IArticle article)
    {
        if (currentDisplay != null) currentDisplay.selectArticle(article);
    }


    /**
     * Repaints article text if is currently in the given mode.
     *
     * @param briefMode <code>TRUE</code> for brief mode, otherwise -- full mode.
     */
    public void repaintIfInMode(boolean briefMode)
    {
        if (currentDisplay != null) currentDisplay.repaintIfInMode(briefMode);
    }

    /**
     * Orders to select next article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectNextArticle(int mode)
    {
        return currentDisplay != null && currentDisplay.selectNextArticle(mode);
    }

    /**
     * Orders to select next article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectFirstArticle(int mode)
    {
        return currentDisplay != null && currentDisplay.selectFirstArticle(mode);
    }

    /**
     * Orders to select previous article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectPreviousArticle(int mode)
    {
        return currentDisplay != null && currentDisplay.selectPreviousArticle(mode);
    }

    /**
     * Orders to select last article.
     *
     * @param mode mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    public boolean selectLastArticle(int mode)
    {
        return currentDisplay != null && currentDisplay.selectLastArticle(mode);
    }

    /**
     * Sets the feed which is required to be displayed.
     *
     * @param feed the feed.
     */
    public void setFeed(IFeed feed)
    {
        FeedType feedType = feed == null ? null : feed.getType();

        if (currentFeed != null)
        {
            currentFeed.removeListener(feedListener);
        }

        currentFeed = feed;

        if (currentFeed != null)
        {
            currentFeed.addListener(feedListener);
        }

        if (feedType != currentFeedType || !initializedDisplay)
        {
            installNewDisplay(feedType);
            currentFeedType = feedType;
            initializedDisplay = true;
        }

        if (currentDisplay != null) currentDisplay.setFeed(feed);
    }

    @Override
    public void setPage(int page)
    {
        if (currentDisplay != null) currentDisplay.setPage(page);
    }

    @Override
    public void setPageSize(int size)
    {
        pageSize = size;
        if (currentDisplay != null) currentDisplay.setPageSize(size);
    }

    /**
     * Deinstalls old display and installs new one.
     *
     * @param aFeedType feed type.
     */
    private void installNewDisplay(FeedType aFeedType)
    {
        if (currentDisplay != null)
        {
            scrollableView.unsetDisplay();
            currentDisplay.prepareForDismiss();
            currentDisplay.removeListener(displayListener);
        }

        currentDisplay = createNewDisplay(aFeedType);

        if (currentDisplay != null)
        {
            scrollableView.setDisplay(currentDisplay);
            Color bgColor = currentDisplay.getConfig().getDisplayBGColor();
            scrollableView.setBackground(bgColor);
            if (viewport != null) viewport.setBackground(bgColor);

            currentDisplay.addListener(displayListener);
        }
    }

    /**
     * Creates display according to feed type.
     *
     * @param aFeedType feed type.
     *
     * @return display.
     */
    private AbstractFeedDisplay createNewDisplay(FeedType aFeedType)
    {
        AbstractFeedDisplay display = null;

        if (aFeedType != null)
        {
            if (FeedType.TEXT == aFeedType)
            {
                display = new HTMLFeedDisplay(htmlDisplayConfig, pageModel, pageCountModel);
            } else if (FeedType.IMAGE == aFeedType)
            {
                display = new ImageFeedDisplay(imageDisplayConfig, pageModel, pageCountModel);
            }
        }

        if (display != null && pageSize != 0) display.setPageSize(pageSize);

        return display != null ? display : new NoFeedDisplay(htmlDisplayConfig, pageCountModel);
    }

    /** Releases all links and resources and prepares itself to be garbage collected. */
    public void prepareForDismiss()
    {
    }

    /**
     * Returns current logger.
     *
     * @return logger object.
     */
    protected Logger getLogger()
    {
        return null;
    }

    /**
     * Creates new article display for addition to the display.
     *
     * @param aArticle article to create display for.
     *
     * @return display.
     */
    protected IArticleDisplay createNewArticleDisplay(IArticle aArticle)
    {
        return null;
    }

    /** Delegate scrolling to the component. */
    public void scrollRectToVisible(Rectangle aRect)
    {
        scrollableView.scrollTo(aRect);
    }

    /**
     * Invoked when selected feed type changes.
     */
    private void onFeedTypeChange()
    {
        if (currentFeedType != currentFeed.getType())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    setFeed(currentFeed);
                }
            });
        }
    }

    @Override
    protected void onConfigPropertyChange(String name)
    {
        if (currentDisplay != null) currentDisplay.onConfigPropertyChange(name);
    }

    /**
     * Listens to changes of selected feed type.
     */
    private class FeedListener extends FeedAdapter
    {
        /**
         * Called when information in feed changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (IFeed.PROP_TYPE.equals(property))
            {
                onFeedTypeChange();
            } else if (IFeed.PROP_ASCENDING_SORTING.equals(property))
            {
                onConfigPropertyChange(IFeedDisplayConfig.SORT_ORDER);
            }
        }
    }

    /**
     * Listener refiring the envent to the others.
     */
    private class DelegatingDisplayListener implements IFeedDisplayListener
    {
        /**
         * Invoked when user selects article or article is selected as result of direct invocation of
         * {@link com.salas.bb.views.feeds.IFeedDisplay#selectArticle(com.salas.bb.domain.IArticle)}
         * method.
         *
         * @param lead              lead article.
         * @param selectedArticles  all selected articles.
         */
        public void articleSelected(IArticle lead, IArticle[] selectedArticles)
        {
            fireArticleSelected(lead, selectedArticles);
        }

        /**
         * Invoked when user clicks on some link at the article text or header. The expected behaviour
         * is openning the link in browser.
         *
         * @param link link clicked.
         */
        public void linkClicked(URL link)
        {
            fireLinkClicked(link);
        }

        /**
         * Invoked when user hovers some link with mouse pointer.
         *
         * @param link link hovered or <code>NULL</code> if previously hovered link is no longer
         *             hovered.
         */
        public void linkHovered(URL link)
        {
            fireLinkHovered(link);
        }

        /**
         * Invoked when user clicks on some quick-link to the other feed.
         *
         * @param feed feed to select.
         */
        public void feedJumpLinkClicked(IFeed feed)
        {
            fireFeedJumpLinkClicked(feed);
        }

        /** Invoked when the user made something to zoom content in. */
        public void onZoomIn()
        {
            fireZoomIn();
        }

        /** Invoked when the user made something to zoom the content out. */
        public void onZoomOut()
        {
            fireZoomOut();
        }
    }

    /**
     * Panel with custom scrollable properties.
     */
    protected static class ScrollablePanel extends VertialScrollablePanel
        implements IScrollContoller
    {
        private IFeedDisplay currentDisplay;
        private JViewport viewport;

        /**
         * Create a new buffered JPanel with the specified layout manager
         *
         * @param layout the LayoutManager to use
         */
        public ScrollablePanel(LayoutManager layout)
        {
            super(layout);
        }

        /**
         * Forwards the <code>scrollRectToVisible()</code> message to the <code>JComponent</code>'s
         * parent. Components that can service the request, such as <code>JViewport</code>, override
         * this method and perform the scrolling.
         *
         * @param aRect the visible <code>Rectangle</code>
         *
         * @see javax.swing.JViewport
         */
        public void scrollRectToVisible(Rectangle aRect)
        {
        }

        /**
         * Makes scrolling to the given place.
         *
         * @param aRect the visible <code>Rectangle</code>
         *
         * @see javax.swing.JViewport
         */
        public void scrollTo(Rectangle aRect)
        {
            super.scrollRectToVisible(aRect);
        }

        /**
         * Sets the display.
         *
         * @param cdisplay display.
         */
        public void setDisplay(IFeedDisplay cdisplay)
        {
            if (currentDisplay != null) unsetDisplay();

            currentDisplay = cdisplay;

            JComponent display = currentDisplay.getComponent();
            display.setVisible(false);
            currentDisplay.setViewport(viewport);
            add(display, BorderLayout.NORTH);
            display.setVisible(true);
        }

        /**
         * Unsets the display.
         */
        public void unsetDisplay()
        {
            if (currentDisplay == null) return;

            JComponent d = currentDisplay.getComponent();
            d.setVisible(false);
            remove(d);

            currentDisplay = null;
        }

        @Override
        public boolean requestFocusInWindow()
        {
            boolean focusing = super.requestFocusInWindow();

            if (currentDisplay != null) currentDisplay.focus();
            
            return focusing;
        }

        /**
         * Sets a viewport to use for displays.
         *
         * @param viewport viewport.
         */
        public void setViewport(JViewport viewport)
        {
            this.viewport = viewport;
        }
    }
}
