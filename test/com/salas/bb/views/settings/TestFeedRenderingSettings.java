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
// $Id: TestFeedRenderingSettings.java,v 1.2 2006/01/08 05:28:35 kyank Exp $
//

package com.salas.bb.views.settings;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>FeedRenderingSettings</code> unit.
 */
public class TestFeedRenderingSettings extends TestCase
{
    /**
     * Tests returning initial and updated font bias.
     */
    public void testGetArticleFontBias()
    {
        FeedRenderingSettings renderingSettings = new FeedRenderingSettings();
        renderingSettings.setParent(new DefaultFRS());
        assertEquals("Initial bias expected to be zero.",
            renderingSettings.getArticleFontBias(), 0);
        
        renderingSettings.setArticleFontBias(-1);
        assertEquals("Property value hasn't propagated.",
            renderingSettings.getArticleFontBias(), -1);
    }

    /**
     * Tests font bias updates logic.
     */
    public void testSetArticleFontBias()
    {
        FeedRenderingSettings renderingSettings = new FeedRenderingSettings();
        renderingSettings.setParent(new DefaultFRS());

        renderingSettings.setArticleFontBias(-3);
        assertEquals("Assuming it starts at zero, the adjusted value should be -3.",
            renderingSettings.getArticleFontBias(), -3);
        
        // TODO it looks weird that we *set* font bias to "1" and get in return "-2" and it's OK!
        // TODO consider renaming method to adjustArticleFontBias() or something
        
        // Fontbias is ADDED to old bias
        renderingSettings.setArticleFontBias(1);
        assertEquals("Expected font bias == -2",
            renderingSettings.getArticleFontBias(), -2);
        
        renderingSettings.setArticleFontBias(-3);
        assertEquals("Expected font bias == -5", renderingSettings.getArticleFontBias(), -5);
 
        // If fontbias argument is 0 then reset it back to zero
        renderingSettings.setArticleFontBias(0);
        assertEquals("Expected font bias == 0", renderingSettings.getArticleFontBias(), 0);
    }
}
