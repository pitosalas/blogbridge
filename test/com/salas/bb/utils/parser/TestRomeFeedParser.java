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
// $Id: TestRomeFeedParser.java,v 1.5 2007/10/03 09:15:15 spyromus Exp $
//

package com.salas.bb.utils.parser;

import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.TUtils;
import junit.framework.TestCase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Tests some feeds against the feed parser.
 */
public class TestRomeFeedParser extends TestCase
{
    /** Localhost URL. */
    private final static URL LOCALHOST;

    /** Data folder. */
    private File data;

    static
    {
        URL url = null;

        try
        {
            url = new URL("http://localhost/");
        } catch (MalformedURLException e)
        {
            // Never happens
        }

        LOCALHOST = url;
    }

    /**
     * Prepares the environment.
     *
     * @throws Exception in case of an error.
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        data = TUtils.getTestDataPath();
    }

    /**
     * Tests links recognition in sam ruby feed.
     *
     * @throws Exception if error.
     */
    public void testSamRuby() throws Exception
    {
        Channel chan = parseAndCheck("sam-ruby.atom", 20);

        // Check the first item
        Item item = chan.getItemAt(0);
        URL link = item.getLink();
        assertEquals("http://intertwingly.net/blog/2007/09/30/Etag-vs-Encoding", link.toString());
    }

    /**
     * Tests parsing a gimp feed.
     *
     * @throws Exception if error.
     */
    public void testGimp() throws Exception
    {
        Channel chan = parseAndCheck("gimp.rdf", 7);

        // Check the first item
        Item item = chan.getItemAt(0);
        assertEquals("Third Release Candidate for GIMP 2.4", item.getTitle());
        assertEquals("http://www.gimp.org", item.getLink().toString());

        GregorianCalendar cal = new GregorianCalendar(2007, Calendar.SEPTEMBER, 24, 1, 24, 18);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(cal.getTime(), item.getPublicationDate());
        assertEquals("If you", item.getText());
    }

    /**
     * Tests parsing JavaMagazin.de feed.
     *
     * @throws Exception if error.
     */
    public void testJavaMagazinDe() throws Exception
    {
        Channel chan = parseAndCheck("javamagazin_de.xml", 15);

        for (int i = 0; i < chan.getItemsCount(); i++)
        {
            Item item = chan.getItemAt(i);
            assertNotNull(item.getPublicationDate());
            assertFalse("Articles need pub. dates.",
                DateUtils.isToday(item.getPublicationDate()));
        }
    }

    /**
     * Tests parsing PhysicsToday feed. (BT#320)
     *
     * @throws Exception if error.
     */
    public void testPhysicsToday() throws Exception
    {
        Channel chan = parseAndCheck("physics-today.xml", 40);

        for (int i = 0; i < chan.getItemsCount(); i++)
        {
            Item item = chan.getItemAt(i);
            assertTrue(StringUtils.isNotEmpty(item.getTitle()));
            assertTrue(StringUtils.isNotEmpty(item.getText()));
            assertNotNull(item.getLink());
        }
    }

    /**
     * Tests parsing undefined namespaces feed. (BT#273)
     *
     * @throws Exception if error.
     */
    public void testLiveJournal() throws Exception
    {
        Channel chan = parseAndCheck("lj.atom", 1);

        for (int i = 0; i < chan.getItemsCount(); i++)
        {
            Item item = chan.getItemAt(i);
            assertTrue(StringUtils.isNotEmpty(item.getTitle()));
            assertTrue(StringUtils.isNotEmpty(item.getText()));
            assertNotNull(item.getLink());
        }
    }

    /**
     * Testing simple links of Atom 1.0.
     *
     * @throws Exception error.
     */
    public void testAtom10_Links() throws Exception
    {
        Channel chan = parseAndCheck("atom_1.0.xml", 2);

        assertEquals("http://example.com/blog", chan.getSiteURL().toString());

        // Check items
        assertEquals("http://example.com/blog/entry2", chan.getItemAt(0).getLink().toString());
        assertEquals("http://example.com/blog/entry1", chan.getItemAt(1).getLink().toString());
    }
    
    /**
     * Testing many versions of links.
     *
     * @throws Exception error.
     */
    public void testAtom10B_Links() throws Exception
    {
        Channel chan = parseAndCheck("atom_1.0_b.xml", 16);
        assertEquals("http://example.org/tests/base/result.html", chan.getSiteURL().toString());

        for (int i = 0; i < chan.getItemsCount(); i++)
        {
            assertEquals("http://example.org/tests/base/result.html", chan.getItemAt(i).getLink().toString());
        }
    }

    /**
     * Testing Tim Bray style feed.
     *
     * @throws Exception error.
     */
    public void testAtom10Bray_Links() throws Exception
    {
        Channel chan = parseAndCheck("atom_1.0_bray.xml", 2);
        assertEquals("http://www.example.com/blog/", chan.getSiteURL().toString());

        assertEquals("http://www.example.com/blog/2006-11-05/entry1", chan.getItemAt(0).getLink().toString());
        assertEquals("http://www.example.com/blog/2006-11-02/entry2", chan.getItemAt(1).getLink().toString());
    }

    /**
     * Testing Sam Ruby style feed.
     *
     * @throws Exception error.
     */
    public void testAtom10Ruby_Links() throws Exception
    {
        Channel chan = parseAndCheck("atom_1.0_ruby.xml", 2);
        assertEquals("http://www.example.com/blog/", chan.getSiteURL().toString());

        assertEquals("http://www.example.com/blog/bloggy-blog", chan.getItemAt(0).getLink().toString());
        assertEquals("http://www.example.com/frog/froggy-frog", chan.getItemAt(1).getLink().toString());
    }

    /**
     * Parses a feed and makes initial checks.
     *
     * @param feed          feed name to parse.
     * @param itemsCount    number of target items.
     *
     * @return channel.
     *
     * @throws IOException          I/O error.
     * @throws FeedParserException  parsing error.
     */
    private Channel parseAndCheck(String feed, int itemsCount)
        throws IOException, FeedParserException
    {
        // Parse
        FeedParserResult res = parse(feed);
        assertNotNull(res);

        // Check channel
        Channel chan = res.getChannel();
        assertNotNull(chan);
        assertEquals(itemsCount, chan.getItemsCount());

        return chan;
    }

    /**
     * Parses a feed given by the name of the file in 'test/data/test-feeds' folder.
     *
     * @param feed feed file name.
     *
     * @return feed parsing result.
     *
     * @throws IOException          I/O error.
     * @throws FeedParserException  parsing error.
     */
    private FeedParserResult parse(String feed) throws IOException, FeedParserException
    {
        // Initialize the stream for the test feed
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(feed(feed)));

        // Create a result object
        FeedParserResult res = new FeedParserResult();

        // Parse
        RomeFeedParser rfp = new RomeFeedParser();
        return rfp.parse(in, res, LOCALHOST);
    }

    /**
     * Returns the feed file for the feed.
     *
     * @param name name.
     *
     * @return file.
     */
    private File feed(String name)
    {
        return new File(data, "test-feeds/" + name);
    }

}
