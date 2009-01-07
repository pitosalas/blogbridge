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
// $Id: ActivityIndicatorView.java,v 1.32 2008/04/07 18:26:04 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.ActivityIndicatorBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Singleton object that is used to give user feedback of activity in BlogBridge. Note the 'model'
 * is so simple that for now it's built right in.
 */
public final class ActivityIndicatorView extends JPanel
{
    private static ActivityIndicatorView INSTANCE;

    private static final int BLINK_PERIOD = 400; // millis

    private NetworkActivityIndicator indNetwork;
    private ActivityIndicatorBox indDisk;

    private List ticketsWebStats = new Vector();
    private List ticketsDisk = new Vector();
    private List ticketsCommon = new Vector();

    /**
     * Creates indicators view.
     *
     * @param connectionState           connection state to get connection information from.
     * @param connectionMouseListener   mouse listener for network monitor icon.
     */
    public ActivityIndicatorView(ConnectionState connectionState,
                                 MouseListener connectionMouseListener)
    {
        indDisk = new ActivityIndicatorBox(
            ResourceUtils.getIcon(ResourceID.ICON_ACTIVITY_DISK_ACTIVE),
            ResourceUtils.getIcon(ResourceID.ICON_ACTIVITY_DISK_PASSIVE)
        );

        indNetwork = new NetworkActivityIndicator(
            connectionState,
            ResourceUtils.getIcon(ResourceID.ICON_CONNECTED),
            ResourceUtils.getIcon(ResourceID.ICON_DISCONNECTED),
            ResourceUtils.getIcon(ResourceID.ICON_ACTIVITY_NETWORK_ACTIVE),
            ResourceUtils.getIcon(ResourceID.ICON_ACTIVITY_NETWORK_PASSIVE)
        );
        indNetwork.addMouseListener(connectionMouseListener);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(indDisk);
        add(indNetwork);

        java.util.Timer blinker = new java.util.Timer(true);
        blinker.scheduleAtFixedRate(new Blinker(), 0, BLINK_PERIOD);

        INSTANCE = this;
    }

    /**
     * Indicate the start of Discovery processing.
     *
     * @param link link which is under discovery.
     *
     * @return ticket.
     */
    public static synchronized ActivityTicket startDiscovery(String link)
    {
        return INSTANCE.startActivity(ActivityTicket.TYPE_NETWORK,
            MessageFormat.format(Strings.message("activity.discovering"),
                new Object[] { link }));
    }

    /**
     * Indicates the start of polling a Feed.
     *
     * @param feed  title of feed.
     *
     * @return task ticket.
     */
    public static synchronized ActivityTicket startPolling(String feed)
    {
        return INSTANCE.startActivity(ActivityTicket.TYPE_NETWORK,
            MessageFormat.format(Strings.message("activity.polling"),
                new Object[] { feed }));
    }

    /**
     * Indicate the start of opening database.
     *
     * @return task ticket.
     */
    public static synchronized ActivityTicket startOpeningDatabase()
    {
        return INSTANCE.startActivity(ActivityTicket.TYPE_DISK,
            Strings.message("activity.opening.database"));
    }

    /**
     * Starts displaying activity of a given type with specified message.
     *
     * @param type  type of activity.
     * @param title message.
     *
     * @return activity ticket.
     */
    private ActivityTicket startActivity(final int type, String title)
    {
        ActivityTicket ticket;

        ticket = new ActivityTicket(type, title);

        List tickets = getTicketsByType(type);
        if (tickets.add(ticket)) updateBox(type);

        return ticket;
    }

    /**
     * Finishes activity represented by a specified ticket.
     *
     * @param ticket activity ticket.
     */
    public static synchronized void finishActivity(ActivityTicket ticket)
    {
        final int type = ticket.getType();
        List tickets = INSTANCE.getTicketsByType(type);

        if (tickets.remove(ticket)) INSTANCE.updateBox(type);
    }

    /**
     * Updates the view of indication box of a givn type.
     *
     * @param aType type of activity.
     */
    private void updateBox(final int aType)
    {
        List ticketsL = getTicketsByType(aType);

        final String[] tasks = new String[ticketsL.size()];
        for (int i = 0; i < ticketsL.size(); i++)
        {
            ActivityTicket ticket = (ActivityTicket)ticketsL.get(i);
            tasks[i] = ticket.getDisplayInfo();
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (aType == ActivityTicket.TYPE_NETWORK)
                {
                    indNetwork.setTasks(tasks);
                } else
                {
                    indDisk.setTasksList(tasks);
                }
            }
        });
    }

    /**
     * Returns array of tickets for the activity of the given type.
     *
     * @param type  type of activity.
     *
     * @return list of tickets.
     */
    private List getTicketsByType(int type)
    {
        List tickets;

        switch(type)
        {
            case ActivityTicket.TYPE_NETWORK:
                tickets = ticketsWebStats;
                break;
            case ActivityTicket.TYPE_DISK:
                tickets = ticketsDisk;
                break;
            default:
                tickets = ticketsCommon;
                break;
        }

        return tickets;
    }

    /**
     * Returns number of tasks of specified type of activity currently running.
     *
     * @param aType type of activity.
     *
     * @return number of tasks.
     */
    static int getNumberOfTasks(int aType)
    {
        return INSTANCE.getTicketsByType(aType).size();
    }

    /**
     * Simple timer task which is intended to kick indication boxes
     * each blink tick. If indication box is in active state it will
     * change icon the the one, which corresponds to the specified
     * state to emulate blink effect.
     */
    private class Blinker extends TimerTask
    {
        private final Blink active = new Blink(true);
        private final Blink passive = new Blink(false);

        private int count = 0;

        public void run()
        {
            boolean activeState = count++ % 2 == 0;
            SwingUtilities.invokeLater(activeState ? active : passive);
        }
    }

    /**
     * Blinker events.
     */
    private class Blink implements Runnable
    {
        private final boolean activeState;

        public Blink(boolean aActiveState)
        {
            activeState = aActiveState;
        }

        public void run()
        {
            indDisk.blink(activeState);
            indNetwork.blink();
        }
    }

    /**
     * Network acitivity indicator component.
     */
    private static class NetworkActivityIndicator extends JLabel
    {
        private static final String OFFLINE = Strings.message("activity.offline");
        private static final String ONLINE = Strings.message("activity.online");

        private final ConnectionState conState;
        private final Icon connected;
        private final Icon disconnected;
        private final Icon active;
        private final Icon inactive;

        private String tasksList;

        /**
         * Creates component.
         *
         * @param conState      connection state listener.
         * @param connected     connected icon.
         * @param disconnected  disconnected icon.
         * @param active        active state icon.
         * @param inactive      inactive state icon.
         */
        public NetworkActivityIndicator(ConnectionState conState, Icon connected,
                                        Icon disconnected, Icon active, Icon inactive)
        {
            this.conState = conState;
            this.connected = connected;
            this.disconnected = disconnected;
            this.active = active;
            this.inactive = inactive;

            setHorizontalAlignment(SwingConstants.CENTER);

            Dimension size = new Dimension(0, 0);
            size = findMaxSize(size, connected);
            size = findMaxSize(size, disconnected);
            size = findMaxSize(size, active);
            size = findMaxSize(size, inactive);

            setMinimumSize(size);
            setMaximumSize(size);
            setPreferredSize(size);
        }

        /**
         * Finds maximum size among given and the icon.
         *
         * @param size  size.
         * @param icon  icon.
         *
         * @return max size.
         */
        private static Dimension findMaxSize(Dimension size, Icon icon)
        {
            size.width = Math.max(size.width, icon.getIconWidth());
            size.height = Math.max(size.height, icon.getIconHeight());

            return size;
        }

        /**
         * Updates icon and tooltip;
         */
        private void updateIconAndTooltip()
        {
            Icon icon;
            String tooltip;

            if (!conState.isOnline())
            {
                icon = disconnected;
                tooltip = OFFLINE;
            } else
            {
                if (tasksList == null)
                {
                    icon = connected;
                    tooltip = ONLINE;
                } else
                {
                    long time = System.currentTimeMillis();
                    boolean activeIcon = (time / BLINK_PERIOD) % 2 == 0;
                    icon = activeIcon ? active : inactive;
                    tooltip = tasksList;
                }
            }

            setIcon(icon);
            setToolTipText(tooltip);
        }

        /**
         * Sets new list of tasks.
         *
         * @param tasks tasks.
         */
        public void setTasks(String[] tasks)
        {
            tasksList = tasks == null || tasks.length == 0 ? null
                : "<html>" + StringUtils.join(tasks, "<br>");
            updateIconAndTooltip();
        }

        /**
         * Orders to make a blink.
         */
        public void blink()
        {
            updateIconAndTooltip();
        }
    }
}
