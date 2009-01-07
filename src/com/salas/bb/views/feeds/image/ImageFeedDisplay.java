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
// $Id: ImageFeedDisplay.java,v 1.42 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.feeds.AbstractFeedDisplay;
import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.feeds.html.ArticlesGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.logging.Logger;

/**
 * View for the image-feeds.
 */
public class ImageFeedDisplay extends AbstractFeedDisplay
{
    private static final Logger LOG = Logger.getLogger(ImageFeedDisplay.class.getName());

    private final IImageFeedDisplayConfig   imageConfig;

    /**
     * Creates view.
     *
     * @param aConfig view configuration.
     * @param pageModel page model to update when page changes.
     * @param pageCountModel page model with the number of pages (updated by the FeedDisplayModel).
     */
    public ImageFeedDisplay(IImageFeedDisplayConfig aConfig, ValueModel pageModel, ValueModel pageCountModel)
    {
        super(aConfig, pageModel, pageCountModel);

        imageConfig = aConfig;

        setBackground(aConfig.getGlobalBGColor());

        setLayout(new GroupLayoutManager());
        for (ArticlesGroup group : groups) add(group, GroupLayoutManager.DIVIDER);

        add(noContentPanel);

        selectedDisplay = null;

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Returns message to show when there's no articles to display.
     *
     * @return the message.
     */
    protected String getNoContentPanelMessage()
    {
        String msg;

        int filter = getConfig().getFilter();
        switch (filter)
        {
            case IFeedDisplayConstants.FILTER_PINNED:
                msg = Strings.message("imagedisplay.no.pinned.pictures");
                break;
            case IFeedDisplayConstants.FILTER_UNREAD:
                msg = Strings.message("imagedisplay.no.unseen.pictures");
                break;
            default:
                msg = Strings.message("imagedisplay.no.pictures");
        }

        return msg;
    }

    /**
     * Converts rectrangle of view port to no-content panel bounds.
     *
     * @param r rectangle.
     *
     * @return bounds rectangle.
     */
    protected Rectangle rectToNoContentBounds(Rectangle r)
    {
        r.width -= 2 * GroupLayoutManager.DEFAULT_GAP;
        r.height -= 2 * GroupLayoutManager.DEFAULT_GAP;

        return r;
    }

    /**
     * Returns current logger.
     *
     * @return logger object.
     */
    protected Logger getLogger()
    {
        return LOG;
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
        return new ImageArticleDisplay(aArticle, imageConfig);
    }

    @Override
    protected void onViewModeChange()
    {
        super.onViewModeChange();
        doLayout();
    }

    // ---------------------------------------------------------------------------------------------
    // Events
    // ---------------------------------------------------------------------------------------------

    /**
     * Processes mouse events.
     *
     * @param e the mouse event
     */
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        Object component = e.getSource();

        int i = e.getID();
        if (i == MouseEvent.MOUSE_PRESSED)
        {
            requestFocus();
            if (component instanceof IArticleDisplay)
            {
                IArticleDisplay articleDisplay = (IArticleDisplay)component;
                // Note that isRightMouseButton may not be very well suited for all systems
                // It should match the popup dialog guesture
                if (!SwingUtilities.isRightMouseButton(e) || !selectedDisplays.contains(articleDisplay))
                {
                    selectDisplay(articleDisplay, false, eventToMode(e));
                }

                // See if popup is necessary
                MouseListener popup = imageConfig.getViewPopupAdapter();
                if (popup != null) popup.mousePressed(e);
            }
        } else if (i == MouseEvent.MOUSE_RELEASED)
        {
            if (component instanceof IArticleDisplay)
            {
                MouseListener popup = imageConfig.getViewPopupAdapter();
                if (popup != null) popup.mouseReleased(e);
            }
        } else if (i == MouseEvent.MOUSE_CLICKED)
        {
                if (SwingUtilities.isLeftMouseButton(e) &&
                    component instanceof ImageArticleDisplay)
                {
                    URL link = null;
                    if (hoveredLink != null)
                    {
                        link = hoveredLink;
                    } else if (e.getClickCount() == 2)
                    {
                        link = ((ImageArticleDisplay)component).getArticle().getLink();
                    }

                    if (link != null) fireLinkClicked(link);
                }
        }
    }
}
