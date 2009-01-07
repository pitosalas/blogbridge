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
// $Id: PagingControl.java,v 1.4 2007/07/19 11:02:28 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.binding.value.ValueModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Shows two buttons and controls their state.
 * Sends the events to the listeners when the user clicks on the buttons.
 */
public class PagingControl extends AbstractSelectorComponent
{
    /** Mode / button code for the previous page button. */
    private static final int PREV = 0;
    /** Mode / button code for the next page button. */
    private static final int NEXT = 1;

    private static final String[] BUTTONS = { "left", "right" };
    private static final Icon[][] ICONS = prepareIcons(BUTTONS);

    /** The maximum number of pages. */
    private int pageCount;

    private int xSep;
    private int xNext;

    /** The model holding the number of pages. */
    private final ValueModel pageCountModel;

    /**
     * Creates a control with the page model.
     *
     * @param pageModel the integer model containing the page number.
     * @param pageCountModel the integer model for the number of pages.
     */
    public PagingControl(ValueModel pageModel, ValueModel pageCountModel)
    {
        super(pageModel);
        this.pageCountModel = pageCountModel;

        pageCountModel.addValueChangeListener(new PageCountModelListener());
        setPageCount((Integer)pageCountModel.getValue());

        pageModel.addValueChangeListener(new PageModelListener());
    }

    /**
     * Sets the maximum number of pages this control should
     * operate with. If the model has page number greater than
     * maximum, it's set to zero. If the <code>maxPages</code>
     * is zero, the page number is still zero and controls are disabled.
     *
     * @param pageCount new number of pages.
     */
    public void setPageCount(int pageCount)
    {
        this.pageCount = pageCount;

        // Sets the last page if there are not enough pages
        if (getCurrentPage() > pageCount - 2)
        {
            model.setValue(pageCount == 0 ? 0 : pageCount - 1);
        }

        repaint();
    }

    /**
     * Returns current page.
     *
     * @return current page.
     */
    public int getCurrentPage()
    {
        return (Integer)model.getValue();
    }

    @Override
    protected Icon getIcon(int button, State state)
    {
        return ICONS[button][state.ordinal()];
    }

    @Override
    protected void setMode(int mode)
    {
        int curPage = getCurrentPage();
        if (mode == PREV)
        {
            // Previous page request
            if (curPage > 0) model.setValue(curPage - 1);
        } else
        {
            // Next page request
            if (curPage < pageCount - 1) model.setValue(curPage + 1);
        }
    }

    /**
     * Invoked when painting of the component is necessary.
     *
     * @param g graphics context.
     */
    protected void paintComponent(Graphics g)
    {
        int curPage = getCurrentPage();

        State prevState = curPage < 1 ? State.OFF
            : pressed == PREV && mouseOver ? State.PRESSED : State.ON;
        State nextState = curPage >= pageCount - 1 ? State.OFF
            : pressed == NEXT && mouseOver ? State.PRESSED : State.ON;

        State sepaState = (prevState == State.ON || nextState == State.ON ||
                prevState == State.PRESSED || nextState == State.PRESSED)
            ? State.PRESSED : State.OFF;

        getIcon(PREV, prevState).paintIcon(this, g, 0, 0);
        getSeparatorIcon(sepaState).paintIcon(this, g, xSep, 0);
        getIcon(NEXT, nextState).paintIcon(this, g, xNext, 0);
    }

    @Override
    protected int locationToMode(Point aPoint)
    {
        int md;
        int x = aPoint.x;

        if (x >= xNext)
        {
            md = NEXT;
        } else
        {
            md = PREV;
        }

        return md;
    }

    /**
     * Reports a desired dimensions for this component. Used during the construction (once).
     *
     * @return dimensions.
     */
    protected Dimension getPreferredDimensions()
    {
        Dimension dimPrev = getIconDimension(PREV, State.ON);
        Dimension divNext = getIconDimension(NEXT, State.ON);
        Dimension dimSep = getIconDimensions(getSeparatorIcon(State.ON));

        xSep = dimPrev.width;
        xNext = xSep + dimSep.width;

        return new Dimension(xNext + divNext.width, dimPrev.height);
    }

    /**
     * Monitors the changes in the page count.
     */
    private class PageCountModelListener implements PropertyChangeListener
    {
        /**
         * Invoked when the property changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            setPageCount((Integer)evt.getNewValue());
        }
    }

    /**
     * Repaints the control when the page number changes from outside.
     */
    private class PageModelListener implements PropertyChangeListener
    {
        /**
         * Invoked when the property changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            repaint();
        }
    }
}
