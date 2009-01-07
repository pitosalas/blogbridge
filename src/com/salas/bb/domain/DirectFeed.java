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
// $Id: DirectFeed.java,v 1.43 2007/11/07 17:16:48 spyromus Exp $
//
package com.salas.bb.domain;

import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.parser.Channel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Feed which represents data taken from some direct resource on the web -- RSS, RDF or Atom feed.
 */
public class DirectFeed extends NetworkFeed implements ITaggable
{
    public static final String PROP_XML_URL                     = "xmlURL";
    public static final String PROP_SITE_URL                    = "siteURL";
    public static final String PROP_DEAD                        = "dead";
    public static final String PROP_CUSTOM_AUTHOR               = "customAuthor";
    public static final String PROP_BASE_AUTHOR                 = "baseAuthor";
    public static final String PROP_AUTHOR                      = "author";
    public static final String PROP_CUSTOM_DESCRIPTION          = "customDescription";
    public static final String PROP_BASE_DESCRIPTION            = "baseDescription";
    public static final String PROP_DESCRIPTION                 = "description";
    public static final String PROP_CUSTOM_TITLE                = "customTitle";
    public static final String PROP_BASE_TITLE                  = "baseTitle";
    public static final String PROP_INLINKS                     = "inLinks";
    public static final String PROP_LAST_METADATA_UPDATE_TIME   = "lastMetaDataUpdateTime";
    public static final String PROP_READING_LIST                = "readingList";
    public static final String PROP_DISABLED                    = "disabled";
    public static final String PROP_SYNC_HASH                   = "syncHash";

    /**
     * Default value for dead-flag.
     */
    public static final boolean DEFAULT_DEAD = false;

    private String baseTitle;
    private String baseAuthor;
    private String baseDescription;
    private URL xmlURL;
    private URL siteURL;

    private String customTitle;
    private String customDescription;
    private String customAuthor;

    private int inLinks;

    private boolean dead;

    private FeedMetaDataHolder metaData;
    private final MetaDataListener metaDataListener;

    private String[]        sharedTags;
    private String[]        userTags;
    private boolean         unsavedUserTags;
    private String          tagsDescription;
    private String          tagsExtended;

    private int syncHash;

    /**
     * Creates direct network feed.
     */
    public DirectFeed()
    {
        dead = DEFAULT_DEAD;

        baseTitle = null;
        baseDescription = null;
        baseAuthor = null;
        metaDataListener = new MetaDataListener();

        FeedMetaDataHolder holder = new FeedMetaDataHolder();
        holder.setComplete(true);
        setMetaData(holder);

        inLinks = -1;

        userTags = null;
        sharedTags = null;
        unsavedUserTags = false;
        tagsDescription = null;

        syncHash = 0;
    }

    /**
     * Returns meta-data object assigned to this feed.
     *
     * @return meta-data.
     */
    public FeedMetaDataHolder getMetaDataHolder()
    {
        return metaData;
    }

    /**
     * Registers meta-data object assigned to this feed.
     *
     * @param aMetaData meta-data.
     */
    public void setMetaData(FeedMetaDataHolder aMetaData)
    {
        if (metaData != null)
        {
            metaData.removePropertyChangeListener(metaDataListener);
        }

        metaData = aMetaData;

        if (metaData != null)
        {
            metaData.addPropertyChangeListener(metaDataListener);

            // Copy some properties to the feed
            URL newXmlURL = metaData.getXmlURL();
            if (xmlURL == null && newXmlURL != null) setXmlURL(newXmlURL);

            String title = metaData.getTitle();
            if (title != null) setBaseTitle(title);

            if (baseAuthor == null) setBaseAuthor(metaData.getAuthor());
            if (baseDescription == null) setBaseDescription(metaData.getDescription());
            if (siteURL == null) setSiteURL(metaData.getHtmlURL());

            if (inLinks == -1)
            {
                Integer inboundLinks = metaData.getInboundLinks();
                if (inboundLinks != null) setInLinks(inboundLinks);
            }
        }

        invalidateVisibilityCache();
    }

    /**
     * Returns the number of inbound links pointing to this feed.
     *
     * @return -1 for unresolved, -2 for resolved as "unknown" or positive number for count.
     */
    public int getInLinks()
    {
        return inLinks;
    }

    /**
     * Sets number of inbound links (-1 - unresolved, -2 - resolved to be "unknown",
     * positive number - number of inlinks).
     *
     * @param aInLinks number of inlinks.
     */
    public void setInLinks(int aInLinks)
    {
        int oldValue = inLinks;
        inLinks = aInLinks;
        firePropertyChanged(PROP_INLINKS, new Integer(oldValue), new Integer(inLinks));
    }

    /**
     * Returns last update time of meta-data.
     *
     * @return last update time.
     */
    public long getLastMetaDataUpdateTime()
    {
        return metaData == null ? -1 : metaData.getLastUpdateTime();
    }

    /**
     * Sets the last update time of meta-data.
     *
     * @param time last update time of meta-data.
     */
    public void setLastMetaDataUpdateTime(long time)
    {
        // It will produce all necessary property change events
        // as the meta-data object events are being listened by
        // MetaDataListener.
        if (metaData != null) metaData.setLastUpdateTime(time);
    }

    /**
     * Gets XML URL.
     *
     * @return URL.
     */
    public URL getXmlURL()
    {
        return xmlURL;
    }

    /**
     * Sets the XML URL.
     *
     * @param url new XML URL.
     */
    public void setXmlURL(URL url)
    {
        URL oldXmlURL = xmlURL;
        if (oldXmlURL != url) xmlURL = CommonUtils.intern(url);

        firePropertyChanged(PROP_XML_URL, oldXmlURL, xmlURL);

        setInvalidnessReason(null);
        setLastPollTime(DEFAULT_LAST_POLL_TIME);
        setInitTime(INIT_TIME_UNINITIALIZED);
    }

    /**
     * Returns URL of the associated site.
     *
     * @return site URL.
     */
    public URL getSiteURL()
    {
        return siteURL;
    }

    /**
     * Sets the Site URL.
     *
     * @param url new Site URL.
     */
    public void setSiteURL(URL url)
    {
        URL oldSiteURL = siteURL;
        if (oldSiteURL != url) siteURL = CommonUtils.intern(url);

        firePropertyChanged(PROP_SITE_URL, oldSiteURL, siteURL);
    }

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    public String getTitle()
    {
        String value = customTitle;

        if (value == null) value = baseTitle;
        if (value == null)
        {
            URL url = getXmlURL();
            value = url == null ? null : url.toString();
        }

        return value;
    }

    /**
     * Returns description of the feed.
     *
     * @return description of the feed.
     */
    public String getDescription()
    {
        String value = customDescription;

        if (value == null) value = baseDescription;

        return value;
    }

    /**
     * Returns the author of the feed.
     *
     * @return author of the feed.
     */
    public String getAuthor()
    {
        String value = customAuthor;

        if (value == null) value = baseAuthor;

        return value;
    }

    /**
     * Returns base title taken from feed.
     *
     * @return base title.
     */
    public String getBaseTitle()
    {
        return baseTitle;
    }

    /**
     * Sets base title.
     *
     * @param aBaseTitle base title.
     */
    public void setBaseTitle(String aBaseTitle)
    {
        String oldBaseTitle = baseTitle;
        String oldTitle = getTitle();
        baseTitle = aBaseTitle;
        String newTitle = getTitle();

        firePropertyChanged(PROP_BASE_TITLE, oldBaseTitle, baseTitle);
        firePropertyChanged(PROP_TITLE, oldTitle, newTitle);
    }

    /**
     * Returns base description.
     *
     * @return base description.
     */
    public String getBaseDescription()
    {
        return baseDescription;
    }

    /**
     * Sets base description.
     *
     * @param aBaseDescription base description.
     */
    public void setBaseDescription(String aBaseDescription)
    {
        String oldBaseDescription = baseDescription;
        String oldDescription = getDescription();
        baseDescription = aBaseDescription;
        String newDescription = getDescription();

        firePropertyChanged(PROP_BASE_DESCRIPTION, oldBaseDescription, baseDescription);
        firePropertyChanged(PROP_DESCRIPTION, oldDescription, newDescription);
    }

    /**
     * Returns base author.
     *
     * @return base author.
     */
    public String getBaseAuthor()
    {
        return baseAuthor;
    }

    /**
     * Sets base author.
     *
     * @param aBaseAuthor base author.
     */
    public void setBaseAuthor(String aBaseAuthor)
    {
        String oldBaseAuthor = baseAuthor;
        String oldAuthor = getAuthor();
        baseAuthor = aBaseAuthor;
        String newAuthor = getAuthor();

        firePropertyChanged(PROP_BASE_AUTHOR, oldBaseAuthor, baseAuthor);
        firePropertyChanged(PROP_AUTHOR, oldAuthor, newAuthor);
    }

    /**
     * Returns <code>TRUE</code> if feed was marked as dead.
     *
     * @return <code>TRUE</code> if feed was marked as dead.
     */
    public boolean isDead()
    {
        return dead;
    }

    /**
     * Marks/unmarks the feed as dead.
     *
     * @param aDead TRUE to mark as dead.
     */
    public void setDead(boolean aDead)
    {
        if (dead != aDead)
        {
            dead = aDead;
            firePropertyChanged(PROP_DEAD, !dead, dead);
        }
    }

    /**
     * Returns TRUE if this feed is updatable, meaning that it's not invalid for some reason and
     * it's proper time to call <code>update()</code> method. The behaviod may differ if the update
     * operation was called directly to this particular feed and not as a part of a bigger update
     * operation (update guide or update all).
     *
     * @param direct if TRUE then the update was requested directly (not through guide/set or by
     *               periodic check).
     */
    protected boolean isUpdatable(boolean direct)
    {
        return (!isDynamic() || !isDisabled()) && super.isUpdatable(direct);
    }

    /**
     * Updates the feed properties from the channel object.
     *
     * @param channel channel object.
     */
    protected void updateFeed(Channel channel)
    {
        super.updateFeed(channel);

        String cTitle = channel.getTitle();
        String cDescription = channel.getDescription();
        String cAuthor = channel.getAuthor();
        URL cSiteURL = channel.getSiteURL();

        if (cTitle != null) setBaseTitle(cTitle);
        if (cDescription != null) setBaseDescription(cDescription);
        if (cAuthor != null) setBaseAuthor(cAuthor);
        if (cSiteURL != null) setSiteURL(cSiteURL);
    }

    /**
     * Handles permanent redirection to a new URL. This method is required to be overriden by
     * sub-classes if they wish to handle redirects.
     *
     * @param newXmlURL new URL.
     */
    protected void redirected(URL newXmlURL)
    {
        // Do not allow redirection when the feed is connected to some reading list
        // because we rely on its XML URL when checking for updates.
        if (getReadingLists().length == 0) setXmlURL(newXmlURL);
    }

    // ---------------------------------------------------------------------------------------------
    // Custom Values
    // ---------------------------------------------------------------------------------------------

    /**
     * Sets custom author name.
     *
     * @param aCustomAuthor custom author name.
     */
    public void setCustomAuthor(String aCustomAuthor)
    {
        String oldCustomAuthor = customAuthor;
        String oldAuthor = getAuthor();
        customAuthor = aCustomAuthor;
        String newAuthor = getAuthor();

        firePropertyChanged(PROP_CUSTOM_AUTHOR, oldCustomAuthor, customAuthor, true, false);
        firePropertyChanged(PROP_AUTHOR, oldAuthor, newAuthor);
    }

    /**
     * Returns custom author.
     *
     * @return custom author.
     */
    public String getCustomAuthor()
    {
        return customAuthor;
    }

    /**
     * Sets custom description for the feed.
     *
     * @param aCustomDescription custom description text.
     */
    public void setCustomDescription(String aCustomDescription)
    {
        String oldCustomDescription = customDescription;
        String oldDescription = getDescription();
        customDescription = aCustomDescription;
        String newDescription = getDescription();

        firePropertyChanged(PROP_CUSTOM_DESCRIPTION, oldCustomDescription, customDescription, true, false);
        firePropertyChanged(PROP_DESCRIPTION, oldDescription, newDescription);
    }

    /**
     * Returns custom description.
     *
     * @return custom description.
     */
    public String getCustomDescription()
    {
        return customDescription;
    }

    /**
     * Sets custom title feed the feed.
     *
     * @param aCustomTitle custom title.
     */
    public void setCustomTitle(String aCustomTitle)
    {
        String oldCustomTitle = customTitle;
        String oldTitle = getTitle();
        customTitle = aCustomTitle;
        String newTitle = getTitle();

        firePropertyChanged(PROP_CUSTOM_TITLE, oldCustomTitle, customTitle, true, false);
        firePropertyChanged(PROP_TITLE, oldTitle, newTitle);
    }

    /**
     * Returns custom title.
     *
     * @return custom title.
     */
    public String getCustomTitle()
    {
        return customTitle;
    }

    /**
     * Returns textual representation of inbound links counter (including messages for the
     * reserved states).
     *
     * @return number of links or status.
     */
    public String getTextualInboundLinks()
    {
        String msg;
        switch (inLinks)
        {
            case -2:
                msg = Strings.message("inlinks.unknown.to.service");
                break;
            case -1:
                msg = Strings.message("inlinks.awaiting.discovery.finish");
                break;
            default:
                msg = Integer.toString(inLinks);
                break;
        }

        return msg;
    }

    // ---------------------------------------------------------------------------------------------

    private final List<ReadingList> readingLists = new CopyOnWriteArrayList<ReadingList>();
    private boolean disabled;

    /**
     * Returns a reading lists this feed belongs to.
     *
     * @return reading list association.
     */
    public ReadingList[] getReadingLists()
    {
        return readingLists.toArray(new ReadingList[0]);
    }

    /**
     * Adds new reading list association.
     *
     * @param list reading list.
     */
    public void addReadingList(ReadingList list)
    {
        synchronized (readingLists)
        {
            if (!readingLists.contains(list)) readingLists.add(list);
        }
    }

    /**
     * Removes the reading list association.
     *
     * @param list reading list.
     */
    public void removeReadingList(ReadingList list)
    {
        synchronized (readingLists)
        {
            readingLists.remove(list);
        }
    }

    /**
     * Removes all reading lists references.
     */
    public void removeAllReadingLists()
    {
        synchronized (readingLists)
        {
            readingLists.clear();
        }
    }

    /**
     * Returns <code>TRUE</code> if this feed is marked as disabled.
     *
     * @return <code>TRUE</code> if this feed is marked as disabled.
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Disables / enables feed.
     *
     * @param aDisabled <code>TRUE</code> to disable feed.
     */
    public void setDisabled(boolean aDisabled)
    {
        boolean old = disabled;
        disabled = aDisabled;

        firePropertyChanged(PROP_DISABLED, old, disabled, true, true);
    }

    /**
     * Returns <code>TRUE</code> if this feed is assigned to some reading list.
     *
     * @return <code>TRUE</code> if this feed is assigned to some reading list.
     */
    public boolean isDynamic()
    {
        return readingLists.size() > 0;
    }

    /**
     * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
     * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
     * criteria for the search feeds.
     *
     * @return match key.
     */
    public String getMatchKey()
    {
        return "DF" + xmlURL;
    }

    /**
     * Sets the ID of the feed.
     *
     * @param aId ID of the feed.
     */
    public void setID(long aId)
    {
        long oldID = getID();
        super.setID(aId);

        if (aId == -1 && oldID != aId)
        {
            // We were removed from the database
            setMetaData(null);
        }
    }
    // ---------------------------------------------------------------------------------------------

    /**
     * Listener of changes in meta-data.
     */
    private class MetaDataListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the
         *            property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            String property = evt.getPropertyName();
            FeedMetaDataHolder md = (FeedMetaDataHolder)evt.getSource();

            if (FeedMetaDataHolder.PROP_COMPLETE.equals(property))
            {
                invalidateVisibilityCache();
            } else if (FeedMetaDataHolder.PROP_INBOUND_LINKS.equals(property) && inLinks == -1)
            {
                Integer inboundLinks = (Integer)evt.getNewValue();
                if (inboundLinks != null) setInLinks(inboundLinks);
            } else if (FeedMetaDataHolder.PROP_LAST_UPDATE_TIME.equals(property))
            {
                // Refire the event with our own name.
                firePropertyChanged(PROP_LAST_METADATA_UPDATE_TIME, evt.getOldValue(),
                    evt.getNewValue());
            } else if (FeedMetaDataHolder.PROP_INVALID.equals(property))
            {
                String reason = md.isInvalid() == null
                    ? Strings.message("feed.invalidness.reason.undiscovered")
                    : md.isInvalid()
                    ? Strings.message("feed.invalidness.reason.invalid") : null;

                setInvalidnessReason(reason);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ITaggable implementation
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns shared tags.
     *
     * @return shared tags.
     */
    public String[] getSharedTags()
    {
        return sharedTags;
    }

    /**
     * Sets shared tags.
     *
     * @param tags shared tags.
     */
    public void setSharedTags(String[] tags)
    {
        String[] oldSharedTags = sharedTags;
        sharedTags = tags;

        firePropertyChanged(PROP_SHARED_TAGS, oldSharedTags, sharedTags);
    }

    /**
     * Returns tags assigned by author.
     *
     * @return author tags.
     */
    public String[] getAuthorTags()
    {
        return new String[0];
    }

    /**
     * Returns the name of taggable object type.
     *
     * @return type name.
     */
    public String getTaggableTypeName()
    {
        return Strings.message("taggable.feed");
    }

    /**
     * Returns link which can be tagged at the service (BB or third-party).
     *
     * @return link or <code>NULL</code> if tagging isn't supported.
     */
    public URL getTaggableLink()
    {
        return this.getXmlURL();
    }

    /**
     * Returns user tags.
     *
     * @return user tags.
     */
    public String[] getUserTags()
    {
        return userTags;
    }

    /**
     * Sets user tags.
     *
     * @param tags user tags.
     */
    public void setUserTags(String[] tags)
    {
        String[] oldUserTags = userTags;
        userTags = tags;

        firePropertyChanged(PROP_USER_TAGS, oldUserTags, userTags, true, false);
    }

    /**
     * Returns <code>TRUE</code> if this object has unsaved user tags.
     *
     * @return <code>TRUE</code> if this object has unsaved user tags.
     */
    public boolean hasUnsavedUserTags()
    {
        return unsavedUserTags;
    }

    /**
     * Sets unsaved user tags flag.
     *
     * @param unsaved <code>TRUE</code> if this object has unsaved user tags.
     */
    public void setUnsavedUserTags(boolean unsaved)
    {
        if (unsavedUserTags != unsaved)
        {
            unsavedUserTags = unsaved;

            firePropertyChanged(PROP_UNSAVED_USER_TAGS, !unsaved, unsaved);
        }
    }

    /**
     * Returns the description of tags.
     *
     * @return description.
     */
    public String getTagsDescription()
    {
        return tagsDescription;
    }

    /**
     * Sets new description text.
     *
     * @param description new description text.
     */
    public void setTagsDescription(String description)
    {
        String oldValue = tagsDescription;
        tagsDescription = description;

        firePropertyChanged(PROP_TAGS_DESCRIPTION, oldValue, tagsDescription, true, false);
    }

    /**
     * Returns tags extended description text.
     *
     * @return tags extended description text.
     */
    public String getTagsExtended()
    {
        return tagsExtended;
    }

    /**
     * Tests new tags extended description text.
     *
     * @param extended new extended description.
     */
    public void setTagsExtended(String extended)
    {
        String oldValue = tagsExtended;
        tagsExtended = extended;

        firePropertyChanged(PROP_TAGS_EXTENDED, oldValue, tagsExtended, true, false);
    }

    /**
     * Returns sync hash.
     *
     * @return sync hash.
     */
    public int getSyncHash()
    {
        return syncHash;
    }

    /**
     * Sets new sync hash.
     *
     * @param hash hash.
     */
    public void setSyncHash(int hash)
    {
        int oldHash = syncHash;
        syncHash = hash;

        firePropertyChanged(PROP_SYNC_HASH, oldHash, hash, false, false);
    }

    /**
     * Calculates new sync hash basing on its current state.
     *
     * @return sync hash.
     */
    public int calcSyncHash()
    {
        return xmlURL == null ? 0 : xmlURL.toString().hashCode();
    }

    /**
     * The part of AND-clause of the isVisible() method.
     *
     * @return the subclause.
     * @see com.salas.bb.domain.NetworkFeed#isVisible
     */
    @Override
    protected boolean isVisibleSubClause()
    {
        return !isDisabled();
    }
}
