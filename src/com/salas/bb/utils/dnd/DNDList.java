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
// $Id: DNDList.java,v 1.23 2007/03/07 18:48:43 spyromus Exp $
//

package com.salas.bb.utils.dnd;

import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uif.util.SystemUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * List component with advanced dragging support.
 */
public class DNDList extends JList
{
    /** Name of dragging property. */
    public static final String PROP_DRAGGING = "dragging";

    /** Mouse dragged fake property used to broadcast the dragging event. */
    public static final String PROP_MOUSE_DRAGGED_EVENT = "mouseDragged";

    /** Number of pixels to deviate from original mouse pressure position to start dragging. */
    private static final int DND_DEVIATION_GESTURE = 8;

    private static final int DIVIDER_X = 2;
    private static final int DIVIDER_WIDTH_SUB = 3;

    public static final String[] STATES = { "Normal", "Armed", "Dragging" };

    public static final int STATE_NORMAL    = 0;
    public static final int STATE_ARMED     = 1;
    public static final int STATE_DRAGGING  = 2;

    private int state;

    private int pressPointX;
    private int pressPointY;

    private DNDPopup    popup;

    /** <code>TRUE</code> if dragging is performed within the list, <code>FALSE</code> otherwise. */
    private boolean     draggingInternal;
    private int         insertPosition;

    /** <code>TRUE</code> if copy-dragging is allowed from this list. */
    private final boolean copyingAllowed;

    /**
     * Constructs list with given data model.
     *
     * @param dataModel data model.
     */
    public DNDList(ListModel dataModel)
    {
        this(dataModel, true);
    }

    /**
     * Constructs list with given data model.
     *
     * @param dataModel data model.
     * @param aCopyingAllowed <code>TRUE</code> if drag-copying is allowed in this list.
     */
    public DNDList(ListModel dataModel, boolean aCopyingAllowed)
    {
        super(dataModel);
        copyingAllowed = aCopyingAllowed;

        setState(STATE_NORMAL);

        popup = new DNDPopup();
        popup.setInvoker(this);
        addMouseMotionListener(popup);

        draggingInternal = false;
        setInsertionPosition(-1);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    /**
     * Returns <code>TRUE</code> when in dragging state.
     *
     * @return <code>TRUE</code> when in dragging state.
     */
    private boolean isDragging()
    {
        return state == STATE_DRAGGING;
    }

    /**
     * Returns <code>TRUE</code> if dragging is performed within the list.
     *
     * @return <code>TRUE</code> if dragging is performed within the list.
     */
    public boolean isDraggingInternal()
    {
        return draggingInternal;
    }

    /**
     * Returns the index within the list where to put items which has been dragged.
     *
     * @return index.
     */
    public int getInsertPosition()
    {
        return insertPosition;
    }

    /**
     * Changes the state of the list.
     *
     * @param state the state.
     *
     * @see #STATE_ARMED
     * @see #STATE_DRAGGING
     * @see #STATE_NORMAL
     */
    private void setState(int state)
    {
        boolean oldIsDragging = isDragging();

        this.state = state;

        firePropertyChange(PROP_DRAGGING, oldIsDragging, isDragging());
    }

    /**
     * Processes mouse buttons events (pressed, clicked, released).
     *
     * @param e even.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        boolean entered = e.getID() == MouseEvent.MOUSE_ENTERED;
        boolean exited = e.getID() == MouseEvent.MOUSE_EXITED;
        boolean released = e.getID() == MouseEvent.MOUSE_RELEASED;
        boolean pressed = e.getID() == MouseEvent.MOUSE_PRESSED;


        // Skip processing of event when entering the list in dragging mode
        if (!entered || !isDragging()) super.processMouseEvent(e);

        if (entered)
        {
            // Mark as internal dragging
            draggingInternal = true;
            if (isDragging()) onDraggingMove(e);
        } else if (exited)
        {
            // Mark as external dragging
            draggingInternal = false;
            setInsertionPosition(-1);
        } else
        {
            // Buttons events
            // Depending on the state and event came process clicks differently
            switch (state)
            {
                case STATE_NORMAL:
                    if (pressed && SwingUtilities.isLeftMouseButton(e) &&
                        (!SystemUtils.IS_OS_MAC ||
                            (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0)) onArm(e);
                    break;
                case STATE_ARMED:
                    if (released) onDisarm();
                    break;
                case STATE_DRAGGING:
                    if (released) onFinishDragging();
                    break;
            }
        }
    }

    /**
     * Invoked when user has pressed the button and ready to release it or continue
     * with dragging.
     *
     * @param e event.
     */
    private void onArm(MouseEvent e)
    {
        if (!isSelectionEmpty())
        {
            setState(STATE_ARMED);
            pressPointX = (int)e.getPoint().getX();
            pressPointY = (int)e.getPoint().getY();
        }
    }

    /**
     * Invoked when user releases mouse button.
     */
    private void onDisarm()
    {
        setState(STATE_NORMAL);
    }

    /**
     * Invoked when we have an approval of user's intention to drag something.
     *
     * @param e event.
     */
    private void onStartDragging(MouseEvent e)
    {
        setState(STATE_DRAGGING);

        // Show popup with image of items we are going to drag
        Image dndObjectImage = createDragImage();
        popup.setImage(dndObjectImage);
        popup.setLocation(e.getPoint());
        popup.setVisible(true);

        // Register dragging start in context
        DNDListContext.startDragging(this, createDNDObjectFromSelectedItems(getSelectedValues()));
    }

    /**
     * Invoked when dragging has finished.
     */
    private void onFinishDragging()
    {
        // Register finish in context.
        DNDListContext.finishDragging();

        // Hide popup and repaint the list.
        popup.setVisible(false);
        repaint();

        onDisarm();
    }

    /**
     * Creates image of items being dragged.
     *
     * @return image.
     */
    private Image createDragImage()
    {
        BufferedImage bi = null;

        int itemsSelected = getSelectedIndices().length;
        int selectedIndex = getSelectedIndex();

        // get renderer for curretly selected cell and record its image
        ListCellRenderer cr = getCellRenderer();
        Component c = cr.getListCellRendererComponent(this, this.getSelectedValue(),
            selectedIndex, true, true);

        if (c != null)
        {
            Rectangle r = getCellBounds(selectedIndex, selectedIndex);
            bi = new BufferedImage(r.width + 5 * (itemsSelected - 1),
                r.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            c.setSize(r.width, r.height);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1f));
            c.paint(g);

            for (int i = 1; i < itemsSelected; i++)
            {
                g.copyArea(r.width - 5, 0, 5, r.height, 5 * i, 0);
            }
        }

        return bi;
    }

    /**
     * This is a hook to customize creation of dragging context object. Each implementation
     * or specific list can tune this to return own class of draggables. A quick example can
     * be having two lists (lorries and vegetables). We can't drag cars to vegetables, but it's
     * possible to drag vegetables to cars. So in each list it's possible to define appropriate
     * holder and analyze it on the recipient's side.
     *
     * @param selectedValues    list of selected values, which are going to be dragged.
     *
     * @return drag object for context.
     */
    protected IDNDObject createDNDObjectFromSelectedItems(Object[] selectedValues)
    {
        return new DefaultDNDObject(selectedValues);
    }

    /**
     * Processing of motion events. When in armed state we verify if it's time to start
     * dragging and in dragging state we compute insertion point and keep an eye on where
     * the pointer is -- inside or outside the control.
     *
     * @param e event.
     */
    protected void processMouseMotionEvent(MouseEvent e)
    {
        boolean consume = false;

        switch (state)
        {
            case STATE_ARMED:
                verifyDraggingGesture(e);
                consume = true;
                break;
            case STATE_DRAGGING:
                boolean dragged = e.getID() == MouseEvent.MOUSE_DRAGGED;

                if (dragged || e.getID() == MouseEvent.MOUSE_MOVED)
                {
                    onDraggingMove(e);

                    // We are required to fire this event to let the components
                    // we drag object over receive mouse dragging/moving events.
                    if (dragged) firePropertyChange(PROP_MOUSE_DRAGGED_EVENT, null, e);

                    consume = !dragged;
                }
                break;
        }

        if (!consume) super.processMouseMotionEvent(e);
    }

    /**
     * Invoked when mouse pointer has been moved while in dragging mode.
     *
     * @param e event.
     */
    private void onDraggingMove(MouseEvent e)
    {
        // Move popup to the pointer.
        popup.setLocation(e.getPoint());
        popup.setVisible(true);

        // Calculate new insertion point.
        int index = -1;
        if (contains(e.getPoint()))
        {
            index = locationToIndex(e.getPoint());
            if (index != -1)
            {
                // if we have row there then calculate the nearest insertion point
                final Rectangle r = getCellBounds(index, index);
                if (e.getPoint().y - r.y > r.height / 2) index++;
            }
        }

        // Update insertion point and repaint the list if necessary.
        setInsertionPosition(index);
    }

    /**
     * Updates the insertion point.
     *
     * @param aIndex new point index or (<code>-1</code>).
     */
    private void setInsertionPosition(int aIndex)
    {
        if (insertPosition != aIndex)
        {
            insertPosition = aIndex;
            if (isDragging()) repaint();
        }
    }

    /**
     * Verifies if it's time to start dragging. We start dragging something from a mouse button
     * press event. User can then continue to move the pointer a bit without actually having an
     * intent to drag the item -- just a slight tremble in the hands. If we detect that the mouse
     * pointer has moved far enough from press position to think of it as intentional dragging,
     * we continue with dragging initiation.
     *
     * @param e event.
     */
    private void verifyDraggingGesture(MouseEvent e)
    {
        int newX = (int)e.getPoint().getX();
        int newY = (int)e.getPoint().getY();

        if (Math.abs(newX - pressPointX) > DND_DEVIATION_GESTURE ||
            Math.abs(newY - pressPointY) > DND_DEVIATION_GESTURE)
        {
            onStartDragging(e);
        }
    }

    /**
     * Invoked by Swing to draw components.
     * Applications should not invoke <code>paint</code> directly,
     * but should instead use the <code>repaint</code> method to
     * schedule the component for redrawing.
     *
     * @param g the <code>Graphics</code> context in which to paint
     */
    public void paint(Graphics g)
    {
        super.paint(g);

        // if in DND mode and position positive then draw insertion line
        if (isDragging() && insertPosition >= 0)
        {
            Point origin;
            final int size = getModel().getSize();
            if (insertPosition >= size)
            {
                // after the last row
                final int newIndex = size - 1;
                if (newIndex != -1)
                {
                    // not an empty list
                    Point p = indexToLocation(newIndex);
                    Rectangle r = getCellBounds(newIndex, newIndex);
                    origin = new Point(0, p.y + r.height);
                } else
                {
                    // list is empty
                    origin = new Point(2, 2);
                }
            } else
            {
                // somewhere in the list
                origin = indexToLocation(insertPosition);
            }

            // draw the line
            g.drawLine(DIVIDER_X, origin.y - 1, getWidth() - DIVIDER_WIDTH_SUB, origin.y - 1);
            g.drawLine(DIVIDER_X, origin.y, getWidth() - DIVIDER_WIDTH_SUB, origin.y);
        }
    }

    /**
     * Creates tooltip.
     *
     * @return tooltip.
     */
    public JToolTip createToolTip()
    {
        JToolTip toolTip = super.createToolTip();

        URL url = ResourceUtils.getURL("resources");
        if (url != null)
        {
            toolTip.putClientProperty(BasicHTML.documentBaseKey, url);
        }

        return toolTip;
    }

    /**
     * Called back by d'n'd context when the copying status changes.
     *
     * @param copying <code>TRUE</code> when copying.
     */
    public void copyModeStateChanged(boolean copying)
    {
        popup.setCopying(copying);
    }

    /**
     * Returns <code>TRUE</code> if drag-copying is allowed in this list.
     *
     * @return <code>TRUE</code> if drag-copying is allowed in this list.
     */
    public boolean isCopyingAllowed()
    {
        return copyingAllowed;
    }
}
