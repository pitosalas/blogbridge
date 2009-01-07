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
// $Id: TestLinkResolver.java,v 1.3 2007/11/01 13:01:40 spyromus Exp $
//

package com.salas.bb.whatshot;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

/** Tests {@link com.salas.bb.whatshot.LinkResolver}. */
public class TestLinkResolver extends TestCase
{
    private URL link;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        link = new URL("http://www.technorati.com/tags/aaa");

        LinkResolver.clearPostProcessingInstructions();
        LinkResolver.clearCustomLinkResolvers();
    }

    /** Processing of an empty title. */
    public void testPostProcessTitle_EmptyTitle()
    {
        assertNull(LinkResolver.postprocessTitle(null));
        assertEquals(" ", LinkResolver.postprocessTitle(" "));
    }

    /** Processing when no instructions are given. */
    public void testPostProcessTitle_NoInstructions()
    {
        assertEquals("abc", LinkResolver.postprocessTitle("abc"));
    }

    /** Processing with one instruction. */
    public void testPostProcessTitle_Instruction()
    {
        LinkResolver.addPostProcessingInstruction(Pattern.compile("^a"), "c");

        assertEquals("cbc", LinkResolver.postprocessTitle("abc"));
    }

    /** Processing with instructions. */
    public void testPostProcessTitle_Instructions()
    {
        LinkResolver.addPostProcessingInstruction(Pattern.compile("^a"), "c");
        LinkResolver.addPostProcessingInstruction(Pattern.compile("b+"), ".");

        assertEquals("c.c", LinkResolver.postprocessTitle("abbc"));
    }

    /** Resolving NULL-link. */
    public void testCustomLinkResolution_EmptyLink()
    {
        assertNull(LinkResolver.customLinkResolution(null));
    }

    /** Resolving test URL without any rules. */
    public void testCustomLinkResolution_NoRules()
    {
        assertNull(LinkResolver.customLinkResolution(link));
    }

    /** Resolving test URL with a single no-match rule. */
    public void testCustomLinkResolution_OneRule()
    {
        LinkResolver.addCustomLinkResolver(new NoMatchCLR());
        assertNull(LinkResolver.customLinkResolution(link));
    }

    /** Resolving test URL with two rules one of which is a match. */
    public void testCustomLinkResolution_TwoRules()
    {
        LinkResolver.addCustomLinkResolver(new NoMatchCLR());
        LinkResolver.addCustomLinkResolver(new MatchCLR());
        assertEquals("abc", LinkResolver.customLinkResolution(link));
    }

    /**
     * Tests resolving unicode titles.
     *
     * @throws IOException never.
     */
    public void testResolveUnicodeTitles() throws IOException
    {
        InputStream is = new ByteArrayInputStream(new byte[] {
            (byte)0xe8, (byte)0xb0, (byte)0xb7, (byte)0xe6,
            (byte)0xad, (byte)0x8c, (byte)0xe6, (byte)0xb2,
            (byte)0xbb, (byte)0xe5, (byte)0x8d, (byte)0xb0 });
        LinkResolver res = new LinkResolver(new ILinkResolverListener() {
            public void onGroupResolved(HotResultGroup group) {
            }
        });
        String title = res.fetchTitle(is);
        assertEquals("\u8c37\u6b4c\u6cbb\u5370", title);
    }

    /** Always no match resolver. */
    private static class NoMatchCLR implements ICustomLinkResolver
    {
        public String resolve(URL link)
        {
            return null;
        }
    }

    /** Always match resolver. */
    private static class MatchCLR implements ICustomLinkResolver
    {
        public String resolve(URL link)
        {
            return "abc";
        }
    }
}
