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
// $Id: ResultGroupPanel.java,v 1.8 2007/06/27 09:49:19 spyromus Exp $
//

package com.salas.bb.search;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The header of items group.
 */
public class ResultGroupPanel extends JPanel
{
    private final MoreItem moreItem;
    private final List<ResultItemPanel> items = new ArrayList<ResultItemPanel>();
    private final JLabel lbCount;

    private boolean collapsed;
    private int itemLimit = 2;
    private long limitTime;
    private JLabel lbTitle;
    private Color backgroundColor1;
    private Color backgroundColor2;

    /**
     * Creates group item.
     *
     * @param name name.
     */
    public ResultGroupPanel(String name)
    {
        this(name, null, Color.decode("#6b9eee"), Color.decode("#488bf4"));
    }

    /**
     * Creates group item.
     *
     * @param name      name.
     * @param tooltip   tooltip.
     * @param color1    first background color.
     * @param color2    second background color.
     */
    public ResultGroupPanel(String name, String tooltip, Color color1, Color color2)
    {
        backgroundColor1 = color1;
        backgroundColor2 = color2;

        moreItem = new MoreItem();
        lbCount = new JLabel("0");

        setLayout(new FormLayout("5px, 10px, 5px, 50px:grow, 5px, 30px, 5px", "2px, p, 2px"));
        CellConstraints cc = new CellConstraints();

        GroupCollapseIcon icon = new GroupCollapseIcon();
        icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(icon, cc.xy(2, 2));

        Font fnt = new Font("Lucida Grande", Font.BOLD, 12);
        lbTitle = new JLabel(name);
        lbTitle.setFont(fnt);
        lbTitle.setForeground(Color.WHITE);
        lbCount.setFont(fnt);
        lbCount.setForeground(Color.WHITE);
        lbCount.setAlignmentX(0.5f);
        add(lbTitle, cc.xy(4, 2));
        add(lbCount, cc.xy(6, 2));

        setBackground(backgroundColor1);
        if (tooltip != null) setToolTipText(tooltip);
    }

    /**
     * Paints background and children.
     *
     * @param g context.
     */
    public void paint(Graphics g)
    {
        // Paint background
        int width = getSize().width;
        int height = getSize().height;

        Graphics2D g2 = (Graphics2D)g;
        Paint storedPaint = g2.getPaint();
        g2.setPaint(new GradientPaint(0, 0, backgroundColor1, 0, height, backgroundColor2));
        g2.fillRect(0, 0, width, height);
        g2.setPaint(storedPaint);

        paintChildren(g);
    }

    /**
     * Sets the limit.
     *
     * @param aItemLimit limit.
     */
    public void setItemLimit(int aItemLimit)
    {
        itemLimit = aItemLimit;

        int hidden = 0;
        for (int i = 0; i < items.size(); i++)
        {
            ResultItemPanel item = items.get(i);
            item.setVisibility(i < itemLimit);
            if (i >= itemLimit) hidden++;
        }

        moreItem.setCount(hidden);
    }

    /**
     * Registers item within the group.
     *
     * @param item item.
     *
     * @return index within the group.
     */
    public int register(ResultItemPanel item)
    {
        int visibleItemsCount = getVisibleItemsCount();

        boolean priority = item.getItem().isPriority();
        int index = priority ? 0 : items.size();
        items.add(index, item);

        boolean filtered = isFiltered(item);
        item.setFiltered(filtered);

        if (!filtered) recalcView();
        if (visibleItemsCount == 0) setVisible(true);

        return index;
    }

    /**
     * Recalculates the view.
     */
    private void recalcView()
    {
        int more = 0;
        int visible = 0;
        for (ResultItemPanel itm : items)
        {
            if (itm.isFiltered())
            {
                itm.setVisibility(false);
            } else if (visible >= itemLimit)
            {
                itm.setVisibility(false);
                more++;
            } else
            {
                itm.setVisibility(true);
                visible++;
            }
        }

        moreItem.setCount(more);
        lbCount.setText(Integer.toString(visible + more));
    }

    /**
     * Collapses / expands the group.
     *
     * @param col <code>TRUE</code> to collapse.
     */
    public void setCollapsed(boolean col)
    {
        collapsed = col;

        for (ResultItemPanel item : items) item.setCollapsed(col);

        moreItem.updateVisibility();
    }

    /**
     * Returns more-component.
     *
     * @return component.
     */
    public JComponent getMoreComponent()
    {
        return moreItem;
    }

    /**
     * Returns the number of items potentially visible to the client.
     *
     * @return items.
     */
    private int getVisibleItemsCount()
    {
        int cnt = 0;

        for (ResultItemPanel item : items) if (item.isVisibility() && !item.isFiltered()) cnt++;

        return cnt;
    }

    /**
     * Invoked when the user asks for more items.
     */
    private void onShowMore()
    {
        for (ResultItemPanel item : items) item.setVisibility(true);

        moreItem.setCount(0);
    }

    /**
     * Sets filtering time. If the time is set to <code>-1</code> then filtering is off.
     * Otherwise it shows the time after which all items are allowed.
     *
     * @param aLimitTime the time used for limiting.
     */
    public void setLimitTime(long aLimitTime)
    {
        limitTime = aLimitTime;

        int shown = 0;
        int hidden = 0;
        for (ResultItemPanel item : items)
        {
            item.setFiltered(isFiltered(item));
            item.setVisibility(!item.isFiltered() && shown < itemLimit);

            if (item.isVisible()) shown++;
            else if (!item.isFiltered()) hidden++;
        }

        setVisible(shown > 0);
        moreItem.setCount(hidden);
    }

    /**
     * Returns <code>TRUE</code> if the item should be filtered out.
     *
     * @param aItem item to check.
     *
     * @return <code>TRUE</code> if the item should be filtered out.
     */
    private boolean isFiltered(ResultItemPanel aItem)
    {
        if (limitTime == -1L) return false;

        Date date = aItem.getItem().getDate();

        return date == null || date.getTime() <= limitTime;
    }

    /**
     * Removes the panel of the item from the group.
     *
     * @param item item to remove the panel of.
     *
     * @return panel removed or <code>NULL</code> if not found.
     */
    public ResultItemPanel removeItemPanelFor(ResultItem item)
    {
        for (ResultItemPanel panel : items)
        {
            if (item == panel.getItem())
            {
                // Panel found
                items.remove(panel);
                recalcView();
                return panel;
            }
        }

        return null;
    }

    /**
     * Removes all items from the given container.
     *
     * @param container container.
     */
    public void removeAllItemsFrom(Container container)
    {
        for (ResultItemPanel item : items) container.remove(item);
    }

    /**
     * Updates the title of the group.
     *
     * @param title new title.
     */
    public void setTitle(String title)
    {
        lbTitle.setText(title);
    }

    /** More ... item component. */
    private class MoreItem extends JPanel
    {
        private final JLabel lbMore;
        private int count;

        public MoreItem()
        {
            setBorder(new ResultItemBorder(Color.decode("#f7f7f7")));
            setBackground(Color.decode("#fafafa"));
            Font font = getFont().deriveFont(10f);

            setLayout(new FormLayout("41px, p, 5px", "2px, p, 2px"));
            CellConstraints cc = new CellConstraints();

            lbMore = new JLabel();
            lbMore.setFont(font);
            lbMore.setForeground(Color.decode("#567fca"));
            lbMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(lbMore, cc.xy(2, 2));

            setCount(0);

            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }

        /**
         * Processes clicks.
         *
         * @param e event.
         */
        protected void processMouseEvent(MouseEvent e)
        {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) onShowMore();
        }

        /**
         * Sets counter and shows/hides label.
         *
         * @param cnt counter.
         */
        public void setCount(int cnt)
        {
            count = cnt;
            lbMore.setText(MessageFormat.format(Strings.message("search.0.more"),
                Integer.toString(count)));
            updateVisibility();
        }

        /**
         * Updates visibility state.
         */
        private void updateVisibility()
        {
            setVisible(count > 0 && !collapsed);
        }
    }
    /**
     * Collapse / expand icon.
     */
    public static class GroupCollapseIcon extends JComponent
    {
        private boolean collapsed;

        /** Creates icon. */
        public GroupCollapseIcon()
        {
            Dimension size = new Dimension(9, 9);
            setMinimumSize(size);
            setPreferredSize(size);
            setMaximumSize(size);

            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }

        /**
         * Mouse event processor.
         *
         * @param e event.
         */
        protected void processMouseEvent(MouseEvent e)
        {
            if (e.getID() == MouseEvent.MOUSE_PRESSED &&
                SwingUtilities.isLeftMouseButton(e))
            {
                setCollapsed(!collapsed);

                ResultGroupPanel group = (ResultGroupPanel)getParent();
                group.setCollapsed(collapsed);
            }
        }

        /**
         * Collapses / expands icon.
         *
         * @param aCollapsed new state.
         */
        public void setCollapsed(boolean aCollapsed)
        {
            collapsed = aCollapsed;
            repaint();
        }

        /**
         * Paints the icon.
         *
         * @param g context.
         */
        public void paint(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;

            g.setColor(Color.WHITE);

            Object aaval = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (collapsed)
            {
                int arrowX = 1;
                int arrowY = 0;
                g.fillPolygon(
                    new int[] { arrowX, arrowX + 7, arrowX },
                    new int[] { arrowY, arrowY + 4, arrowY + 9 },
                    3 );
            } else
            {
                int arrowX = 0;
                int arrowY = 1;
                g.fillPolygon(
                    new int[] { arrowX, arrowX + 4, arrowX + 9 },
                    new int[] { arrowY, arrowY + 7, arrowY },
                    3 );
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaval);
        }
    }
}
