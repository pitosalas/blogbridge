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
// $Id: TestAmazonGateway.java,v 1.6 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.utils.amazon;

import junit.framework.TestCase;

import java.util.List;

/**
 * This suite contains tests for <code>AmazonGateway</code> unit.
 */
public class TestAmazonGateway extends TestCase
{
    private static final String SUBSCRIPTION_ID = "0EJA6NQAYZJX1C9AGW82";
    private static final String AFFILATE_ID     = "noizerampcrea-20";

    private AmazonGateway gateway;

    protected void setUp() throws Exception
    {
        super.setUp();
        gateway = new AmazonGateway(SUBSCRIPTION_ID, AFFILATE_ID);
    }

    /**
     * Tests reading item from Amazon.
     */
    public void testItemSearchByISBN() throws AmazonException
    {
        List items = gateway.itemsSearch("0470093552", AmazonSearchIndex.Books, "daterank", 10);

        assertEquals("There's single item with such ISBN.", 1, items.size());

        AmazonItem item = (AmazonItem)items.get(0);

        assertEquals("http://www.amazon.com/exec/obidos/ASIN/0470093552/" + AFFILATE_ID + "/", item.getURL().toString());
        assertAttirbutes(item, "Author", new String[] { "Jeff Magee", "Jeff Kramer" });
        assertAttirbute(item, "Binding", "Hardcover");
        assertAttirbute(item, "NumberOfPages", "434");
        assertAttirbute(item, "Publisher", "Wiley");
        assertAttirbute(item, "Title", "Concurrency: State Models and Java Programs");
        assertAttirbute(item, "PublicationDate", "2006-07-05");

        assertNotNull("Lowet new price is present.", item.getLowestNewPrice());
        assertNotNull("Lowest used price is present", item.getLowestUsedPrice());

        assertImage(item.getSmallImage(), "http://ecx.images-amazon.com/images/I/01Z6QEDPZKL.jpg", 46, 60);
        assertImage(item.getMediumImage(), "http://ecx.images-amazon.com/images/I/21BACY15QBL.jpg", 107, 140);
        assertImage(item.getLargeImage(), "http://ecx.images-amazon.com/images/I/51W5D3GKKCL.jpg", 363, 475);
    }

    /**
     * Tests reading several items from Amazon.
     */
    public void testItemSearchByKeyword() throws AmazonException
    {
        List items = gateway.itemsSearch("java", AmazonSearchIndex.Books, "daterank", 10);

        assertEquals("There should be more than 10 items, but default limit is 10.", 10, items.size());
    }

    private void assertImage(AmazonImageDetails image, String url, int width, int height)
    {
        assertEquals("Wrong URL", url, image.getURL().toString());
        assertEquals("Wrong width.", width, image.getWidth());
        assertEquals("Wrong height.", height, image.getHeight());
    }

    private void assertAttirbute(AmazonItem item, String name, String value)
    {
        assertAttirbutes(item, name, new String[] { value });
    }

    private void assertAttirbutes(AmazonItem item, String name, String[] values)
    {
        List itemValues = item.getAttributeValues(name);

        if (values == null)
        {
            assertNull("Should be no values for attribute " + name, itemValues);
        } else
        {
            assertEquals("Wrong number of values.", values.length, itemValues.size());
            for (int i = 0; i < values.length; i++)
            {
                String value = values[i];
                assertTrue("Item has no value " + value + " for attribute " + name,
                    itemValues.contains(value));
            }
        }
    }
}
