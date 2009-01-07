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
// $Id: TestMDDiscoveryLogic.java,v 1.4 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.discovery;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;

import com.salas.bb.domain.FeedMetaDataHolder;

/**
 * This suite contains tests for <code>MDDiscoveryLogic</code> unit.
 */
public class TestMDDiscoveryLogic extends TestCase
{
    /**
     * Tests handling of processing response from service.
     * The response is PROCESSING. We are discovering something NEW.
     */
    public void testProcessServiceResultsProcessingNew()
    {
        Map results = createServiceResults(MDDiscoveryLogic.STATUS_PROCESSING);

        FeedMetaDataHolder newHolder = new FeedMetaDataHolder();
        MDDiscoveryRequest request = new MDDiscoveryRequest(null, newHolder);

        MDDiscoveryLogic.processServiceResults(results, request);

        assertFalse("Discovery is incomplete. Service is processing request.",
            newHolder.isComplete());
    }

    /**
     * Tests handling of processing response from service.
     * The response is PROCESSING. We are updating some meta-data.
     */
    public void testProcessServiceResultsProcessingUpdate()
    {
        Map results = createServiceResults(MDDiscoveryLogic.STATUS_PROCESSING);

        FeedMetaDataHolder holder = new FeedMetaDataHolder();
        holder.setComplete(false);
        MDDiscoveryRequest request = new MDDiscoveryRequest(null, holder);

        MDDiscoveryLogic.processServiceResults(results, request);

        assertFalse("Discovery is incomplete. Service is processing request.",
            holder.isComplete());
    }

    /**
     * Tests filling the holder with information on valid discovery.
     */
    public void testProcessServiceResultsValid()
    {
        Map results = createServiceResults(MDDiscoveryLogic.STATUS_VALID);
        results.put(MDDiscoveryLogic.KEY_DATA_URL, "http://data");
        results.put(MDDiscoveryLogic.KEY_HTML_URL, "http://html");
        results.put(MDDiscoveryLogic.KEY_TITLE, "title");
        results.put(MDDiscoveryLogic.KEY_AUTHOR, "author");
        results.put(MDDiscoveryLogic.KEY_DESCRIPTION, "description");
        results.put(MDDiscoveryLogic.KEY_INBOUND_LINKS, new Integer(1));

        FeedMetaDataHolder holder = new FeedMetaDataHolder();
        MDDiscoveryRequest request = new MDDiscoveryRequest(null, holder);

        MDDiscoveryLogic.processServiceResults(results, request);

        assertTrue("Discovery has finished with VALID status.",
            request.isServiceDiscoveryComplete());
        assertFalse("Information is valid.", holder.isInvalid().booleanValue());
        assertEquals("http://data", holder.getXmlURL().toString());
        assertEquals("http://html", holder.getHtmlURL().toString());
        assertEquals("title", holder.getTitle());
        assertEquals("description", holder.getDescription());
        assertEquals("author", holder.getAuthor());
        assertEquals(new Integer(1), holder.getInboundLinks());
    }

    /**
     * Tests marking holder as complete and invalid on INVALID response from service.
     */
    public void testProcessServiceResultsInvalid()
    {
        Map results = createServiceResults(MDDiscoveryLogic.STATUS_INVALID);

        FeedMetaDataHolder holder = new FeedMetaDataHolder();
        MDDiscoveryRequest request = new MDDiscoveryRequest(null, holder);

        MDDiscoveryLogic.processServiceResults(results, request);

        assertTrue("Discovery has finished with INVALID status.",
            request.isServiceDiscoveryComplete());
        assertTrue("Information is valid.", holder.isInvalid().booleanValue());
    }

    /**
     * Tests detecting URL's to local resourses.
     */
    public void testIsLocalResource()
        throws MalformedURLException
    {
        assertTrue(MDDiscoveryRequest.isLocalURL(new URL("file://a")));
        assertTrue(MDDiscoveryRequest.isLocalURL(new URL("http://localhost/test")));
        assertTrue(MDDiscoveryRequest.isLocalURL(new URL("http://127.0.0.1:5585/test")));

        assertFalse(MDDiscoveryRequest.isLocalURL(new URL("http://www.google.com/")));
    }

    /**
     * Verifies resolving the state of discovery when both discoveries are incomplete.
     */
    public void testUpdateHolderStatusBothIncomplete()
    {
        MDDiscoveryRequest request = prepareRequest(false, false, null);

        // Invalidness state is undiscovered (i.e. holder.isInvalid() == null)
        // We don't mark meta-data as invalid after direct discovery failure as
        // we can count only on positive results -- it's not really reliable, but fast.

        MDDiscoveryLogic.updateHolderStatus(request);
        assertFalse("Discovery is incomplete by both methods.", request.getHolder().isComplete());
        assertEquals(-1, request.getHolder().getLastUpdateTime());
    }

    /**
     * Verifies resolving the state of discovery when service discovery is
     * incomplete, but direct query failed.
     */
    public void testUpdateHolderStatusServiceIncompleteDirectInvalid()
    {
        MDDiscoveryRequest request = prepareRequest(false, true, null);

        // Invalidness state is undiscovered (i.e. holder.isInvalid() == null)
        // We don't mark meta-data as invalid after direct discovery failure as
        // we can count only on positive results -- it's not really reliable, but fast.

        MDDiscoveryLogic.updateHolderStatus(request);
        assertFalse("Discovery is incomplete by service and direct hasn't reported success.",
            request.getHolder().isComplete());
        assertEquals(-1, request.getHolder().getLastUpdateTime());
    }

    /**
     * Verifies resolving the state of discovery when service discovery is
     * incomplete, but direct query succeed.
     */
    public void testUpdateHolderStatusServiceIncompleteDirectValid()
    {
        MDDiscoveryRequest request = prepareRequest(false, true, Boolean.FALSE);

        MDDiscoveryLogic.updateHolderStatus(request);
        assertTrue("Discovery is incomplete by service, but direct discoverer reported success.",
            request.getHolder().isComplete());
        assertFalse(request.getHolder().getLastUpdateTime() == -1);
    }

    /**
     * Verifies resolving the state of discovery when direct discovery is
     * incomplete, but service query failed.
     */
    public void testUpdateHolderStatusDirectIncompleteServiceInvalid()
    {
        MDDiscoveryRequest request = prepareRequest(true, false, Boolean.TRUE);

        MDDiscoveryLogic.updateHolderStatus(request);
        assertFalse("Discovery is incomplete by direct query, but service reported failure.",
            request.getHolder().isComplete());
        assertTrue(request.getHolder().getLastUpdateTime() == -1);
    }

    /**
     * Verifies resolving the state of discovery when direct discovery is
     * incomplete, but service query succeed.
     */
    public void testUpdateHolderStatusDirectIncompleteServiceValid()
    {
        MDDiscoveryRequest request = prepareRequest(true, false, Boolean.FALSE);

        MDDiscoveryLogic.updateHolderStatus(request);
        assertTrue("Discovery is incomplete by direct query, but service reported success.",
            request.getHolder().isComplete());
        assertFalse(request.getHolder().getLastUpdateTime() == -1);
    }

    /**
     * Verifies resolving the state of discovery when both discoveries
     * has finished with failure.
     */
    public void testUpdateHolderStatusBothCompleteInvalid()
    {
        MDDiscoveryRequest request = prepareRequest(true, true, Boolean.TRUE);

        MDDiscoveryLogic.updateHolderStatus(request);
        assertTrue("Both queries complete and invalid.", request.getHolder().isComplete());
        assertFalse(request.getHolder().getLastUpdateTime() == -1);
    }

    /**
     * Verifies resolving the state of discovery when both discoveries
     * has finished with success.
     */
    public void testUpdateHolderStatusBothCompleteValid()
    {
        MDDiscoveryRequest request = prepareRequest(true, true, Boolean.FALSE);

        MDDiscoveryLogic.updateHolderStatus(request);
        assertTrue("Both queries complete and invalid.", request.getHolder().isComplete());
        assertFalse(request.getHolder().getLastUpdateTime() == -1);
    }

    /**
     * Prepares the request object.
     *
     * @param serviceComplete   <code>TRUE</code> to mark request as processed with service.
     * @param directComplete    <code>TRUE</code> to mark request as processed by direct discovery.
     * @param isInvalid         <code>NULL</code> to leave in undetermined state and
     *                          <code>TRUE</code> to mark as invalid.
     *
     * @return request object.
     */
    private static MDDiscoveryRequest prepareRequest(boolean serviceComplete,
        boolean directComplete, Boolean isInvalid)
    {
        FeedMetaDataHolder holder = new FeedMetaDataHolder();
        MDDiscoveryRequest request = new MDDiscoveryRequest(null, holder);

        request.setServiceDiscoveryComplete(serviceComplete);
        request.setDirectDiscoveryComplete(directComplete);
        if (isInvalid != null) holder.setInvalid(isInvalid.booleanValue());

        return request;
    }

    /**
     * Creates service results.
     *
     * @param statusCode status code.
     *
     * @return results map.
     */
    private static Map createServiceResults(int statusCode)
    {
        Map results = new HashMap();
        results.put(MDDiscoveryLogic.KEY_STATUS_CODE, new Integer(statusCode));

        return results;
    }
}
