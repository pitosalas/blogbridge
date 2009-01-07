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
// $Id: ProgressCellRenderer.java,v 1.2 2006/01/08 04:53:44 kyank Exp $
//

package com.salas.bb.networking.manager;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renders progress bar for the list of tasks.
 */
public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer
{
    /**
     * Creates renderer.
     */
    public ProgressCellRenderer()
    {
        setMinimum(0);
        setMaximum(99);
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        setBorderPainted(false);
        setStringPainted(true);
    }

    /**
     * Returns component for rendering cell.
     *
     * @param table         destination table.
     * @param value         value in the cell.
     * @param isSelected    TRUE if cell is selected.
     * @param hasFocus      TRUE if cell has focus.
     * @param row           row index.
     * @param column        column index.
     *
     * @return component.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column)
    {
        double progress = ((Double)value).doubleValue();

        setValue((int)progress);
        setBackground(table.getBackground());

        return this;
    }
}
