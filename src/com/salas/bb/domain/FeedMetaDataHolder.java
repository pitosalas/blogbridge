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
// $Id: FeedMetaDataHolder.java,v 1.5 2006/01/18 10:17:33 spyromus Exp $
//

package com.salas.bb.domain;

import com.jgoodies.binding.beans.Model;
import com.salas.bb.utils.SoftPCLWrapper;
import com.salas.bb.utils.CommonUtils;

import java.net.URL;
import java.beans.PropertyChangeListener;

/**
 * Holder of meta information for a blog/feed.
 */
public final class FeedMetaDataHolder extends Model
{
    public static final String PROP_XML_URL             = "xmlURL";
    public static final String PROP_HTML_URL            = "htmlURL";
    public static final String PROP_INBOUND_LINKS       = "inboundLinks";
    public static final String PROP_AUTHOR              = "author";
    public static final String PROP_DESCRIPTION         = "description";
    public static final String PROP_TITLE               = "title";
    public static final String PROP_LAST_UPDATE_TIME    = "lastUpdateTime";
    public static final String PROP_INVALID             = "invalid";
    public static final String PROP_COMPLETE            = "complete";

    private static final String INLINKS_UNKNOWN             = "Unknown to the service";
    private static final String INLINKS_AWAITING_DISCOVERY  = "Awaiting discovery finish";

    private boolean     complete;
    private Boolean     invalid;
    private long        lastUpdateTime;

    private URL         xmlURL;
    private URL         htmlURL;
    private Integer     inboundLinks;
    private String      title;
    private String      author;
    private String      description;

    private String      requestedBy;

    /**
     * Creates holder.
     */
    public FeedMetaDataHolder()
    {
        complete = false;
        invalid = null;
        lastUpdateTime = -1;

        xmlURL = null;
        htmlURL = null;
        inboundLinks = null;

        title = null;
        author = null;
        description = null;

        requestedBy = null;
    }

    /**
     * Returns <code>TRUE</code> if meta-data is invalid.
     *
     * @return <code>TRUE</code> if meta-data is invalid.
     */
    public Boolean isInvalid()
    {
        return invalid;
    }

    /**
     * Returns <code>TRUE</code> only if meta-data is discovered as invalid.
     *
     * @return <code>TRUE</code> only if meta-data is discovered as invalid.
     */
    public boolean isDiscoveredInvalid()
    {
        return invalid != null && invalid.booleanValue();
    }

    /**
     * Returns <code>TRUE</code> only if meta-data is discovered as valid.
     *
     * @return <code>TRUE</code> only if meta-data is discovered as valid.
     */
    public boolean isDiscoveredValid()
    {
        return invalid != null && !invalid.booleanValue();
    }

    /**
     * Sets <code>TRUE</code> if meta-data is invalid.
     *
     * @param aInvalid <code>TRUE</code> if meta-data is invalid.
     */
    public void setInvalid(boolean aInvalid)
    {
        Boolean oldValue = invalid;
        invalid = Boolean.valueOf(aInvalid);

        firePropertyChange(PROP_INVALID, oldValue, invalid);
    }

    /**
     * Returns <code>TRUE</code> when discovery is complete.
     *
     * @return <code>TRUE</code> when discovery is complete.
     */
    public boolean isComplete()
    {
        return complete;
    }

    /**
     * Sets the completeness state of this holder.
     *
     * @param aComplete <code>TRUE</code> when discovery complete.
     */
    public void setComplete(boolean aComplete)
    {
        boolean oldValue = complete;
        complete = aComplete;

        firePropertyChange(PROP_COMPLETE, oldValue, complete);
    }

    /**
     * Returns XML URL of the feed.
     *
     * @return XML URL.
     */
    public URL getXmlURL()
    {
        return xmlURL;
    }

    /**
     * Sets new XML URL.
     *
     * @param aXmlURL XML URL.
     */
    public void setXmlURL(URL aXmlURL)
    {
        URL oldURL = xmlURL;
        xmlURL = aXmlURL;

        firePropertyChange(PROP_XML_URL, oldURL, xmlURL);
    }

    /**
     * Returns HTML URL.
     *
     * @return HTML URL.
     */
    public URL getHtmlURL()
    {
        return htmlURL;
    }

    /**
     * Sets HTML URL.
     *
     * @param aHtmlURL HTML URL.
     */
    public void setHtmlURL(URL aHtmlURL)
    {
        URL oldURL = htmlURL;
        htmlURL = aHtmlURL;

        firePropertyChange(PROP_HTML_URL, oldURL, htmlURL);
    }

    /**
     * Returns textual representation of inbound links counter (including messages for the
     * reserved states).
     *
     * @return number of links or status.
     */
    public String getTextualInboundLinks()
    {
        int inLinks = inboundLinks == null ? -1 : inboundLinks.intValue();

        String msg;
        switch (inLinks)
        {
            case -2:
                msg = INLINKS_UNKNOWN;
                break;
            case -1:
                msg = INLINKS_AWAITING_DISCOVERY;
                break;
            default:
                msg = Integer.toString(inLinks);
                break;
        }

        return msg;
    }

    /**
     * Returns inbound links.
     *
     * @return inbound links.
     */
    public Integer getInboundLinks()
    {
        return inboundLinks;
    }

    /**
     * Sets inbound links number.
     *
     * @param aLinks    number of inbound links.
     */
    public void setInboundLinks(Integer aLinks)
    {
        Integer oldValue = inboundLinks;
        inboundLinks = aLinks;

        firePropertyChange(PROP_INBOUND_LINKS, oldValue, inboundLinks);
    }

    /**
     * Returns author.
     *
     * @return author.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Sets the name of author.
     *
     * @param aAuthor author.
     */
    public void setAuthor(String aAuthor)
    {
        String oldValue = author;
        author = aAuthor;

        firePropertyChange(PROP_AUTHOR, oldValue, author);
    }

    /**
     * Returns description.
     *
     * @return description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param aDescription description.
     */
    public void setDescription(String aDescription)
    {
        String oldValue = description;
        description = aDescription;

        firePropertyChange(PROP_DESCRIPTION, oldValue, description);
    }

    /**
     * Returns title.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param aTitle title.
     */
    public void setTitle(String aTitle)
    {
        String oldValue = title;
        title = aTitle;

        firePropertyChange(PROP_TITLE, oldValue, title);
    }

    /**
     * Returns the time of last update.
     *
     * @return last update time or <code>-1</code> if not updated yet.
     */
    public long getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    /**
     * Sets the time of last update.
     *
     * @param time time of last update.
     */
    public void setLastUpdateTime(long time)
    {
        long oldValue = lastUpdateTime;
        lastUpdateTime = time;

        firePropertyChange(PROP_LAST_UPDATE_TIME, oldValue, lastUpdateTime);
    }

    /**
     * Returns the name of object this holder was originally requested.
     *
     * @return object name.
     */
    public String getRequestedBy()
    {
        return requestedBy;
    }

    /**
     * Sets the name of object this holder was originally requested.
     *
     * @param aRequestedBy requester.
     */
    public void setRequestedBy(String aRequestedBy)
    {
        requestedBy = aRequestedBy;
    }

    /**
     * Compares this holder with another. Holders are equal when their URL's match.
     *
     * @param o other holder.
     *
     * @return <code>TRUE</code> if holders are equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final FeedMetaDataHolder holder = (FeedMetaDataHolder)o;

        if (CommonUtils.areDifferent(htmlURL, holder.htmlURL)) return false;
        return !CommonUtils.areDifferent(xmlURL, holder.xmlURL);
    }

    /**
     * Returns the hash code of this holder. The hash code is likely to change
     * over the time, so, please keep this in mind when putting them in
     * long-live maps or lists as they don't recalculate codes.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        int result;
        result = (xmlURL != null ? xmlURL.toString().hashCode() : 0);
        result = 29 * result + (htmlURL != null ? htmlURL.toString().hashCode() : 0);
        return result;
    }

    /**
     * Returns <code>TRUE</code> if any feeds are listening to this holder.
     *
     * @return <code>TRUE</code> if any feeds are listening to this holder.
     */
    public boolean hasListeningFeeds()
    {
        boolean hasFeeds = false;

        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        for (int i = 0; !hasFeeds && i < listeners.length; i++)
        {
            PropertyChangeListener listener = listeners[i];
            hasFeeds = listener instanceof IFeed;
        }

        return hasFeeds;
    }

    /**
     * Soft wrapper for meta-data change listener which knows how to unregister itself from
     * <code>FeedMetaData</code> emitter.
     */
    public static class SoftMDCLWrapper extends SoftPCLWrapper
    {
        /**
         * Creates wrapper.
         *
         * @param listener actual listener.
         */
        public SoftMDCLWrapper(PropertyChangeListener listener)
        {
            super(listener);
        }

        /**
         * Removes listener from feed meta-data.
         *
         * @param source source of events.
         */
        protected void removeThisListener(Object source)
        {
            FeedMetaDataHolder cmd = (FeedMetaDataHolder)source;
            cmd.removePropertyChangeListener(this);
        }
    }
}
