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
// $Id: TestSmartFeedPlugin.java,v 1.1 2007/04/06 09:29:57 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import junit.framework.TestCase;

/**
 * Some tests.
 */
public class TestSmartFeedPlugin extends TestCase
{
    /** Tests URL preprocessing. */
    public void testPreprocessURL()
    {
        // No keys
        assertEquals("http://test/", SmartFeedPlugin.preprocessURL("http://test/"));

        // Keys
        assertEquals("http://test/{0}", SmartFeedPlugin.preprocessURL("http://test/{query}"));
        assertEquals("http://test/{0}", SmartFeedPlugin.preprocessURL("http://test/{keys}"));
        assertEquals("http://test/{0}?a=1", SmartFeedPlugin.preprocessURL("http://test/{keys}?a=1"));
        
        // Limit
        assertEquals("http://test/{1}", SmartFeedPlugin.preprocessURL("http://test/{max}"));
        assertEquals("http://test/{1}", SmartFeedPlugin.preprocessURL("http://test/{limit}"));
        assertEquals("http://test/{1}?a=1", SmartFeedPlugin.preprocessURL("http://test/{max}?a=1"));
    }
}
