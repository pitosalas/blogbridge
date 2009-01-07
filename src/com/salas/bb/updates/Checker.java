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
// $Id: Checker.java,v 1.6 2006/05/29 12:48:31 spyromus Exp $
//

package com.salas.bb.updates;

import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bbutilities.opml.utils.EmptyEntityResolver;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * Checks for new versions available for download. Makes XML-RPC call to the service
 * and analyzes the response.
 */
public final class Checker
{
    private static final Logger LOG = Logger.getLogger(Checker.class.getName());

    /**
     * Checks server for new version.
     *
     * @param currentVersion    current application version to tell service.
     *
     * @return result object or <code>NULL</code> if no updates available.
     *
     * @throws IOException when I/O error happens.
     */
    public CheckResult checkForUpdates(String currentVersion)
        throws IOException
    {
        CheckResult result = null;
        try
        {
            String response = queryServiceForUpdates(currentVersion);
            result = convertResponseToResult(response);
        } catch (JDOMException e)
        {
            LOG.log(Level.SEVERE, Strings.error("updates.failed.to.parse.response"), e);
            throw new IOException(Strings.error("updates.failed.to.parse.response"));
        } catch (ServerServiceException e)
        {
            LOG.log(Level.SEVERE, Strings.error("updates.failed.to.query.service.for.updates"), e);
            throw new IOException(Strings.error("updates.failed.to.query.service.for.updates"));
        }

        return result;
    }

    /**
     * Queries some service for updates availability.
     *
     * @param currentVersion    currently installed version.
     *
     * @return response of the service in XML format.
     */
    private String queryServiceForUpdates(String currentVersion)
        throws ServerServiceException
    {
        return ServerService.checkForUpdates(currentVersion);
    }

    /**
     * Converts response of the service (in XML format) into valid result object.
     *
     * @param aResponse response.
     *
     * @return result object or <code>NULL</code> if no updates available.
     *
     * @throws IOException      when I/O exception happens.
     * @throws JDOMException    when response is unparsable XML.
     */
    private CheckResult convertResponseToResult(String aResponse)
        throws IOException, JDOMException
    {
        CheckResult result = null;

        if (!StringUtils.isEmpty(aResponse))
        {
            Document doc = parse(aResponse);
            result = convertDocumentToResult(doc);
        }

        return result;
    }

    /**
     * Converts parsed XML document into result object.
     *
     * @param aDocument document.
     *
     * @return result object.
     */
    private CheckResult convertDocumentToResult(Document aDocument)
    {
        Element root = aDocument.getRootElement();

        String recentVersion = root.getChildTextTrim("version");
        long releaseTime = Long.parseLong(root.getChildTextTrim("releaseTime"));
        VersionChange[] changes = changesToList(root.getChild("changes"));
        Map locations = locationsToMap(root.getChild("locations"));

        return new CheckResult(recentVersion, releaseTime, changes, locations);
    }

    /**
     * Converts list of locations to map.
     *
     * @param aLocations    locations element to lookup locations at.
     *
     * @return populated map (type- to path).
     */
    private Map locationsToMap(Element aLocations)
    {
        Map locations;

        if (aLocations != null)
        {
            List locationElements = aLocations.getChildren("location");
            locations = new HashMap(locationElements.size());
            for (int i = 0; i < locationElements.size(); i++)
            {
                Element locationElement = (Element)locationElements.get(i);
                String type = locationElement.getAttributeValue("type");
                String path = locationElement.getAttributeValue("path");
                String description = locationElement.getAttributeValue("description");
                String sizeS = locationElement.getAttributeValue("size");
                long size = Long.parseLong(sizeS);

                locations.put(type, new Location(type, description, path, size));
            }
        } else locations = new HashMap();

        return locations;
    }

    /**
     * Converts list of "change" elements under "changes" element to array
     * of version change objects.
     *
     * @param aChanges  changes element.
     *
     * @return array of changes.
     */
    private VersionChange[] changesToList(Element aChanges)
    {
        VersionChange[] changes;
        if (aChanges != null)
        {
            List changesElements = aChanges.getChildren("change");
            int count = changesElements.size();
            changes = new VersionChange[count];

            for (int i = 0; i < count; i++)
            {
                Element change = (Element)changesElements.get(i);
                changes[i] = changeToItem(change);
            }
        } else changes = new VersionChange[0];

        return changes;
    }

    /**
     * Converts change element to version change object.
     *
     * @param aChange   change element.
     *
     * @return version change object.
     */
    private VersionChange changeToItem(Element aChange)
    {
        int type = Integer.parseInt(aChange.getAttributeValue("type"));
        String details = aChange.getTextTrim();

        return new VersionChange(type, details);
    }

    /**
     * Parses XML response into document.
     *
     * @param aResponse response to parse.
     *
     * @return document.
     *
     * @throws IOException      when I/O exception happens.
     * @throws JDOMException    when response is unparsable XML.
     */
    private Document parse(String aResponse)
        throws IOException, JDOMException
    {
        SAXBuilder builder = new SAXBuilder(false);

        builder.setEntityResolver(EmptyEntityResolver.INSTANCE);

        return builder.build(new StringReader(aResponse));
    }
}
