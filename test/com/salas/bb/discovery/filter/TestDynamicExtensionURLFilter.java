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

import com.jgoodies.binding.value.ValueHolder;

import java.net.URL;

/**
 * Tests dynamic extension filter.
 */
public class TestDynamicExtensionURLFilter extends ExtensionURLFilterTestCase
{
    /**
     * Tests dynamic changes to the bean parameter holding the list of extensions.
     */
    public void testDynamicChange()
    {
        URL url = url("test.avi?test=parameter");
        ValueHolder extensions = new ValueHolder(null);
        DynamicExtensionURLFilter f = new DynamicExtensionURLFilter(extensions, "value");

        // We start with empty list of extensions
        assertFalse("No extensions in the filter", f.matches(url));

        // We set some extensions, but it should be still false
        extensions.setValue("mpg");
        assertFalse("Wrong extensions in the filter", f.matches(url));

        // We add right extension
        extensions.setValue("mpg, aVi");
        assertTrue("Right extensions in the filter", f.matches(url));

        // We remove that right extension
        extensions.setValue(" mov ");
        assertFalse("Wrong extensions in the filter", f.matches(url));

        // We clear the filter extensions
        extensions.setValue(null);
        assertFalse("No extensions in the filter", f.matches(url));
    }
}
