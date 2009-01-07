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
// $Id $
//

package com.salas.bb.discovery.filter;

import com.salas.bb.utils.StringUtils;

import java.net.URL;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Filter which is based on checking the extensions of files being asked
 * by the URL.
 */
public class ExtensionURLFilter implements IURLFilter
{
    private final Pattern pattern;

    /**
     * Creates extension filter for the given list of extensions.
     *
     * @param extensions extensions.
     */
    public ExtensionURLFilter(String extensions)
    {
        this(stringToExtensions(extensions));
    }

    /**
     * Creates extension filter for the given list of extensions.
     *
     * @param extensions extensions.
     */
    public ExtensionURLFilter(String[] extensions)
    {
        String pat = extensionsToPattern(extensions);
        pattern = pat == null ? null : Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Converts extensions to pattern.
     *
     * @param extensions the list of file extensions.
     *
     * @return pattern.
     */
    static String extensionsToPattern(String[] extensions)
    {
        // If there's nothing given...
        if (extensions == null) return null;

        // Remove empty strings and null's
        List deNulled = new ArrayList(extensions.length);
        for (int i = 0; i < extensions.length; i++)
        {
            String extension = extensions[i];
            if (extension != null && extension.trim().length() > 0) deNulled.add(extension);
        }
        extensions = (String[])deNulled.toArray(new String[deNulled.size()]);

        // If there's nothing left...
        if (extensions.length == 0) return null;

        // Convert into the pattern
        String ext = StringUtils.join(extensions, "|");
        StringBuffer pat = new StringBuffer("\\.(");
        pat.append(ext).append(")$");

        return pat.toString();
    }

    /**
     * Checks if the URL matches the filter.
     *
     * @param url URL to check.
     *
     * @return <code>TRUE</code> if the url matches filter.
     */
    public boolean matches(URL url)
    {
        boolean matches = false;

        Pattern pat = getPattern();
        if (url != null && pat != null)
        {
            String path = url.getPath();
            matches = pat.matcher(path).find();
        }

        return matches;
    }

    /**
     * Returns the pattern to use for matching.
     *
     * @return pattern.
     */
    protected Pattern getPattern()
    {
        return pattern;
    }

    /**
     * Convert the string with the list of extensions into the extensions list object.
     *
     * @param ext extensions.
     *
     * @return the list.
     */
    static String[] stringToExtensions(String ext)
    {
        if (ext == null || ext.trim().length() == 0) return null;

        // Split using comma as a delimeter
        String[] ext1 = StringUtils.split(ext.trim(), ",");

        List extensions = new ArrayList();

        // Scan through the results and split each of them into space-delimetered blocks
        for (int i = 0; i < ext1.length; i++)
        {
            String str = ext1[i];
            String[] ext2 = StringUtils.split(str.trim());
            for (int j = 0; j < ext2.length; j++)
            {
                extensions.add(ext2[j]);
            }
        }

        return (String[])extensions.toArray(new String[extensions.size()]);
    }
}
