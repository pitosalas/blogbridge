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
// $Id: BloglinesImporter.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.opml;

import com.salas.bbutilities.opml.Importer;
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;

/**
 * Custom Bloglines OPML exports importer.
 */
public class BloglinesImporter extends Importer
{
    /**
     * Pre-processes the document before analyzing to straighten some minor format issues from known
     * providers.
     *
     * @param aDoc document which is just created from resource and will be parsed after this call
     *             finish.
     */
    protected void preprocessDocument(Document aDoc)
    {
        // Bloglines has Subscriptions node right within the body.
        // We don't need this node because we wish to have first-level
        // folders reflect in guides list. So, our goal to move
        // the children of the Subscriptions outline one level upper
        // and remove Subscriptions outline itself.

        if (aDoc == null) return;

        Element bodyEl = aDoc.getRootElement().getChild("body");
        Element subscriptionsEl = bodyEl.getChild("outline");
        List outlineEls = subscriptionsEl.getChildren("outline");

        bodyEl.removeContent(subscriptionsEl);
        int size = outlineEls.size();
        for (int i = 0; i < size; i++)
        {
            Element outline = (Element)outlineEls.get(0);
            subscriptionsEl.removeContent(outline);
            bodyEl.addContent(outline);
        }
    }
}
