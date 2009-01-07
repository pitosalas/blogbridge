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
// $Id: CustomPopupButton.java,v 1.5 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uif.component.ToolBarButton;
import com.jgoodies.uif.util.ComponentTreeUtils;
import com.jgoodies.uif.util.CompoundIcon;
import com.jgoodies.uif.util.NullIcon;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A combination of an action button and a popup menu.<p>
 *
 * It is intended to be used in cases where you have a list of
 * frequently used items, which you want to choose from a menu.
 * To quickly choose the most recently used element, you click
 * the action button. This is popular in recent Web browsers.
 *
 * The original code is by Karsten Lentzsch. The modified version
 * isn't listening to the enableness state and shows lable instead of
 * arrow icon if it's set.
 */
public class CustomPopupButton extends JPanel
{
    private final JButton       mainButton;
    private final JPopupMenu    popupMenu;
    private final String        arrowCaption;

    private AbstractButton      arrowButton;
    private boolean             mouseIsOver;

    /**
     * Constructs a popup button using the given main button and popup menu.
     *
     * @param mainButton   main button.
     * @param arrowCaption caption of arrow-button.
     * @param popupMenu    menu to pop-up.
     */
    public CustomPopupButton(JButton mainButton, String arrowCaption, JPopupMenu popupMenu)
    {
        this.mainButton = mainButton;
        this.popupMenu = popupMenu;
        this.arrowCaption = arrowCaption;
        mouseIsOver = false;

        build();
    }

    /**
     * Adds this popup button's two components to the given tool bar individually. Useful because
     * the tool bar may handle buttons as special components and would not work well with a panel.
     * This is the case with the Windows toolbar in XP style.<p>
     * <p/>
     * Tries to adjust the arrow button with the main button's height in case the main button is
     * larger due to larger insets.
     *
     * @param toolBar the tool bar to add the components to
     */
    public void addTo(JToolBar toolBar)
    {
        toolBar.add(mainButton);
        toolBar.add(arrowButton);
        adjustButtonHeights();
    }

    /**
     * Builds the popup button component.
     */
    protected void build()
    {
        arrowButton = createArrowButton();

        mainButton.getModel().addChangeListener(new MainButtonChangeListener());
        mainButton.addMouseListener(new MainButtonMouseListener());
        popupMenu.addPopupMenuListener(new RolloverPopupMenuListener());

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridwidth = 1;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 0.0;
        add(mainButton, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(arrowButton, gbc);

        setOpaque(false);
    }

    /**
     * Creates and answers the arrow button.
     */
    private AbstractButton createArrowButton()
    {
        Icon mainButtonIcon = mainButton.getIcon();
        int iconHeight = mainButtonIcon != null ? mainButtonIcon.getIconHeight() : 16;

        AbstractButton button;
        int horizontalInsets;
        if (arrowCaption == null)
        {
            Icon arrowIcon = new ArrowIcon();
            Icon compoundIcon = new CompoundIcon(
                new NullIcon(new Dimension(arrowIcon.getIconWidth(), iconHeight)),
                    arrowIcon, CompoundIcon.CENTER);
            button = new ToolBarButton(compoundIcon);
            horizontalInsets = 0;
        } else
        {
            button = new JButton(arrowCaption);
            horizontalInsets = 5;
        }

        button.setModel(new DelegatingButtonModel(mainButton.getModel()));
        button.addActionListener(new ArrowButtonActionListener());
        button.addMouseListener(new ArrowButtonMouseListener());

        Insets insets = button.getMargin();
        button.setMargin(new Insets(insets.top, horizontalInsets, insets.bottom, horizontalInsets));

        return button;
    }

    /** Shows the menu and sets the button to armed. */
    private class ArrowButtonActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            popupMenu.show(mainButton, 0, mainButton.getHeight());
            arrowButton.getModel().setArmed(true);
        }
    }

    /** Switches the rollover property on and off. */
    private class ArrowButtonMouseListener extends MouseAdapter
    {
        public void mouseEntered(MouseEvent e)
        {
            mouseIsOver = true;
            mainButton.getModel().setRollover(true);
        }

        public void mouseExited(MouseEvent e)
        {
            mouseIsOver = false;
            mainButton.getModel().setRollover(popupMenu.isVisible());
        }
    }

    /** Listener of main button changes. */
    private class MainButtonChangeListener implements ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            arrowButton.repaint();
        }
    }

    /** Listener of mouse motion. */
    private class MainButtonMouseListener extends MouseAdapter
    {
        public void mouseEntered(MouseEvent e)
        {
            mouseIsOver = true;
            arrowButton.getModel().setRollover(true);
        }

        public void mouseExited(MouseEvent e)
        {
            mouseIsOver = false;
            arrowButton.getModel().setRollover(popupMenu.isVisible());
        }
    }

    /** Listener for mouse rollovers. */
    private class RolloverPopupMenuListener implements PopupMenuListener
    {
        public void popupMenuCanceled(PopupMenuEvent e)
        {
            // Implements PopupMenuListener; do nothing
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
        {
            arrowButton.getModel().setRollover(mouseIsOver);
            arrowButton.getModel().setPressed(false);
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e)
        {
            arrowButton.getModel().setRollover(true);
            arrowButton.getModel().setPressed(true);
        }
    }

    /**
     * Overrides <code>Container.getAlignmentX</code> to return the vertical alignment.
     *
     * @return the value of the <code>alignmentX</code> property
     *
     * @see #setAlignmentX
     * @see java.awt.Component#getAlignmentX
     */
    public float getAlignmentX()
    {
        return mainButton.getAlignmentX();
    }

    /**
     * If the maximum size has been set to a non-<code>null</code> value just returns it.  If the UI
     * delegate's <code>getMaximumSize</code> method returns a non-<code>null</code> value then
     * return that; otherwise defer to the component's layout manager.
     *
     * @return the value of the <code>maximumSize</code> property
     *
     * @see #setMaximumSize
     * @see javax.swing.plaf.ComponentUI
     */
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    /**
     * In addition to the superclass behavior, we update the popup menu that is not in the component
     * tree. And we need readjust the main button and arrow button heights.
     */
    public void updateUI()
    {
        super.updateUI();
        if (null == popupMenu)
            return;

        ComponentTreeUtils.updateComponentTreeUI(popupMenu);
        adjustButtonHeights();
    }

    private void adjustButtonHeights()
    {
        Dimension d = mainButton.getMinimumSize();
        d.width = arrowButton.getMinimumSize().width;
        arrowButton.setMaximumSize(d);
    }


    // Helper Classes *******************************************************

    /** An icon implementation for the arrow button. */
    private static class ArrowIcon implements Icon
    {
        private static final int ICON_HEIGHT = 4;
        private static final int ICON_WIDTH = 2 * ICON_HEIGHT + 1;

        public int getIconWidth()
        {
            return ICON_WIDTH;
        }

        public int getIconHeight()
        {
            return ICON_HEIGHT;
        }

        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            AbstractButton b = (AbstractButton)c;
            ButtonModel m = b.getModel();
            int w = getIconWidth() - 2;
            int h = ICON_HEIGHT;

            g.translate(x, y);

            g.setColor(UIManager.getColor(m.isEnabled() ? "controlText" : "textInactiveText"));
            for (int i = 0; i < h; i++)
                g.drawLine(i + 1, i, w - i, i);

            g.translate(-x, -y);
        }
    }

    /** A button model that delegates everything to a delegate. */
    private static final class DelegatingButtonModel extends DefaultButtonModel
    {
        private final ButtonModel delegate;

        private DelegatingButtonModel(ButtonModel delegate)
        {
            this.delegate = delegate;
        }

        public boolean isRollover()
        {
            return delegate.isRollover();
        }

        public boolean isArmed()
        {
            return super.isArmed() || delegate.isArmed();
        }

        public boolean isPressed()
        {
            return super.isPressed() || delegate.isPressed();
        }

        public void setRollover(boolean b)
        {
            delegate.setRollover(b);
        }
    }

}