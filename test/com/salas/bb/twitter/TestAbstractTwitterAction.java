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
// $Id$
//

package com.salas.bb.twitter;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Tests twitter action.
 */
public class TestAbstractTwitterAction extends TestCase
{
    /** Tests extraction of the user name. */
    public void testURLToScreenName()
        throws MalformedURLException
    {
        assertEquals("spyromus", AbstractTwitterAction.urlToScreenName(new URL("http://twitter.com/spyromus")));
        assertEquals("spyromus", AbstractTwitterAction.urlToScreenName(new URL("http://twitter.com/spyromus?a")));
        assertEquals("spyromus", AbstractTwitterAction.urlToScreenName(new URL("http://twitter.com/spyromus#a")));
        assertEquals(null, AbstractTwitterAction.urlToScreenName(new URL("http://twitter.com/friendship/a.json")));
    }
}
