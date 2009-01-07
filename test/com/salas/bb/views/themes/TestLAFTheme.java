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
// $Id: TestLAFTheme.java,v 1.6 2007/02/06 15:34:02 spyromus Exp $
//

package com.salas.bb.views.themes;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * @see LAFTheme
 */
public class TestLAFTheme extends TestCase
{
    private LAFTheme theme;

    protected void setUp()
        throws Exception
    {
        theme = new LAFTheme();
    }

    /**
     * Test that all color properties defined.
     */
    public void testThatAllColorPropertiesDefined()
    {
        List keys = getColorKeys();

        for (int i = 0; i < keys.size(); i++)
        {
            ThemeKey.Color key = (ThemeKey.Color)keys.get(i);
            assertNotNull("Key " + key.getKey() + " isn't defined.", theme.getColor(key));
        }
    }

    /**
     * Test that all font properties defined.
     */
    public void testThatAllFontPropertiesDefined()
    {
        List keys = getFontKeys();

        for (Object key1 : keys)
        {
            ThemeKey.Font key = (ThemeKey.Font)key1;
            assertNotNull("Key " + key.getKey() + " isn't defined.", theme.getFont(key));
        }
    }

    private List getColorKeys()
    {
        return getKeys(ThemeKey.Color.class);
    }

    private List getFontKeys()
    {
        return getKeys(ThemeKey.Font.class);
    }

    private List getKeys(Class clazz)
    {
        final Field[] fields = clazz.getDeclaredFields();
        ArrayList list = new ArrayList();
        for (Field field : fields)
        {
            if (field.getDeclaringClass() == clazz)
            {
                try
                {
                    list.add(field.get(null));
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }
}
