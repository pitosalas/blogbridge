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
// $Id: TestGuidesSet.java,v 1.15 2006/01/16 10:27:25 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This suite contains tests for <code>GuidesSet</code> unit.
 */
public class TestGuidesSet extends TestCase
{
    private GuidesSet set;

    protected void setUp()
        throws Exception
    {
        set = new GuidesSet();
    }

    // ---------------------------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests the state of the set right after the construction.
     */
    public void testConstruction()
    {
        assertEquals("Set should be empty.", 0, set.getGuidesCount());
    }

    /**
     * Tests the addition of guides to the set.
     */
    public void testAdd()
    {
        DummyEmptyGuide guide = new DummyEmptyGuide();
        set.add(guide);

        assertEquals("Guide isn't added.", 1, set.getGuidesCount());
        assertTrue("Wrong guide is added.", guide == set.getGuideAt(0));
    }

    /**
     * Tests the addition of duplicate guides to the set.
     */
    public void testAddDuplicate()
    {
        DummyEmptyGuide guide = new DummyEmptyGuide();
        set.add(guide);
        set.add(guide);

        assertEquals("Guide isn't added.", 1, set.getGuidesCount());
        assertTrue("Wrong guide is added.", guide == set.getGuideAt(0));
    }

    /**
     * Tests handling of errors during addition.
     */
    public void testAddFailure()
    {
        try
        {
            set.add(null);
            fail("Guide should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests removing of the guides from the set.
     */
    public void testRemove()
    {
        DummyEmptyGuide guide = new DummyEmptyGuide();
        guide.setTitle("1");
        set.add(guide);
        set.add(new DummyEmptyGuide());

        set.remove(guide);
        assertEquals("Guide wasn't removed.", 1, set.getGuidesCount());
        assertFalse("Wrong guide was removed.", guide == set.getGuideAt(0));
    }

    /**
     * Tests removing guides wich doesn't belong to the set.
     */
    public void testRemoveEmpty()
    {
        DummyEmptyGuide guide = new DummyEmptyGuide();
        guide.setTitle("1");
        set.add(new DummyEmptyGuide());

        set.remove(guide);
        assertEquals("Something was removed. It shouldn't.", 1, set.getGuidesCount());
    }

    /**
     * Tests handling of errors during removal of guides.
     */
    public void testRemoveFail()
    {
        try
        {
            set.remove(null);
            fail("Guide should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests clearing the set.
     */
    public void testClear()
    {
        set.add(new StandardGuide());
        set.add(new StandardGuide());

        set.clear();
        assertEquals("Wrong number of guides.", 0, set.getGuidesCount());
    }

    /**
     * Tests getting the list of titles for all guides.
     */
    public void testGetGuidesTitles()
    {
        Set titles;

        // Empty set of titles -- not guides
        titles = set.getGuidesTitles();
        assertEquals("Should be not titles.", 0, titles.size());

        // There's one guide in the set
        StandardGuide guide = new StandardGuide();
        guide.setTitle("A");
        set.add(guide);

        titles = set.getGuidesTitles();
        assertEquals("Should be not titles.", 1, titles.size());
        assertEquals("Wrong title", "A", titles.toArray()[0]);

        // There's another guide with the same title
        StandardGuide guide2 = new StandardGuide();
        guide2.setTitle("A");
        set.add(guide2);

        titles = set.getGuidesTitles();
        assertEquals("Should be not titles.", 1, titles.size());
        assertTrue("Wrong title", titles.contains("A"));

        // There's third guide with different title
        StandardGuide guide3 = new StandardGuide();
        guide3.setTitle("B");
        set.add(guide3);

        titles = set.getGuidesTitles();
        assertEquals("Should be not titles.", 2, titles.size());
        assertTrue("Wrong title", titles.contains("A"));
        assertTrue("Wrong title", titles.contains("B"));
    }

    /**
     * Tests finding index of registered and unregistered guides.
     */
    public void testIndexOf()
    {
        IGuide guide1 = new StandardGuide();
        guide1.setTitle("1");
        set.add(guide1);

        assertEquals("Wrong index.", 0, set.indexOf(guide1));
        assertEquals("Wrong index.", -1, set.indexOf(new StandardGuide()));
    }

    /**
     * Tests handling of incorrect input for index finding method.
     */
    public void testIndexOfFail()
    {
        try
        {
            set.indexOf(null);
            fail("Guide should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests working of the counter when adding and removing feeds.
     */
    public void testGetGuidesCount()
    {
        assertEquals("Wrong initial count.", 0, set.getGuidesCount());

        DummyEmptyGuide guide = new DummyEmptyGuide();
        set.add(guide);
        assertEquals("Wrong count.", 1, set.getGuidesCount());

        set.remove(guide);
        assertEquals("Wrong count.", 0, set.getGuidesCount());
    }

    /**
     * Tests returning of guides list.
     */
    public void testGetStandardGuides()
    {
        IGuide[] guides;

        guides = set.getStandardGuides(null);
        assertEquals("List should be empty -- no guides.", 0, guides.length);

        StandardGuide guide = new StandardGuide();
        guide.setTitle("1");
        set.add(guide);
        guides = set.getStandardGuides(null);
        assertEquals("There should be guide in the list.", 1, guides.length);
        assertTrue("Wrong guide in the list.", guide == guides[0]);

        guides = set.getStandardGuides(guide);
        assertEquals("There should be no guide in the list.", 0, guides.length);

        StandardGuide guide2 = new StandardGuide();
        guide2.setTitle("2");
        set.add(guide2);
        guides = set.getStandardGuides(guide);
        assertEquals("There should be guide in the list.", 1, guides.length);
        assertTrue("Wrong guide in the list.", guide2 == guides[0]);
    }

    /**
     * Tests returning the list of used icon keys.
     */
    public void testGetGuidesIconKeys()
    {
        IGuide guide1 = new DummyEmptyGuide();
        guide1.setIconKey("1");
        IGuide guide2 = new DummyEmptyGuide();
        guide2.setIconKey("1");
        IGuide guide3 = new DummyEmptyGuide();
        guide3.setIconKey("3");

        // No guides
        Collection keys;
        keys = set.getGuidesIconKeys();
        assertEquals("There's no guides, no keys.", 0, keys.size());

        // Add some guides
        set.add(guide1);
        set.add(guide2);
        set.add(guide3);

        keys = set.getGuidesIconKeys();
        assertEquals("Wrong number of keys.", 2, keys.size());
        assertTrue("Missing key.", keys.contains("1"));
        assertTrue("Missing key.", keys.contains("3"));
    }

    /**
     * Tests setting as read/unread the guides in the set.
     */
    public void testSetRead()
    {
        IGuide guide1 = new DummyEmptyGuide();
        guide1.setTitle("1");
        IGuide guide2 = new DummyEmptyGuide();
        guide1.setTitle("2");

        set.add(guide1);
        set.add(guide2);

        set.setRead(true);
        assertTrue("Wrong state.", guide1.isRead());
        assertTrue("Wrong state.", guide2.isRead());

        set.setRead(false);
        assertFalse("Wrong state.", guide1.isRead());
        assertFalse("Wrong state.", guide2.isRead());

        set.setRead(true);
        assertTrue("Wrong state.", guide1.isRead());
        assertTrue("Wrong state.", guide2.isRead());
    }

    /**
     * Tests the finding of first feed who's XML URL matches the given.
     */
    public void testFindFirstFeedByXmlURLSingleGuide()
        throws MalformedURLException
    {
        DirectFeed feed0 = new DirectFeed();
        feed0.setBaseTitle("0");
        feed0.setXmlURL(new URL("file://0"));
        DirectFeed feed1 = new DirectFeed();
        feed1.setBaseTitle("1");
        feed1.setXmlURL(new URL("file://test"));
        DirectFeed feed2 = new DirectFeed();
        feed2.setBaseTitle("2");
        feed2.setXmlURL(new URL("file://test"));

        StandardGuide guide1 = new StandardGuide();
        guide1.add(feed0);
        guide1.add(feed1);
        guide1.add(feed2);

        set.add(guide1);

        NetworkFeed feed = set.findDirectFeed(new URL("file://test"));
        assertTrue("Wrong feed found: " + feed, feed == feed1);
    }

    /**
     * Tests the finding of first feed who's XML URL matches the given.
     */
    public void testFindFirstFeedByXmlURLSecondGuide()
        throws MalformedURLException
    {
        DirectFeed feed0 = new DirectFeed();
        feed0.setBaseTitle("0");
        feed0.setXmlURL(new URL("file://0"));
        DirectFeed feed1 = new DirectFeed();
        feed1.setBaseTitle("1");
        feed1.setXmlURL(new URL("file://test"));
        DirectFeed feed2 = new DirectFeed();
        feed2.setBaseTitle("2");
        feed2.setXmlURL(new URL("file://test"));

        StandardGuide guide1 = new StandardGuide();
        guide1.add(feed0);
        StandardGuide guide2 = new StandardGuide();
        guide2.add(feed1);
        guide2.add(feed2);

        set.add(guide1);
        set.add(guide2);

        NetworkFeed feed = set.findDirectFeed(new URL("file://test"));
        assertTrue("Wrong feed found: " + feed, feed == feed1);
    }

    /**
     * Tests the finding of first feed who's XML URL matches the given.
     */
    public void testFindFirstFeedByXmlURLNoMatch()
        throws MalformedURLException
    {
        DirectFeed feed0 = new DirectFeed();
        feed0.setBaseTitle("0");
        feed0.setXmlURL(new URL("file://0"));

        StandardGuide guide1 = new StandardGuide();
        guide1.add(feed0);

        set.add(guide1);

        NetworkFeed feed = set.findDirectFeed(new URL("file://test"));
        assertNull("Feed should not be found.", feed);
    }

    /**
     * Tests finding the guides by their title.
     */
    public void testFindGuidesByTitle()
    {
        IGuide guide1 = new DummyEmptyGuide();
        guide1.setTitle("1");
        guide1.setIconKey("a");
        IGuide guide2 = new DummyEmptyGuide();
        guide2.setTitle("2");
        IGuide guide3 = new DummyEmptyGuide();
        guide3.setTitle("1");
        guide1.setIconKey("b");

        // Empty set
        Collection guides;
        guides = set.findGuidesByTitle("1");
        assertEquals("Set is empty.", 0, guides.size());

        // Add guides with similar titles
        set.add(guide1);
        set.add(guide2);
        set.add(guide3);
        guides = set.findGuidesByTitle("1");
        assertEquals("Wrong number of guides in result.", 2, guides.size());
        assertTrue("Missing guide.", guides.contains(guide1));
        assertTrue("Missing guide.", guides.contains(guide3));
    }

    /**
     * Tests handling of the incorrect input.
     */
    public void testFindGuidesByTitleFail()
    {
        try
        {
            set.findGuidesByTitle(null);
            fail("Title should be specified. NPE is expected.");
        } catch (Exception e)
        {
            // Expected
        }
    }

    /**
     * Tests the relocation of guides.
     */
    public void testRelocateGuide()
    {
        IGuide guide1 = new DummyEmptyGuide();
        guide1.setTitle("1");
        IGuide guide2 = new DummyEmptyGuide();
        guide2.setTitle("2");

        // Add one guide and relocate it to the same position
        set.add(guide1);
        set.relocateGuide(guide1, 0);

        assertEquals("Wrong guides count.", 1, set.getGuidesCount());
        assertTrue("Wrong guide.", set.getGuideAt(0) == guide1);

        // Add another guide and check two relocations: back and forth
        set.add(guide2);
        set.relocateGuide(guide1, 1);

        assertEquals("Wrong guides count.", 2, set.getGuidesCount());
        assertTrue("Wrong guide.", set.getGuideAt(0) == guide2);
        assertTrue("Wrong guide.", set.getGuideAt(1) == guide1);

        set.relocateGuide(guide1, 0);

        assertEquals("Wrong guides count.", 2, set.getGuidesCount());
        assertTrue("Wrong guide.", set.getGuideAt(0) == guide1);
        assertTrue("Wrong guide.", set.getGuideAt(1) == guide2);
    }

    /**
     * Tests the relocation failures because of IOOB.
     */
    public void testRelocateGuideFailureIOOB()
    {
        IGuide guide1 = new DummyEmptyGuide();

        set.add(guide1);

        try
        {
            set.relocateGuide(guide1, 1);
            fail("Relocation position is out of the list.");
        } catch (IndexOutOfBoundsException e)
        {
            // Expected
        }

        try
        {
            set.relocateGuide(guide1, -1);
            fail("Relocation position is out of the list.");
        } catch (IndexOutOfBoundsException e)
        {
            // Expected
        }
    }

    /**
     * Tests the relocation failures because of unspecified guide.
     */
    public void testRelocateGuideFailureNull()
    {
        try
        {
            set.relocateGuide(null, 1);
            fail("Guide should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests the relocation failures because of guide belonging to different set.
     */
    public void testRelocateGuideFailureBelonging()
    {
        try
        {
            set.relocateGuide(new DummyEmptyGuide(), 1);
            fail("Guide does not belong to this set. IA expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    public void testGetFeedsXmlURLs()
        throws MalformedURLException
    {
        assertEquals("Please remove all guides from set by this point.",
            0, set.getGuidesCount());

        StandardGuide guide1 = new StandardGuide();
        guide1.setTitle("1");
        StandardGuide guide2 = new StandardGuide();
        guide2.setTitle("2");

        DirectFeed feed1 = new DirectFeed();
        feed1.setXmlURL(new URL("file://1"));
        DirectFeed feed2 = new DirectFeed();
        feed2.setXmlURL(new URL("file://2"));
        DirectFeed feed3 = new DirectFeed();
        feed3.setXmlURL(new URL("file://3"));
        DirectFeed feed4 = new DirectFeed();
        feed4.setXmlURL(new URL("file://1"));
        SearchFeed feed5 = new SearchFeed();
        feed5.setBaseTitle("5");

        guide1.add(feed1);
        guide1.add(feed5);
        guide2.add(feed2);
        guide2.add(feed3);
        guide2.add(feed4);

        set.add(guide1);
        set.add(guide2);

        Collection urls = set.getFeedsXmlURLs();
        assertEquals("URL's of network feeds (de-duplicated) should be returned.",
            3, urls.size());
        assertTrue(urls.contains(feed1.getXmlURL()));
        assertTrue(urls.contains(feed2.getXmlURL()));
        assertTrue(urls.contains(feed3.getXmlURL()));
    }
    
    /**
     * Tests returning of XML URL's collection with empty guides list.
     */
    public void testGetFeedsXmlURLsEmpty()
    {
        assertEquals("Please remove all guides from set by this point.",
            0, set.getGuidesCount());

        assertEquals("There should be empty collection of XML URL's.",
            0, set.getFeedsXmlURLs().size());
    }
}
