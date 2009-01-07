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
// $Id $
//

package com.salas.bb.discovery.filter;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.salas.bb.utils.StringUtils;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Dynamic extension URL filter watches the changes in some property of
 * some bean object and recompiles the pattern every time it's necessary.
 */
public class DynamicExtensionURLFilter extends ExtensionURLFilter
{
    private PropertyAdapter adapter;

    private Pattern pattern;
    private int extendsionsHash;
    private final Object patternLock = new Object();

    /**
     * Creates extension URL filter with dynamic list.
     *
     * @param bean          bean to watch for the list.
     * @param propertyName  the name of the property holding the list of extensions.
     */
    public DynamicExtensionURLFilter(Object bean, String propertyName)
    {
        super((String[])null);

        if (bean == null) throw new NullPointerException("Bean should be specified");
        if (propertyName == null) throw new NullPointerException("Property name should be specified");

        adapter = new PropertyAdapter(bean, propertyName);
    }

    /**
     * Returns the pattern to use for matching.
     *
     * @return pattern.
     */
    protected Pattern getPattern()
    {
        // Calculate current extensions hash code
        Object val = adapter.getValue();
        String ext = val == null ? null : val.toString().trim();
        int hash = ext == null ? 0 : ext.hashCode();

        // Check if the codes differ -- if the extensions list has changed
        if (extendsionsHash != hash)
        {
            synchronized (patternLock)
            {
                if (extendsionsHash != hash)
                {
                    // Recalculate the pattern
                    String[] extensions = stringToExtensions(ext);
                    String patternStr = extensionsToPattern(extensions);

                    pattern = patternStr == null ? null : Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                    extendsionsHash = hash;
                }
            }
        }

        return pattern;
    }
}
