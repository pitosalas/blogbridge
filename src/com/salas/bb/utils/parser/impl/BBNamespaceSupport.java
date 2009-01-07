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
// $Id: BBNamespaceSupport.java,v 1.2 2007/10/03 10:13:31 spyromus Exp $
//

package com.salas.bb.utils.parser.impl;

import com.salas.bb.utils.StringUtils;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;

/**
 * Namespace support that always returns some URI even though it hasn't directly defined in the
 * XML file. If it's a known namespace prefix, like widely used "dc", the implicit URI is returned.
 */
class BBNamespaceSupport extends NamespaceSupport
{
    /**
     * @see com.sun.org.apache.xerces.internal.xni.NamespaceContext#reset()
     */
    @Override
    public void reset()
    {
        super.reset();

        put("dc", "http://purl.org/dc/elements/1.1/");
        put("wfw", "http://wellformedweb.org/CommentAPI/");
        put("feedburner", "http://rssnamespace.org/feedburner/ext/1.0");

        // We can add 3 more before the fNamespace array overflows
    }

    /**
     * Registers a known prefix.
     *
     * @param prefix    prefix.
     * @param uri       URI.
     */
    private void put(String prefix, String uri)
    {
        // bind "xmlns" prefix to the XMLNS uri
        fNamespace[fNamespaceSize++] = prefix;
        fNamespace[fNamespaceSize++] = uri;

        fContext[fCurrentContext] = fNamespaceSize;
    }

    @Override
    public String getURI(String prefix)
    {
        String uri = super.getURI(prefix);
        
        if (uri == null && StringUtils.isNotEmpty(prefix))
        {
            uri = "http://localhost/" + prefix;
            declarePrefix(prefix, uri);
        }

        return uri;
    }
}
