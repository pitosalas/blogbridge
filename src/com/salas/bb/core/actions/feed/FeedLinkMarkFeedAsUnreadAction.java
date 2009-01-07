package com.salas.bb.core.actions.feed;

import com.salas.bb.domain.IFeed;

import java.awt.event.ActionEvent;

/**
 * Mark feed as read action for the feed link.
 */
public class FeedLinkMarkFeedAsUnreadAction extends MarkFeedAsUnreadAction
{
    private static IFeed feed;

    /**
     * Enable the action upon construction.
     */
    public FeedLinkMarkFeedAsUnreadAction()
    {
        super();
        setEnabled(true);
    }

    /**
     * Sets the feed.
     *
     * @param feed feed.
     */
    public static void setFeed(IFeed feed)
    {
        FeedLinkMarkFeedAsUnreadAction.feed = feed;
    }

    /**
     * Returns the list of feeds to mark.
     *
     * @return feeds.
     */
    protected IFeed[] getFeeds()
    {
        return new IFeed[] { feed };
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        try
        {
            super.doAction(event);
        } catch (Exception e)
        {
            // Release feed after job is done
            feed = null;
        }
    }
}
