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
// $Id: IFeedDisplayConstants.java,v 1.7 2008/02/29 06:10:28 spyromus Exp $
//

package com.salas.bb.views.feeds;

/**
 * List of available article view modes.
 */
public interface IFeedDisplayConstants
{
    /** Title-only view mode. */
    int MODE_MINIMAL            = 0;
    /** Brief view mode -- short plain text excerpt. */
    int MODE_BRIEF              = 1;
    /** Full view mode -- complete article text. */
    int MODE_FULL               = 2;

    /** Show all articles. */
    int FILTER_ALL              = 0;
    /** Show unread articles only. */
    int FILTER_UNREAD           = 1;
    /** Show pinned articles only. */
    int FILTER_PINNED           = 2;
    /** Show positive articles only. */
    int FILTER_POSITIVE         = 3;
    /** Show negative articles only. */
    int FILTER_NEGATIVE         = 4;
    /** Show non-negative articles only. */
    int FILTER_NON_NEGATIVE     = 5;
}
