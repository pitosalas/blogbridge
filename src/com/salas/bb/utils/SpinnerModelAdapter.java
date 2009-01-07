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
// $Id: SpinnerModelAdapter.java,v 1.1 2006/02/22 13:37:21 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;

import javax.swing.*;

/** Model for adapting bounded range model to spinner number model. */
public class SpinnerModelAdapter extends SpinnerNumberModel
{
    private BoundedRangeAdapter model;

    /**
     * Constructs a <code>SpinnerNumberModel</code> with no <code>minimum</code> or
     * <code>maximum</code> value, <code>stepSize</code> equal to one, and an initial value
     * of zero.
     *
     * @param aModel model to adapt.
     */
    public SpinnerModelAdapter(BoundedRangeAdapter aModel)
    {
        model = aModel;
        setMaximum(new Integer(model.getMaximum()));
        setMinimum(new Integer(model.getMinimum()));
        setValue(new Integer(model.getValue()));
    }

    /**
     * Sets the current value for this sequence. This method fires a <code>ChangeEvent</code>
     * if the value has changed.
     *
     * @param value the current (non <code>null</code>)<code>Number</code> for this
     *        sequence
     *
     * @throws IllegalArgumentException if <code>value</code> is <code>null</code> or not a
     *
     * @see javax.swing.SpinnerModel#addChangeListener
     */
    public void setValue(Object value)
    {
        super.setValue(value);
        Integer v = (Integer) value;
        model.setValue(v.intValue());
    }
}
