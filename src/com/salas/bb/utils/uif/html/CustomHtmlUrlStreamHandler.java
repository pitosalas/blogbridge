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
// $Id: CustomHtmlUrlStreamHandler.java,v 1.4 2006/01/08 05:10:10 kyank Exp $
//

package com.salas.bb.utils.uif.html;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;

/**
 * Tweaked <code>URLStreamHandler</code> for loading images of <code>CusomeImageView</code>.
 * It overrides comparison methods to not to get InetAddress of URL, but compare the stringified
 * versions of URL's, assuming that we will not hit many situations of referring the same
 * image file with different URL's. In the negative case we will have another image object
 * int the cache.
 */
class CustomHtmlUrlStreamHandler extends URLStreamHandler
{
    /**
     * Opens a connection to the object referenced by the
     * <code>URL</code> argument.
     * This method should be overridden by a subclass.
     * <p/>
     * <p>If for the handler's protocol (such as HTTP or JAR), there
     * exists a public, specialized URLConnection subclass belonging
     * to one of the following packages or one of their subpackages:
     * java.lang, java.io, java.util, java.net, the connection
     * returned will be of that subclass. For example, for HTTP an
     * HttpURLConnection will be returned, and for JAR a
     * JarURLConnection will be returned.
     *
     * @param u the URL that this connects to.
     * @return a <code>URLConnection</code> object for the <code>URL</code>.
     * @throws java.io.IOException if an I/O error occurs while opening the
     *                             connection.
     */
    public URLConnection openConnection(URL u) throws IOException
    {
        return new URL(u.toString()).openConnection();
    }

    /**
     * Provides the default hash calculation. May be overidden by handlers for
     * other protocols that have different requirements for hashCode
     * calculation.
     *
     * @param u a URL object
     * @return an <tt>int</tt> suitable for hash table indexing
     */
    protected int hashCode(URL u)
    {
        return u == null ? 0 : u.toString().hashCode();
    }

    /**
     * Compare two urls to see whether they refer to the same file, i.e., having the same protocol,
     * host, port, and path. This method requires that none of its arguments is null. This is
     * guaranteed by the fact that it is only called indirectly by java.net.URL class.
     *
     * @param u1 a URL object
     * @param u2 a URL object
     *
     * @return true if u1 and u2 refer to the same file
     */
    protected boolean sameFile(URL u1, URL u2)
    {
        String s1 = u1 == null ? null : u1.toString();
        String s2 = u2 == null ? null : u2.toString();

        return (s1 == null && s2 == null) ||
               (s1 != null && s2 != null && s1.equalsIgnoreCase(s2));
    }

    /**
     * Provides the default equals calculation. May be overidden by handlers for other protocols
     * that have different requirements for equals(). This method requires that none of its
     * arguments is null. This is guaranteed by the fact that it is only called by java.net.URL
     * class.
     *
     * @param u1 a URL object
     * @param u2 a URL object
     *
     * @return <tt>true</tt> if the two urls are considered equal, ie. they refer to the same
     *         fragment in the same file.
     */
    protected boolean equals(URL u1, URL u2)
    {
        return sameFile(u1, u2);
    }

    /**
     * Compares the host components of two URLs.
     *
     * @param u1 the URL of the first host to compare
     * @param u2 the URL of the second host to compare
     *
     * @return <tt>true</tt> if and only if they are equal, <tt>false</tt> otherwise.
     */
    protected boolean hostsEqual(URL u1, URL u2)
    {
        String h1 = u1 == null ? null : u1.getHost();
        String h2 = u2 == null ? null : u2.getHost();

        return (h1 == null && h2 == null) ||
               (h1 != null && h2 != null && h1.equalsIgnoreCase(h2));
    }
}
