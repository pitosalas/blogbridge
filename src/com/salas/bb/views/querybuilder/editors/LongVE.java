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
// $Id: LongVE.java,v 1.4 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views.querybuilder.editors;

import com.jgoodies.binding.value.ValueModel;

import javax.swing.*;
import java.awt.*;

/**
 * Basic usage long editor based on spinner component.
 */
class LongVE extends JSpinner implements IValueEditor
{
    private long min;
    private long max;

    /**
     * Constructs a spinner with an long model.
     *
     * @param aMin   minimum value.
     * @param aMax   maximum value.
     * @param aModel value model.
     */
    public LongVE(long aMin, long aMax, ValueModel aModel)
    {
        super();

        min = aMin;
        max = aMax;

        setModel(new SpinnerModel(1, min, max, 1, aModel));
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

    /** Model which adopts the spinner to the value mode. */
    private static class SpinnerModel extends SpinnerNumberModel
    {
        private ValueModel  model;
        private long        def;

        /** Creates model. */
        public SpinnerModel(long aDef, long min, long max, long step, ValueModel aModel)
        {
            super(new Long(getModelValue(aModel, aDef)), new Long(min), new Long(max),
                new Long(step));

            model = aModel;
            def = aDef;
        }

        /**
         * Sets the value.
         *
         * @param value value.
         */
        public void setValue(Object value)
        {
            model.setValue(value.toString());
            super.setValue(value);
        }

        /**
         * Returns the value of the current element of the sequence.
         *
         * @return the value property
         *
         * @see #setValue
         * @see #getNumber
         */
        public Object getValue()
        {
            return new Long(getModelValue(model, def));
        }

        private static long getModelValue(ValueModel aModel, long aDef)
        {
            long value = aDef;

            if (aModel != null)
            {
                try
                {
                    value = Long.parseLong(aModel.getValue().toString());
                } catch (NumberFormatException e)
                {
                    // Invalid format -- skip to default
                }
            }

            return value;
        }
    }
}
