// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: HotResultGroup.java,v 1.4 2007/10/03 11:46:49 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.salas.bb.search.ResultGroup;
import com.salas.bb.utils.IdentityList;

import java.net.URL;
import java.util.List;

/**
 * What's Hot results group.
 */
class HotResultGroup extends ResultGroup<HotResultItem> implements Comparable<HotResultGroup>
{
    private Engine.HotLink hotlink;
    private List<HotResultItem> allItems = new IdentityList<HotResultItem>();
    private boolean visible = false;
    private boolean resolved = false;

    public HotResultGroup(int seq, Engine.HotLink hotlink)
    {
        super(seq, null);

        this.hotlink = hotlink;
    }

    @Override
    public String getName()
    {
        return hotlink.getTitle();
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean setVisible(boolean visible)
    {
        boolean changed = false;
        if (this.visible != visible)
        {
            this.visible = visible;
            changed = true;
        }
        return changed;
    }

    public void addItem(HotResultItem item)
    {
        allItems.add(item);
    }

    public List<HotResultItem> getAllItems()
    {
        return allItems;
    }

    public boolean show(HotResultItem item)
    {
        boolean shown = false;

        if (!contains(item))
        {
            add(item);
            shown = true;
        }

        return shown;
    }

    public boolean hide(HotResultItem item)
    {
        return remove(item);
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public URL getLink()
    {
        return hotlink.getLink();
    }
    
    public void setResolvedTitle(String title)
    {
        resolved = true;
        hotlink.setPageTitle(title);
    }

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.<p>
     *
     * @param other the Object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     *         specified object.
     */
    public int compareTo(HotResultGroup other)
    {
        int v = ((Integer)other.size()).compareTo(size());
        if (v == 0 && getName() != null && other.getName() != null) v = getName().compareToIgnoreCase(other.getName());
        return v;
    }
}
