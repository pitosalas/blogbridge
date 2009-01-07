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
// $Id: TestAtom10ParserV2.java,v 1.1 2007/10/01 17:03:27 spyromus Exp $
//

package com.salas.bb.utils.parser.impl;

import junit.framework.TestCase;

import java.net.URISyntaxException;

/**
 * Tests Atom 1.0 V2 parser.
 */
public class TestAtom10ParserV2 extends TestCase
{
    /**
     * Resolving NULL links.
     *
     * @throws URISyntaxException if error.
     */
    public void testFormURI_NULL() throws URISyntaxException
    {
        assertEquals("/a", Atom10ParserV2.formURI(null, "/a"));
        assertEquals("/a", Atom10ParserV2.formURI("/a", null));
        assertNull(Atom10ParserV2.formURI(null, null));
    }

    /**
     * Resolving two relative links.
     *
     * @throws URISyntaxException if error.
     */
    public void testFormURI_RelativeRelative() throws URISyntaxException
    {
        assertEquals("/b", Atom10ParserV2.formURI("/a", "b"));
    }

    /**
     * Resolving absolute-relative combination.
     * 
     * @throws URISyntaxException if error.
     */
    public void testFormURI_AbsoluteRelative() throws URISyntaxException
    {
        assertEquals("http://localhost/blog/abc", Atom10ParserV2.formURI("http://localhost/blog/", "abc"));
        assertEquals("http://localhost/abc", Atom10ParserV2.formURI("http://localhost/blog", "abc"));
    }

    /**
     * Resolving relative-absolute combination.
     *
     * @throws URISyntaxException if error.
     */
    public void testFormURI_RelativeAbsolute() throws URISyntaxException
    {
        assertEquals("http://localhost/blog/", Atom10ParserV2.formURI("abc", "http://localhost/blog/"));
    }

    /**
     * Resolving absolute-absolute combination.
     *
     * @throws URISyntaxException if error.
     */
    public void testFormURI_AbsoluteAbsolute() throws URISyntaxException
    {
        assertEquals("http://localhost/blog/", Atom10ParserV2.formURI("http://alpha/", "http://localhost/blog/"));
    }

    /**
     * Resolving absolute-relative-2 combination.
     *
     * @throws URISyntaxException if error.
     */
    public void testFormURI_AbsoluteRelative2() throws URISyntaxException
    {
        assertEquals("http://localhost/a", Atom10ParserV2.formURI("http://localhost/blog/", "../a"));
    }
}
