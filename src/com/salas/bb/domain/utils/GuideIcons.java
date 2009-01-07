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
// $Id: GuideIcons.java,v 1.3 2006/02/21 09:41:37 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.jgoodies.uif.util.ResourceUtils;

import javax.swing.*;
import java.util.*;

/**
 * <p>Holder of the information about currenctly installed channel guide icons. It loads the
 * icons names during initialization and holds them after that.</p>
 *
 * <p>In addition, it provides some useful models to read the data.</p>
 */
public final class GuideIcons
{
    private static final List iconKeys;
    private static final String[] abstractIconKeys;

    static
    {
        iconKeys = new ArrayList();

        // Reads from resource bundle all keys and records the keys in format: cg.xyz.icon.
        ResourceBundle bundle = ResourceUtils.getBundle();
        if (bundle != null)
        {
            Enumeration keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                if (key.startsWith("cg.") && key.endsWith(".icon"))
                {
                    iconKeys.add(key);
                }
            }

            abstractIconKeys = bundle.getString("cg.abstract.icons").split(",");
            for (int i = 0; i < abstractIconKeys.length; i++)
            {
                abstractIconKeys[i] = "cg." + abstractIconKeys[i] + ".icon";
            }
        } else
        {
            abstractIconKeys = new String[0];
        }
    }

    /**
     * Hidden utility class constructor.
     */
    private GuideIcons()
    {
    }

    /**
     * Returns the names of all icons registered for use by channel guides.
     *
     * @return the list of icons names.
     */
    public static String[] getIconsNames()
    {
        return (String[])iconKeys.toArray(new String[iconKeys.size()]);
    }

    /**
     * Finds first unused icon name using the owned collection of icons names and
     * provided list of used icons names.
     *
     * @param usedIconsNames list of used icons names.
     *
     * @return unused icon name index or <code>-1</code> if not found.
     */
    public static int findUnusedIconName(Collection usedIconsNames)
    {
        String icon = null;

        // Look for first unused
        for (int i = 0; icon == null && i < abstractIconKeys.length; i++)
        {
            String icn = abstractIconKeys[i];
            if (!usedIconsNames.contains(icn)) icon = icn;
        }

        if (icon == null) icon = abstractIconKeys[0];

        return iconKeys.indexOf(icon);
    }

    /**
     * Adopts the list of guide icons names to combo-box model.
     */
    public static class ComboBoxModel extends DefaultComboBoxModel
    {
        /**
         * Returns the size of model.
         *
         * @return size.
         */
        public int getSize()
        {
            return GuideIcons.getIconsNames().length;
        }

        /**
         * Returns icon name at the specified index.
         *
         * @param index index.
         *
         * @return icon name.
         */
        public Object getElementAt(int index)
        {
            return GuideIcons.getIconsNames()[index];
        }
    }
}
