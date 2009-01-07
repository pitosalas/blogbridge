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
// $Id: ToolbarPlugin.java,v 1.1 2007/08/24 09:04:03 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.views.mainframe.BBToolBarBuilder;
import org.jdom.Element;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plugin for toolbar modification.
 */
public class ToolbarPlugin implements IPlugin
{
    private final Element plugin;

    /**
     * Creates the plug-in and initializes it with data from the element.
     *
     * @param plugin plug-in node.
     */
    public ToolbarPlugin(Element plugin)
    {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------------
    // IPlugin implementation
    // ------------------------------------------------------------------------

    /**
     * Returns the name of plug-in type (Theme, Actions ...).
     *
     * @return the name of plug-in type.
     */
    public String getTypeName()
    {
        return "Toolbar";
    }

    /** Initializes plug-in. */
    public void initialize()
    {
        List<Element> nodes = (List<Element>)plugin.getChildren();
        BBToolBarBuilder.ToolbarLayout layout = BBToolBarBuilder.getLayout();
        layout.clear();
        
        for (Element node : nodes)
        {
            String name = node.getName();
            if ("separator".equalsIgnoreCase(name))
            {
                layout.appendLargeGap();
            } else if ("action".equalsIgnoreCase(name))
            {
                layout.appendAction(new LazyAction(node));
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Lazy action that is constructed from the toolbar plug-in 'action' subnodes.
     */
    private static class LazyAction implements Action, PropertyChangeListener
    {
        private static final String LABEL             = "label";
        private static final String SHORT_DESCRIPTION = "tooltip";
        private static final String LONG_DESCRIPTION  = "helptext";
        private static final String ICON              = "icon";
        private static final String GRAY_ICON         = ICON + ".gray";

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        private Action              action;
        private String              actionName;
        private Map<String, Object> overrides;

        /**
         * Creates a lazy action from the XML element.
         *
         * @param action action node.
         */
        private LazyAction(Element action)
        {
            // Get the action name
            actionName = action.getAttributeValue("name");
            if (actionName == null) throw new IllegalArgumentException("Property 'name' should hold the action name");

            // Optional overrides
            overrides = new HashMap<String, Object>();
            overrides.put(Action.NAME, action.getAttributeValue(LABEL));
            overrides.put(Action.SHORT_DESCRIPTION, action.getAttributeValue(SHORT_DESCRIPTION));
            overrides.put(Action.LONG_DESCRIPTION, action.getAttributeValue(LONG_DESCRIPTION));

            // Optional icon overrides. We save only keys here to
            // be able to pick them later during the action first access
            overrides.put(Action.SMALL_ICON, action.getAttributeValue(ICON));
            overrides.put(ActionManager.SMALL_GRAY_ICON, action.getAttributeValue(GRAY_ICON));
        }

        /**
         * Returns the action instance.
         *
         * @return action.
         */
        private synchronized Action getAction()
        {
            if (action == null)
            {
                action = ActionManager.get(actionName);
                action.addPropertyChangeListener(this);

                convertImage(Action.SMALL_ICON);
                convertImage(ActionManager.SMALL_GRAY_ICON);
            }
            return action;
        }

        /**
         * Converts an image path into an image.
         *
         * @param key key of the icons in the action overrides.
         */
        private void convertImage(String key)
        {
            Object val = overrides.get(key);
            if (val instanceof String)
            {
                String k = (String)val;

                if (k.indexOf('/') != -1 || k.indexOf('\\') != -1 || k.matches("\\.(gif|jpg|png)\\s*$"))
                {
                    overrides.put(key, ResourceUtils.readImageIcon(k));
                } else if (k.matches("^[a-zA-Z0-9\\.]+$"))
                {
                    overrides.put(key, ResourceUtils.getIcon(k));
                }
            }
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            getAction().actionPerformed(e);
        }

        /**
         * Gets one of this object's properties using the associated key.
         *
         * @see #putValue
         */
        public Object getValue(String key)
        {
            Object val = overrides.get(key);
            if (val == null) val = getAction().getValue(key);
            return val;
        }

        /**
         * Sets one of this object's properties using the associated key. If the value has changed, a
         * <code>PropertyChangeEvent</code> is sent to listeners.
         *
         * @param key   a <code>String</code> containing the key
         * @param value an <code>Object</code> value
         */
        public void putValue(String key, Object value)
        {
            Object old = getValue(key);
            overrides.put(key, value);
            pcs.firePropertyChange(key, old, value);
        }

        /**
         * Sets the enabled state of the <code>Action</code>.  When enabled, any component associated with this object is
         * active and able to fire this object's <code>actionPerformed</code> method. If the value has changed, a
         * <code>PropertyChangeEvent</code> is sent to listeners.
         *
         * @param b true to enable this <code>Action</code>, false to disable it
         */
        public void setEnabled(boolean b)
        {
            getAction().setEnabled(b);
        }

        /**
         * Returns the enabled state of the <code>Action</code>. When enabled, any component associated with this object is
         * active and able to fire this object's <code>actionPerformed</code> method.
         *
         * @return true if this <code>Action</code> is enabled
         */
        public boolean isEnabled()
        {
            return getAction().isEnabled();
        }

        /**
         * Adds a <code>PropertyChange</code> listener. Containers and attached components use these methods to register
         * interest in this <code>Action</code> object. When its enabled state or other property changes, the registered
         * listeners are informed of the change.
         *
         * @param listener a <code>PropertyChangeListener</code> object
         */
        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(listener);
        }

        /**
         * Removes a <code>PropertyChange</code> listener.
         *
         * @param listener a <code>PropertyChangeListener</code> object
         *
         * @see #addPropertyChangeListener
         */
        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.removePropertyChangeListener(listener);
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            // WARN: Probably this isn't correct. If we override a property the new value may be
            //       not what we really have.
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
}
