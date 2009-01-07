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
// $Id: BBFormBuilder.java,v 1.12 2008/02/28 07:47:52 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Enhanced builder of forms. This class helps build JGoodies forms incrementally. You need
 * to understand JGoodies Forms to use this class. Note that this is not a general users guide for
 * this class just practical notes. If you want to add more information as you learn to use this, feel free!
 * 
 * Some key ideas to help you get going:
 * 
 * When constructed, a a JGoodies "columns definition" string is supplied which establishes
 * the number of columns, and the constraints on the widths of the columns
 * 
 * From that point, a series of "append" methods are called, each supplying one or more components,
 * and indicating whether the components should span more than one column.
 * 
 * When you use this class you need to keep track of where you are, what column and what row, because
 * by default this 'cursor' will wrap from the end of one row to the beginning of the next one, automatically.
 * 
 * If you want to override this, you can for example call one of the nextColumn() and/or nextLine() related methods.
 * 
 * What does nextline() do? nextLine() moves the cursor down several lines. It doesn't
 * create rows.
 * 
 * If you have just filled some row the cursor at the end of this row and
 * there are no rows below it.
 * 
 * If you continue with append(someComponent) it will see that cursor points to the column after the 
 * last defined. Consequentially, it will move cursor one line down and to the very left. If there's 
 * a row present, it will use this row to add component.
 * If there's no row it will:
 *   a. create gap row
 *   b. move cursor one more line down
 *   c. create content row (with preferred height)
 *   d. add your component

 * Now what happens when you do nextline() after filling the row. You tell
 * that you wish to move cursor one line down. It is actually what the
 * engine does implicitly and the only case when you need to use it
 * explicitly is when you don't wish to fill the row to the end. You have
 * more columns to the right, but you wish to break to the next line, so
 * you tell nextLine().

 * What happens when you tell nextLine(2). You jump two rows down. In other
 * words, you jump over one row. This row doesn't exist and engine fails to
 * create the second row after that missing... that's an exception.

 * Having all this in mind, if you want to create a big gap, dont do nextLine(2). Instead:
 *
 * appendUnrelatedComponentsRow() -- it's a big fixed-height gap.
 * go to the next line
 * and go to the next line again, or you will end up with components
 * placed on this gap row.

 * The shorter recording of this (thanks to my builder) is appendUnrelatedComponentsRow(2).
 */
public class BBFormBuilder
{
    private DefaultFormBuilder builder;

    /**
     * Constructs an instance of builder for given columns layout. Users and instance of
     * <code>JPanel</code> as layout container.
     *
     * @param columnsDefinition columns layout.
     */
    public BBFormBuilder(String columnsDefinition)
    {
        this(new FormLayout(columnsDefinition));
    }

    /**
     * Constructs an instance of builder for the given layout. Uses an instance of
     * <code>JPanel</code> as layout container.
     *
     * @param layout the form layout used to layout the container
     */
    public BBFormBuilder(FormLayout layout)
    {
        this(layout, null, new JPanel(null));
    }

    /**
     * Constructs an instance of builder for the given layout. Uses an instance of
     * <code>JPanel</code> as layout container.
     *
     * @param layout the form layout used to layout the container
     * @param bundle the resource bundle used to lookup i15d strings
     */
    public BBFormBuilder(FormLayout layout, ResourceBundle bundle)
    {
        this(layout, bundle, new JPanel(null));
    }

    /**
     * Constructs an instance of builder for the given layout. Uses an instance of
     * <code>JPanel</code> as layout container.
     *
     * @param columnsDefinition columns layout.
     * @param panel             layout container.
     */
    public BBFormBuilder(String columnsDefinition, JPanel panel)
    {
        this(new FormLayout(columnsDefinition), panel);
    }

    /**
     * Constructs an instance of builder for the given FormLayout and layout container.
     *
     * @param layout the <code>FormLayout</code> used to layout the container
     * @param panel  the layout container
     */
    public BBFormBuilder(FormLayout layout, JPanel panel)
    {
        this(layout, null, panel);
    }

    /**
     * Sets the offset of the leading column, often 0 or 1.
     *
     * @param columnOffset the new offset of the leading column
     */
    public void setLeadingColumnOffset(int columnOffset)
    {
        builder.setLeadingColumnOffset(columnOffset);
    }

    /**
     * Constructs an instance of builder for the given FormLayout and layout container.
     *
     * @param layout the <code>FormLayout</code> used to layout the container
     * @param bundle the <code>ResourceBundle</code> used to lookup i15d strings
     * @param panel  the layout container
     */
    public BBFormBuilder(FormLayout layout, ResourceBundle bundle, JPanel panel)
    {
        builder = new DefaultFormBuilder(layout, bundle, panel);
        builder.setLineGapSize(Sizes.DLUY2);
        builder.setParagraphGapSize(Sizes.DLUY7);
    }

    /**
     * Adds a string label.
     *
     * @param str string.
     */
    public void append(String str)
    {
        append(new JLabel(str));
    }

    /**
     * Adds a component to the panel using the default constraints with a column span of 1. Then
     * proceeds to the next data column.
     *
     * @param component the component to add
     */
    public void append(Component component)
    {
        append(component, 1);
    }

    /**
     * Adds a component to the panel using the default constraints with the given columnSpan.
     * Proceeds to the next data column.
     *
     * @param component  the component to append
     * @param columnSpan the column span used to add
     */
    public void append(Component component, int columnSpan)
    {
        builder.append(component, columnSpan);
    }

    /**
     * Appends two components one after another.
     *
     * @param c1    first component.
     * @param c2    second component.
     */
    public void append(Component c1, Component c2)
    {
        append(c1);
        append(c2);
    }

    /**
     * Appends labeled component.
     *
     * @param textWithMnemonic  text with mnemonic mark.
     * @param c                 component.
     */
    public void append(String textWithMnemonic, Component c)
    {
        append(textWithMnemonic, 1, c, 1, null, 1);
    }

    /**
     * Adds a text label to the panel and proceeds to the next column.
     *
     * @param textWithMnemonic the label's text - may mark a mnemonic.
     * @param columnSpan       number of columns to span this label onto.
     *
     * @return the added label.
     */
    public JLabel append(String textWithMnemonic, int columnSpan)
    {
        JLabel label = ComponentsFactory.createLabel(textWithMnemonic);

        append(label, columnSpan);

        return label;
    }

    /**
     * Appends a text label with two components. The label is
     * linked with the first component.
     *
     * @param textWithMnemonic  text for label with optional mnemonic.
     * @param c1                first component (label is linked to).
     * @param c2                second component.
     */
    public void append(String textWithMnemonic, Component c1, Component c2)
    {
        append(textWithMnemonic, 1, c1, 1, c2, 1);
    }

    /**
     * Appends a text label spanning across several columns with two components. The label is
     * linked with the first component.
     *
     * @param textWithMnemonic  text for label with optional mnemonic.
     * @param c                 component (label is linked to).
     * @param columnSpan        component column span.
     *
     * @return a label object.
     */
    public JLabel append(String textWithMnemonic, Component c, int columnSpan)
    {
        return append(textWithMnemonic, 1, c, columnSpan, null, 1);
    }

    /**
     * Appends a text label spanning across several columns with two components. The label is
     * linked with the first component.
     *
     * @param textWithMnemonic  text for label with optional mnemonic.
     * @param labelSpan         number of columns for label to span.
     * @param c1                first component (label is linked to).
     * @param c2                second component.
     */
    public void append(String textWithMnemonic, int labelSpan, Component c1, Component c2)
    {
        append(textWithMnemonic, labelSpan, c1, 1, c2, 1);
    }

    /**
     * Appends a text label spanning across several columns with component. The label is
     * linked with the component.
     *
     * @param textWithMnemonic  text for label with optional mnemonic.
     * @param labelSpan         number of columns for label to span.
     * @param c                 component (label is linked to).
     * @param colSpan           number of columns to span the component.
     *
     * @return label.
     */
    public JLabel append(String textWithMnemonic, int labelSpan, Component c, int colSpan)
    {
        return append(textWithMnemonic, labelSpan, c, colSpan, null, 1);
    }

    /**
     * Appends a text label spanning acros several columns with two components. The label is
     * linked with the first component.
     *
     * @param textWithMnemonic  text for label with optional mnemonic.
     * @param labelSpan         number of columns for label to span.
     * @param c1                first component (label is linked to).
     * @param columnSpan1       columns to span first component on to.
     * @param c2                second component.
     * @param columnSpan2       columns to span second component on to.
     *
     * @return label.
     */
    public JLabel append(String textWithMnemonic, int labelSpan, Component c1, int columnSpan1,
        Component c2, int columnSpan2)
    {
        JLabel label = append(textWithMnemonic, labelSpan);

        if (c1 != null)
        {
            append(c1, columnSpan1);
            label.setLabelFor(c1);
        }

        if (c2 != null) append(c2, columnSpan2);

        return label;
    }

    /**
     * Appends component with defined column span and aligning properties.
     *
     * @param c             component.
     * @param columnSpan    column span.
     * @param horiz         horizontal alignment.
     * @param vert          vertical alignment.
     */
    public void append(Component c, int columnSpan,
        CellConstraints.Alignment horiz, CellConstraints.Alignment vert)
    {
        builder.append("");

        int column = builder.getColumn() - 2;
        builder.setColumn(column);

        CellConstraints cc = new CellConstraints(column,  builder.getRow(), columnSpan, 1,
            horiz, vert);

        builder.add(c, cc);
        builder.nextColumn(columnSpan + 1);
    }

    /**
     * Appends label with defined column span and aligning properties.
     *
     * @param textWithMnemonic text with mnemonic chars.
     * @param columnSpan    column span.
     * @param horiz         horizontal alignment.
     * @param vert          vertical alignment.
     *
     * @return label.
     */
    public JLabel append(String textWithMnemonic, int columnSpan,
        CellConstraints.Alignment horiz, CellConstraints.Alignment vert)
    {
        JLabel label = ComponentsFactory.createLabel(textWithMnemonic);

        append(label, columnSpan, horiz, vert);

        return label;
    }

    /**
     * Appends row between related components.
     */
    public void appendRelatedComponentsGapRow()
    {
        builder.appendRelatedComponentsGapRow();
    }

    /**
     * Appends row between related components and moves cursor to the next <code>lines</code>.
     *
     * @param lines number of lines to move down.
     */
    public void appendRelatedComponentsGapRow(int lines)
    {
        appendRelatedComponentsGapRow();
        if (lines > 0) builder.nextLine(lines);
    }

    /**
     * Appends row between unrelated components.
     */
    public void appendUnrelatedComponentsGapRow()
    {
        builder.appendUnrelatedComponentsGapRow();
    }

    /**
     * Appends row between unrelated components and moves cursor to the next <code>lines</code>.
     *
     * @param lines number of lines to move down.
     */
    public void appendUnrelatedComponentsGapRow(int lines)
    {
        appendUnrelatedComponentsGapRow();
        if (lines > 0) builder.nextLine(lines);
    }

    /**
     * Appends row with arbitrary specs.
     *
     * @param encodedSpecs specs.
     */
    public void appendRow(String encodedSpecs)
    {
        builder.appendRow(encodedSpecs);
    }

    /**
     * Moves cursor to the next line.
     *
     * @return this builder.
     */
    public BBFormBuilder nextLine()
    {
        return nextLine(1);
    }

    /**
     * Moves cursor <code>lines</code> below.
     *
     * @param lines lines.
     *
     * @return this builder.
     */
    public BBFormBuilder nextLine(int lines)
    {
        builder.nextLine(lines);

        return this;
    }

    /**
     * Moves cursor to the next column.
     *
     * @return this builder.
     */
    public BBFormBuilder nextColumn()
    {
        return nextColumn(1);
    }

    /**
     * Moves cursor <code>columns</code> columns to the right.
     *
     * @param columns number of columns to shift.
     * 
     * @return this builder.
     */
    public BBFormBuilder nextColumn(int columns)
    {
        builder.nextColumn(columns);

        return this;
    }

    /**
     * Returns the panel being constructed.
     *
     * @return panel.
     */
    public JPanel getPanel()
    {
        return builder.getPanel();
    }

    /**
     * Sets default dialog border around the panel.
     */
    public void setDefaultDialogBorder()
    {
        builder.setDefaultDialogBorder();
    }

    /**
     * Appends separator.
     *
     * @param text  separator text.
     */
    public void appendSeparator(String text)
    {
        builder.appendSeparator(text);
    }
}
