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
// $Id: MDDiscoveryLogic.java,v 1.20 2007/07/17 07:15:47 spyromus Exp $
//

package com.salas.bb.discovery;

import com.salas.bb.discovery.filter.IURLFilter;
import com.salas.bb.domain.FeedMetaDataHolder;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.discovery.DiscoveryResult;
import com.salas.bb.utils.discovery.UrlDiscovererException;
import com.salas.bb.utils.discovery.impl.DirectDiscoverer;
import com.salas.bb.utils.net.CyclicRedirectionException;
import com.salas.bb.utils.net.NotAuthenticatedException;
import com.salas.bb.utils.net.UISException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains all discovery logic: direct discovery, local resource check, talking to
 * service.
 */
public final class MDDiscoveryLogic
{
    private static final Logger LOG = Logger.getLogger(MDDiscoveryLogic.class.getName());

    static final int STATUS_VALID               = 0;
    static final int STATUS_PROCESSING          = 1;
    static final int STATUS_INVALID             = 2;

    static final String KEY_STATUS_CODE         = "code";
    static final String KEY_HTML_URL            = "htmlUrl";
    static final String KEY_DATA_URL            = "dataUrl";
    static final String KEY_INBOUND_LINKS       = "inboundLinks";
    static final String KEY_TITLE               = "title";
    static final String KEY_AUTHOR              = "author";
    static final String KEY_DESCRIPTION         = "description";

    private static IURLFilter urlFilter;

    /**
     * Registers a new filter to use with the discoverer.
     *
     * @param urlFilter filter.
     */
    public static void setURLFilter(IURLFilter urlFilter)
    {
        MDDiscoveryLogic.urlFilter = urlFilter;
    }

    /**
     * Discovery processing.
     *
     * @param aRequest          discovery request.
     * @param aConnectionState  connection state interface.
     */
    public static void processDiscovery(MDDiscoveryRequest aRequest,
                                        ConnectionState aConnectionState)
    {
        FeedMetaDataHolder holder = aRequest.getHolder();
        holder.setComplete(false);

        if (!isFilteredOut(aRequest.getUrl()))
        {
            queryDirectly(aRequest);
            if (aConnectionState.isServiceAccessible() && !aRequest.isLocal()) queryService(aRequest);
        } else
        {
            // If the URL is filtered out and no discovery necessary,
            // we mark the link as invalid and all discovery methods as complete.
            holder.setInvalid(true);
            aRequest.setDirectDiscoveryComplete(true);
            aRequest.setServiceDiscoveryComplete(true);
        }

        updateHolderStatus(aRequest);
    }

    /**
     * Checks if the URL is filtered out and shouldn't be discovered.
     *
     * @param url URL to check.
     *
     * @return <code>TRUE</code> if it's filtered out.
     */
    private static boolean isFilteredOut(URL url)
    {
        return urlFilter != null && urlFilter.matches(url);
    }

    /**
     * Analyses the status of request and validness state of holder to judge
     * about the discovery completion.
     *
     * @param request request to take information from.
     */
    static void updateHolderStatus(MDDiscoveryRequest request)
    {
        boolean complete = false;

        boolean directComplete = request.isDirectDiscoveryComplete();
        boolean serviceComplete = request.isServiceDiscoveryComplete();
        boolean local = request.isLocal();

        FeedMetaDataHolder holder = request.getHolder();
        if (holder.isInvalid() != null)
        {
            boolean invalid = holder.isInvalid();

            if (((directComplete || serviceComplete) && !invalid) ||
                 (directComplete && serviceComplete && invalid))
            {
                complete = true;
            }
        } else if (local && directComplete)
        {
            complete = true;
        }

        if (complete)
        {
            holder.setComplete(true);
            holder.setLastUpdateTime(System.currentTimeMillis());
        }
    }

    /**
     * Makes an attempt to interpret URL as direct feed URL. If XML URL is already discovered
     * then there's no need to continue with this discovery.
     *
     * @param request   discovery request.
     */
    private static void queryDirectly(MDDiscoveryRequest request)
    {
        FeedMetaDataHolder holder = request.getHolder();
        if (request.isDirectDiscoveryComplete() || holder.getXmlURL() != null) return;

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Direct discovery started: URL=" + request.getUrl());
        }

        DirectDiscoverer dd = new DirectDiscoverer();
        String failureMessage = null;
        try
        {
            DiscoveryResult res = dd.discover(request.getUrl());
            if (res != null)
            {
                URL link = res.getLink();
                if (link != null)
                {
                    holder.setXmlURL(link);
                    holder.setInvalid(false);
                }
            }

            request.setDirectDiscoveryComplete(true);
        } catch (UrlDiscovererException e)
        {
            Throwable cause = e.getCause();
            failureMessage = causeToFailureMessage(cause);

            if (failureMessage != null)
            {
                request.setDirectDiscoveryComplete(true);

                // We can't set invalid flag here because it is very brief check
                // and we can count only on positive result.
            } else
            {
                LOG.log(Level.FINE, "Direct discovery failed temporary.", e);
            }
        } catch (IllegalArgumentException e)
        {
            // This exception is thrown by parser when it is unable to find root element of
            // the feed. Usually it happens when we give it non-xml file as input. Here we mark direct
            // discovery as complete as it doesn't make any sense to continue hitting the same exception.

            request.setDirectDiscoveryComplete(true);
        }

        if (request.isDirectDiscoveryComplete() && LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Direct discovery finished: URL=" + request.getUrl() +
                (holder.getXmlURL() != null ? " Discovered" : " Undiscovered") +
                (failureMessage != null ? " (" + failureMessage + ")" : ""));
        }
    }

    /**
     * Converts cause of direct discovery failure into short descriptive message.
     *
     * @param aCause cause.
     *
     * @return message or <code>NULL</code> if failure is temporary.
     */
    private static String causeToFailureMessage(Throwable aCause)
    {
        String aFailureMessage = null;

        if (aCause instanceof FileNotFoundException)
        {
            aFailureMessage = "Not Found";
        } else if (aCause instanceof UnknownHostException)
        {
            aFailureMessage = "Host Unknown";
        } else if (aCause instanceof UISException)
        {
            aFailureMessage = "UIS";
        } else if (aCause instanceof CyclicRedirectionException)
        {
            aFailureMessage = "Cyclic Redirection";
        } else if (aCause instanceof NotAuthenticatedException)
        {
            aFailureMessage = "Not Authenticated";
        }

        return aFailureMessage;
    }

    /**
     * Calls service and reads the meta-data information.
     *
     * @param request   discovery request.
     */
    private static void queryService(MDDiscoveryRequest request)
    {
        if (request.isServiceDiscoveryComplete()) return;

        ServerService ss = ServerService.getInstance();
        try
        {
            Map result = ss.discover(request.getUrl().toString());

            processServiceResults(result, request);
        } catch (ServerServiceException e)
        {
            boolean log = true;
            Throwable cause = e.getCause();
            if (cause instanceof IOException)
            {
                String msg = cause.getMessage();
                log = msg == null || msg.indexOf("Connection refused") == -1;
            }

            if (log) LOG.log(Level.WARNING, "Service Error.", e);
        }
    }

    /**
     * Processes the results from service and updates holder with information.
     *
     * @param results   results to process.
     * @param request   request to take holder from.
     */
    static void processServiceResults(Map results, MDDiscoveryRequest request)
    {
        // Read data into the wrapper
        Integer statusCodeI = (Integer)results.get(KEY_STATUS_CODE);
        int statusCode = statusCodeI == null ? -1 : statusCodeI;

        // Make decision what to do with information
        FeedMetaDataHolder holder = request.getHolder();
        switch (statusCode)
        {
            case STATUS_PROCESSING:
                break;
            case STATUS_VALID:
                copyData(results, holder, request.getUrl());
                holder.setInvalid(false);
                request.setServiceDiscoveryComplete(true);
                break;
            case STATUS_INVALID:
                if (holder.isInvalid() == null) holder.setInvalid(true);
                request.setServiceDiscoveryComplete(true);
                break;
            default:
                LOG.warning(MessageFormat.format("Unrecognized service code: {0}", statusCode));
        }
    }

    /**
     * Copies data from result into holder.
     *
     * @param result    result from service.
     * @param holder    holder to fill.
     * @param url       URL being discovered (for informational purposes).
     */
    private static void copyData(Map result, FeedMetaDataHolder holder, URL url)
    {
        // Inbound links, Category & Location
        holder.setInboundLinks((Integer)result.get(KEY_INBOUND_LINKS));
        holder.setTitle(fromUtf8(result.get(KEY_TITLE)));
        holder.setAuthor(fromUtf8(result.get(KEY_AUTHOR)));
        holder.setDescription(fromUtf8(result.get(KEY_DESCRIPTION)));

        // HTML URL
        String htmlUrl = (String)result.get(KEY_HTML_URL);
        if (holder.getHtmlURL() == null && htmlUrl != null)
        {
            try
            {
                holder.setHtmlURL(new URL(htmlUrl));
            } catch (MalformedURLException e)
            {
                LOG.warning(MessageFormat.format("Service returned malformed HTML URL ({0}) for {1}",
                    htmlUrl, url));
            }
        }

        // XML URL
        String xmlURL = (String)result.get(KEY_DATA_URL);
        if (holder.getXmlURL() == null && xmlURL != null)
        {
            try
            {
                holder.setXmlURL(new URL(xmlURL));
            } catch (MalformedURLException e)
            {
                LOG.warning(MessageFormat.format("Service returned malformed XML URL ({0}) for {1}",
                    xmlURL, url));
            }
        }
    }

    /**
     * Converts strings and bytes arrays to the string.
     *
     * @param object string or bytes array.
     *
     * @return string.
     */
    private static String fromUtf8(Object object)
    {
        String result = Constants.EMPTY_STRING;

        if (object instanceof String)
        {
            result = (String)object;
        } else if (object instanceof byte[])
        {
            result = StringUtils.fromUTF8((byte[])object);
        }

        return result;
    }
}
