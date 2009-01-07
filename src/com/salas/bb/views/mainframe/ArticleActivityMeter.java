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
// $Id: ArticleActivityMeter.java,v 1.14 2006/05/31 12:49:31 spyromus Exp $
//
package com.salas.bb.views.mainframe;


import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Article activity meter component.
 */
public class ArticleActivityMeter extends JComponent
{
    private static final int NUM_DAYS           = 7;
    private static final int NUM_BLOCKS_PER_DAY = 6;

    /** Size of each drawn square, including gap. */
    private static final int BLOCK_SIZE         = 4;

    /** Width of gap. */
    private static final int BLOCK_GAP          = 1;

    /** Size of overflow triangle at top. */
    private static final int OVER_SIZE          = 4;

    /** Border around all blocks. */
    private static final int BORDER_WIDTH       = 1;

    private static final Color ARTICLE_READ_COLOR       = new Color(84, 194, 84); //113, 113, 255);
    private static final Color ARTICLE_UNREAD_COLOR     = new Color(194, 84, 84); //217, 0, 0);
    private static final Color NO_ARTICLE_COLOR         = new Color(194, 194, 194); //229, 229, 229);
    private static final Color ARTICLE_HOVER_HIGHLIGHT  = new Color(230, 230, 230); //0, 255, 0);
    
    /** If <code>TRUE</code>, newest days are shown on right side. */
    private static final boolean SHOW_NEWEST_ON_RIGHT   = true;

    /** Our fixed size. */
    private static final Dimension MY_SIZE = 
        new Dimension(NUM_DAYS * BLOCK_SIZE + BORDER_WIDTH * 2, 
            NUM_BLOCKS_PER_DAY * BLOCK_SIZE + OVER_SIZE + BORDER_WIDTH * 2);

    /** Day mouse hovering over; -1 for none.  */
    private int myHoverDay = -1;

    /** Article statistics we display. */
    private UnreadStats myUnreadStats;

    /** 
     * Constructs ArticleActivityMeter, used to show
     * last N days of read/unread articles.
     */
    public ArticleActivityMeter()
    {
        setFocusable(false);
    }

    /**
     * Process mouse clicks, entries and exits.
     *
     * @param e event.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_EXITED:
                // Clear the highlight when the mouse leaves
                setHoverDay(-1);
                break;
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
            case MouseEvent.MOUSE_CLICKED:
                // We need this to enable parent to activate dragging when user clicks over
                // the this component.
                UifUtilities.delegateEventToParent(this, e);
                break;
            default:
                break;
        }
    }

    /**
     * Process mouse movement event and delegate dragging to the parent.
     *
     * @param e event.
     */
    protected void processMouseMotionEvent(MouseEvent e)
    {
        super.processMouseMotionEvent(e);

        // Track mouse motion in order to highlight the day's blocks
        // under the mouse.
        if (e.getID() == MouseEvent.MOUSE_MOVED)
        {
            setHoverDay(pointToDay(e.getPoint()));
        } else if (e.getID() == MouseEvent.MOUSE_DRAGGED)
        {
            // We need this to let the parent handle dragging of feeds correctly.
            UifUtilities.delegateEventToParent(this, e);
        }
    }

    /**
     * Initializes control for display.
     *
     * @param stats unread statistics to display.
     */
    public void init(UnreadStats stats)
    {
        myUnreadStats = stats;

        setForeground(Color.DARK_GRAY);

        setSize(MY_SIZE);
        setPreferredSize(MY_SIZE);

        initToolTip();
    }

    /** 
     * Highlight the particular day, for mouse tracking purposes.
     *
     * @param day index of the day to highlight in range [0; MAX_DAYS) or
     *            <code>-1</code> to clear highlight.
     */
    public void setHoverDay(int day)
    {
        if (day != myHoverDay)
        {
            myHoverDay = day;
            initToolTip();
            repaint();
        }
    }

    /** 
     * Set the tooltip corresponding to whatever day is hovered over,
     * or no tooltip if nothing being hovered on.
     */
    protected void initToolTip()
    {
        String toolTip;

        if (myHoverDay < 0)
        {
            // Using an empty string here as opposed to a null keeps us registered with the
            // tooltip manager, which ensures that the tooltip appears even when only
            // once mouse move occurs over the component.
            toolTip = "";
        } else
        {
            Object[] args = new Object[3];
            String dayString, message;

            // Identify day of week: "today", "yesterday", or "last X".
            UnreadStats.DayCount dayCount = myUnreadStats.getDayCount(myHoverDay);
            if (myHoverDay < 2)
            {
                if (myHoverDay == 0)
                {
                    dayString = Strings.message("activitymeter.today");
                } else
                {
                    dayString = Strings.message("activitymeter.yesterday");
                }
            } else
            {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1 * (myHoverDay));
                SimpleDateFormat dateFormat = new SimpleDateFormat("'" + Strings.message("activitymeter.last") +
                    " 'EEEE");
                dayString = dateFormat.format(cal.getTime());
            }

            int total = dayCount.getTotal();
            int read = dayCount.getRead();
            int unread = dayCount.getUnread();

            args[0] = dayString;
            args[1] = new Integer(total);
            args[2] = new Integer(unread);

            // Choose a message corresponding to # of read/unread articles. 
            if (total == 0)
            {
                message = Strings.message("activitymeter.no.articles");
            } else if (total == 1)
            {
                message = (unread == 1)
                    ? Strings.message("activitymeter.1.article.unread")
                    : Strings.message("activitymeter.1.article");
            } else if (read == 0)
            {
                message = Strings.message("activitymeter.n.articles.all.unread");
            } else if (unread == 0)
            {
                message = Strings.message("activitymeter.n.articles");
            } else
            {
                message = Strings.message("activitymeter.n.articles.m.unread");
            }

            toolTip = MessageFormat.format(message, args);
        }

        setToolTipText(toolTip);
    }

   
    /** 
     * Paints the activity meter, with or without the mouse hover highlight.
     * The drawing is not particularly efficient: each of the 35
     * blocks and 5 overflow indicators is drawn individually.
     * It's likely this could be improved by creating cached images
     * for larger chunks of the display, so that we can paint
     * with fewer operations. Some candidates:
     * - create a cached image of 7x5 "no-article" blocks, and then draw
     *   the read/unread article blocks on top where necessary.
     * - create cached images for columns of 5 read and 5 unread blocks, since
     *   those often commonly appear.
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g)
    {
        // Draw the green hover highlight background
        // if we are in hover mode
        if (myHoverDay >= 0)
        {
            int x = dayToX(myHoverDay) - BLOCK_GAP;
            g.setColor(ARTICLE_HOVER_HIGHLIGHT);
            g.fillRect(x, 0, BLOCK_SIZE + BLOCK_GAP, 
                    BLOCK_SIZE * NUM_BLOCKS_PER_DAY + OVER_SIZE + BORDER_WIDTH * 2);
        }
        // Draw days from left to right. 
        for (int day = 0; day < NUM_DAYS; ++day)
        {
            UnreadStats.DayCount dc = myUnreadStats.getDayCount(day);
            int read, unread;
            unread = dc.getUnread();
            read = unread + dc.getRead();

            // Draw blocks for this day from bottom to top.
            // init coords to just below bottom-most block for this day
            int bx;
            if (SHOW_NEWEST_ON_RIGHT)
            {
                bx = dayToX(day);
            } else
            {
                bx = day * BLOCK_SIZE + BORDER_WIDTH;
            }
            
            int by = OVER_SIZE + (NUM_BLOCKS_PER_DAY * BLOCK_SIZE) + BORDER_WIDTH;

            for (int j = 0; j < NUM_BLOCKS_PER_DAY + 1; ++j)
            {
                Color col;
                if (j < unread)
                {
                    col = ARTICLE_UNREAD_COLOR;
                } else if (j < read)
                {
                    col = ARTICLE_READ_COLOR;
                } else
                {
                    col = NO_ARTICLE_COLOR;
                }

                if (day == myHoverDay) col = col.darker();
                
                g.setColor(col);

                if (j < NUM_BLOCKS_PER_DAY)
                {
                    // draw the normal block 
                    by -= BLOCK_SIZE;
                    g.fillRect(bx, by, BLOCK_SIZE - BLOCK_GAP, BLOCK_SIZE - BLOCK_GAP);
                } else //if (j < read)
                {
                    // Draw an overflow triangle, if the overflow
                    // corresponds to an unread or read value. 
                    by -= BLOCK_GAP + 1;
                    g.fillRect(bx, by, BLOCK_SIZE - BLOCK_GAP, 1);
                    by--;
                    g.fillRect(bx + 1, by, BLOCK_SIZE - BLOCK_GAP - 1, 1);
                    by--;
                    g.fillRect(bx + 2, by, BLOCK_SIZE - BLOCK_GAP - 2, 1);
                }
            }
        }
    }
    
    /** 
     * Finds the x coordinate of block drawn on a given day.
     *
     * @param day 0 - today, 1 - yesterday, etc.
     *
     * @return x-coordinate of right edge of block.
     */
    static int dayToX(int day)
    {
        int x;
        
        if (SHOW_NEWEST_ON_RIGHT)
        {
            x = MY_SIZE.width - ((day + 1) * BLOCK_SIZE) - BORDER_WIDTH;
        } else
        {
            x = day * BLOCK_SIZE + BORDER_WIDTH;
        }

        return x;
    }
    
    /** 
     * Given a point in our coordinates, returns the number of the day
     * it corresponds to.
     *
     * @param p the point.
     *
     * @return the day (0 == today, 1 == yesterday, etc.)
     */
    static int pointToDay(Point p)
    {
        int n;
        
        if (SHOW_NEWEST_ON_RIGHT)
        {
            n = (MY_SIZE.width - BORDER_WIDTH - p.x) / BLOCK_SIZE;
        } else
        {
            n = (p.x - BORDER_WIDTH) / BLOCK_SIZE;
        }
        n = Math.min(n, NUM_DAYS - 1);
        n = Math.max(n, 0);
        
        return n;
    }
}
