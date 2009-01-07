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
// $Id: NoFeedDisplay.java,v 1.7 2007/06/13 14:15:24 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;

/**
 * Feed display for showing something when no feed selected.
 */
public class NoFeedDisplay extends AbstractFeedDisplay implements IFeedDisplay
{
    private static final Logger LOG = Logger.getLogger(NoFeedDisplay.class.getName());

    private final IFeedDisplayConfig config;
    private NoFeedDisplay.ViewportSizeMonitor viewportSizeMonitor;

    /**
     * Creates feed display.
     *
     * @param aConfig configuration.
     * @param pageCountModel model.
     */
    public NoFeedDisplay(IFeedDisplayConfig aConfig, ValueModel pageCountModel)
    {
        super(null, null, pageCountModel);

        // There are no pages
        pageCountModel.setValue(0);

        config = aConfig;
        viewportSizeMonitor = new ViewportSizeMonitor();

        setLayout(new FormLayout("5dlu, center:pref:grow, 5dlu", "5dlu:grow, pref, 5dlu:grow"));

        JLabel label = new JLabel(Strings.message("panel.articles.no.feed.selected"));
        label.setBackground(config.getDisplayBGColor());
        setBackground(config.getDisplayBGColor());

        CellConstraints cc = new CellConstraints();
        add(label, cc.xy(2, 2));
    }

    /**
     * Get display configuration.
     *
     * @return configuration.
     */
    public IFeedDisplayConfig getConfig()
    {
        return config;
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
        return null;
    }

    /**
     * Orders view to select and show article if it can be visible.
     *
     * @param article article to select.
     */
    public void selectArticle(IArticle article)
    {
    }

    /**
     * Sets the feed which is required to be displayed.
     *
     * @param feed the feed.
     */
    public void setFeed(IFeed feed)
    {
    }

    /**
     * Sets the viewport which will be used for showing this component.
     *
     * @param aViewport viewport.
     */
    public void setViewport(JViewport aViewport)
    {
        if (viewport != null)
        {
            viewport.removeComponentListener(viewportSizeMonitor);
        }

        super.setViewport(aViewport);

        if (aViewport != null)
        {
            aViewport.addComponentListener(viewportSizeMonitor);
            onViewportResize();
        }
    }

    /**
     * Called when the viewport has been resized and this component
     * size requires updates.
     */
    private void onViewportResize()
    {
        Rectangle viewRect = viewport.getViewRect();
        setPreferredSize(new Dimension(viewRect.width, viewRect.height));
        setBounds(viewRect);
    }

    /**
     * Monitors changes in view port size and make the size of this component
     * match.
     */
    private class ViewportSizeMonitor extends ComponentAdapter
    {
        /** Invoked when the component's size changes. */
        public void componentResized(ComponentEvent e)
        {
            onViewportResize();
        }
    }
}
