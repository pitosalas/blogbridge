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
// $Id: IArticleDisplay.java,v 1.4 2008/02/28 12:36:16 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IArticleListener;

import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Article display interface.
 */
public interface IArticleDisplay
{
    /**
     * Returns visual component.
     *
     * @return visual component.
     */
    Component getComponent();

    /**
     * Returns assigned article.
     *
     * @return article.
     */
    IArticle getArticle();

    /**
     * Returns listener.
     *
     * @return listener.
     */
    IArticleListener getArticleListener();

    /**
     * Registers hyperlink listener.
     *
     * @param aListener listener.
     */
    void addHyperlinkListener(HyperlinkListener aListener);

    /**
     * Sets <code>TRUE</code> if the display is currently selected.
     *
     * @param sel <code>TRUE</code> if the display is currently selected.
     */
    void setSelected(boolean sel);

    /**
     * Sets <code>TRUE</code> if the display should become collapsed.
     *
     * @param col <code>TRUE</code> if the display is currently selected.
     */
    void setCollapsed(boolean col);

    /**
     * Requests focus and returns the state.
     *
     * @return <code>FALSE</code> if focus isn't likely to be changed.
     */
    boolean focus();

    /**
     * Invoked when article should update highlights.
     */
    void updateHighlights();

    /**
     * Updates a color code.
     */
    void updateColorCode();

    /**
     * Invoked on theme change.
     */
    void onThemeChange();

    /**
     * Invoked when view mode changes.
     */
    void onViewModeChange();

    /**
     * Invoked when font bias changes.
     */
    void onFontBiasChange();

    /**
     * Returns current article display view mode.
     *
     * @return mode.
     */
    int getViewMode();

    /**
     * Sets a view model of this view.
     *
     * @param aMode new mode.
     */
    void setViewMode(int aMode);
}
