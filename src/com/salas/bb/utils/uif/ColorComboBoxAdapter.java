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
// $Id: ColorComboBoxAdapter.java,v 1.1 2008/02/28 11:35:40 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.value.ValueModel;

import java.awt.*;

/**
 * Adapts colors to combo box.
 */
public class ColorComboBoxAdapter extends ComboBoxAdapter
{
    protected ValueModel model;

    public ColorComboBoxAdapter(Color[] colors, ValueModel model)
    {
        super(colors, model);
        this.model = model;
    }

    /**
     * Returns currently selected item.
     *
     * @return item.
     */
    public Object getSelectedItem()
    {
        return model.getValue();
    }

    /**
     * Selects different item.
     *
     * @param o item to select.
     */
    public void setSelectedItem(Object o)
    {
        model.setValue(o);
    }
}
