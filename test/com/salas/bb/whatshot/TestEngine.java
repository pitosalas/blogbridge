// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: TestEngine.java,v 1.1 2007/06/27 11:43:45 spyromus Exp $
//

package com.salas.bb.whatshot;

import junit.framework.TestCase;

import java.net.URL;

/** Tests {@link Engine}. */
public class TestEngine extends TestCase
{
    private Engine.HotLink link;
    private URL url;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        Engine.clearFilterCriteria();

        url = new URL("http://www.blogbridge.com/");
        link = new Engine.HotLink(url);
    }

    /** Tests matching empty link. */
    public void testMatchesFilters_EmptyLink()
    {
        assertFalse(Engine.matchesFilters(null));
    }

    /** Tests matching with no criteria. */
    public void testMatchesFilters_NoCriteria()
    {
        assertFalse(Engine.matchesFilters(link));
    }

    /** Tests matching. */
    public void testMatchesFilters_Match()
    {
        Engine.addFilterCriteria(new MatchHLC());
        assertTrue(Engine.matchesFilters(link));
    }

    /** Tests no matching. */
    public void testMatchesFilters_NoMatch()
    {
        Engine.addFilterCriteria(new NoMatchHLC());
        assertFalse(Engine.matchesFilters(link));
    }

    /**
     * Simple match criteria.
     */
    private class MatchHLC implements IHotLinkCriteria
    {
        /**
         * Returns <code>TRUE</code> if a hot link matches.
         *
         * @param link link.
         *
         * @return <code>TRUE</code> if a hot link matches.
         */
        public boolean matches(Engine.HotLink link)
        {
            return link.getLink() == url;
        }
    }

    /**
     * Simple no-match criteria.
     */
    private class NoMatchHLC implements IHotLinkCriteria
    {
        /**
         * Returns <code>TRUE</code> if a hot link matches.
         *
         * @param link link.
         *
         * @return <code>TRUE</code> if a hot link matches.
         */
        public boolean matches(Engine.HotLink link)
        {
            return false;
        }
    }
}
