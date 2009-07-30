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
// $Id: IFeedDisplay.java,v 1.16 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;

import javax.swing.*;

/**
 * Feed view is a representation of feed. It is capable of displaying the feed's items
 * in some unique manner.
 */
public interface IFeedDisplay
{
    /**
     * Returns displayable feed view component.
     *
     * @return displayable feed view component.
     */
    JComponent getComponent();

    /**
     * Sets the feed which is required to be displayed.
     *
     * @param feed the feed.
     */
    void setFeed(IFeed feed);

    /**
     * Orders view to select and show article if it can be visible.
     *
     * @param article article to select. 
     */
    void selectArticle(IArticle article);

    /**
     * Orders to select next article.
     *
     * @param mode  mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    boolean selectFirstArticle(int mode);

    /**
     * Orders to select next article.
     *
     * @param mode  mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    boolean selectNextArticle(int mode);

    /**
     * Orders to select previous article.
     *
     * @param mode  mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    boolean selectPreviousArticle(int mode);

    /**
     * Orders to select last article.
     *
     * @param mode  mode of selection.
     *
     * @return <code>TRUE</code> if article has been selected.
     */
    boolean selectLastArticle(int mode);

    /**
     * Adds listener.
     *
     * @param l listener.
     */
    void addListener(IFeedDisplayListener l);

    /**
     * Removes listener.
     *
     * @param l listener.
     */
    void removeListener(IFeedDisplayListener l);

    /**
     * Sets the viewport which will be used for showing this component.
     *
     * @param aViewport viewport.
     */
    void setViewport(JViewport aViewport);

    /**
     * Releases all links and resources and prepares itself to be garbage collected.
     */
    void prepareForDismiss();

    /**
     * Get display configuration.
     *
     * @return configuration.
     */
    IFeedDisplayConfig getConfig();

    /**
     * Repaints all highlights in all visible articles.
     */
    void repaintHighlights();

    /**
     * Repaints all sentiments color codes.
     */
    void repaintSentimentsColorCodes();

    /**
     * Requests focus for this display.
     */
    void focus();

    /**
     * Returns currently selected text in currently selected article.
     *
     * @return text.
     */
    String getSelectedText();

    /**
     * Repaints article text if is currently in the given mode.
     *
     * @param briefMode <code>TRUE</code> for brief mode, otherwise -- full mode.
     */
    void repaintIfInMode(boolean briefMode);

    /**
     * Returns <code>TRUE</code> during firing the article(s) selection event, so that
     * it's possible to learn if it's the source of this event. This is particularily useful
     * when it's necessary to skip sending the event back to the display.
     *
     * @return <code>TRUE</code> if current event has come from this component.
     */
    boolean isArticleSelectionSource();

    /**
     * Sets the view page.
     *
     * @param page page.
     */
    void setPage(int page);

    /**
     * Sets the view page size (in articles).
     *
     * @param size size of the page.
     */
    void setPageSize(int size);

    /**
     * Cycles view mode forward.
     */
    void cycleViewModeForward();

    /**
     * Cycles view mode backward.
     */
    void cycleViewModeBackward();
}
