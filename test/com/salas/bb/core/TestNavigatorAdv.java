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
// $Id: TestNavigatorAdv.java,v 1.10 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import com.salas.bb.utils.parser.Channel;
import junit.framework.TestCase;

/**
 * @see NavigatorAdv
 */
public class TestNavigatorAdv extends TestCase
{
    private HighlightsCalculator    hCalc = new HighlightsCalculator();
    private ScoresCalculator        csCalc = new ScoresCalculator();

    private GuideModel       navModel;
    private NavigatorAdv     nav;

    protected void setUp() throws Exception
    {
        hCalc.keywordsChanged("a");

        // Remove all color mappings so none of the feeds get hidden
        FeedDisplayModeManager fdmm = FeedDisplayModeManager.getInstance();
        fdmm.clear();

        navModel = new GuideModel(csCalc, fdmm);
        navModel.setSortingEnabled(false);

        nav = new NavigatorAdv(navModel, null);
    }

    /**
     * Regular "next" operation test. (Bounds of single guide)
     */
    public void testNIGAny() throws Exception
    {
        CustomDummyFeed f1, f2, f3;
        IGuide cg;

        // Feed with unread article with "abc" text
        f1 = new CustomDummyFeed();
        f1.addArticle(new DummyArticle("Test", "abc"));

        // Feed with unread article with "def" text
        f2 = new CustomDummyFeed();
        f2.addArticle(new DummyArticle("Test", "def"));

        // Feed with unread article with "ghi" text
        f3 = new CustomDummyFeed();
        f3.addArticle(new DummyArticle("Test", "ghi"));

        cg = new StandardGuide();
        cg.add(f1);
        cg.add(f2);
        cg.add(f3);

        navModel.setGuide(cg);

        // The timeout is necessary as we do very async processing
        Thread.sleep(500);

        // Find feed from before-first place
        assertTrue(f1 == nav.getNextInGuide(navModel, null, false));

        // Find feed from the start place (first thing which is done during destination search)
        assertTrue(f2 == nav.getNextInGuide(navModel, f1, false));

        // Find feed from the other place if there's next feed available in the guide
        assertTrue(f3 == nav.getNextInGuide(navModel, f2, false));

        // Find feed from the other place if there's NO next feed available in the guide
        assertNull(nav.getNextInGuide(navModel, f3, false));
    }

    /**
     * "Next Unread" operation test. (Bounds of single guide)
     */
    public void testNIGUnread() throws Exception
    {
        CustomDummyFeed f1, f2, f3;
        IArticle a1, a2, a3;
        IGuide cg;

        a1 = new DummyArticle("Test", "abc");
        a2 = new DummyArticle("Test", "def");
        a3 = new DummyArticle("Test", "ghi");

        // Feed with unread article with "abc" text
        f1 = new CustomDummyFeed();
        f1.addArticle(a1);

        // Feed with unread article with "def" text
        f2 = new CustomDummyFeed();
        f2.addArticle(a2);

        // Feed with unread article with "ghi" text
        f3 = new CustomDummyFeed();
        f3.addArticle(a3);

        cg = new StandardGuide();
        cg.add(f1);
        cg.add(f2);
        cg.add(f3);

        navModel.setGuide(cg);

        // The timeout is necessary as we do very async processing
        Thread.sleep(500);

        // Find feed from before-first place
        assertTrue(f1 == nav.getNextInGuide(navModel, null, true));

        // The same, but the first feed is read now
        a1.setRead(true);
        assertTrue(f2 == nav.getNextInGuide(navModel, null, true));

        // Find feed from the start place (first thing which is done during destination search)
        assertTrue(f2 == nav.getNextInGuide(navModel, f1, true));

        // Find feed from the other place if there's next feed available in the guide
        assertTrue(f3 == nav.getNextInGuide(navModel, f2, true));

        // Find feed from the other place if there's NO next feed available in the guide
        assertNull(nav.getNextInGuide(navModel, f3, true));
    }

    /**
     * Regular "previous" operation test. (Bounds of single guide)
     */
    public void testPIGAny() throws Exception
    {
        CustomDummyFeed f1, f2, f3;
        IGuide cg;

        // Feed with unread article with "abc" text
        f1 = new CustomDummyFeed();
        f1.addArticle(new DummyArticle("Test", "abc"));

        // Feed with unread article with "def" text
        f2 = new CustomDummyFeed();
        f2.addArticle(new DummyArticle("Test", "def"));

        // Feed with unread article with "ghi" text
        f3 = new CustomDummyFeed();
        f3.addArticle(new DummyArticle("Test", "ghi"));

        cg = new StandardGuide();
        cg.add(f1);
        cg.add(f2);
        cg.add(f3);

        navModel.setGuide(cg);

        // The timeout is necessary as we do very async processing
        Thread.sleep(500);

        // Find feed from after-last place
        assertTrue(f3 == nav.getPrevInGuide(navModel, null, false));

        // Find feed from the place in the middle
        assertTrue(f2 == nav.getPrevInGuide(navModel, f3, false));

        // Find feed from the last place
        assertNull(nav.getPrevInGuide(navModel, f1, false));
    }

    /**
     * "Previous Unread" operation test. (Bounds of single guide)
     */
    public void testPIGUnread() throws Exception
    {
        CustomDummyFeed f1, f2, f3;
        IArticle a1, a2, a3;
        IGuide cg;

        a1 = new DummyArticle("Test", "abc");
        a2 = new DummyArticle("Test", "def");
        a3 = new DummyArticle("Test", "ghi");

        // Feed with unread article with "abc" text
        f1 = new CustomDummyFeed();
        f1.addArticle(a1);

        // Feed with unread article with "def" text
        f2 = new CustomDummyFeed();
        f2.addArticle(a2);

        // Feed with unread article with "ghi" text
        f3 = new CustomDummyFeed();
        f3.addArticle(a3);

        cg = new StandardGuide();
        cg.add(f1);
        cg.add(f2);
        cg.add(f3);

        navModel.setGuide(cg);

        // The timeout is necessary as we do very async processing
        Thread.sleep(500);

        // Find feed from after-last place
        assertTrue(f3 == nav.getPrevInGuide(navModel, null, true));

        // The same, but the last feed is read now
        a3.setRead(true);
        assertTrue(f2 == nav.getPrevInGuide(navModel, null, true));

        // Find feed from the middle
        assertTrue(f1 == nav.getPrevInGuide(navModel, f2, true));

        // Find feed from the last place
        assertNull(nav.getPrevInGuide(navModel, f1, true));
    }

    private static class CustomDummyFeed extends DataFeed
    {
        /**
         * Fetches the feed by some specific means.
         *
         * @return the feed or NULL if there was an error or no updates required.
         */
        protected Channel fetchFeed()
        {
            return null;
        }

        /**
         * Returns title of feed.
         *
         * @return title.
         */
        public String getTitle()
        {
            return null;
        }

        public void addArticle(IArticle article)
        {
            this.appendArticle(article);
        }

        /**
         * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
         * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
         * criteria for the search feeds.
         *
         * @return match key.
         */
        public String getMatchKey()
        {
            return null;
        }
    }

    private static class DummyArticle extends StandardArticle
    {
        public DummyArticle(String title, String text)
        {
            super(text);
            
            setTitle(title);
        }
    }
}
