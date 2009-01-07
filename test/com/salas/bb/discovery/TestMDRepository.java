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
// $Id: TestMDRepository.java,v 1.2 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.discovery;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;

import com.salas.bb.domain.FeedMetaDataHolder;

/**
 * This suite contains tests for <code>MDRepository</code> unit.
 */
public class TestMDRepository extends TestCase
{
    private MDRepository repository;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        repository = new MDRepository();
    }

    /**
     * Tests looking up in empty repository.
     */
    public void testLookupMissing()
        throws MalformedURLException
    {
        assertNull("There's no record with such URL.",
            repository.lookup(new URL("file://some")));
        assertEquals("Repository is empty.",
            0, repository.lookupValid().length);
    }

    /**
     * Tests recording holder for some URL and looking it up after that.
     */
    public void testRecord()
        throws MalformedURLException
    {
        FeedMetaDataHolder holder = createHolder(true, false);

        URL url = new URL("file://some");

        repository.record(holder, url);

        assertTrue("Repository should return exactly the same object.",
            repository.lookup(url) == holder);

        assertEquals("Repository now has 1 record.",
            1, repository.lookupValid().length);
        assertTrue("Repository should return the new record.",
            repository.lookupValid()[0] == holder);
    }

    /**
     * Tests returning only valid holders.
     */
    public void testLookupValid()
        throws MalformedURLException
    {
        FeedMetaDataHolder valid = createHolder(true, false);
        FeedMetaDataHolder invalid = createHolder(true, true);

        repository.record(valid, new URL("file://valid"));
        repository.record(invalid, new URL("file://invalid"));

        FeedMetaDataHolder[] validHolders = repository.lookupValid();
        assertEquals("Only valid holders should be returned.",
            1, validHolders.length);
        assertTrue("The only record should be valid holder.",
            validHolders[0] == valid);
    }

    public void testForget()
        throws MalformedURLException
    {
        FeedMetaDataHolder holder1 = createHolder(true, false);
        holder1.setXmlURL(new URL("file://1"));

        FeedMetaDataHolder holder2 = createHolder(true, false);
        holder2.setXmlURL(new URL("file://2"));

        FeedMetaDataHolder holder3 = createHolder(true, false);
        holder3.setXmlURL(new URL("file://3"));

        repository.record(holder1, holder1.getXmlURL());
        repository.record(holder2, holder2.getXmlURL());
        repository.record(holder3, holder3.getXmlURL());

        repository.forget(new FeedMetaDataHolder[] { holder1, holder3 });

        FeedMetaDataHolder[] validHolders = repository.lookupValid();
        assertEquals("Only one holder (2) is left.",
            1, validHolders.length);
        assertTrue("The only record should be returned.",
            validHolders[0] == holder2);
    }
    /**
     * Creates holder.
     *
     * @param aComplete <code>TRUE</code> if complete.
     * @param aInvalid  <code>TRUE</code> if invalid.
     *
     * @return holder.
     */
    private static FeedMetaDataHolder createHolder(boolean aComplete, boolean aInvalid)
    {
        FeedMetaDataHolder holder = new FeedMetaDataHolder();

        holder.setComplete(aComplete);
        holder.setInvalid(aInvalid);

        return holder;
    }
}
