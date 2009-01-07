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
// $Id: IHighlightsAdvisor.java,v 1.4 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.utils.TextRange;

/**
 * Advisor knows about current keywords and search-words. It is capable of
 * telling the ranges to highlight in given text.
 */
public interface IHighlightsAdvisor
{

    /**
     * Returns the ranges to highlight in text as search-words.
     *
     * @param aText text.
     *
     * @return ranges.
     */
    TextRange[] getSearchwordsRanges(String aText);
}
