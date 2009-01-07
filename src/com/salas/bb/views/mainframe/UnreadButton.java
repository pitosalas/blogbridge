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
// $Id: UnreadButton.java,v 1.19 2007/09/17 12:14:11 spyromus Exp $
//
package com.salas.bb.views.mainframe;


import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;

/**
 * UnreadButton is a combination label/button for showing the article unread count. It is drawn like
 * a flat label, but displays as a button when the mouse rolls over it. Note this derives from
 * JLabel, not JButton, and has its own implementation for button pressing. (By not using JButton,
 * we have a little more flexibility in the button appearance, and avoid some complications in
 * ensuring correct display when the overall look-and-feel is changed.)
 * 
 */
public class UnreadButton extends JLabel
{
    /** 
     * Font to draw button text in. (Beware of default font name, which
     * differs between Java 1.4 and 1.5.)
     */
    private static Font myFont = new Font("SansSerif", Font.BOLD, 9);

    /** ToolTip template for single unread item. */
    private String toolTipTemplateSingle;
    
    /** ToolTip template for multiple unread read items. */
    private String toolTipTemplateMany;

    /** Max #digits we display in unread count. */
    private static final int MAX_DISPLAY_DIGITS = 3;

    /** Button state has not been initalized. */
    private static final int BUTTON_STATE_UNSET = -1;
    /** Normal state: hasn't been moused on/over. */
    private static final int BUTTON_STATE_PLAIN = 0;
    /** Mouse over button - button raised. */
    private static final int BUTTON_STATE_RAISED = 1;
    /** Mouse pressed, mouse still tracking inside button. */
    private static final int BUTTON_STATE_PRESSED_ON = 2;
    /** Mouse pressed, mouse tracking outside button. */
    private static final int BUTTON_STATE_PRESSED_OFF = 3;
    
    /** How to draw button in normal state. */ 
    private static DrawSpecs drawSpecsPlain = new DrawSpecs(
            "unreadPlain.icon",         // button icon 
            Color.BLACK,                // text color 
            null);                      // text shadow color
    
    /** How to draw button in raised state. */ 
    private static DrawSpecs drawSpecsRaised = new DrawSpecs(
            "unreadRaised.icon",        // button icon 
            Color.WHITE,                // text color 
            new Color(253, 101, 102));  // text shadow color
    
    /** How to draw button in pressed state. */ 
    private static DrawSpecs drawSpecsPressed = new DrawSpecs(
            "unreadPressed.icon",       // button icon
            new Color(240, 179, 179),   // text color 
            new Color(219, 72, 72));    // text shadow color
    

    /** Size we want to be.*/
    private static final Dimension MY_SIZE = new Dimension(
        drawSpecsPlain.getImageIcon().getIconWidth() + 1,
        drawSpecsPlain.getImageIcon().getIconHeight() + 1);

    /** To show or not to show the menu on mouse click. */
    private static volatile boolean showMenuOnClick = true;

    /** Current button state, one of <code>BUTTON_STATE_XXX</code>.*/
    private int myButtonState = BUTTON_STATE_UNSET;

    /** Count of unread articles. */
    private int myUnreadCount;

    /** The object this button is attached to. */
    private Object attachedToObject;

    /**
     * Construct an UnreadButton.
     */
    public UnreadButton()
    {
        setFocusable(false);
        setHorizontalAlignment(SwingConstants.RIGHT);
        setVerticalAlignment(SwingConstants.TOP);
        initSize();
        setIconTextGap(0);
        setIcon(new ButtonIcon());
        setOpaque(false);

        setHorizontalTextPosition(SwingConstants.CENTER);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);

        setButtonState(BUTTON_STATE_PLAIN);
    }

    /**
     * Sets the on-click menu state.
     *
     * @param value <code>TRUE</code> to show the menu, otherwise -- to invoke action directly.
     */
    public static void setShowMenuOnClick(boolean value)
    {
        showMenuOnClick = value;
    }

    /**
     * Set our button to draw one of these states
     *      Normal: mouse not over button 
     *      Raised: mouse over button; button is shown raised 
     *      Pressed On: button pressed down
     *      Pressed Off: button pressed but mouse tracked off.
     * @param state
     *        One of the BUTTON_STATE_XXX constants
     */
    protected void setButtonState(int state)
    {
        if (state != myButtonState)
        {
            myButtonState = state;

            repaint();
        }
    }

    /** 
     * Get our draw state for button.
     * @return current button state
     */
    protected int getButtonState()
    {
        return myButtonState;
    }

    /**
     * Initialize control.
     * @param unreadCount number of unread items
     */
    public void init(int unreadCount)
    {
        update(unreadCount);
        setFont(myFont);
        setButtonState(BUTTON_STATE_PLAIN);
    }
    
    /**
     * Update the unread count for the control.  Like <code>init</code>
     * except that the existing mouse state is preserved. 
     * @param unreadCount number of unread items
     */
    public void update(int unreadCount)
    {
        myUnreadCount = unreadCount;
        repaint();

        setEnabled(myUnreadCount > 0);
        setVisible(myUnreadCount > 0);
        initToolTip();
    }
    
    /**
     * Set up the tool tip message.
     * @param templateSingle
     *        The message text for a single unread item. Must be suitable for passing to
     *        <code>MessageFormat.format</code> with one arg {0} representing the unread arg
     *        count.
     * @param templateMany
     *        The message text for multiple unread items. Must be suitable for passing to
     *        <code>MessageFormat.format</code> with one arg {0} representing the unread arg
     *        count.
     */
    public void initToolTipMessage(String templateSingle, String templateMany)
    {
        toolTipTemplateSingle = templateSingle;
        toolTipTemplateMany = templateMany;
        initToolTip();
    }

    /**
     * Set up our tooltip. Report number of unread articles, and presence of keyword hit if any.
     * 
     */
    private void initToolTip()
    {
        String tip = null;
        if (myUnreadCount > 0 && toolTipTemplateSingle != null)
        {
            Integer[] args = { myUnreadCount };
            tip = MessageFormat.format((myUnreadCount == 1)
                ? toolTipTemplateSingle : toolTipTemplateMany, args);
        }

        setToolTipText(tip);
    }

    /** 
     * Set up our size.
     */
    protected void initSize()
    {
        setSize(MY_SIZE);
        setMinimumSize(MY_SIZE);
        setMaximumSize(MY_SIZE);
        setPreferredSize(MY_SIZE);
    }


    /**
     * Adds an <code>ActionListener</code> to the button.
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l)
    {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an <code>ActionListener</code> from the button.
     * 
     * @param l the listener to be removed
     */
    public void removeActionListener(ActionListener l)
    {
        listenerList.remove(ActionListener.class, l);
    }

    /**
     * Get the list of action listeners.
     * @return array of action listeners
     */
    public ActionListener[] getActionListeners()
    {
        return listenerList.getListeners(ActionListener.class);
    }

    /**
     * Inform the action listeners that the button has been clicked.
     *
     * @param source source of the event (object this button was attached to).
     */
    protected void invokeAction(Object source)
    {
        ActionEvent action = new ActionEvent(source, 0, "markRead");
        ActionListener[] listeners = getActionListeners();
        for (ActionListener listener : listeners) listener.actionPerformed(action);
    }

    /**
     * Records the object this button is attached to.
     *
     * @param object an object.
     */
    public void setAttachedToObject(Object object)
    {
        attachedToObject = object;
    }

    /***
     * ButtonIcon provides the image for our button.
     * All state is driven from the parent class. 
     */
    class ButtonIcon implements Icon
    {
        /**
         * Our height is the JLabel's height.
         * @see javax.swing.Icon#getIconHeight()
         */
        public int getIconHeight()
        {
            return MY_SIZE.height;
        }

        /**
         * Our width is the JLabel's width.
         * @see javax.swing.Icon#getIconWidth()
         */
        public int getIconWidth()
        {
            return MY_SIZE.width;
        }

        /**
         * Paint ourself in our current state.
         * @param c component we paint in
         * @param g graphics context
         * @param x x coord to paint in
         * @param y y coord to paint in
         * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
         */
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            DrawSpecs drawSpecs;

            switch (getButtonState())
            {
                case BUTTON_STATE_PLAIN:
                    drawSpecs = drawSpecsPlain;
                    break;

                case BUTTON_STATE_RAISED:
                case BUTTON_STATE_PRESSED_OFF:
                    drawSpecs = drawSpecsRaised;
                    break;

                case BUTTON_STATE_PRESSED_ON:
                    drawSpecs = drawSpecsPressed;
                    break;

                default:
                    return;
            }

            ImageIcon imageIcon = drawSpecs.getImageIcon();
            g.setColor(Color.WHITE);
            g.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getIconWidth(),
                    imageIcon.getIconHeight(), UnreadButton.this);

            g.setFont(myFont);
            String text = Integer.toString(myUnreadCount);
            if (text.length() > MAX_DISPLAY_DIGITS) text = "...";
            FontMetrics fm = g.getFontMetrics(myFont);
            Rectangle2D r = fm.getStringBounds(text, g);

            int base = fm.getAscent();

            // center text
            int dx = (int)Math.round((MY_SIZE.width - 1 - r.getWidth()) / 2);
            int dy = (int)(base + Math.round((MY_SIZE.height - 1 - r.getHeight()) / 2));

            // tweak for proper layout
//            --dy;

            Color shadowColor = drawSpecs.getShadowColor();
            Color textColor = drawSpecs.getTextColor();
            if (shadowColor != null)
            {
                g.setColor(shadowColor);
                g.drawString(text, dx + 1, dy);
            }
            g.setColor(textColor);
            g.drawString(text, dx, dy);
        }

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
            case MouseEvent.MOUSE_PRESSED:
                // Display pushed-in button if we're enabled.
                if (isEnabled())
                {
                    setButtonState(BUTTON_STATE_PRESSED_ON);
                } else
                {
                    UifUtilities.delegateEventToParent(this, e);
                }
                break;

            case MouseEvent.MOUSE_RELEASED:
                // If mouse has been released and is still tracking over us,
                // revert button display and invoke the action.
                if (isEnabled())
                {
                    setButtonState(BUTTON_STATE_PLAIN);

                    Rectangle r = getBounds();
                    r.setLocation(0, 0);
                    if (r.contains(e.getPoint()))
                    {
                        final Object source = attachedToObject;
                        if (showMenuOnClick)
                        {
                            // Show popup menu with confirmation item
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem item = new JMenuItem(Strings.message("unreadbutton.mark.as.read"));
                            item.addActionListener(new ActionListener()
                            {
                                public void actionPerformed(ActionEvent e)
                                {
                                    invokeAction(source);
                                }
                            });
                            menu.add(item);
                            menu.show(this, e.getPoint().x, e.getPoint().y);
                        } else invokeAction(source);
                    }
                } else
                {
                    UifUtilities.delegateEventToParent(this, e);
                }
                break;

            case MouseEvent.MOUSE_CLICKED:
                UifUtilities.delegateEventToParent(this, e);
                break;

            case MouseEvent.MOUSE_ENTERED:
                // When mouse enters our bounds, display raised button. If had been pressed
                // previously and then tracked outside our bounds, revert to presed state
                // if mouse re-enters our bounds.
                if (isEnabled())
                {
                    switch (getButtonState())
                    {
                        case BUTTON_STATE_PLAIN:
                            setButtonState(BUTTON_STATE_RAISED);
                            break;
                        case BUTTON_STATE_PRESSED_OFF:
                            setButtonState(BUTTON_STATE_PRESSED_ON);
                            break;
                        default:
                            break;
                    }
                }
                break;

            case MouseEvent.MOUSE_EXITED:
                // When mouse moves out, normally revert to normal button state (i.e. button not
                // displayed). But if user has pressed mouse and tracked outside our bounds, then
                // display in raised state.
                if (isEnabled())
                {
                    // If user clicks on button and then drags out of bounds,
                    // button stays pressed.
                    switch (getButtonState())
                    {
                        case BUTTON_STATE_RAISED:
                            setButtonState(BUTTON_STATE_PLAIN);
                            break;
                        case BUTTON_STATE_PRESSED_ON:
                            setButtonState(BUTTON_STATE_PRESSED_OFF);
                            break;
                        default:
                            break;
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * Delegate handling of all motion events to the parent.
     *
     * @param e event.
     */
    protected void processMouseMotionEvent(MouseEvent e)
    {
        UifUtilities.delegateEventToParent(this, e);
    }

    /**
     * DrawSpecs describes the icon and colors used to draw the button
     * in one of its states.
     */
    static class DrawSpecs
    {
        /** Icon for drawing button in this state. */
        private final ImageIcon imageIcon;
        /** Color to draw text. */
        private final Color textColor;
        /** Color to draw text shadow, or null if none. */
        private final Color shadowColor;

        /**
         * @param iconName Name of the icon used to draw button background.
         * @param text Color of the text.
         * @param shadow Color of the text shadow.
         */
        DrawSpecs(String iconName, Color text, Color shadow)
        {
            imageIcon = ResourceUtils.getIcon(iconName);
            textColor = text;
            shadowColor = shadow;
        }

        /**
         * @return Icon for button background.
         */
        ImageIcon getImageIcon()
        {
            return imageIcon;
        }

        /**
         * @return Color to draw text.
         */
        Color getTextColor()
        {
            return textColor;
        }

        /**
         * @return Color to draw text shadow.
         */
        Color getShadowColor()
        {
            return shadowColor;
        }
    }
}
