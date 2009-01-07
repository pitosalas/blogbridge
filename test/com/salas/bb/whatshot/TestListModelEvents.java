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
// $Id: TestListModelEvents.java,v 1.6 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.*;
import com.salas.bb.search.IResultsListModelListener;
import com.salas.bb.search.ResultGroup;
import com.salas.bb.search.ResultItem;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.MatchBuilder;
import org.jmock.core.constraint.IsEqual;

import java.util.Date;
import java.util.List;

/**
 * Tests the events fired by the {@link ListModel}.
 */
public class TestListModelEvents extends MockObjectTestCase
{
    /** Mock listener. */
    private Mock listener;
    /** Model to operate. */
    private ListModel model;

    private StandardArticle article11;
    private StandardArticle article12;
    private StandardArticle article13;
    private StandardArticle article21;
    private StandardArticle article22;
    private StandardArticle article31;

    private DirectFeed feed1;
    private DirectFeed feed2;
    private DirectFeed feed3;

    private StandardGuide guide1;
    private StandardGuide guide2;

    private GuidesSet set;

    private int seq;
    private Engine engine;
    private ValueHolder mdlStarz;
    private ValueHolder mdlOnlyUnread;
    private ValueHolder mdlToday;

    /**
     * Configures the environment.
     *
     * @throws Exception if something goes wrong.
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        listener = new Mock(IResultsListModelListener.class);
        seq = 0;

        ResourceUtils.setBundlePath("Resource");

        // Create 6 articles: 3 for the first feed, 2 for the second and 1 for the third
        article11 = article(mention("1", "1"), "11");
        article12 = article(mention("1", "2"), "12");
        article13 = article(mention("1", "3"), "13");
        article21 = article(mention("2", "1"), "21");
        article22 = article(mention("2", "2"), "22");
        article31 = article(mention("3", "1"), "31");

        // Create 3 feeds
        feed1 = new DirectFeed();
        feed1.appendArticle(article11);
        feed1.appendArticle(article12);
        feed1.appendArticle(article13);
        feed2 = new DirectFeed();
        feed2.appendArticle(article21);
        feed2.appendArticle(article22);
        feed3 = new DirectFeed();
        feed3.appendArticle(article31);

        // Create 2 guides
        guide1 = new StandardGuide();
        guide1.setTitle("1");
        guide1.add(feed1);
        guide1.add(feed2);
        guide2 = new StandardGuide();
        guide2.setTitle("2");
        guide1.add(feed3);

        // Create the guides set
        set = new GuidesSet();
        set.add(guide1);
        set.add(guide2);

        // Create a model with an item
        engine = new Engine(set);
        mdlStarz = new ValueHolder(1);
        mdlOnlyUnread = new ValueHolder(false);
        mdlToday = new ValueHolder(TimeOption.THIS_WEEK);
        model = new ListModel(engine, mdlStarz, mdlOnlyUnread, mdlToday);
        model.addListener((IResultsListModelListener)listener.proxy());
    }

    private StandardArticle article(String text, String title)
    {
        StandardArticle article = new StandardArticle(text);
        article.setTitle(title);
        article.setPublicationDate(new Date());
        return article;
    }

    /**
     * Tests initial scanning of the set.
     */
    public void testInitialScan()
    {
        // Current model configuration is: 1 star, all guides, read/unread
        expectOnClear();
        expectOnGroupAdded("1");
        expectOnItemAdded(article11, "1");
        expectOnItemAdded(article12, "1");
        expectOnItemAdded(article13, "1");
        expectOnItemAdded(article21, "1");
        expectOnItemAdded(article31, "1");
        expectOnGroupAdded("2");
        expectOnItemAdded(article12, "2");
        expectOnItemAdded(article21, "2");
        expectOnItemAdded(article22, "2");
        expectOnGroupAdded("3");
        expectOnItemAdded(article13, "3");
        expectOnItemAdded(article31, "3");
        scan();
        
        listener.verify();
    }

    /**
     * Tests hiding read items and empty groups.
     */
    public void testSwitchingUnreadOnly()
    {
        // Set the following articles as read: 12, 22, 13
        // This will:
        // 1) remove 2 articles from the first set
        // 2) remove 2 articles from the second set and hide it
        // 3) remove 1 article from the third set and hide it
        article12.setRead(true);
        article22.setRead(true);
        article13.setRead(true);

        // Skip any events
        listener.expects(atLeastOnce()).method(ANYTHING);

        // Current model configuration is: 1 star, all guides, read/unread
        scan();

        // Configure expectations
        expectOnClear();
        expectOnGroupAdded("1");
        expectOnItemAdded(article11, "1");
        expectOnItemAdded(article21, "1");
        expectOnItemAdded(article31, "1");

        // Change mode to unread only
        mdlOnlyUnread.setValue(true);

        listener.verify();
    }

    /**
     * Tests showing read items and empty groups.
     */
    public void testSwitchingNotUnreadOnly()
    {
        // Set the following articles as read: 12, 22, 13
        // This will:
        // 1) remove 2 articles from the first set
        // 2) remove 2 articles from the second set and hide it
        // 3) remove 1 article from the third set and hide it
        article12.setRead(true);
        article22.setRead(true);
        article13.setRead(true);

        // Skip any events
        listener.expects(atLeastOnce()).method(ANYTHING);

        // Current model configuration is: 1 star, all guides, read/unread
        scan();
        mdlOnlyUnread.setValue(true);

        // Configure expectations
        expectOnClear();
        expectOnGroupAdded("1");
        expectOnItemAdded(article11, "1");
        expectOnItemAdded(article12, "1");
        expectOnItemAdded(article13, "1");
        expectOnItemAdded(article21, "1");
        expectOnItemAdded(article31, "1");
        expectOnGroupAdded("2");
        expectOnItemAdded(article12, "2");
        expectOnItemAdded(article21, "2");
        expectOnItemAdded(article22, "2");
        expectOnGroupAdded("3");
        expectOnItemAdded(article13, "3");
        expectOnItemAdded(article31, "3");

        // Switch non-unread only mode on
        mdlOnlyUnread.setValue(false);

        listener.verify();
    }

    /**
     * Single-threaded equivalent of {@link com.salas.bb.whatshot.ListModel#scan()}.
     */
    private void scan()
    {
        List<Engine.HotLink> links = engine.scan();
        model.processLinks(links);
    }

    /**
     * Returns the HTML text mentioning the links given.
     *
     * @param links links.
     *
     * @return HTML.
     */
    private String mention(String ... links)
    {
        String text = "";

        for (String link : links)
        {
            text += "<a href=\"http://" + link + ".com\">link</a>";
        }

        return text;
    }

    private void expectOnClear()
    {
        MatchBuilder b = listener.expects(once()).method("onClear").with(same(model));
        setupIdAfter(b);
    }

    private void expectOnGroupAdded(String groupName)
    {
        MatchBuilder b = listener.expects(once()).method("onGroupAdded").with(
                same(model), groupEq("http://" + groupName + ".com"), eq(true));
        setupIdAfter(b);
    }

    private void expectOnGroupUpdated(String groupName)
    {
        MatchBuilder b = listener.expects(once()).method("onGroupUpdated").with(
                same(model), groupEq("http://" + groupName + ".com"));
        setupIdAfter(b);
    }

    private void expectOnGroupRemoved(String groupName)
    {
        MatchBuilder b = listener.expects(once()).method("onGroupRemoved").with(
                same(model), groupEq("http://" + groupName + ".com"));
        setupIdAfter(b);
    }

    private void expectOnItemAdded(IArticle article, String groupName)
    {
        MatchBuilder b = listener.expects(once()).method("onItemAdded").with(
                same(model), itemEq(article), groupEq("http://" + groupName + ".com"));
        setupIdAfter(b);
    }

    private void expectOnItemRemoved(IArticle article, String groupName)
    {
        MatchBuilder b = listener.expects(once()).method("onItemRemoved").with(
                same(model), itemEq(article), groupEq("http://" + groupName + ".com"));
        setupIdAfter(b);
    }

    private void setupIdAfter(MatchBuilder b)
    {
        if (seq > 0) b.after(Integer.toString(seq - 1));
        b.id(Integer.toString(seq++));
    }

    private GroupEq groupEq(String link)
    {
        return new GroupEq(link);
    }

    private ItemEq itemEq(IArticle article)
    {
        return new ItemEq(article);
    }

    private static class GroupEq extends IsEqual
    {
        private final String link;

        public GroupEq(String link)
        {
            super(link);
            this.link = link;
        }

        @Override
        public boolean eval(Object o)
        {
            if (!(o instanceof ResultGroup)) return false;
            ResultGroup g = (ResultGroup)o;

            return g.getName().equals(link);
        }

        @Override
        public StringBuffer describeTo(StringBuffer stringBuffer)
        {
            return stringBuffer.append("groupEq(<").append(link).append(">)");
        }
    }

    private static class ItemEq extends IsEqual
    {
        private final IArticle article;

        public ItemEq(IArticle article)
        {
            super(article);
            this.article = article;
        }

        @Override
        public boolean eval(Object o)
        {
            if (!(o instanceof ResultItem)) return false;
            ResultItem i = (ResultItem)o;

            return i.getObject() == article;
        }

        @Override
        public StringBuffer describeTo(StringBuffer stringBuffer)
        {
            return stringBuffer.append("itemEq(<").append(article.getTitle()).append(">)");
        }
    }
}
