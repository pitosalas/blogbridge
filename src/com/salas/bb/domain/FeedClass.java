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
// $Id: FeedClass.java,v 1.6 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.domain;

/**
 * Classes of feeds. Classes may be OR'ed to produce full picture.
 */
public interface FeedClass
{
    /**
     * Channel with rating below the threshold.
     */
    int LOW_RATED       = 1;

    /**
     * Channel with all articles read.
     */
    int READ            = 2;

    /**
     * Invalid channel.
     */
    int INVALID         = 4;

    /**
     * Undiscovered channel.
     */
    int UNDISCOVERED    = 8;


    /** Disabled flag set. */
    int DISABLED          = 32;

    /**
     * Number of bits used by classes.
     */
    int SHIFT           = 6;

    /** The mask for all classes that can change as the result of an update. */
    int MASK_UPDATABLE = READ | INVALID;
    /** The mask used to clear the classes that can make a feed visible after update. */
    int MASK_UNUPDATABLE = 0xFFFFFFFF ^ MASK_UPDATABLE;
}
