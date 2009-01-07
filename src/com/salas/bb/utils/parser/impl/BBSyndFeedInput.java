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
// $Id: BBSyndFeedInput.java,v 1.1 2007/10/03 09:15:15 spyromus Exp $
//

package com.salas.bb.utils.parser.impl;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.WireFeedInput;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * Custom syndicated feed input that uses the <code>BBWireFeedInput</code> with custom
 * namespace context.
 */
public class BBSyndFeedInput extends SyndFeedInput
{
    /** Modified WireFeedInput. */
    private final WireFeedInput _feedInput;

    /**
     * Creates a syndicated feed input.
     */
    public BBSyndFeedInput()
    {
        this(false);
    }

    /**
     * Creates a syndicated feed input.
     *
     * @param validate validate or not.
     */
    public BBSyndFeedInput(boolean validate)
    {
        super(validate);

        _feedInput = new BBWireFeedInput();
    }

    @Override
    public void setXmlHealerOn(boolean heals)
    {
        _feedInput.setXmlHealerOn(heals);
    }

    @Override
    public boolean getXmlHealerOn()
    {
        return _feedInput.getXmlHealerOn();
    }

    @Override
    public SyndFeed build(File file) throws IOException, IllegalArgumentException, FeedException
    {
        return new SyndFeedImpl(_feedInput.build(file));
    }

    @Override
    public SyndFeed build(Reader reader) throws IllegalArgumentException, FeedException
    {
        return new SyndFeedImpl(_feedInput.build(reader));
    }

    @Override
    public SyndFeed build(InputSource is) throws IllegalArgumentException, FeedException
    {
        return new SyndFeedImpl(_feedInput.build(is));
    }

    @Override
    public SyndFeed build(org.w3c.dom.Document document) throws IllegalArgumentException, FeedException
    {
        return new SyndFeedImpl(_feedInput.build(document));
    }

    @Override
    public SyndFeed build(org.jdom.Document document) throws IllegalArgumentException, FeedException
    {
        return new SyndFeedImpl(_feedInput.build(document));
    }
}
