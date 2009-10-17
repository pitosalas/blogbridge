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
// $Id: TestImportGuidesAction.java,v 1.39 2007/05/09 14:17:57 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.*;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.IProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.domain.utils.GuideIcons;
import com.salas.bbutilities.opml.objects.DefaultOPMLFeed;
import com.salas.bbutilities.opml.objects.DirectOPMLFeed;
import com.salas.bbutilities.opml.objects.OPMLGuide;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @see ImportGuidesAction
 */
public class TestImportGuidesAction extends TestCase
{
    private boolean dbInitialized = false;
    private static final int NUM_CHANNELS = 3;

    protected void setUp() throws Exception
    {
        GlobalModel gm = new GlobalModel(null);
        GlobalModel.setSINGLETON(gm);
        ResourceUtils.setBundlePath("Resource");
    }

    /**
     * @see ImportGuidesAction#countFeeds
     */
    public void testCountChannels()
    {
        final OPMLGuide emptyGuide = new OPMLGuide("test", null, false, null, null, false, 0, false, false, false);

        final OPMLGuide guide1 = new OPMLGuide("guide1", null, false, null, null, false, 0, false, false, false);
        final ArrayList<DefaultOPMLFeed> al1 = new ArrayList<DefaultOPMLFeed>();
        al1.add(new DirectOPMLFeed("1", "1", "1", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        guide1.setFeeds(al1);

        final OPMLGuide[] guides = {emptyGuide, guide1};

        assertEquals(1, ImportGuidesAction.countFeeds(guides));
    }

    /**
     * @see ImportGuidesAction#appendGuides
     */
    public void testAppendGuides()
        throws Exception
    {
        initDB();

        // Setup guides for appending
        final OPMLGuide g1, g2;
        final ArrayList<DefaultOPMLFeed> a1, a2;

        g1 = new OPMLGuide("g1", "icon1", false, null, null, false, 0, false, false, false);
        a1 = new ArrayList<DefaultOPMLFeed>();
        a1.add(new DirectOPMLFeed("1", "file://1", "file://1", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a1.add(new DirectOPMLFeed("2", "file://2", "file://2", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        g1.setFeeds(a1);

        g2 = new OPMLGuide("g2", null, false, null, null, false, 0, false, false, false);
        a2 = new ArrayList<DefaultOPMLFeed>();
        a2.add(new DirectOPMLFeed("3", "file://3", "file://3", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a2.add(new DirectOPMLFeed("4", "file://4", "file://4", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        g2.setFeeds(a2);

        final OPMLGuide[] guides = {g1, g2};

        // Put in ChannelGuideSet guide with duplicate name to check how dedupe works
        GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        final StandardGuide guide = new StandardGuide();
        guide.setTitle("g1");
        cgs.add(guide);

        // Append guides
        ImportGuidesAction.appendGuides(null, guides, cgs);

        // Check
        assertEquals(NUM_CHANNELS, cgs.getGuidesCount());

        String[] iconsNames = GuideIcons.getIconsNames();

        // Guide 0
        IGuide guide0 = cgs.getGuideAt(0);
        assertEquals("g1", guide0.getTitle());
        assertNull("Icon name wasn't specified.", guide0.getIconKey());
        assertEquals(0, guide0.getFeedsCount());

        // Guide 1
        final IGuide guide1 = cgs.getGuideAt(1);
        assertEquals("g1_2", guide1.getTitle());
        assertEquals("icon1", guide1.getIconKey());
        assertEquals(2, guide1.getFeedsCount());

        final DirectFeed pcge1 = (DirectFeed)guide1.getFeedAt(0);
        assertEquals("file://1", pcge1.getXmlURL().toString());
        assertEquals("file://1", pcge1.getSiteURL().toString());
        final DirectFeed pcge2 = (DirectFeed)guide1.getFeedAt(1);
        assertEquals("file://2", pcge2.getXmlURL().toString());
        assertEquals("file://2", pcge2.getSiteURL().toString());

        // Guide 2
        final IGuide guide2 = cgs.getGuideAt(2);
        assertEquals("g2", guide2.getTitle());
        assertEquals("Wrong icon name.", iconsNames[0], guide2.getIconKey());
        assertEquals(2, guide2.getFeedsCount());
        final DirectFeed pcge3 = (DirectFeed)guide2.getFeedAt(0);
        assertEquals("file://3", pcge3.getXmlURL().toString());
        assertEquals("file://3", pcge3.getSiteURL().toString());
        final DirectFeed pcge4 = (DirectFeed)guide2.getFeedAt(1);
        assertEquals("file://4", pcge4.getXmlURL().toString());
        assertEquals("file://4", pcge4.getSiteURL().toString());
    }

    private synchronized void initDB()
    {
        if (dbInitialized) return;

        GlobalModel.SINGLETON.getUserPreferences().setUsingPersistence(false);

//        InformaBackEnd.getInstance().connect();
        dbInitialized = true;
    }

    /**
     * @see ImportGuidesAction#replaceGuides
     */
    public void testReplaceGuides()
        throws Exception
    {
        initDB();

        // Setup guides for replacing
        final OPMLGuide g1, g2;
        final ArrayList<DefaultOPMLFeed> a1, a2;

        g1 = new OPMLGuide("g1", "icon1", false, null, null, false, 0, false, false, false);
        a1 = new ArrayList<DefaultOPMLFeed>();
        a1.add(new DirectOPMLFeed("1", "file://1", "file://1", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a1.add(new DirectOPMLFeed("2", "file://2", "file://2", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        g1.setFeeds(a1);

        g2 = new OPMLGuide("g1", null, false, null, null, false, 0, false, false, false);
        a2 = new ArrayList<DefaultOPMLFeed>();
        a2.add(new DirectOPMLFeed("3", "file://3", "file://3", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a2.add(new DirectOPMLFeed("4", "file://4", "file://4", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        g2.setFeeds(a2);

        final OPMLGuide[] guides = {g1, g2};

        // Put in ChannelGuideSet guide with duplicate name to check how dedupe works
        GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        final StandardGuide guide = new StandardGuide();
        guide.setTitle("g1");
//        InformaBackEnd.getInstance().connect(guide);
        cgs.add(guide);

        // Append guides
        ImportGuidesAction.replaceGuides(null, guides, cgs);

        // Check
        assertEquals(2, cgs.getGuidesCount());

        // Guide 0
        final IGuide guide0 = cgs.getGuideAt(0);
        assertEquals("g1", guide0.getTitle());
        assertEquals("icon1", guide0.getIconKey());
        assertEquals(2, guide0.getFeedsCount());
        final DirectFeed pcge1 = (DirectFeed)guide0.getFeedAt(0);
        assertEquals("file://1", pcge1.getXmlURL().toString());
        assertEquals("file://1", pcge1.getSiteURL().toString());
        final DirectFeed pcge2 = (DirectFeed)guide0.getFeedAt(1);
        assertEquals("file://2", pcge2.getXmlURL().toString());
        assertEquals("file://2", pcge2.getSiteURL().toString());

        String[] iconsNames = GuideIcons.getIconsNames();

        // Guide 1
        final IGuide guide1 = cgs.getGuideAt(1);
        assertEquals("g1_2", guide1.getTitle());
        assertEquals("Wrong icon name.", iconsNames[0], guide1.getIconKey());
        assertEquals(2, guide1.getFeedsCount());
        final DirectFeed pcge3 = (DirectFeed)guide1.getFeedAt(0);
        assertEquals("file://3", pcge3.getXmlURL().toString());
        assertEquals("file://3", pcge3.getSiteURL().toString());
        final DirectFeed pcge4 = (DirectFeed)guide1.getFeedAt(1);
        assertEquals("file://4", pcge4.getXmlURL().toString());
        assertEquals("file://4", pcge4.getSiteURL().toString());
    }

    /**
     * @see ImportGuidesAction#replaceGuides
     */
    public void testReplaceGuidesEmpty()
    {
        initDB();

        // Setup guides for replacing
        final OPMLGuide g1, g2;
        final ArrayList<DefaultOPMLFeed> a1, a2;

        g1 = new OPMLGuide("g1", "icon1", false, null, null, false, 0, false, false, false);
        a1 = new ArrayList<DefaultOPMLFeed>();
        a1.add(new DirectOPMLFeed("1", "file://1", "file://1", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a1.add(new DirectOPMLFeed("2", "file://2", "file://2", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        g1.setFeeds(a1);

        g2 = new OPMLGuide("g1", null, false, null, null, false, 0, false, false, false);
        a2 = new ArrayList<DefaultOPMLFeed>();
        a2.add(new DirectOPMLFeed("3", "file://3", "file://3", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a2.add(new DirectOPMLFeed("4", "file://4", "file://4", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        g2.setFeeds(a2);

        final OPMLGuide[] guides = {g1, g2};

        // Leave cgs empty
        GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        assertEquals(0, cgs.getGuidesCount());

        // Append guides
        ImportGuidesAction.replaceGuides(null, guides, cgs);

        // Check
        assertEquals(2, cgs.getGuidesCount());

        // Guide 0
        final IGuide guide0 = cgs.getGuideAt(0);
        assertEquals("g1", guide0.getTitle());
        assertEquals("icon1", guide0.getIconKey());
        assertEquals(2, guide0.getFeedsCount());
        final DirectFeed pcge1 = (DirectFeed)guide0.getFeedAt(0);
        assertEquals("file://1", pcge1.getXmlURL().toString());
        assertEquals("file://1", pcge1.getSiteURL().toString());
        final DirectFeed pcge2 = (DirectFeed)guide0.getFeedAt(1);
        assertEquals("file://2", pcge2.getXmlURL().toString());
        assertEquals("file://2", pcge2.getSiteURL().toString());

        String[] iconsNames = GuideIcons.getIconsNames();

        // Guide 1
        final IGuide guide1 = cgs.getGuideAt(1);
        assertEquals("g1_2", guide1.getTitle());
        assertEquals("Wrong icon name.", iconsNames[0], guide1.getIconKey());
        assertEquals(2, guide1.getFeedsCount());
        final DirectFeed pcge3 = (DirectFeed)guide1.getFeedAt(0);
        assertEquals("file://3", pcge3.getXmlURL().toString());
        assertEquals("file://3", pcge3.getSiteURL().toString());
        final DirectFeed pcge4 = (DirectFeed)guide1.getFeedAt(1);
        assertEquals("file://4", pcge4.getXmlURL().toString());
        assertEquals("file://4", pcge4.getSiteURL().toString());
    }

    /**
     * @see ImportGuidesAction#getUniqueTitle
     */
    public void testGetUniqueTitle()
    {
        final Set<String> a1 = new HashSet<String>();
        assertEquals("a", ImportGuidesAction.getUniqueTitle("a", a1));

        a1.add("a");
        assertEquals("a_2", ImportGuidesAction.getUniqueTitle("a", a1));

        a1.add("a_2");
        assertEquals("a_3", ImportGuidesAction.getUniqueTitle("a", a1));
    }

    /**
     * @see ImportGuidesAction#appendGuide
     */
    public void testAppendGuide()
        throws Exception
    {
        initDB();

        final OPMLGuide guide = new OPMLGuide("g1", "icon", false, null, null, false, 0, false, false, false);
        final ArrayList<DefaultOPMLFeed> a1 = new ArrayList<DefaultOPMLFeed>();
        a1.add(new DirectOPMLFeed("1", "file://1", "file://1", 0, null, null, -1, null, null, null, null, null, null, false, 1, false, 0, false, 0));
        a1.add(new DirectOPMLFeed("2", "file://2", "file://2", 0, null, null, 1, "A", "B", "C", "D", "E", "F", false, 1, false, 0, false, 0));
        guide.setFeeds(a1);

        // Put in GuidesSet guide with duplicate name to check how dedupe works
        GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        final StandardGuide g = new StandardGuide();
        g.setTitle("g1");
        cgs.add(g);

        // Note that unique title. Deduping made manually.
        ImportGuidesAction.appendGuide(null, guide, "g1_2", cgs);

        // Check
        assertEquals(2, cgs.getGuidesCount());

        // Guide 0
        assertEquals("g1", cgs.getGuideAt(0).getTitle());
        assertEquals(0, cgs.getGuideAt(0).getFeedsCount());

        // Guide 1
        final IGuide guide1 = cgs.getGuideAt(1);
        assertEquals("g1_2", guide1.getTitle());
        assertEquals("icon", guide1.getIconKey());
        assertEquals(2, guide1.getFeedsCount());

        final DirectFeed feed1 = (DirectFeed)guide1.getFeedAt(0);
        assertEquals("file://1", feed1.getXmlURL().toString());
        assertEquals("file://1", feed1.getSiteURL().toString());
        assertNull(feed1.getCustomTitle());
        assertNull(feed1.getCustomAuthor());
        assertNull(feed1.getCustomDescription());
        assertEquals(-1, feed1.getPurgeLimit());

        final DirectFeed feed2 = (DirectFeed)guide1.getFeedAt(1);
        assertEquals("file://2", feed2.getXmlURL().toString());
        assertEquals("file://2", feed2.getSiteURL().toString());
        assertEquals("A", feed2.getCustomTitle());
        assertEquals("B", feed2.getCustomAuthor());
        assertEquals("C", feed2.getCustomDescription());
        assertEquals(1, feed2.getPurgeLimit());
    }

    // ------------------------------------------------------------------------
    // Testing deduplication during import
    // ------------------------------------------------------------------------

    /** Tests no sharing due to the empty set. */
    public void testReplaceFeedsWithShares_emptySet()
    {
        GuidesSet set = new GuidesSet();
        StandardGuide guide = new StandardGuide();

        // Checking there are no errors
        ImportGuidesAction.replaceFeedsWithShares(set, guide);

        // Adding feed and checking if a guide is processed somehow -- shouldn't
        DirectFeed df1 = directFeed(1);
        guide.add(df1);
        ImportGuidesAction.replaceFeedsWithShares(set, guide);
        assertEquals(1, guide.getFeedsCount());
        assertTrue(df1 == guide.getFeedAt(0));
    }

    /** Tests sharing in guides. */
    public void testReplaceFeedsWithShares_replacing()
    {
        GuidesSet set = new GuidesSet();

        // Adding a guide with feeds
        StandardGuide guide1 = new StandardGuide();
        DirectFeed df1 = directFeed(1);
        QueryFeed qf1 = queryFeed(1);
        SearchFeed sf1 = searchFeed(1);
        guide1.add(df1);
        guide1.add(qf1);
        guide1.add(sf1);
        set.add(guide1);

        // Creating the second guide with the same and other feeds
        StandardGuide guide2 = new StandardGuide();
        DirectFeed df2 = directFeed(2);
        QueryFeed qf2 = queryFeed(2);
        SearchFeed sf2 = searchFeed(2);
        guide2.add(directFeed(1));
        guide2.add(df2);
        guide2.add(queryFeed(1));
        guide2.add(qf2);
        guide2.add(searchFeed(1));
        guide2.add(sf2);

        // Checking
        ImportGuidesAction.replaceFeedsWithShares(set, guide2);
        assertEquals(6, guide2.getFeedsCount());
        assertTrue(df2 == guide2.getFeedAt(0));
        assertTrue(qf2 == guide2.getFeedAt(1));
        assertTrue(sf2 == guide2.getFeedAt(2));
        assertTrue(df1 == guide2.getFeedAt(3));
        assertTrue(qf1 == guide2.getFeedAt(4));
        assertTrue(sf1 == guide2.getFeedAt(5));
    }

    /**
     * Testing the sharing in a reading list.
     * @throws MalformedURLException nowhen.
     */
    public void testReplaceFeedsWithShares_readingList()
            throws MalformedURLException
    {
        GuidesSet set = new GuidesSet();

        // Adding a guide with feeds
        StandardGuide guide1 = new StandardGuide();
        DirectFeed df1 = directFeed(1);
        QueryFeed qf1 = queryFeed(1);
        SearchFeed sf1 = searchFeed(1);
        guide1.add(df1);
        guide1.add(qf1);
        guide1.add(sf1);
        set.add(guide1);

        // Creating the second guide with the same and other feeds
        StandardGuide guide2 = new StandardGuide();
        ReadingList rl = new ReadingList(new URL("file://a"));
        DirectFeed df2 = directFeed(2);
        rl.add(directFeed(1));
        rl.add(df2);
        guide2.add(rl);

        // Checking
        ImportGuidesAction.replaceFeedsWithShares(set, guide2);
        assertEquals(2, guide2.getFeedsCount());
        assertTrue(df2 == guide2.getFeedAt(0));
        assertTrue(df1 == guide2.getFeedAt(1));
    }
                  
    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private static DirectFeed directFeed(int n)
    {
        DirectFeed df = new DirectFeed();
        try
        {
            df.setXmlURL(new URL("file://" + n));
        } catch (MalformedURLException e)
        {
            // Fall through
        }

        return df;
    }

    private static QueryFeed queryFeed(int n)
    {
        QueryFeed qf = new QueryFeed();
        qf.setQueryType(QueryType.getQueryType(QueryType.TYPE_AMAZON_BOOKS));
        qf.setParameter(Integer.toString(n));

        return qf;
    }

    private static SearchFeed searchFeed(int n)
    {
        // Create Query
        Query q = new Query();
        Collection props = q.getAvailableProperties();
        IProperty p = (IProperty)props.iterator().next();
        IComparisonOperation op = (IComparisonOperation)p.getComparsonOperations().iterator().next();
        ICriteria c = q.addCriteria();
        c.setProperty(p);
        c.setComparisonOperation(op);
        c.setValue(Integer.toString(n));

        SearchFeed sf = new SearchFeed();
        sf.setQuery(q);
        return sf;
    }
}
