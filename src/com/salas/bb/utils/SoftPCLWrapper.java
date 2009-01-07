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
// $Id: SoftPCLWrapper.java,v 1.3 2006/01/08 05:04:21 kyank Exp $
//

package com.salas.bb.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;

/**
 * Wrappes the actual listener of properties changes. It removes hard link between
 * source of events and listener, allowing the listener to be garbage collected.
 * The <code>removeThisListener</code> method can be implemented by a sub-class to
 * introduce self-removing logic. It can be useful to remove the wrapper if it
 * no longer holds actual listener.   
 */
public abstract class SoftPCLWrapper extends SoftReference implements PropertyChangeListener
{
    /**
     * Creates wrapper for the listener.
     *
     * @param listener listener.
     */
    public SoftPCLWrapper(PropertyChangeListener listener)
    {
        super(listener);
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source and the property that has
     *            changed.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        PropertyChangeListener listener = (PropertyChangeListener)get();

        if (listener == null)
        {
            removeThisListener(evt.getSource());
        } else
        {
            listener.propertyChange(evt);
        }
    }

    /**
     * This method should be overridden if self-removal logic should take place.
     * It will be called when it's found that the wrapped listener is no longer
     * existing and this wrapper can be safely removed from the source of events.
     *
     * @param source source of events.
     */
    protected abstract void removeThisListener(Object source);
}
