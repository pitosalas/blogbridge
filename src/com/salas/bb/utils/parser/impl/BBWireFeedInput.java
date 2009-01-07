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
// $Id: BBWireFeedInput.java,v 1.2 2007/10/03 10:13:31 spyromus Exp $
//

package com.salas.bb.utils.parser.impl;

import com.sun.org.apache.xerces.internal.impl.Constants;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.syndication.io.SAXBuilder;
import com.sun.syndication.io.WireFeedInput;
import org.jdom.JDOMException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Wire feed input that specifies a custom Namespace property.
 */
class BBWireFeedInput extends WireFeedInput
{
    @Override
    protected SAXBuilder createSAXBuilder()
    {
        SAXBuilder builder = super.createSAXBuilder();
        try
        {
            XMLReader parser = builder.createParser();

            try
            {
                String prop = Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
                NamespaceSupport nsContext = new BBNamespaceSupport();

                builder.setProperty(prop, nsContext);
                parser.setProperty(prop, nsContext);
            } catch (SAXNotRecognizedException e)
            {
                // ignore
            } catch (SAXNotSupportedException e)
            {
                // ignore
            }
        } catch (JDOMException e)
        {
            throw new IllegalStateException("JDOM could not create a SAX parser");
        }

        return builder;
    }
}
