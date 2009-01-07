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
// $Id: BBToolBarButton.java,v 1.19 2007/09/18 12:23:08 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.util.Mode;
import com.jgoodies.uif.util.Modes;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * A <code>JButton</code> subclass for use in tool bars.
 * Adds a special configuration to its superclass; useful in toolbars.
 *
 * @author Karsten Lentzsch
 */
public final class BBToolBarButton extends JButton
{
    /**
     * Reused for tool bar buttons.
     */
    private static final Insets EMPTY_INSETS = new InsetsUIResource(0, 0, 0, 0);

    /**
     * Holds the mode that is used to set the <em>borderPainted</em>
     * property if the UI changes.
     *
     * @see #getBorderPaintedMode()
     * @see #setBorderPaintedMode(Mode)
     */
    private Mode borderPaintedMode = Modes.LAF_NON_AQUA;

    /**
     * Holds the mode that is used to determine the margin size
     * if the button is configured.
     *
     * @see #getWideMarginMode()
     * @see #setWideMarginMode(Mode)
     */
//    private Mode wideMarginMode = Modes.LAF_AQUA;
    private Mode wideMarginMode = Modes.NEVER;


    // Instance Creation ******************************************************

    /**
     * Constructs tool bar button and sets properties as described
     * by the given action.
     *
     * @param action provides the properties for this button
     */
    public BBToolBarButton(Action action)
    {
        super(action);
        configureButton();
    }


    // Accessing Properties ***************************************************

    /**
     * Returns this button's border painted mode that can be used to
     * change the <em>borderPainted</em> property if the UI changes.
     *
     * @return the current border painted mode
     */
    public Mode getBorderPaintedMode()
    {
        return borderPaintedMode;
    }


    /**
     * Sets a new border painted mode that can be used to change the
     * <em>borderPainted</em> property if the UI changes.
     *
     * @param newMode the mode to be set
     *
     * @throws NullPointerException if the new mode is <code>null</code>
     */
    public void setBorderPaintedMode(Mode newMode)
    {
        if (newMode == null)
            throw new NullPointerException(Strings.error("unspecified.ui.border.painted.mode"));
        borderPaintedMode = newMode;
        configureButton();
    }


    /**
     * Returns this button's wide margin mode that can be used
     * to change the button's margin during the configuration.
     *
     * @return the current wide margin mode
     */
    public Mode getWideMarginMode()
    {
        return wideMarginMode;
    }


    /**
     * Sets a new border painted mode that can be used to change
     * the <em>borderPainted</em> property if the UI changes.
     *
     * @param newMode the mode to be set
     *
     * @throws NullPointerException if the new mode is <code>null</code>
     */
    public void setWideMarginMode(Mode newMode)
    {
        if (newMode == null)
            throw new NullPointerException(Strings.error("unspecified.ui.wide.margin.mode"));
        wideMarginMode = newMode;
        configureButton();
    }


    // Updating the UI ******************************************************
    /**
     * In addition to the superclass behavior that updates the UI
     * this method configures several button properties. For details see
     * {@link BBToolBarButton#configureBBToolbarButton(AbstractButton, boolean, boolean)}.
     * This configuration honors the border painted mode
     * and the wide margin mode.<p>
     *
     * This method is invoked during the superclass construction
     * and therefore we check for an uninitialized field.
     */
    public void updateUI()
    {
        super.updateUI();
        if (getBorderPaintedMode() != null)
        {
            configureButton();
        }
    }


    /**
     * Configures an <code>AbstractButton</code> for being used
     * in a tool bar.
     *
     * @param button        - Button being configured
     * @param borderPainted - true if we want border painted
     * @param wideMargin    - true if we want a wide margin
     */
    public static void configureBBToolbarButton(AbstractButton button,
                                       boolean borderPainted,
                                       boolean wideMargin)
    {
        button.setHorizontalTextPosition(CENTER);
        button.setVerticalTextPosition(BOTTOM);
        button.setAlignmentY(CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(borderPainted);
        if (button.getMargin() instanceof UIResource)
        {
            button.setMargin(getButtonMargin(button, wideMargin));
        }
        button.setMnemonic(0);
        button.setFocusable(false);
        if (SystemUtils.IS_OS_MAC)
        { 
            button.setIconTextGap(0);
        }
    }

    /**
     * Returns the button's margin.
     *
     * @param button     - Button being configured
     * @param wideMargin - true to indicate wide margin desired.
     * @return - indicated insets
     */
    private static Insets getButtonMargin(AbstractButton button, boolean wideMargin)
    {
        Dimension defaultIconSize =
                UIManager.getDimension("jgoodies.defaultIconSize");
        Icon icon = button.getIcon();
        if (defaultIconSize == null || icon == null)
            return EMPTY_INSETS;

        int hpad = Math.max(0, defaultIconSize.width - icon.getIconWidth());
        int vpad = Math.max(0, defaultIconSize.height - icon.getIconHeight());
        int top = vpad / 2;
        int left = hpad / 2;
        int bottom = top + vpad % 2;
        int right = left + hpad % 2;
        if (wideMargin)
        {
            top += 2;
            left += 2;
            bottom += 2;
            right += 2;
        }
        return new InsetsUIResource(top, left, bottom, right);
    }

    /*
     * Configures this button.
     */
    private void configureButton()
    {
        configureBBToolbarButton(this,
            getBorderPaintedMode().enabled(),
            getWideMarginMode().enabled());
        setDefaultCapable(false);
    }

    /**
     * Configures the button's properties from the given Action. Note that these are (also)
     * <code>ToolbarCommandAction</code>, which can return the special pressed icon for toolbar
     * buttons.
     *
     * @param a - Action to be used. (Class Action, implements ToolbarCommandAction)
     */
    protected void configurePropertiesFromAction(Action a)
    {
        super.configurePropertiesFromAction(a);
        Icon icon = getIcon();
        Icon grayIcon = (Icon)a.getValue(ActionManager.SMALL_GRAY_ICON);
        if (grayIcon != null)
        {
            setRolloverIcon(icon);
            setIcon(grayIcon);
        }
        if (icon != null)
        {
            putClientProperty("hideActionText", Boolean.TRUE);
        }

        if (a instanceof IToolbarCommandAction)
        {
            Icon pressedIcon = ((IToolbarCommandAction) a).getPressedIcon();
            if (pressedIcon != null) setPressedIcon(pressedIcon);
        }
    }
}
