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
// $Id: CommandCellRenderer.java,v 1.3 2006/01/08 04:53:44 kyank Exp $
//

package com.salas.bb.networking.manager;

import com.jgoodies.uif.util.ResourceUtils;

import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Renderer of command cell.
 */
public class CommandCellRenderer extends JLabel implements TableCellRenderer
{
    private static final Icon ICON_RESUME = ResourceUtils.getIcon("nm.task.resume");
    private static final Icon ICON_PAUSE = ResourceUtils.getIcon("nm.task.pause");

    /**
     * Creates renderer.
     */
    public CommandCellRenderer()
    {
        setHorizontalAlignment(SwingConstants.CENTER);
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
        int status = ((Integer)value).intValue();

        setIcon(iconForStatus(status));

        return this;
    }

    /**
     * Returns icon to be displayed for a given net task status.
     *
     * @param status status.
     *
     * @return icon or NULL for no icon.
     */
    private Icon iconForStatus(int status)
    {
        Icon icon = null;

        switch (status)
        {
            case NetTask.STATUS_PAUSED:
                icon = ICON_RESUME;
                break;
            case NetTask.STATUS_CONNECTING:
            case NetTask.STATUS_RUNNING:
                icon = ICON_PAUSE;
                break;
            default:
                break;
        }

        return icon;
    }
}
