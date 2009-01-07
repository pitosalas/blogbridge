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
// $Id: IFeedParser.java,v 1.4 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.utils.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface for feed parsers. The parser can be given some URL of XML resource and
 * it will return the result of parsing operation (sucessful outcome) or
 * exception telling the details of failure.
 */
public interface IFeedParser
{
    /**
     * Parses the resource by the given URL and returns the objects.
     *
     * @param xmlURL            XML URL of the resource.
     * @param title             feed title (if known).
     * @param lastUpdateTime    time of last update (server time-zone) or (-1) if not known.
     *
     * @return result.
     *
     * @throws FeedParserException  in case of any problems with parsing.
     * @throws NullPointerException if the URL is NULL.
     * @throws IOException          if there's a problem with reading feed.
     */
    FeedParserResult parse(URL xmlURL, String title, long lastUpdateTime)
        throws FeedParserException, IOException;

    /**
     * Parses the resource presented by a stream and returns the objects.
     *
     * @param stream    XML stream.
     * @param rootURL   URL for the relative links resolution.
     *
     * @return result.
     *
     * @throws com.salas.bb.utils.parser.FeedParserException  in case of any problems with parsing.
     * @throws NullPointerException if the URL is NULL.
     * @throws java.io.IOException  if there's a problem with reading feed.
     */
    FeedParserResult parse(InputStream stream, URL rootURL)
        throws IOException, FeedParserException;
}
