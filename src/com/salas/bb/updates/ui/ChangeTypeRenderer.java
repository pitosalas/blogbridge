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
// $Id: ChangeTypeRenderer.java,v 1.3 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.updates.ui;

import com.salas.bb.utils.uif.IconSource;

import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Renders the change type column as icons.
 */
public class ChangeTypeRenderer implements TableCellRenderer
{
    // Order depends on values of TYPE_XYZ constants of VersionChange class
    private static final String[] KEYS = { "change.feature.icon", "change.fix.icon" };

    private JPanel panel;
    private JLabel iconLabel;

    /**
     * Creates renderer.
     */
    public ChangeTypeRenderer()
    {
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
        layout.setVgap(0);

        panel = new JPanel(layout);
        panel.setBorder(null);

        iconLabel = new JLabel("");
        iconLabel.setBorder(null);
        panel.add(iconLabel);
    }

    /**
     * Returns the component used for drawing the cell.  This method is used to configure the
     * renderer appropriately before drawing.
     *
     * @param table         the <code>JTable</code> that is asking the renderer to draw; can be
     *                      <code>null</code>
     * @param value         the value of the cell to be rendered.  It is up to the specific renderer
     *                      to interpret and draw the value.  For example, if <code>value</code>
     *                      is the string "true", it could be rendered as a string or it could be
     *                      rendered as a check box that is checked. <code>null</code> is a valid.
     * @param isSelected    true if the cell is to be rendered with the selection highlighted;
     *                      otherwise false.
     * @param hasFocus      if true, render cell appropriately.  For example, put a special border
     *                      on the cell, if the cell can be edited, render in the color used to
     *                      indicate editing.
     * @param row           the row index of the cell being drawn.  When drawing the header, the
     *                      value of <code>row</code> is -1
     * @param column        the column index of the cell being drawn
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column)
    {
        String iconKey = KEYS[((Integer)value).intValue()];
        Icon icon = IconSource.getIcon(iconKey);
        iconLabel.setIcon(icon);

        panel.setBackground(table.getBackground());

        return panel;
    }
}
