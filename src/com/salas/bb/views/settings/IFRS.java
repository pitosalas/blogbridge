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
// $Id: IFRS.java,v 1.2 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views.settings;

import com.salas.bb.views.themes.ITheme;

import java.beans.PropertyChangeListener;

/**
 * Channel rendering settings common interface.
 */
public interface IFRS
{
    /**
     * Returns value of a setting under specified key.
     *
     * @param key key name of the setting.
     *
     * @return value of the setting.
     */
    Object get(String key);

    /**
     * Registers new value of the setting.
     *
     * @param key   key name of the setting.
     * @param value value of the setting or NULL to remove setting value.
     */
    void set(String key, Object value);

    /**
     * Registers listener of settings changes.
     *
     * @param l new listener object.
     */
    void addListener(PropertyChangeListener l);

    /**
     * Removes listener of settings changes.
     *
     * @param l listener object.
     */
    void removeListener(PropertyChangeListener l);

    /**
     * Returns current theme.
     *
     * @return theme or NULL if not defined.
     */
    ITheme getTheme();
}
