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
// $Id: BooksHTMLFormatter.java,v 1.5 2006/05/31 11:28:30 spyromus Exp $
//

package com.salas.bb.utils.amazon;

import com.salas.bb.utils.i18n.Strings;

import java.util.List;

/**
 * Formats Amazon items as HTML.
 */
class BooksHTMLFormatter implements IHTMLFormatter
{
    private static final String MISSING_IMAGE_URL =
        "http://www.blogbridge.com/images/no-img-110x165.gif";

    private static final int MISSING_IMAGE_WIDTH    = 110;
    private static final int IMAGE_CELL_WIDTH       = 130;

    /**
     * Formats item as HTML string.
     *
     * @param item item to format.
     *
     * @return HTML or <code>NULL</code> if item wasn't specified.
     */
    public String format(AmazonItem item)
    {
        String text = null;

        if (item != null)
        {
            StringBuffer buf = new StringBuffer();

            buf.append("<table border='0' width='100%'><tr>");

            outputImageCell(item, buf);
            outputBookDetailsCell(item, buf);

            buf.append("</tr></table>");

            buf.append("<br><a href='").append(item.getURL()).append("'>");
            buf.append(Strings.message("amazon.buy.now")).append("</a>");

            text = buf.toString();
        }

        return text;
    }

    /**
     * Outputs details cell.
     *
     * @param item  item to take details from.
     * @param buf   buffer to write to.
     */
    private void outputBookDetailsCell(AmazonItem item, StringBuffer buf)
    {
        String authors = getAuthors(item);
        String publisher = getTextAttribute(item, "Publisher");
        String isbn = getTextAttribute(item, "ISBN");
        String pubDate = getTextAttribute(item, "PublicationDate");
        String pages = getTextAttribute(item, "NumberOfPages");

        String listPrice = item.getListPrice();
        boolean listP = listPrice != null;
        String newPrice = item.getLowestNewPrice();
        boolean newP = newPrice != null;
        String usedPrice = item.getLowestUsedPrice();
        boolean usedP = usedPrice != null;

        buf.append("<td valign='top'>");
        if (authors != null && authors.trim().length() > 0)
        {
            buf.append("<i>by ").append(authors).append("</i><br><br>");
        }

        buf.append("<b>").append(Strings.message("amazon.price")).append("</b> ");

        if (listP) buf.append(listPrice).append(" ").append(Strings.message("amazon.price.list"));
        if (newP)
        {
            if (listP) buf.append(", ");
            buf.append(newPrice).append(" ").append(Strings.message("amazon.new"));
        }
        if (usedP)
        {
            if (listP || newP) buf.append(", ");
            buf.append(usedPrice).append(" ").append(Strings.message("amazon.used"));
        } else if (!newP && !listP) buf.append(Strings.message("amazon.not.yet.released"));
        buf.append("<br>");

        if (pages != null) buf.append("<b>").append(Strings.message("amazon.pages")).append("</b> ").append(pages).append("<br>");
        if (publisher != null) buf.append("<b>").append(Strings.message("amazon.publisher")).append("</b> ").append(publisher).append("<br>");
        if (isbn != null) buf.append("<b>").append(Strings.message("amazon.isbn")).append("</b> ").append(isbn).append("<br>");
        if (pubDate != null) buf.append("<b>").append(Strings.message("amazon.publication.date")).append("</b> ").append(pubDate);

        buf.append("</td>");
    }

    /**
     * Returns comma-separated list of authors.
     *
     * @param item item.
     *
     * @return list of authors.
     */
    private String getAuthors(AmazonItem item)
    {
        List values = item.getAttributeValues("Author");
        StringBuffer authors = new StringBuffer();

        if (values != null && values.size() > 0)
        {
            authors.append(values.get(0).toString());
            for (int i = 1; i < values.size(); i++)
            {
                authors.append(", ").append(values.get(i).toString());
            }
        }

        return authors.toString();
    }

    /**
     * Gets first value of the attribute from item.
     *
     * @param item          item.
     * @param attributeName attribute name.
     *
     * @return value or <code>NULL</code> if attribute is not found.
     */
    private String getTextAttribute(AmazonItem item, String attributeName)
    {
        List values = item.getAttributeValues(attributeName);

        return values == null || values.size() == 0 ? null : values.get(0).toString();
    }

    /**
     * Outputs the image from item if it's present.
     *
     * @param item  item.
     * @param buf   buffer to add table cell to.
     */
    private void outputImageCell(AmazonItem item, StringBuffer buf)
    {
        AmazonImageDetails image = item.getMediumImage();
        String url = image == null ? MISSING_IMAGE_URL : image.getURL().toString();
        int width = image == null ? MISSING_IMAGE_WIDTH : image.getWidth();
        width = Math.max(IMAGE_CELL_WIDTH, width);

        buf.append("<td valign='top' width='").append(width).append("'>");
        buf.append("<a href='").append(item.getURL()).append("'>");
        buf.append("<img src='").append(url).append("' border='0'>");
        buf.append("</a></td>");
    }
}
