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
// $Id: StarsSelectionComponent.java,v 1.17 2006/05/31 12:49:31 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import com.salas.bb.core.FeedFormatter;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Small panel for selection of number of stars from 1-5.
 * It binds itself to the specified propoerty.
 */
public class StarsSelectionComponent extends JPanel
    implements ChangeListener, MouseListener, MouseMotionListener
{
    private static final String UI_CLASS_ID = "ComboPanelUI";

    private static final int BORDER_THICKNESS = 2;
    private JLabel              lbIcon;
    private BoundedRangeModel   model;

    static
    {
        UIManager.getDefaults().put(UI_CLASS_ID, ComboPanelUI.class.getName());
    }

    /**
     * Creates new component.
     *
     * @param aModel model of control.
     */
    public StarsSelectionComponent(BoundedRangeModel aModel)
    {
        initGUI();
        setModel(aModel);
        lbIcon.addMouseListener(this);
        lbIcon.addMouseMotionListener(this);
    }

    /**
     * Adds mouse listener to the component.
     *
     * @param l listener.
     */
    public synchronized void addMouseListener(MouseListener l)
    {
        lbIcon.addMouseListener(l);
    }

    /**
     * Removes mouse listener to the component.
     *
     * @param l listener.
     */
    public synchronized void removeMouseListener(MouseListener l)
    {
        lbIcon.removeMouseListener(l);
    }

    /**
     * Returns a string that specifies the name of the L&F class that renders this component.
     *
     * @return "PanelUI"
     *
     * @see javax.swing.JComponent#getUIClassID
     * @see javax.swing.UIDefaults#getUI
     */
    public String getUIClassID()
    {
        return UI_CLASS_ID;
    }

    private void initGUI()
    {
        lbIcon = new JLabel();
        
        // Setting ToolTipText on the overall JPanel doesn't work.
        // It has something to do with the custom mouse handling. Not sure what.
        lbIcon.setToolTipText(Strings.message("starzcomponent.tooltip"));
        
        lbIcon.setBorder(BorderFactory.createEmptyBorder(
            BORDER_THICKNESS - 2, BORDER_THICKNESS, BORDER_THICKNESS - 1, BORDER_THICKNESS));

        // We put stars selector onto panel to get background effect.
        // width: 5 stas (12px each) + 4 spaces between them (1px each) + border (3px per side)
        // height: star (12px) + border (3px per side)
        final Dimension dim = new Dimension(64 + BORDER_THICKNESS * 2 + 4,
            12 + BORDER_THICKNESS * 2 + 1);
        setLayout(new BorderLayout());
        add(lbIcon, BorderLayout.CENTER);
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
    }

    /**
     * Sets new model for the control.
     *
     * @param aModel model.
     */
    public void setModel(BoundedRangeModel aModel)
    {
        if (model != null) model.removeChangeListener(this);
        model = aModel;
        if (model != null)
        {
            model.addChangeListener(this);
            setStars(model.getValue());
        }
    }

    private void setStars(int value)
    {
        lbIcon.setIcon(FeedFormatter.getStarzIcon(value - 1, !isEnabled()));
    }

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e)
    {
        setStars(model.getValue());
    }

    /**
     * Sets whether or not this component is enabled.
     *
     * @param enabled enables on true the component & disables on false
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        setStars(model.getValue());
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component.
     *
     * @param e mouse event.
     */
    public void mouseClicked(MouseEvent e)
    {
        if (!isEnabled()) return;

        Point p = e.getPoint();
        int star = ((int)p.getX() / 13) + 1;
        model.setValue(star);
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e mouse event.
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e mouse event.
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e mouse event.
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e mouse event.
     */
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     *
     * @param e mouse event.
     */
    public void mouseDragged(MouseEvent e)
    {
        mouseClicked(e);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no buttons have been
     * pushed.
     *
     * @param e mouse event.
     */
    public void mouseMoved(MouseEvent e)
    {
    }

    /**
     * Custom UI.
     */
    public static class ComboPanelUI extends BasicPanelUI
    {
        // Shared UI object
        private static PanelUI panelUI;

        /**
         * Creates UI for a given component.
         *
         * @param c component.
         *
         * @return UI.
         */
        public static ComponentUI createUI(JComponent c)
        {
            if (panelUI == null)
            {
                panelUI = new ComboPanelUI();
            }
            return panelUI;
        }

        /**
         * Installs UI to a given component.
         *
         * @param c component.
         */
        public void installUI(JComponent c)
        {
            LookAndFeel.installColorsAndFont(c,
                         "ComboBox.background",
                         "ComboBox.foreground",
                         "ComboBox.font");
            LookAndFeel.installBorder(c, "ComboBox.border");
        }
    }
}
