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
// $Id: IDates.java,v 1.2 2006/01/08 04:48:16 kyank Exp $
//

package com.salas.bb.domain.query.general;

/**
 * Set of dates values names.
 */
public interface IDates
{
    /** Synthetic date value: Today */
    String VALUE_TODAY = "today";
    /** Synthetic date value: Yesterday */
    String VALUE_YESTERDAY = "yesterday";
    /** Synthetic date value: Last Week */
    String VALUE_LAST_WEEK = "lastWeek";
    /** Synthetic date value: Two Weeks Ago */
    String VALUE_TWO_WEEKS_AGO = "twoWeeksAgo";
}
