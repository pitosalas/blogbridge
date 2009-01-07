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
// $Id: UrlDiscovererIF.java,v 1.3 2007/10/01 17:03:27 spyromus Exp $
//

package com.salas.bb.utils.discovery;

import java.net.URL;

/**
 * Common URL discoverer interface. URL Discoverers are intended to resolveURI
 * given source URL into valid XML URL ready for parsing in some specific ways.
 * <p/>
 * For example, some discoverers may use online web directories to get this information,
 * some may parse the source (HTML page) and find references in META records. There are
 * many more possible ways which can be implemented.
 */
public interface UrlDiscovererIF
{

    /**
     * Resolves source URL into corresponding XML URL ready for parsing.
     *
     * @param source source URL.
     *
     * @return result of discovery or <code>null</code> in case if URL cannot be resolved.
     *
     * @throws UrlDiscovererException in case of any errors.
     */
    DiscoveryResult discover(URL source)
        throws UrlDiscovererException;
}
