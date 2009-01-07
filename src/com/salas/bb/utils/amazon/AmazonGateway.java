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
// $Id: AmazonGateway.java,v 1.6 2007/02/06 15:33:00 spyromus Exp $
//

package com.salas.bb.utils.amazon;

import com.salas.bb.utils.i18n.Strings;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gateway to Amazon.com.
 */
public class AmazonGateway
{
    private static final Logger LOG = Logger.getLogger(AmazonGateway.class.getName());

    private static final int ITEMS_PER_PAGE         = 10;

    // The FAQ on AWS says that 1 second between calls is a guideline only that's why
    // we lower this number because there will be no stations generating continuous load
    private static final int TIME_BETWEEN_CALLS_MS  = 10;

    private static final MessageFormat ITEM_SEARCH_FORMAT;
    private static final MessageFormat ITEM_URL_FORMAT;

    private String  subscriptionId;
    private String  affilateId;

    static
    {
        ITEM_SEARCH_FORMAT = new MessageFormat("http://webservices.amazon.com/onca/xml" +
            "?Service=AWSECommerceService" +
            "&SubscriptionId={0}" +
            "&Operation=ItemSearch" +
            "&Keywords={1}" +
            "&SearchIndex={2}" +
            "&Sort={3}" +
            "&ItemPage={4}" +
            "&ResponseGroup=Small,OfferSummary,Images,ItemAttributes");

        ITEM_URL_FORMAT = new MessageFormat("http://www.amazon.com/exec/obidos/ASIN/{0}/{1}/");
    }

    /**
     * Creates gateway to talk to <code>amazon.com</code>.
     *
     * @param subscriptionId    subscription ID to issue AWS queries.
     * @param affilateId        affilate ID to produce referrence links.
     */
    public AmazonGateway(String subscriptionId, String affilateId)
    {
        this.subscriptionId = subscriptionId;
        this.affilateId = affilateId;
    }

    /**
     * <p>Returns collection of items from <code>Amazon.com</code>. <code>searchIndex</code>
     * defines the category of search, <code>keywords</code> define list of associated keywords,
     * <code>sort</code> is category-specific sort order.</p>
     *
     * <p>This method is synchronized as it's vital to have 1 second between calls -- terms of
     * contract. If multiple threads will be accessing this method we will break the contract.</p>
     *
     * @param keywords      list of keywords separated with spaces.
     * @param searchIndex   search index.
     * @param sort          sort order or <code>NULL</code> for original sorting.
     * @param maxItems      maximum number of items to return.
     *
     * @return list of {@link AmazonItem} instances.
     *
     * @throws AmazonException if fetching cannot be finished for some reason.
     */
    public synchronized List<AmazonItem> itemsSearch(String keywords, AmazonSearchIndex searchIndex,
                                         String sort, int maxItems) throws AmazonException
    {
        ArrayList<AmazonItem> items = new ArrayList<AmazonItem>(maxItems);
        int pages = (int)Math.ceil(maxItems / (double)ITEMS_PER_PAGE);

        // We fetch items in pages. Pause between queries should be at least 1 second.
        // We continue fetching to the next page if only the previous page had full list of 10
        // items.
        for (int page = 0; (items.size() == page * ITEMS_PER_PAGE) && page < pages; page++)
        {
            if (page > 0) sleepBetweenCalls();
            items.addAll(fetchItemsPage(keywords, searchIndex, sort, page + 1));
        }

        return items;
    }

    /**
     * Fetches the items corresponding to keywords, index type and sort order from the given page.
     *
     * @param keywords      list of keywords separated with spaces.
     * @param searchIndex   search index.
     * @param sort          sort order or <code>NULL</code> for original sorting.
     * @param page          page number (first is 1).
     *
     * @return items from the page.
     *
     * @throws AmazonException if fetching cannot be finished for some reason.
     */
    private List<AmazonItem> fetchItemsPage(String keywords, AmazonSearchIndex searchIndex, String sort,
                                int page) throws AmazonException
    {
        List<AmazonItem> itemsOnThePage = new ArrayList<AmazonItem>(ITEMS_PER_PAGE);

        String restCallURL = ITEM_SEARCH_FORMAT.format(new Object[] {
            subscriptionId,
            keywords.replaceAll(" ", "+"),
            searchIndex.toString(),
            sort,
            Integer.toString(page)
        });

        Document doc = getResponseDocument(restCallURL);

        Element rootEl = doc.getRootElement();
        clearNamespace(rootEl);

        Element itemsEl = rootEl.getChild("Items");
        if (itemsEl != null)
        {
            List itemsElements = itemsEl.getChildren("Item");
            for (Object itemsElement : itemsElements)
            {
                Element itemElement = (Element)itemsElement;
                try
                {
                    itemsOnThePage.add(convertElementToItem(itemElement, searchIndex));
                } catch (MalformedURLException e)
                {
                    LOG.log(Level.WARNING, Strings.error("amazon.failed.to.create.item"), e);
                }
            }
        }

        return itemsOnThePage;
    }

    /**
     * This method ensures that we have valid document or error. It handles server errors from
     * 5xx series and retries automatically when it gets one. It respects the agreement to
     * call service no faster than 1 time a second.
     *
     * @param requestURL request URL.
     *
     * @return response document.
     *
     * @throws AmazonException in case when commnication failed or invalid document returned.
     */
    private Document getResponseDocument(String requestURL)
        throws AmazonException
    {
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = null;
        boolean retry = true;

        while (retry)
        {
            try
            {
                doc = builder.build(new URL(requestURL));
                retry = false;
            } catch (IOException e)
            {
                String message = e.getMessage();
                if (message != null && message.indexOf("code: 5") != -1)
                {
                    LOG.warning(MessageFormat.format(Strings.error("amazon.ioexception.calling.amazon.service.retrying"),
                        message));

                    sleepBetweenCalls();
                    retry = true;
                } else throw new AmazonException(e);
            } catch (JDOMException e)
            {
                throw new AmazonException(e);
            }
        }

        return doc;
    }

    /**
     * Clears namespace from this element and all of its sub-elements.
     *
     * @param element element.
     */
    private void clearNamespace(Element element)
    {
        if (element == null) return;

        element.setNamespace(null);
        for (Object o : element.getChildren()) clearNamespace((Element)o);
    }

    /**
     * Converts response element to the item.
     *
     * @param itemElement   item element.
     * @param searchIndex   search index type.
     *
     * @return amazon item object.
     *
     * @throws MalformedURLException if item link is not correct.
     */
    private AmazonItem convertElementToItem(Element itemElement, AmazonSearchIndex searchIndex)
        throws MalformedURLException
    {
        String asin = itemElement.getChildText("ASIN");
        URL url = new URL(ITEM_URL_FORMAT.format(new Object[] { asin, affilateId }));

        AmazonItem item = new AmazonItem(asin, url, searchIndex);

        addAttributesToItem(item, itemElement.getChild("ItemAttributes"));
        addOfferSummaryToItem(item, itemElement);
        addImagesToItem(item, itemElement);

        return item;
    }

    /**
     * Takes attributes from ItemAttributes element and adds them to item.
     *
     * @param item              item.
     * @param attributesElement item attributes element.
     */
    private void addAttributesToItem(AmazonItem item, Element attributesElement)
    {
        if (attributesElement != null)
        {
            List children = attributesElement.getChildren();
            for (Object aChildren : children)
            {
                Element attributeElement = (Element)aChildren;
                item.addAttribute(attributeElement.getName(), attributeElement.getTextTrim());
            }
        }
    }

    /**
     * Adds offer summary information to the item.
     *
     * @param item          item.
     * @param itemElement   offer summary.
     */
    private void addOfferSummaryToItem(AmazonItem item, Element itemElement)
    {
        Element itemAttributesElement = itemElement.getChild("ItemAttributes");
        if (itemAttributesElement != null)
        {
            item.setListPrice(getPrice(itemAttributesElement.getChild("ListPrice")));
        }

        Element offerSummaryElement = itemElement.getChild("OfferSummary");
        if (offerSummaryElement != null)
        {
            item.setLowestNewPrice(getPrice(offerSummaryElement.getChild("LowestNewPrice")));
            item.setLowestUsedPrice(getPrice(offerSummaryElement.getChild("LowestUsedPrice")));
        }
    }

    /**
     * Takes the price from element.
     *
     * @param priceElement price element.
     *
     * @return returns formatted price or <code>NULL</code> if price is not found.
     */
    private String getPrice(Element priceElement)
    {
        return priceElement == null ? null : priceElement.getChildText("FormattedPrice");
    }

    /**
     * Adds image to item. Image details are taken from item element.
     *
     * @param item          item to add image to.
     * @param itemElement   item element.
     */
    private void addImagesToItem(AmazonItem item, Element itemElement)
    {
        item.setSmallImage(getImage(itemElement, "SmallImage"));
        item.setMediumImage(getImage(itemElement, "MediumImage"));
        item.setLargeImage(getImage(itemElement, "LargeImage"));
    }

    /**
     * Converts image element to details object.
     *
     * @param itemElement       item element to get image element from.
     * @param imageElementName  image element name.
     *
     * @return image details or <code>NULL</code> if element is not present.
     */
    private AmazonImageDetails getImage(Element itemElement, String imageElementName)
    {
        AmazonImageDetails imageDetails = null;

        Element imageElement = itemElement.getChild(imageElementName);
        if (imageElement != null)
        {
            String urlString = imageElement.getChildText("URL");
            String heightString = imageElement.getChildText("Height");
            String widthString = imageElement.getChildText("Width");

            try
            {
                URL url = new URL(urlString);
                int height = Integer.parseInt(heightString);
                int width = Integer.parseInt(widthString);
                imageDetails = new AmazonImageDetails(url, width, height);
            } catch (MalformedURLException e)
            {
                LOG.log(Level.WARNING, MessageFormat.format(
                    Strings.error("invalid.url"), urlString), e);
            } catch (NumberFormatException e)
            {
                LOG.warning(MessageFormat.format(
                    Strings.error("amazon.invalid.image.dimensions"),
                    widthString, heightString));
            }
        }

        return imageDetails;
    }

    /**
     * Sleeps between calls to AWS to follow the call frequency contract.
     */
    private static void sleepBetweenCalls()
    {
        try
        {
            Thread.sleep(TIME_BETWEEN_CALLS_MS);
        } catch (InterruptedException e)
        {
            LOG.warning(Strings.error("interrupted"));
        }
    }
}
