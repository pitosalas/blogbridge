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
// $Id: ConnectionState.java,v 1.7 2006/05/30 08:25:28 spyromus Exp $
//

package com.salas.bb.utils;

import com.salas.bb.utils.i18n.Strings;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This object represents current connection state. When the connection state is set
 * to "online" the service can be accessible or not. If it's not, some dependent
 * functionality should be disabled across the application and reenabled once it
 * becomes accessible again.
 *
 * User can change the online state explicitly, but he can't change the
 * service availability manually. This parameter is determined experimentally.
 * It's clear that the service can not be reported as accessible when the user
 * explicitly defined that there's no connection to the Internet.
 */
public final class ConnectionState
{
    private static final Logger LOG = Logger.getLogger(ConnectionState.class.getName());

    /** Online flag. */
    public static final String PROP_ONLINE              = "online";
    /** Service availability flag. */
    public static final String PROP_SERVICE_ACCESSIBLE  = "serviceAccessible";

    private final PropertyChangeSupport pcs;

    /** Connection state set by user. */
    private boolean online;

    /** The detected accessibility of the service. */
    private boolean serviceAccessible;

    /**
     * Creates connection state object.
     */
    public ConnectionState()
    {
        pcs = new PropertyChangeSupport(this);

        online = true;
        serviceAccessible = false;
    }

    /**
     * Returns <code>TRUE</code> if currently the state is ONLINE.
     *
     * @return <code>TRUE</code> if currently the state is ONLINE.
     */
    public boolean isOnline()
    {
        return online;
    }

    /**
     * Returns <code>TRUE</code> if the service is accessible and we are currently ONLINE.
     *
     * @return <code>TRUE</code> if the service is accessible.
     */
    public boolean isServiceAccessible()
    {
        return online && serviceAccessible;
    }

    /**
     * Sets the online state of connection.
     *
     * @param anOnline <code>TRUE</code> to set connection as being ONLINE.
     */
    public void setOnline(boolean anOnline)
    {
        boolean oldOnline = isOnline();
        boolean oldServiceAccessible = isServiceAccessible();
        online = anOnline;

        pcs.firePropertyChange(PROP_ONLINE, oldOnline, isOnline());
        pcs.firePropertyChange(PROP_SERVICE_ACCESSIBLE, oldServiceAccessible, isServiceAccessible());
    }

    /**
     * Changes the state of the service accessibility.
     *
     * @param anAccessible <code>TRUE</code> to mark service as accessible.
     */
    public void setServiceAccessible(boolean anAccessible)
    {
        boolean oldServiceAccessible = isServiceAccessible();
        serviceAccessible = anAccessible;

        pcs.firePropertyChange(PROP_SERVICE_ACCESSIBLE, oldServiceAccessible, isServiceAccessible());
    }

    /**
     * Adds property change listener.
     *
     * @param propertyName  name of the property to listen.
     * @param l             listener object.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(propertyName, l);
    }

    /**
     * Adds property change listener.
     *
     * @param l             listener object.
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Removes listener from the notification list.
     *
     * @param l             listener object.
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Calls the task when the service is available. If it's available right now,
     * the task is executed immediately. Otherwise it's scheduled for better times.
     *
     * @param aTask task to execute.
     */
    public void callWhenServiceIsAvailable(Runnable aTask)
    {
        if (isServiceAccessible())
        {
            aTask.run();
        } else
        {
            PropertyChangeListener l = new BooleanPropertyTrigger(aTask, true);
            addPropertyChangeListener(PROP_SERVICE_ACCESSIBLE, l);
        }
    }

    /**
     * Boolean trigger expects some value to be reported and calles the given task.
     * In addition to this it unsubscribes itself from the emitter, thus it's single-shot.
     */
    private class BooleanPropertyTrigger implements PropertyChangeListener
    {
        private final boolean   triggerValue;
        private final Runnable  task;

        /**
         * Creates trigger.
         *
         * @param aTask         task to execute.
         * @param aTriggerValue value which is triggering the event.
         *
         * @throws NullPointerException in case when task isn't specified.
         */
        public BooleanPropertyTrigger(Runnable aTask, boolean aTriggerValue)
            throws NullPointerException
        {
            if (aTask == null) throw new NullPointerException(Strings.error("unspecified.task"));

            task = aTask;
            triggerValue = aTriggerValue;
        }

        /**
         * Called when some property changes its value.
         *
         * @param evt event object.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            Object value = evt.getNewValue();
            if (value instanceof Boolean && ((Boolean)value).booleanValue() == triggerValue)
            {
                removePropertyChangeListener(this);
                try
                {
                    task.run();
                } catch (Throwable e)
                {
                    LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
                }
            }
        }
    }
}
