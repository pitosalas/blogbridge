// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: SwingPropertyChangeSupport.java,v 1.1 2007/06/12 16:11:32 spyromus Exp $
//

package com.salas.bb.utils.swingworker;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;

/**
 * Extended Swing property change support that knows how to send the notifications from the EDT.
 * <p/>
 * Note: This is a backport from Java 1.6
 */
public final class SwingPropertyChangeSupport extends PropertyChangeSupport
{
    // Serialization version ID
    static final long serialVersionUID = 7162625831330845068L;

    /**
     * whether to notify listeners on EDT
     *
     * @serial
     * @since 1.6
     */
    private final boolean notifyOnEDT;

    /**
     * Constructs a SwingPropertyChangeSupport object.
     *
     * @param sourceBean The bean to be given as the source for any events.
     *
     * @throws NullPointerException if {@code sourceBean} is {@code null}
     */
    public SwingPropertyChangeSupport(Object sourceBean)
    {
        this(sourceBean, false);
    }

    /**
     * Constructs a SwingPropertyChangeSupport object.
     *
     * @param sourceBean  the bean to be given as the source for any events
     * @param notifyOnEDT whether to notify listeners on the <i>Event Dispatch Thread</i> only
     *
     * @throws NullPointerException if {@code sourceBean} is {@code null}
     * @since 1.6
     */
    public SwingPropertyChangeSupport(Object sourceBean, boolean notifyOnEDT)
    {
        super(sourceBean);
        this.notifyOnEDT = notifyOnEDT;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/>
     * If {@link #isNotifyOnEDT} is {@code true} and called off the <i>Event Dispatch Thread</i> this implementation
     * uses {@code SwingUtilities.invokeLater} to send out the notification on the <i>Event Dispatch Thread</i>. This
     * ensures  listeners are only ever notified on the <i>Event Dispatch Thread</i>.
     *
     * @throws NullPointerException if {@code evt} is {@code null}
     * @since 1.6
     */
    public void firePropertyChange(final PropertyChangeEvent evt)
    {
        if (evt == null)
        {
            throw new NullPointerException();
        }
        if (!isNotifyOnEDT()
                || SwingUtilities.isEventDispatchThread())
        {
            super.firePropertyChange(evt);
        } else
        {
            SwingUtilities.invokeLater(
                    new Runnable()
                    {
                        public void run()
                        {
                            firePropertyChange(evt);
                        }
                    });
        }
    }

    /**
     * Returns {@code notifyOnEDT} property.
     *
     * @return {@code notifyOnEDT} property
     *
     * @since 1.6
     */
    public final boolean isNotifyOnEDT()
    {
        return notifyOnEDT;
    }
}
