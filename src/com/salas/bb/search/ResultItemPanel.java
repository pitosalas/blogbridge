// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: ResultItemPanel.java,v 1.5 2007/06/12 10:35:32 spyromus Exp $
//

package com.salas.bb.search;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.uif.DelegatingMouseListener;
import com.salas.bb.views.feeds.ArticlePinControl;
import com.salas.bb.views.feeds.ArticleReadControl;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * Item representative.
 */
public class ResultItemPanel extends JPanel
{
    private final ResultItem item;

    /** TRUE when this item can be shown to the user. */
    private boolean visibility;
    /** TRUE when this item is a part of collapsed group. */
    private boolean collapsed;
    /** TRUE when this item is filtered out and can't be displayed. */
    private boolean filtered;

    private JLabel      lbTitle;
    private JLabel      lbDate;
    private ArticlePinControl lbPin;
    private JLabel lbUnread;

    /**
     * Creates item.
     *
     * @param aItem item.
     */
    public ResultItemPanel(ResultItem aItem)
    {
        item = aItem;

        Font font = getFont().deriveFont(10f);

        setBorder(new ResultItemBorder(Color.decode("#f7f7f7")));
        setLayout(new FormLayout("20px, 16px, 5px, 50px:grow, 8px, p, 8px, p, 8px, p, 5px", "2px, p, 2px"));
        CellConstraints cc = new CellConstraints();

        JLabel lbIcon = new JLabel(item.getType().getIcon());
        add(lbIcon, cc.xy(2, 2));

        lbTitle = new JLabel(item.toString());
        lbTitle.setFont(font);
        add(lbTitle, cc.xy(4, 2));

        lbDate = new JLabel(dateToString(item.getDate()));
        lbDate.setFont(font);
        add(lbDate, cc.xy(6, 2));

        if (aItem.getObject() instanceof IArticle)
        {
            IArticle article = (IArticle)aItem.getObject();

            lbPin = new ArticlePinControl(article);
            add(lbPin, cc.xy(10, 2));

            lbUnread = new ArticleReadControl(article);
            add(lbUnread, cc.xy(8, 2));

            // Setup the tooltip and delegate the mouse events to the parent (this panel)
            IFeed feed = article.getFeed();
            lbTitle.setToolTipText(feed == null ? null : feed.getTitle());
            DelegatingMouseListener ml = new DelegatingMouseListener(lbTitle, false, 2);
            lbTitle.addMouseListener(ml);
        }

        setSelected(false);

        visibility = false;
        collapsed = false;
    }

    /**
     * Converts date to the string to display.
     *
     * @param date date.
     *
     * @return string.
     */
    private String dateToString(Date date)
    {
        if (date == null) return null;

        return TimeRange.findRangeName(date.getTime());
    }

    /**
     * Returns associated item.
     *
     * @return item.
     */
    public ResultItem getItem()
    {
        return item;
    }

    /**
     * Selects / deselects the item.
     *
     * @param sel <code>TRUE</code> to select.
     */
    public void setSelected(boolean sel)
    {
        setBackground(sel ? Color.decode("#3875d7") : Color.WHITE);
        Color fore = sel ? Color.WHITE : Color.BLACK;
        lbTitle.setForeground(fore);
        lbDate.setForeground(fore);
    }

    /**
     * Sets the visibility of an item. Each item can or cannot be visible due to
     * the maximum number of items allowed to be shown in a group.
     *
     * @param aVisibility <code>TRUE</code> if is able to be shown.
     */
    public void setVisibility(boolean aVisibility)
    {
        visibility = aVisibility;
        updateVisibility();
    }

    /**
     * Returns <code>TRUE</code> if item is visible.
     *
     * @return <code>TRUE</code> if item is visible.
     */
    public boolean isVisibility()
    {
        return visibility;
    }

    /**
     * Sets the group collapse flag. This flag is set when the item is part of collapsed
     * group.
     *
     * @param aCollapsed <code>TRUE</code> if the group is collapsed.
     */
    public void setCollapsed(boolean aCollapsed)
    {
        collapsed = aCollapsed;
        updateVisibility();
    }

    /**
     * Sets the filtered state of the item. When <code>TRUE</code> the item cannot be
     * displayed.
     *
     * @param aFiltered <code>TRUE</code> if filtered out.
     */
    public void setFiltered(boolean aFiltered)
    {
        filtered = aFiltered;
        updateVisibility();
    }

    /**
     * Returns <code>TRUE</code> if the item is filtered.
     *
     * @return <code>TRUE</code> if filtered.
     */
    public boolean isFiltered()
    {
        return filtered;
    }

    /**
     * Updates visibility depending on the visibility and group collapse flags.
     */
    private void updateVisibility()
    {
        setVisible(visibility && !collapsed && !filtered);
    }
}
