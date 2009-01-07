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
// $Id: ColorListCellRenderer.java,v 1.2 2008/02/29 07:17:19 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.SystemUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer of color samples.
 */
public class ColorListCellRenderer extends JPanel implements ListCellRenderer
{
    private final String noColorText;
    private JPanel  colorSample;
    private JLabel  label;

    /**
     * Creates a renderer component.
     *
     * @param noColorText text to show when no color is given.
     */
    public ColorListCellRenderer(String noColorText)
    {
        this.noColorText = noColorText;
        CellConstraints cc = new CellConstraints();
        CellConstraints cc2 = new CellConstraints();

        colorSample = new JPanel(new FormLayout("1dlu, center:p:grow, 1dlu", "0dlu, p:grow, 0dlu"));
        label = new JLabel();
        colorSample.add(label, cc.xy(2, 2));

        if (SystemUtils.IS_OS_MAC)
        {
            setLayout(new FormLayout("0, 10dlu:grow, 2dlu", "2dlu:grow, pref, 1dlu:grow"));
        } else
        {
            setLayout(new FormLayout("0, pref:grow, 0", "0, pref:grow, 1dlu"));
        }

        add(colorSample, cc2.xy(2, 2, "f, f"));
    }

    /**
     * Returns the component for rendering color sample.
     *
     * @param list          list requesting the component.
     * @param value         color value or NULL.
     * @param index         index of item in list.
     * @param isSelected    TRUE if item is selected.
     * @param cellHasFocus  TRUE if item has focus.
     *
     * @return component.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus)
    {
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

        if (value == null)
        {
            label.setText(noColorText);
            colorSample.setOpaque(false);
        } else
        {
            label.setText(" ");
            colorSample.setOpaque(true);
            colorSample.setBackground((Color)value);
        }

        return this;
    }
}
