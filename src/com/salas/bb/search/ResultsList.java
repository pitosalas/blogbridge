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
// $Id: ResultsList.java,v 1.14 2007/09/19 16:09:25 spyromus Exp $
//

package com.salas.bb.search;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.IArticle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Search list, which is capable of displaying result items in a grouped, sorted and
 * filtered way.
 */
public class ResultsList extends JPanel implements IResultsListModelListener, Scrollable
{
    /** Any dates allowed. */
    public static final int DATE_ANY        = 0;
    /** Today items only allowed. */
    public static final int DATE_TODAY      = 1;
    /** Items since yesterday are allowed. */
    public static final int DATE_YESTERDAY  = 2;
    /** Items during this week are allowed. */
    public static final int DATE_WEEK       = 3;
    /** Items during this month are allowed. */
    public static final int DATE_MONTH      = 4;
    /** Items during this year are allowed. */
    public static final int DATE_YEAR       = 5;

    /** The map of group keys to groups. */
    private final Map<Integer, ResultGroupPanel> groups = new TreeMap<Integer, ResultGroupPanel>();

    /** Current date filtering option. */
    private int dateRange = DATE_ANY;

    /** Limit time is based on the {@link #dateRange} property and works as filter. */
    private long limitTime;

    /** Number of items to show in each group initially. */
    private int itemGroupLimit;
    /** Currently selected item. */
    private ResultItemPanel selectedItem;

    /** Action listeners. */
    private final List<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * Creates search results list.
     *
     * @param model model this list will be using.
     */
    public ResultsList(ResultsListModel model)
    {
        // Register the model
        model.addListener(this);

        itemGroupLimit = 5;
        limitTime = getLimitTime(dateRange);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setFocusable(true);
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Invoked when the model is cleared.
     *
     * @param model model.
     */
    public void onClear(IResultsListModel model)
    {
        groups.clear();
        removeAll();

        revalidate();
    }

    /**
     * Invoked when an item is added to the model.
     *
     * @param model model.
     * @param item  item added.
     * @param group group the item was added to.
     */
    public void onItemAdded(IResultsListModel model, ResultItem item, ResultGroup group)
    {
        // Create the item panel and register it
        ResultItemPanel itemPanel = new ResultItemPanel(item);
        ResultGroupPanel groupPanel = groups.get(group.getKey());

        // Register the item within the panel
        int index = groupPanel.register(itemPanel) + 1;
        index += indexOf(groupPanel);
        add(itemPanel, index);

        // Revalidate the list
        revalidate();
    }

    /**
     * Invoked when an item is removed from the group.
     *
     * @param model model.
     * @param item  item removed.
     * @param group group the item was removed from.
     */
    public void onItemRemoved(IResultsListModel model, ResultItem item, ResultGroup group)
    {
        // Remove item panel from the group
        ResultGroupPanel groupPanel = groups.get(group.getKey());
        ResultItemPanel itemPanel = groupPanel.removeItemPanelFor(item);

        if (itemPanel != null) remove(itemPanel);
        
        // Revalidate the list
        revalidate();
    }

    /**
     * Invoked when the model adds a group to hold new items.
     *
     * @param model     model.
     * @param group     group added.
     * @param ordered   when <code>TRUE</code> group is added in the order of appearance.
     */
    public void onGroupAdded(IResultsListModel model, ResultGroup group, boolean ordered)
    {
        // Create the group panel and initialize it with parameters
        ResultGroupPanel groupPanel = createGroupPanel(group);
        groupPanel.setItemLimit(itemGroupLimit);
        groupPanel.setLimitTime(limitTime);
        groups.put(group.getKey(), groupPanel);

        // Find a group before which to add this one
        ResultGroupPanel preGroup = null;
        if (!ordered)
        {
            Integer key = group.getKey();
            Iterator<Integer> it = groups.keySet().iterator();
            while (preGroup == null && it.hasNext())
            {
                Object otherKey = it.next();
                if (key == otherKey && it.hasNext()) preGroup = groups.get(it.next());
            }
        }

        // Add the group and its more-component
        int index = preGroup == null ? -1 : indexOf(preGroup);
        add(groupPanel, index);
        add(groupPanel.getMoreComponent(), index == -1 ? -1 : index + 1);
    }

    /**
     * Creates group panel.
     *
     * @param group group.
     *
     * @return panel.
     */
    protected ResultGroupPanel createGroupPanel(ResultGroup group)
    {
        return new ResultGroupPanel(group.getName());
    }

    /**
     * Invoked when the model removes a group to hold new items.
     *
     * @param model model.
     * @param group group added.
     */
    public void onGroupRemoved(IResultsListModel model, ResultGroup group)
    {
        // Remove the panel and it's more-component
        ResultGroupPanel panel = groups.remove(group.getKey());
        remove(panel);
        remove(panel.getMoreComponent());

        // Remove all items
        panel.removeAllItemsFrom(this);

        revalidate();
    }

    /**
     * Invoked when the model changes a group to hold new items.
     *
     * @param model model.
     * @param group group added.
     */
    public void onGroupUpdated(IResultsListModel model, ResultGroup group)
    {
        // Update the group title
        ResultGroupPanel groupPanel = groups.get(group.getKey());
        if (groupPanel != null)
        {
            String title = group.getName();
            groupPanel.setTitle(title);
        }
    }

    /**
     * Sets the number of item to display in the group max.
     *
     * @param limit limit.
     */
    public void setItemGroupLimit(int limit)
    {
        itemGroupLimit = limit;

        for (ResultGroupPanel group : groups.values()) group.setItemLimit(limit);
    }

    /**
     * Sets date range for filtering out unwanted items.
     *
     * @param aDateRange range.
     */
    public void setDateRange(int aDateRange)
    {
        if (dateRange != aDateRange)
        {
            dateRange = aDateRange;
            limitTime = getLimitTime(dateRange);

            for (ResultGroupPanel group : groups.values()) group.setLimitTime(limitTime);
        }
    }

    /**
     * Adds action listener.
     *
     * @param l listener.
     */
    public void addActionListener(ActionListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes action listeners.
     *
     * @param l listener.
     */
    public void removeActionListener(ActionListener l)
    {
        listeners.remove(l);
    }

    /**
     * Returns currently selected item.
     *
     * @return item.
     */
    public ResultItem getSelectedItem()
    {
        return selectedItem == null ? null : selectedItem.getItem();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns index of component.
     *
     * @param comp component.
     *
     * @return index.
     */
    private int indexOf(Component comp)
    {
        int index = -1;

        for (int i = 0; index == -1 && i < getComponentCount(); i++)
        {
            if (getComponent(i) == comp) index = i;
        }

        return index;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked when new item is selected.
     *
     * @param item item.
     */
    private void onItemSelected(ResultItemPanel item)
    {
        requestFocusInWindow();
        
        if (selectedItem != null) selectedItem.setSelected(false);

        selectedItem = item;

        if (selectedItem != null)
        {
            selectedItem.setSelected(true);
            scrollRectToVisible(selectedItem.getBounds());
        }
    }

    /**
     * Invoked when item selection is confirmed (fired).
     */
    void onItemFired()
    {
        ActionEvent event = null;

        for (ActionListener listener : listeners)
        {
            if (event == null) event = new ActionEvent(this, 0, "");
            listener.actionPerformed(event);
        }
    }

    /**
     * Invoked when item selection is used to toggle the read state.
     */
    private void onItemToggleReadState()
    {
        if (selectedItem != null)
        {
            Object o = selectedItem.getItem().getObject();
            if (o instanceof IArticle)
            {
                IArticle article = (IArticle)o;

                GlobalController.readArticles(!article.isRead(),
                    null,
                    null,
                    article);
            }
        }
    }

    /**
     * Invoked when item selection is used to toggle the pin state.
     */
    private void onItemTogglePinState()
    {
        if (selectedItem != null)
        {
            Object o = selectedItem.getItem().getObject();
            if (o instanceof IArticle)
            {
                IArticle article = (IArticle)o;
                GlobalController.pinArticles(!article.isPinned(),
                    null,
                    null,
                    article);
            }
        }
    }

    /**
     * Invoked when previous visible item selection is requested.
     */
    public void onPrevItemSelected()
    {
        if (selectedItem != null)
        {
            int index = indexOf(selectedItem);
            ResultItemPanel toSelect = null;
            for (int i = index - 1; toSelect == null && i > 0; i--)
            {
                Component c = getComponent(i);
                if (c instanceof ResultItemPanel && c.isVisible()) toSelect = (ResultItemPanel)c;
            }

            if (toSelect != null) onItemSelected(toSelect);
        }
    }

    /**
     * Invoked when next visible item selection is requested.
     */
    public void onNextItemSelected()
    {
        int index = selectedItem == null ? 0 : indexOf(selectedItem);
        ResultItemPanel toSelect = null;
        for (int i = index + 1; toSelect == null && i < getComponentCount(); i++)
        {
            Component c = getComponent(i);
            if (c instanceof ResultItemPanel && c.isVisible()) toSelect = (ResultItemPanel)c;
        }

        if (toSelect != null) onItemSelected(toSelect);
    }

    /**
     * Calculates limit time basing on the date range.
     *
     * @param dateRange date range.
     *
     * @return limit time.
     */
    private static long getLimitTime(int dateRange)
    {
        if (dateRange == DATE_ANY) return -1L;

        // Get today time
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long time;

        switch (dateRange)
        {
            case DATE_YESTERDAY:
                cal.add(Calendar.DATE, -1);
                break;
            case DATE_WEEK:
                cal.add(Calendar.DATE, -7);
                break;
            case DATE_MONTH:
                cal.set(Calendar.DATE, 1);
                break;
            case DATE_YEAR:
                cal.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                break;
        }

        time = cal.getTimeInMillis();

        return time;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the preferred size of the viewport for a view component. For example, the preferred size of a
     * <code>JList</code> component is the size required to accommodate all of the cells in its list. However, the value
     * of <code>preferredScrollableViewportSize</code> is the size required for <code>JList.getVisibleRowCount</code>
     * rows. A component without any properties that would affect the viewport size should just return
     * <code>getPreferredSize</code> here.
     *
     * @return the preferredSize of a <code>JViewport</code> whose view is this <code>Scrollable</code>
     *
     * @see javax.swing.JViewport#getPreferredSize
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment that will completely expose
     * one new row or column, depending on the value of orientation.  Ideally, components should handle a partially
     * exposed row or column by returning the distance required to completely expose the item.
     * <p/>
     * Scrolling containers, like JScrollPane, will use this method each time the user requests a unit scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
     *
     * @return The "unit" increment for scrolling in the specified direction. This value should always be positive.
     *
     * @see javax.swing.JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        int increment = 1;

        if (orientation == SwingConstants.VERTICAL)
        {
            int px = visibleRect.x;
            int py = visibleRect.y;

            increment = getIncrementForBorderChild(direction, px, py);
            if (increment == 0)
            {
                // Nowhere to move for the border child
                // See if we can move one child up / down
                if (direction < 0)
                {
                    // up
                    if (py > 0) increment = getIncrementForBorderChild(direction, px, py - 1);
                } else
                {
                    // down
                    int height = getHeight();
                    if (py < height - visibleRect.height) increment = getIncrementForBorderChild(direction, px, py + 1);
                }

                // We need to compensate for these '-1' or '+1' above
                if (increment > 0) increment++;
            }
        }

        return increment;
    }

    private int getIncrementForBorderChild(int direction, int px, int py)
    {
        int increment = 0;

        Component comp = findComponentAt(px, py);
        if (comp != null)
        {
            Point inCompPoint = SwingUtilities.convertPoint(this, px, py, comp);
            int y = inCompPoint.y;
            if (direction < 0)
            {
                // up
                if (y > 0) increment = y;
            } else
            {
                // down
                int height = comp.getHeight();
                if (y < height) increment = height - y;
            }
        }

        return increment;
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment that will completely expose
     * one block of rows or columns, depending on the value of orientation.
     * <p/>
     * Scrolling containers, like JScrollPane, will use this method each time the user requests a block scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction   Less than zero to scroll up/left, greater than zero for down/right.
     *
     * @return The "block" increment for scrolling in the specified direction. This value should always be positive.
     *
     * @see javax.swing.JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        int px = visibleRect.x;
        int py = visibleRect.y;
        int he = getHeight();
        int maxy = he - visibleRect.height;
        Component started = findComponentAt(px, py);

        int inc = 0;
        boolean found = false;

        if (direction > 0)
        {
            for (int y = py + 1; !found && y < maxy; y++)
            {
                Component comp = findComponentAt(px, y);
                if (comp != started && comp instanceof ResultGroupPanel)
                {
                    found = true;
                    inc = getIncrementForBorderChild(-direction, px, y) + (y - py) - 2;
                } else
                {
                    // Jump to the end of the item
                    y += getIncrementForBorderChild(direction, px, y);
                }
            }
        } else
        {
            for (int y = py - 1; !found && y >= 0; y--)
            {
                Component comp = findComponentAt(px, y);
                if (comp != started && comp instanceof ResultGroupPanel)
                {
                    found = true;
                    inc = getIncrementForBorderChild(direction, px, y) + (py - y);
                } else
                {
                    // Jump to the end of the item
                    y -= getIncrementForBorderChild(direction, px, y);
                }
            }
        }

        return inc;
    }

    /**
     * Return true if a viewport should always force the width of this <code>Scrollable</code> to match the width of the
     * viewport. For example a normal text view that supported line wrapping would return true here, since it would be
     * undesirable for wrapped lines to disappear beyond the right edge of the viewport.  Note that returning true for a
     * Scrollable whose ancestor is a JScrollPane effectively disables horizontal scrolling.
     * <p/>
     * Scrolling containers, like JViewport, will use this method each time they are validated.
     *
     * @return True if a viewport should force the Scrollables width to match its own.
     */
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    /**
     * Return true if a viewport should always force the height of this Scrollable to match the height of the viewport.
     * For example a columnar text view that flowed text in left to right columns could effectively disable vertical
     * scrolling by returning true here.
     * <p/>
     * Scrolling containers, like JViewport, will use this method each time they are validated.
     *
     * @return True if a viewport should force the Scrollables height to match its own.
     */
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

// ---------------------------------------------------------------------------------------------

    /**
     * Processes mouse events.
     *
     * @param e event.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        Component c = getComponentAt(e.getPoint());
        if (!(c instanceof ResultItemPanel)) return;

        switch (e.getID())
        {
            case MouseEvent.MOUSE_PRESSED:
                onItemSelected((ResultItemPanel)c);
                break;

            case MouseEvent.MOUSE_CLICKED:
                if (e.getClickCount() == 2) onItemFired();
                break;
        }
    }

    /**
     * Processes navigation events.
     *
     * @param e event.
     */
    protected void processKeyEvent(KeyEvent e)
    {
        if (e.getID() != KeyEvent.KEY_PRESSED) return;

        int code = e.getKeyCode();

        switch (code)
        {
            case KeyEvent.VK_UP:
                onPrevItemSelected();
                break;

            case KeyEvent.VK_DOWN:
                onNextItemSelected();
                break;

            case KeyEvent.VK_ENTER:
                onItemFired();
                break;

            case KeyEvent.VK_Q:
                onItemToggleReadState();
                break;

            case KeyEvent.VK_P:
                onItemTogglePinState();
                break;

            case KeyEvent.VK_PAGE_UP:
                Rectangle visible = getVisibleRect();
                visible.y = Math.max(0, visible.y - visible.height);
                scrollRectToVisible(visible);
                break;

            case KeyEvent.VK_PAGE_DOWN:
                visible = getVisibleRect();
                visible.y = Math.max(0, Math.min(getHeight() - visible.height, visible.y + visible.height));
                scrollRectToVisible(visible);
                break;

            case KeyEvent.VK_HOME:
                visible = getVisibleRect();
                visible.y = 0;
                scrollRectToVisible(visible);
                break;

            case KeyEvent.VK_END:
                visible = getVisibleRect();
                visible.y = Math.max(0, getHeight() - visible.height);
                scrollRectToVisible(visible);
                break;
        }
    }
}
