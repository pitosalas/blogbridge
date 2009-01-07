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
// $Id: ChoiceVE.java,v 1.6 2006/05/29 12:50:08 spyromus Exp $
//

package com.salas.bb.views.querybuilder.editors;

import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Value editor for several choices.
 */
class ChoiceVE extends JComboBox implements IValueEditor
{
    private final Map valueToHolder;

    /**
     * Creates editor with multiple choices in drop-down.
     *
     * @param labels    labels.
     * @param values    values.
     * @param aModel    model.
     */
    public ChoiceVE(String[] labels, String[] values, ValueModel aModel)
    {
        if (labels.length != values.length)
        {
            throw new IllegalArgumentException(Strings.error("ui.values.do.not.match.labels"));
        }

        valueToHolder = new HashMap();
        ValueLabelHolder[] holders = initValueToHolder(valueToHolder, labels, values);
        setModel(new ChoiceModel(holders, aModel));
    }

    private static ValueLabelHolder[] initValueToHolder(Map map, String[] labels, String[] values)
    {
        ValueLabelHolder[] holders = new ValueLabelHolder[labels.length];
        for (int i = 0; i < labels.length; i++)
        {
            String label = labels[i];
            String value = values[i];
            ValueLabelHolder holder = new ValueLabelHolder(label, value);

            map.put(value, holder);
            holders[i] = holder;
        }

        return holders;
    }

    /**
     * Returns the component to be used for displaying the editor.
     *
     * @return component.
     */
    public Component getVisualComponent()
    {
        return this;
    }

    /**
     * Adapter of property model to combo-box model.
     */
    private class ChoiceModel extends DefaultComboBoxModel
    {
        private ValueModel model;

        /**
         * Constructs a DefaultComboBoxModel object initialized with an array of objects.
         *
         * @param items an array of Object objects
         * @param aModel model.
         */
        public ChoiceModel(final Object items[], ValueModel aModel)
        {
            super(items);
            model = aModel;
        }

        /**
         * Returns currently selected item according to the model.
         *
         * @return selected item.
         */
        public Object getSelectedItem()
        {
            Object value = model.getValue();
            return valueToHolder.get(value);
        }

        /**
         * Set the value of the selected item. The selected item may be null. <p>
         *
         * @param anObject The combo box value or null for no selection.
         */
        public void setSelectedItem(Object anObject)
        {
            ValueLabelHolder holder = (ValueLabelHolder)anObject;
            model.setValue(holder.value);
        }
    }

    /**
     * Simple holder for value-label pair.
     */
    private static class ValueLabelHolder
    {
        private String value;
        private String label;

        /**
         * Creates value label object.
         *
         * @param aLabel    label.
         * @param aValue    value.
         */
        public ValueLabelHolder(String aLabel, String aValue)
        {
            label = aLabel;
            value = aValue;
        }

        /**
         * Returns a string representation of the object.
         *
         * @return a string representation of the object.
         */
        public String toString()
        {
            return label;
        }
    }
}
