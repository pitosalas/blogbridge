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
// $Id: TestBloglinesImporter.java,v 1.2 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.opml;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;

/**
 * This suite contains tests for <code>BloglinesImporter</code> unit.
 */
public class TestBloglinesImporter extends TestCase
{
    /**
     * Tests bloglines specific preprocessing.
     */
    public void testPreprocessDocument()
    {
        // Prepare the hierarchy
        // root
        //   body
        //     outline   <- subscriptions
        //       outline   <- guide
        //         outline    <- guide feed
        //       outline   <- root feed

        Element root = new Element("opml");
        Document doc = new Document(root);

        Element body = new Element("body");
        Element subscriptions = new Element("outline");
        Element rootFeed = new Element("outline");
        rootFeed.setAttribute("type", "rss");
        Element guide = new Element("outline");
        Element guideFeed = new Element("outline");

        guide.addContent(guideFeed);
        subscriptions.addContent(guide);
        subscriptions.addContent(rootFeed);
        body.addContent(subscriptions);
        root.addContent(body);

        // Preprocess
        BloglinesImporter imp = new BloglinesImporter();
        imp.preprocessDocument(doc);

        // Verify the structure
        // root
        //   body
        //     outline   <-- guide
        //       outline   <-- guide feed
        //     outline   <- root feed

        Element dRoot = doc.getRootElement();
        assertTrue("Wrong root element.", root == dRoot);
        Element dBody = dRoot.getChild("body");
        assertTrue("Wrong body element.", body == dBody);
        List rootOutlines = dBody.getChildren("outline");
        assertEquals("Wrong number of outlines. Should be root feed and guide.",
            2, rootOutlines.size());
        assertTrue("Should be guide.", guide == rootOutlines.get(0));
        assertTrue("Should be root feed.", rootFeed == rootOutlines.get(1));
        assertTrue("Should have feed.", guideFeed == guide.getChild("outline"));
    }
}
