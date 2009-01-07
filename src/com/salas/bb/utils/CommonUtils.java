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
// $Id: CommonUtils.java,v 1.11 2007/11/07 17:16:48 spyromus Exp $
//

package com.salas.bb.utils;

import com.salas.bb.utils.i18n.Strings;
import sun.net.www.protocol.http.Handler;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities which do not belong to any special group (strings, dates and etc).
 */
public final class CommonUtils
{
    private static final Logger LOG = Logger.getLogger(CommonUtils.class.getName());

    /**
     * Hidden utility class constructor.
     */
    private CommonUtils()
    {
    }

    /**
     * Tells if the objects are different. NULL different to any object, but two
     * NULL's are no different.
     *
     * @param obj1  object or NULL.
     * @param obj2  object or NULL.
     *
     * @return TRUE if objects are different.
     */
    public static boolean areDifferent(Object obj1, Object obj2)
    {
        obj1 = unrollURLs(obj1);
        obj2 = unrollURLs(obj2);

        return (obj1 == null && obj2 != null) ||
               (obj1 != null && (obj2 == null || !obj1.equals(obj2)));
    }

    /**
     * Returns string representation of object if it has <code>URL</code> type.
     *
     * @param obj   object.
     *
     * @return value.
     */
    private static Object unrollURLs(Object obj)
    {
        return (obj instanceof URL) ? obj.toString().toLowerCase() : obj;
    }

    /**
     * Copy from a stream to a file which can be found in the classpath.
     *
     * @param src  Path of source.
     * @param dest Path to destination file.
     *
     * @return boolean indicating whether copy succeeded.
     */
    public static boolean copyResourceToFile(final String src, final String dest)
    {
        boolean result = false;
        InputStream srcStream = CommonUtils.class.getClassLoader().getResourceAsStream(src);

        if (srcStream != null)
        {
            try
            {
                // Create channel on the destination
                FileOutputStream dstStream = new FileOutputStream(dest);
                int ch; // the buffer
                while ((ch = srcStream.read()) != -1)
                {
                    dstStream.write(ch);
                }
                srcStream.close();
                dstStream.close();
                result = true;
            } catch (IOException e)
            {
                result = false;
            }
        }

        return result;
    }

    /**
     * Puts URLs into system clipboard. If <code>hrefFormat</code> is set to <code>TRUE</code>
     * the link will be surrounded with &lt;a href=&quote;...&quote;&gt;.
     *
     * @param urls          URLs to put.
     * @param hrefFormat    TRUE for HREF format.
     */
    public static void copyURLsToClipboard(URL[] urls, boolean hrefFormat)
    {
        if (urls == null || urls.length == 0) return;

        java.util.List<String> links = new ArrayList<String>();
        for (URL url : urls)
        {
            if (url == null) continue;

            String link = url.toString();
            if (hrefFormat) link = "<a href=\"" + link + "\">";

            links.add(link);
        }

        copyTextToClipboard(StringUtils.join(links.iterator(), "\n"));
    }

    /**
     * Puts some text to system clipboard.
     *
     * @param text text to put.
     */
    public static void copyTextToClipboard(String text)
    {
        if (text != null)
        {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection s = new StringSelection(text);
            try
            {
                clipboard.setContents(s, s);
            } catch (IllegalStateException e)
            {
                // The clipboard is unavailable.
            }
        }
    }

    /**
     * Returns the text in the clipboard or <code>NULL</code> if the clipboard is
     * clear, the contents are unavailable or there was an error.
     *
     * @return the text or <code>NULL</code>.
     */
    public static String getTextFromClipboard()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents;
        try
        {
            contents = clipboard.getContents(null);
        } catch (IllegalStateException e)
        {
            // The clipboard is unavailable:
            // - no clipboard on a system
            // - data of unknown format inside
            contents = null;
        }

        String text = null;

        if (contents != null)
        {
            try
            {
                if (contents.isDataFlavorSupported(DataFlavor.stringFlavor))
                {
                    text = (String)contents.getTransferData(DataFlavor.stringFlavor);
                }
            } catch (UnsupportedFlavorException e)
            {
                LOG.log(Level.SEVERE, Strings.error("string.flavor.was.reported.as.unsupported"), e);
            } catch (IOException e)
            {
                LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
            }
        }

        return text;
    }

    public static URL intern(URL u)
    {
        if (u == null) return u;

        try
        {
//            String f = u.getFile();
//            String r = u.getRef();
//            if (f != null && r != null) f += "#" + r;
            
            URL url = new URL(u.getProtocol().intern(), u.getHost(), u.getPort(), "", InternHTTPHandler.INSTANCE);
            InternHTTPHandler.update(url, u);
            u = url;
        } catch (MalformedURLException e)
        {
            // Ignore
        }
        
        return u;
    }


    private static class InternHTTPHandler extends Handler
    {
        private final static InternHTTPHandler INSTANCE = new InternHTTPHandler();

        public static void update(URL u, URL from)
        {
            INSTANCE.setURL(u, from.getProtocol(), from.getHost(), from.getPort(), from.getAuthority(),
                from.getUserInfo(), from.getPath(), from.getQuery(), from.getRef());
        }

        @Override
        protected void setURL(URL u, String protocol, String host, int port,
                              String authority, String userInfo, String path, String query, String ref)
        {
            host = StringUtils.intern(host);
            authority = StringUtils.intern(authority);
            userInfo = StringUtils.intern(userInfo);
            path = StringUtils.intern(path);
            query = StringUtils.intern(query);
            ref = StringUtils.intern(ref);

            super.setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
        }
    }
}
