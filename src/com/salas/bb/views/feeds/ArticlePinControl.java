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
// $Id: ArticlePinControl.java,v 1.3 2007/11/08 12:22:07 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;

/**
 * Article pin control.
 */
public class ArticlePinControl extends AbstractArticleControl
{
    /** Selected icon. */
    private static ImageIcon ICON_SELECTED = ResourceUtils.getIcon("pin.sel.icon");
    /** Unselected icon. */
    private static ImageIcon ICON_UNSELECTED = ResourceUtils.getIcon("pin.unsel.icon");

    /** Selected guide. */
    private final IGuide guide;
    /** Selected feed. */
    private final IFeed feed;

    /**
     * Creates pin icon.
     *
     * @param article article of the icon.
     */
    public ArticlePinControl(IArticle article)
    {
        this(null, null, article);
    }

    /**
     * Creates pin icon.
     *
     * @param guide guide that is currently selected.
     * @param feed feed that is currently selected.
     * @param article article of the icon.
     */
    public ArticlePinControl(IGuide guide, IFeed feed, IArticle article)
    {
        super(article,
            ICON_SELECTED,
            ICON_UNSELECTED,
            Strings.message("articledisplay.pin.unpin"),
            Strings.message("articledisplay.pin.pin"));
        this.guide = guide;
        this.feed = feed;
    }

    /**
     * Invoked when article state is toggled.
     *
     * @param article article.
     */
    protected void onToggleState(IArticle article)
    {
        GlobalController.pinArticles(!article.isPinned(),
            guide,
            feed,
            article);
    }

    /**
     * Invoked when the control needs to know current article state.
     *
     * @param article article.
     *
     * @return <code>TRUE</code> to show selected icon and message.
     */
    protected boolean getCurrentState(IArticle article)
    {
        return article.isPinned();
    }
}