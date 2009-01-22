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
// $Id: ResourceID.java,v 1.20 2006/12/11 13:54:37 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.uif.application.ResourceIDs;

/**
 * Collection of resource ID's.
 */
public class ResourceID extends ResourceIDs
{
    /** Path of tutorial. */
    public static final String URL_TUTORIAL = "tutorial.path";

    /** Path to about text. */
    public static final String URL_ABOUT = "about.path";

    /** URL of starting points index. */
    public static final String URL_STARTING_POINTS_INDEX = "server.points.index.url";

    /** URL of BlogBridge service entry point. */
    public static final String URL_SERVICE = "server.service.url";

    /** URL of HTML article style sheet. */
    public static final String URL_DEFAULT_STYLESHEET = "default.stylesheet.url";
    /** URL of Post editor style sheet. */
    public static final String URL_EDITOR_STYLESHEET = "editor.stylesheet.url";

    /** Icon of unfinished step for intstaller progress dialog. */
    public static final String ICON_INSTALLER_STEP_UNFINISHED = "installer.step.unfinished.icon";

    /** Icon of finished step for intstaller progress dialog. */
    public static final String ICON_INSTALLER_STEP_FINISHED = "installer.step.finished.icon";

    /** Icon of failed step for installer progress dialog. */
    public static final String ICON_INSTALLER_STEP_FAILED = "installer.step.failed.icon";

    /** Default key of guide icon. */
    public static final String ICON_GUIDE_DEFAULT_KEY = "cg.default.icon";

    /** Icon of collapsed articles group. */
    public static final String ICON_GROUP_COLLAPSED = "collapsed.icon";

    /** Icon of collapsed articles group (mouseover). */
    public static final String ICON_GROUP_COLLAPSED_OVER = "collapsed.over.icon";

    /** Icon of expanded articles group. */
    public static final String ICON_GROUP_EXPANDED = "expanded.icon";

    /** Icon of expanded articles group (mouseover). */
    public static final String ICON_GROUP_EXPANDED_OVER = "expanded.over.icon";

    /** Icon for preferences page. */
    public static final String ICON_PREFERENCES = "preferences.icon";

    /** Icon for 'connected' state of connection indicator. */
    public static final String ICON_CONNECTED = "connected.icon";

    /** Icon for 'disconnected' state of connection indicator. */
    public static final String ICON_DISCONNECTED = "disconnected.icon";

    /** Accepted license version. */
    public static final String VERSION_ACCEPTED_LICENSE = "setup.acceptedLicense";

    /** Icon for active state of webstat activity indicator. */
    public static final String ICON_ACTIVITY_NETWORK_ACTIVE = "actind.network.active";

    /** Icon for passive state of webstat activity indicator. */
    public static final String ICON_ACTIVITY_NETWORK_PASSIVE = "actind.network.passive";

    /** Icon for active state of disk activity indicator. */
    public static final String ICON_ACTIVITY_DISK_ACTIVE = "actind.disk.active";

    /** Icon for passive state of disk activity indicator. */
    public static final String ICON_ACTIVITY_DISK_PASSIVE = "actind.disk.passive";

    /** URL of license welcome message text. */
    public static final String LICENSE_WELCOME_TEXT_URL = "license.welcome.text.url";

    /** Icon for query feed Feedster. */
    public static final String ICON_QUERYFEED_FEEDSTER = "queryfeed.feedster.icon";
    /** Icon for query feed Technorati. */
    public static final String ICON_QUERYFEED_TECHNORATI = "queryfeed.technorati.icon";
    /** Icon for query feed Flickr. */
    public static final String ICON_QUERYFEED_FLICKR = "queryfeed.flickr.icon";
    /** Icon for query feed Findory. */
    public static final String ICON_QUERYFEED_FINDORY = "queryfeed.findory.icon";
    /** Icon for query feed Delicious. */
    public static final String ICON_QUERYFEED_DELICIOUS = "queryfeed.delicious.icon";
    /** Icon for query feed Connotea. */
    public static final String ICON_QUERYFEED_CONNOTEA = "queryfeed.connotea.icon";
    /** Icon for query feed Amazon. */
    public static final String ICON_QUERYFEED_AMAZON = "queryfeed.amazon.icon";
    /** Icon for query feed Google BlogSearch. */
    public static final String ICON_QUERYFEED_GOOGLE = "queryfeed.google.icon";
    /** Icon for query feed Digg. */
    public static final String ICON_QUERYFEED_DIGG = "queryfeed.digg.icon";
    /** Icon for query feed Monster. */
    public static final String ICON_QUERYFEED_MONSTER = "queryfeed.monster.icon";
    /** Icon for search feed My own feeds. */
    public static final String ICON_SEARCHFEED_MYFEEDS = "searchfeed.myfeeds.icon";

    /** The list of file extensions not to start discovery for. */
    public static final String NO_DISCOVERY_EXTENSIONS = "no.discovery.extensions";

    /** Icon for twitter. */
    public static final String ICON_TWITTER = "twitter.icon";
}
