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
// $Id: TestEnclosureAutoSaver.java,v 1.1 2007/05/02 10:27:07 spyromus Exp $
//

package com.salas.bb.core.autosave;

import com.salas.bb.utils.parser.RomeFeedParser;
import junit.framework.TestCase;

/**
 * Tests enclusure saver.
 */
public class TestEnclosureAutoSaver extends TestCase
{
    /** Tests finding enclosures. */
    public void testGetEnclosures()
    {
        // Prepare the format enclosure strings
        String lnk1 = "cc.mp3";
        String lnk2 = "bb/cc.mp3";
        String lnk3 = "/aa/bb/cc.mp3";
        String lnk4 = "http://aa/bb/cc.mp3";
        String enc1 = RomeFeedParser.formatEnclosure(lnk1, 0);
        String enc2 = RomeFeedParser.formatEnclosure(lnk2, 0);
        String enc3 = RomeFeedParser.formatEnclosure(lnk3, 0);
        String enc4 = RomeFeedParser.formatEnclosure(lnk4, 0);

        // Empty
        assertEnclosures(null);
        assertEnclosures("");
        assertEnclosures(" ");

        // Not present
        assertEnclosures("Some text");
        
        // Present
        assertEnclosures("Some text " + enc1, lnk1);
        assertEnclosures("Some text " + enc1 + enc2, lnk1, lnk2);
        assertEnclosures("Some text " + enc1 + enc2 + enc3, lnk1, lnk2, lnk3);
        assertEnclosures("Some text " + enc1 + enc2 + enc3 + enc4, lnk1, lnk2, lnk3, lnk4);
    }

    private void assertEnclosures(String html, String ... targetEnclosures)
    {
        String[] res = EnclosureAutoSaver.getEnclosures(html);
        assertEquals(targetEnclosures.length, res.length);
        int i = 0;
        for (String enclosure : targetEnclosures)
        {
            assertEquals(enclosure, res[i++]);
        }
    }

    /** Tests converting an enclosure URL to the title. */
    public void testEnclosureToParts()
    {
        // Empty
        assertEncParts("", "", null);
        assertEncParts("", "", "");
        assertEncParts("", "", " ");
        assertEncParts("", "", "/");
        assertEncParts("", "", "bb/");
        assertEncParts("", "", "/bb/");

        // Simple
        assertEncParts("aa", "", "aa");
        assertEncParts("aa", ".mp3", " aa.mp3 ");

        // Relative
        assertEncParts("aa", "", "/aa");
        assertEncParts("aa", ".mp3", "/aa.mp3");
        assertEncParts("aa", "", "bb/aa");
        assertEncParts("aa", ".mp3", "bb/aa.mp3");
        assertEncParts("aa", "", "\\aa");
        assertEncParts("aa", ".mp3", "\\aa.mp3");
        assertEncParts("aa", "", "bb\\aa");
        assertEncParts("aa", ".mp3", "bb\\aa.mp3");

        // Absolute
        assertEncParts("aa", ".mp3", "http://test.com/bb/aa.mp3");
    }

    /**
     * Helper method to test extraction of a title and an extension.
     *
     * @param name  name.
     * @param ext   extension.
     * @param enc   source enclosure URL.
     */
    private void assertEncParts(String name, String ext, String enc)
    {
        EnclosureAutoSaver.EnclosureParts parts = EnclosureAutoSaver.enclosureToParts(enc);
        assertEquals(name, parts.name);
        assertEquals(ext, parts.extension);
    }
}
