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
// $Id: ActionAlias.java,v 1.4 2006/04/12 14:02:27 kyank Exp $
//

package com.salas.bb.core.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

/**
 * An Action that provides an alternate menu name or other UI property for
 * another underlying action. Allows us to have the same Action appear
 * with different names in the UI.
 */
public class ActionAlias extends AbstractAction implements PropertyChangeListener
{
    /** The real action. */
    private final AbstractAction  proxiedAction;
    
    /**
     * Creates alias.
     *
     * @param anAction The real action that we shadow.
     */
    public ActionAlias(AbstractAction anAction)
    {
        proxiedAction = anAction;
        proxiedAction.addPropertyChangeListener(this);
    }
    
    /**
     * Notifies us of action executed. Forwards execution to proxied action.
     *
     * @param event event object.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        proxiedAction.actionPerformed(event);
    }

    /**
     * Returns property value. The priority is given to own properties, if the
     * property is missing then the proxied action will be questioned for the
     * same property value.
     *
     * @param name name of the property.
     *
     * @return value of the property or <code>NULL</code> if not defined.
     *
     * @see javax.swing.Action#getValue(java.lang.String)
     */
    public Object getValue(String name)
    {
        Object value = super.getValue(name);
        return value == null ? proxiedAction.getValue(name) : value;
    }
    
    /**
     * Tests if action is enabled. The test is delegated to the proxied action.
     *
     * @return <code>TRUE</code> if action is enabled.
     *
     * @see javax.swing.Action#isEnabled()
     */
    public boolean isEnabled()
    {
        return proxiedAction.isEnabled();
    }
    
    /**
     * Enables / disables action. The call is forwarded to the proxied action.
     *
     * @param enabled <code>TRUE</code> to enable.
     *
     * @see javax.swing.Action#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        proxiedAction.setEnabled(enabled);
    }
    
    /**
     * Notification that our proxiedAction changed a property. If it is not
     * a property that we have overriden locally in this {@link ActionAlias}
     * then pass along the notification. Locally overriden property changes should
     * get notified by our superclass.
     *
     * @param evt the change event object
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propertyName = evt.getPropertyName();
        if (!hasProperty(propertyName))
        {
            firePropertyChange(propertyName, evt.getOldValue(), evt.getNewValue());
        }
    }

    /**
     * Returns <code>TRUE</code> if this action has own value for some property.
     *
     * @param aPropertyName name of property to check.
     *
     * @return <code>TRUE</code> if this action has own value for some property.
     */
    private boolean hasProperty(String aPropertyName)
    {
        // As of Java 6, "enabled" is considered a property of all Actions, but we want to share
        // the proxiedAction's value for this property, so we treat it as a special case.
        if (aPropertyName.equals("enabled"))
        {
            return false;
        }
        return super.getValue(aPropertyName) != null;
    }
}

