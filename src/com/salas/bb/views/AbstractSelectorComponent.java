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
// $Id: AbstractSelectorComponent.java,v 1.2 2007/06/13 10:49:48 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.utils.uif.IconSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract selector component that knows how to track its state.
 */
public abstract class AbstractSelectorComponent extends JComponent
{
    /** None-state for the pressed field. */
    private static final int NONE = -1;

    /**
     * Prepares on-off-pressed icons for all given modes.
     *
     * @param modeNames names of modes.
     * 
     * @return icons[modes.length][3].
     */
    protected static Icon[][] prepareIcons(String[] modeNames)
    {
        Icon[][] icons = new Icon[modeNames.length][3];
        for (int mode = 0; mode < modeNames.length; mode++)
        {
            String name = modeNames[mode];
            for (State state : State.values())
            {
                icons[mode][state.ordinal()] = getIconFromResources(name, state);
            }
        }

        return icons;
    }

    /**
     * Returns icon from the resources.
     *
     * @param modeName mode name.
     * @param state state.
     *
     * @return icon.
     */
    protected static Icon getIconFromResources(String modeName, State state)
    {
        return getIcon(modeName + "." + STATES[state.ordinal()]);
    }

    /** Button / icon state. */
    protected enum State { ON, OFF, PRESSED }
    /** The names of the states. */
    protected static final String[] STATES = { "on", "off", "pressed" };

    /** What's pressed at the moment. */
    protected int pressed = NONE;
    /** <code>TRUE</code> when the mouse is over the component. */
    protected boolean mouseOver;
    /** A flag to avoid repainting when induced by self. */
    protected boolean selfEvent;

    /** The model to update. */
    protected final ValueModel model;

    private static final Icon[] SEPARATOR = new Icon[]
    {
        ViewTypeSelector.getIcon("separator.on"),
        ViewTypeSelector.getIcon("separator.off"),
        ViewTypeSelector.getIcon("separator.pressed")
    };

    /**
     * Creates a selector component for a given model.
     *
     * @param valueModel model.
     */
    public AbstractSelectorComponent(ValueModel valueModel)
    {
        selfEvent = false;
        model = valueModel;
        model.addValueChangeListener(new ModelChangeListener());

        Dimension size = getPreferredDimensions();

        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    /**
     * Returns the dimensions for the icon in the given state.
     *
     * @param mode  mode.
     * @param state state.
     *
     * @return dimensions or <code>NULL</code> if the icon isn't found or invalid.
     */
    protected Dimension getIconDimension(int mode, State state)
    {
        Icon icon = getIcon(mode, state);
        return getIconDimensions(icon);
    }

    /**
     * Returns the dimensions of the icon.
     *
     * @param aIcon icon.
     *
     * @return dimensions or <code>NULL</code> if icon is not specified.
     */
    protected static Dimension getIconDimensions(Icon aIcon)
    {
        return aIcon == null ? null : new Dimension(aIcon.getIconWidth(), aIcon.getIconHeight());
    }

    /**
     * Returns icon for a given mode and state.
     *
     * @param mode  mode.
     * @param state state.
     *
     * @return icon.
     */
    protected abstract Icon getIcon(int mode, State state);


    /**
     * Returns separator icon in the given state.
     *
     * @param state state.
     *
     * @return icon.
     */
    protected static Icon getSeparatorIcon(State state)
    {
        return SEPARATOR[state.ordinal()];
    }

    /**
     * Invoked when painting of the component is necessary.
     *
     * @param g graphics context.
     */
    protected abstract void paintComponent(Graphics g);

    /**
     * Returns the state of the mode button.
     *
     * @param md    mode.
     *
     * @return state.
     */
    protected State getCurrentState(int md)
    {
        return pressed == md && mouseOver ? State.PRESSED : getMode() == md ? State.ON : State.OFF;
    }

    /**
     * Returns the state of a separator component between two buttons with
     * the given states.
     *
     * @param firstState    first state.
     * @param secondState   second state.
     *
     * @return separator state.
     */
    protected static State getSeparatorState(State firstState, State secondState)
    {
        return (firstState == State.ON || secondState == State.ON)
            ? State.ON : (firstState == State.PRESSED || secondState == State.PRESSED)
            ? State.PRESSED : State.OFF;
    }

    /**
     * Processes mouse events occurring on this component by dispatching them to any registered
     * <code>MouseListener</code> objects.
     *
     * @param e the mouse event
     */
    protected void processMouseEvent(MouseEvent e)
    {
        int id = e.getID();
        int md;

        switch (id)
        {
            case MouseEvent.MOUSE_PRESSED:
                md = locationToMode(e.getPoint());
                if (md != NONE)
                {
                    mouseOver = true;
                    pressed = md;
                    repaint();
                }
                break;

            case MouseEvent.MOUSE_RELEASED:
                if (pressed != NONE)
                {
                    md = locationToMode(e.getPoint());
                    if (pressed == md && mouseOver)
                    {
                        selfEvent = true;
                        try
                        {
                            setMode(md);
                        } finally
                        {
                            selfEvent = false;
                        }
                    }
                    pressed = NONE;
                    repaint();
                }
                break;

            case MouseEvent.MOUSE_ENTERED:
                mouseOver = true;
                repaint();
                break;

            case MouseEvent.MOUSE_EXITED:
                mouseOver = false;
                repaint();
                break;
        }
    }

    /**
     * Processes mouse motion events, such as MouseEvent.MOUSE_DRAGGED.
     *
     * @param e the <code>MouseEvent</code>
     *
     * @see java.awt.event.MouseEvent
     */
    protected void processMouseMotionEvent(MouseEvent e)
    {
        int id = e.getID();
        if (!contains(e.getPoint())) return;

        if (id == MouseEvent.MOUSE_DRAGGED)
        {
            int md = locationToMode(e.getPoint());
            if (pressed != md)
            {
                if (mouseOver)
                {
                    mouseOver = false;
                    repaint();
                }
            } else if (!mouseOver)
            {
                mouseOver = true;
                repaint();
            }
        }
    }

    /**
     * Converts a point (mouse pointer) within the component coordinates
     * into the mode (button).
     *
     * @param aPoint    point.
     *
     * @return button / mode.
     */
    protected abstract int locationToMode(Point aPoint);

    /**
     * Returns current mode.
     *
     * @return mode.
     */
    private int getMode()
    {
        return (Integer)model.getValue();
    }

    /**
     * Sets different mode.
     *
     * @param mode mode.
     */
    protected void setMode(int mode)
    {
        model.setValue(mode);
    }

    /**
     * Reports a desired dimensions for this component.
     * Used during the construction (once).
     *
     * @return dimensions.
     */
    protected abstract Dimension getPreferredDimensions();

    /**
     * Returns icon by its resource key. It automatically appends ".mac" to the
     * key on Mac platform.
     *
     * @param key key of the icon resource.
     *
     * @return icon.
     */
    protected static Icon getIcon(String key)
    {
        if (SystemUtils.IS_OS_MAC) key += ".mac";
        return IconSource.getIcon(key);
    }

    /**
     * Listens for mode changes.
     */
    protected class ModelChangeListener implements PropertyChangeListener
    {
        /**
         * Invoked when mode changes.
         *
         * @param evt event object.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (!selfEvent) repaint();
        }
    }
}
