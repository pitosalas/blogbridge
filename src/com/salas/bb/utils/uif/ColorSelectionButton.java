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
// $Id: ColorSelectionButton.java,v 1.3 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Button for color selection.
 */
public class ColorSelectionButton extends JButton implements ActionListener
{
    private String  dialogTitle;

    /**
     * Creates button.
     *
     * @param aDialogTitle  label of color selection dialog.
     * @param label         button label.
     * @param initColor     initial color.
     */
    public ColorSelectionButton(String aDialogTitle, String label, Color initColor)
    {
        super(label);
        setColor(initColor);
        dialogTitle = aDialogTitle;

        addActionListener(this);
    }

    /**
     * Invoked when someone presses button.
     *
     * @param e event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        Color newColor = JColorChooser.showDialog(JOptionPane.getRootFrame(),
            dialogTitle, this.getBackground());

        // Change color of button background if it was changed.
        if (newColor != null)
        {
            setBackground(newColor);
        }
    }

    /**
     * Sets currently selected color.
     *
     * @param color color.
     */
    public void setColor(Color color)
    {
        setBackground(color);
    }

    /**
     * Returns currently selected color.
     *
     * @return color.
     */
    public Color getColor()
    {
        return getBackground();
    }
}
