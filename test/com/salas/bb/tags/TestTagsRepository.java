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
// $Id: TestTagsRepository.java,v 1.2 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.tags;

import junit.framework.TestCase;
import com.salas.bb.domain.*;

import java.util.Set;

/**
 * This suite contains tests for <code>TagsRepository</code> unit.
 */
public class TestTagsRepository extends TestCase
{
    private TagsRepository repository;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repository = new TagsRepository();
    }

    /**
     * Tests simple adding missing tags.
     */
    public void testAddMissingTags()
    {
        Set usedTags;

        repository.addMissingTags(null);
        usedTags = repository.getUsedTags();
        assertEquals(0, usedTags.size());

        repository.addMissingTags(new String[0]);
        usedTags = repository.getUsedTags();
        assertEquals(0, usedTags.size());

        repository.addMissingTags(new String[] { "a" });
        usedTags = repository.getUsedTags();
        assertEquals(1, usedTags.size());
        assertTrue(usedTags.contains("a"));

        repository.addMissingTags(new String[] { "a", "B" });
        usedTags = repository.getUsedTags();
        assertEquals(2, usedTags.size());
        assertTrue(usedTags.contains("a"));
        assertTrue(usedTags.contains("b"));
    }

    /**
     * Tests adding missing tags from lists of taggable objects.
     */
    public void testAddMissingTagsFromTaggable()
    {
        Set usedTags;

        DirectFeed feed = new DirectFeed();
        feed.setUserTags(new String[] { "a", "b" });

        // Adding from taggable
        repository.addMissingTagsFromTaggable(feed);
        usedTags = repository.getUsedTags();
        assertEquals("DirectFeed is ITaggable.",
            2, usedTags.size());
        assertTrue(usedTags.contains("a"));
        assertTrue(usedTags.contains("b"));

        // Adding from non-taggable
        repository.addMissingTagsFromTaggable("test");
        usedTags = repository.getUsedTags();
        assertEquals("Nothing should be added because string isn't ITaggable.",
            2, usedTags.size());
    }

    /**
     * Tests loading of tags from gides set.
     */
    public void testLoadFromGuidesSet()
    {
        DirectFeed feed = new DirectFeed();
        feed.setUserTags(new String[] { "a", "b" });

        QueryFeed qfeed = new QueryFeed();

        StandardArticle article = new StandardArticle("a");
        article.setUserTags(new String[] { "c" });
        feed.appendArticle(article);

        StandardGuide guide = new StandardGuide();
        guide.add(qfeed);
        guide.add(feed);

        GuidesSet set = new GuidesSet();
        set.add(guide);

        // Preparations over -- check the scanning
        repository.loadFromGuidesSet(set);
        Set usedTags = repository.getUsedTags();
        assertEquals("Two tags in feed and one in article.",
            3, usedTags.size());
        assertTrue(usedTags.contains("a"));
        assertTrue(usedTags.contains("b"));
        assertTrue(usedTags.contains("c"));
    }
}
