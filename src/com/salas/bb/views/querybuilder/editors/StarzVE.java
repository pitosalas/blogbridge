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
// $Id: StarzVE.java,v 1.3 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views.querybuilder.editors;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.views.mainframe.StarsSelectionComponent;

import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * Starz selector component.
 */
class StarzVE extends StarsSelectionComponent implements IValueEditor
{
    /**
     * Constructs a stars selection component.
     *
     * @param aModel value model.
     */
    public StarzVE(ValueModel aModel)
    {
        super(new BoundedRangeAdapter(new StringToIntegerValueModel(aModel, 1), 0, 1, 5));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
     * Bridge between string value model (wrapped) and integer value model consumer.
     */
    private static class StringToIntegerValueModel implements ValueModel
    {
        private final ValueModel    wrapped;
        private final Integer       def;

        /**
         * Creates bridge.
         *
         * @param aWrapped string value model.
         * @param aDef default value when string isn't parsable.
         */
        public StringToIntegerValueModel(ValueModel aWrapped, int aDef)
        {
            wrapped = aWrapped;
            def = new Integer(aDef);
        }

        /**
         * Returns value.
         *
         * @return value.
         */
        public Object getValue()
        {
            Integer value;

            try
            {
                value = new Integer(wrapped.toString());
            } catch (NumberFormatException e)
            {
                value = def;
            }

            return value;
        }

        /**
         * Sets value.
         *
         * @param value value.
         */
        public void setValue(Object value)
        {
            wrapped.setValue(value.toString());
        }

        /**
         * Adds change listener.
         *
         * @param listener listener.
         */
        public void addValueChangeListener(PropertyChangeListener listener)
        {
            wrapped.addValueChangeListener(listener);
        }

        /**
         * Removes change listener.
         *
         * @param listener listener.
         */
        public void removeValueChangeListener(PropertyChangeListener listener)
        {
            wrapped.removeValueChangeListener(listener);
        }
    }
}
