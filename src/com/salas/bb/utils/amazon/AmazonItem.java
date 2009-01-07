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
// $Id: AmazonItem.java,v 1.2 2006/01/08 05:00:08 kyank Exp $
//

package com.salas.bb.utils.amazon;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

/**
 * Amazon item. Amazon has different types of items and this holder can be used to
 * contain any type of item. It just has no specifics related to particular type.
 */
public class AmazonItem
{
    private String              asin;
    private URL                 url;
    private AmazonSearchIndex   type;

    private AmazonImageDetails  smallImage;
    private AmazonImageDetails  mediumImage;
    private AmazonImageDetails  largeImage;

    private String              listPrice;
    private String              lowestNewPrice;
    private String              lowestUsedPrice;

    private Map                 attributes;

    /**
     * Creates item.
     *
     * @param anASIN    ASIN number.
     * @param aURL      URL to item.
     * @param aType     index type.
     */
    public AmazonItem(String anASIN, URL aURL, AmazonSearchIndex aType)
    {
        asin = anASIN;
        url = aURL;
        type = aType;
        attributes = new HashMap();
    }

    /**
     * Returns small image.
     *
     * @return small image (can be <code>NULL</code>).
     */
    public AmazonImageDetails getSmallImage()
    {
        return smallImage;
    }

    /**
     * Returns medium image.
     *
     * @return medium image (can be <code>NULL</code>).
     */
    public AmazonImageDetails getMediumImage()
    {
        return mediumImage;
    }

    /**
     * Returns large image.
     *
     * @return large image (can be <code>NULL</code>).
     */
    public AmazonImageDetails getLargeImage()
    {
        return largeImage;
    }

    /**
     * Sets small image.
     *
     * @param image small image.
     */
    public void setSmallImage(AmazonImageDetails image)
    {
        smallImage = image;
    }

    /**
     * Sets medium image.
     *
     * @param image medium image.
     */
    public void setMediumImage(AmazonImageDetails image)
    {
        mediumImage = image;
    }

    /**
     * Sets large image.
     *
     * @param image large image.
     */
    public void setLargeImage(AmazonImageDetails image)
    {
        largeImage = image;
    }

    /**
     * Returns current list price.
     *
     * @return list price.
     */
    public String getListPrice()
    {
        return listPrice;
    }

    /**
     * Sets list price.
     *
     * @param aListPrice list price.
     */
    public void setListPrice(String aListPrice)
    {
        listPrice = aListPrice;
    }

    /**
     * Returns formatted lowest price of new item.
     *
     * @return price or <code>NULL</code> if new items aren't available.
     */
    public String getLowestNewPrice()
    {
        return lowestNewPrice;
    }

    /**
     * Sets the formatted lowest price of new item.
     *
     * @param price price.
     */
    public void setLowestNewPrice(String price)
    {
        lowestNewPrice = price;
    }

    /**
     * Returns formatteed lowest price of used item.
     *
     * @return price or <code>NULL</code> if used items aren't available.
     */
    public String getLowestUsedPrice()
    {
        return lowestUsedPrice;
    }

    /**
     * Sets the formatted lowest price of used item.
     *
     * @param price price.
     */
    public void setLowestUsedPrice(String price)
    {
        lowestUsedPrice = price;
    }

    /**
     * Adds an attribute to the item.
     *
     * @param name  name of attribute.
     * @param value value.
     */
    public void addAttribute(String name, String value)
    {
        List values = (List)attributes.get(name);
        if (values == null)
        {
            values = new ArrayList();
            attributes.put(name, values);
        }

        values.add(value);
    }

    /**
     * Returns the list of values of an attribute.
     *
     * @param name  name of attribute.
     *
     * @return list of string values or <code>NULL</code> if attribute wasn't there.
     */
    public List getAttributeValues(String name)
    {
        return (List)attributes.get(name);
    }

    /**
     * Returns single value of an attribute.
     *
     * @param name  name of attribute.
     *
     * @return value or <code>NULL</code> if attribute wasn't there.
     */
    public String getAttributeValue(String name)
    {
        List values = getAttributeValues(name);

        return values == null || values.size() == 0 ? null : values.get(0).toString();
    }

    /**
     * Returns item's URL on the Amazon web-site.
     *
     * @return link.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Returns this item as HTML formatted text description.
     *
     * @return HTML text.
     */
    public String toHTML()
    {
        return type.formatAsHTML(this);
    }
}
