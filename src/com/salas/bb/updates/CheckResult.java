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
// $Id: CheckResult.java,v 1.3 2006/01/08 04:57:15 kyank Exp $
//

package com.salas.bb.updates;

import java.util.Map;

/**
 * <p>This is the informational structure containing the details on new version,
 * available for download, which was reported by the service. This structure
 * represents the response of the service in object form as the original
 * response presented as Vector/Table structure of data fields and completely
 * unusable.</p>
 *
 * <p>The information comes from {@link Checker} and being analyzed by the
 * application modules.</p>
 */
public class CheckResult
{
    /** Recent version number. The version of application available for download. */
    private String          recentVersion;

    /** Recent version release time stamp. */
    private long            releaseTime;

    /**
     * Map of distribution type to download locations.
     * Keys are <code>Location.TYPE_XYZ</code> and values are {@link Location} objects.
     */
    private Map             locations;

    /** List of changes in latest first order. */
    private VersionChange[] changes;

    /**
     * Creates results object.
     *
     * @param aRecentVersion    version name.
     * @param aReleaseTime      release time stamp.
     * @param aChanges          list of changes.
     * @param aLocations        map of distribution type to download locations.
     */
    public CheckResult(String aRecentVersion, long aReleaseTime, VersionChange[] aChanges,
        Map aLocations)
    {
        recentVersion = aRecentVersion;
        releaseTime = aReleaseTime;
        changes = aChanges;
        locations = aLocations;
    }

    /**
     * Returns recent version number.
     *
     * @return version number.
     */
    public String getRecentVersion()
    {
        return recentVersion;
    }

    /**
     * Returns release time.
     *
     * @return release time.
     */
    public long getReleaseTime()
    {
        return releaseTime;
    }

    /**
     * Returns list of changes.
     *
     * @return list of changes.
     */
    public VersionChange[] getChanges()
    {
        return changes;
    }

    /**
     * Returns map of distribution type to download locations.
     *
     * @return map of distribution type to download locations.
     */
    public Map getLocations()
    {
        return locations;
    }
}
