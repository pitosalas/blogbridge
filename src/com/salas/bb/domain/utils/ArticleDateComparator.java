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
// $Id: ArticleDateComparator.java,v 1.5 2007/02/05 12:34:42 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.IArticle;

import java.util.Comparator;

/**
 * Compares two articles by their publication dates.
 */
public class ArticleDateComparator implements Comparator<IArticle>
{
    private static final int EQ_EQUAL = 0;
    private static final int EQ_LESS  = -1;

    private boolean newerFirst;

    /**
     * Sometimes we can't afford ourseleves to report equality (sorting is good ex.)
     * so we can choose to report something different.
     */
    private int     equalResult;

    /**
     * Creates comparator where older dates go in first order.
     */
    public ArticleDateComparator()
    {
        this(false);
    }

    /**
     * Creates comparator where the dates order depends on the setting.
     *
     * @param aNewerFirst TRUE to let the newer dates go first.
     */
    public ArticleDateComparator(boolean aNewerFirst)
    {
        this(aNewerFirst, true);
    }

    /**
     * Creates comparator where the dates order depends on the setting.
     *
     * @param aNewerFirst     <code>TRUE</code> to let the newer dates go first.
     * @param aCanReportEqual <code>TRUE</code> if we can report equality.
     */
    public ArticleDateComparator(boolean aNewerFirst, boolean aCanReportEqual)
    {
        newerFirst = aNewerFirst;
        equalResult = aCanReportEqual ? EQ_EQUAL : EQ_LESS;
    }

    /**
     * Compares two articles by their dates. NULL-date is always bigger.
     *
     * @param a1 first article object.
     * @param a2 second article object.
     *
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second.
     */
    public int compare(IArticle a1, IArticle a2)
    {
        java.util.Date d1 = a1.getPublicationDate();
        java.util.Date d2 = a2.getPublicationDate();

        int result = d1 == null
            ? d2 == null ? 0 : 1
            : d2 == null ? -1 : d1.compareTo(d2);

        if (result == 0) result = equalResult;

        return newerFirst ? -result : result;
    }
}
