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
// $Id: SmartFeedPlugin.java,v 1.5 2007/04/06 09:32:48 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.salas.bb.domain.FeedType;
import com.salas.bb.domain.querytypes.DefaultQueryType;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import org.jdom.Element;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SmartFeed plugin.
 */
public class SmartFeedPlugin extends DefaultQueryType implements IPlugin
{
    private final static Map<String, Integer> MODES = new HashMap<String, Integer>();
    private final static Map<String, FeedType> TYPES = new HashMap<String, FeedType>();

    private final String iconResource;
    private final ClassLoader loader;

    static {
        MODES.put("full", IFeedDisplayConstants.MODE_FULL);
        MODES.put("brief", IFeedDisplayConstants.MODE_BRIEF);
        MODES.put("mini", IFeedDisplayConstants.MODE_MINIMAL);

        TYPES.put("text", FeedType.TEXT);
        TYPES.put("link", FeedType.LINK);
        TYPES.put("image", FeedType.IMAGE);
        TYPES.put("twitter", FeedType.TWITTER);
    }

    /**
     * Private constructor.
     *
     * @param id            id type.
     * @param type          type of the feed.
     * @param name          name.
     * @param icon          icon resource.
     * @param url           URL.
     * @param parameterName parameter name.
     * @param description   description.
     * @param mode          view mode.
     * @param loader        class loader.
     */
    private SmartFeedPlugin(int id, FeedType type, String name, String icon,
        String url, String parameterName, String description,
        int mode, ClassLoader loader)
    {
        super(id, type, name, null, url, parameterName, description, mode);

        this.iconResource = icon;
        this.loader = loader;
    }

    /**
     * Sets the parameters before the initialization.
     *
     * @param params parameters.
     */
    public void setParameters(Map<String, String> params)
    {
    }

    /**
     * Returns the name of plug-in type (Theme, Actions ...).
     *
     * @return the name of plug-in type.
     */
    public String getTypeName()
    {
        return "Smart Feed";
    }

    @Override
    public ImageIcon getIcon()
    {
        return IconSource.loadIcon(loader.getResource(iconResource));
    }

    /** Initializes plug-in. */
    public void initialize()
    {
        registerType(this);
    }

    /**
     * Creates the plug-in by parsing the element.
     *
     * @param element   element.
     * @param loader    loader.
     *
     * @return plug-in.
     *
     * @throws LoaderException in case anything wrong happens.
     */
    public static IPlugin create(Element element, ClassLoader loader)
        throws LoaderException
    {
        String name  = element.getAttributeValue("name");
        String type  = element.getAttributeValue("type");
        String mode  = element.getAttributeValue("mode");
        String id    = element.getAttributeValue("id");
        String icon  = element.getAttributeValue("icon");
        String url   = element.getAttributeValue("url");
        String descr = element.getAttributeValue("description");
        String param = element.getAttributeValue("parameter");

        if (StringUtils.isEmpty(name)) throw new LoaderException("Name can't be empty");
        if (StringUtils.isEmpty(descr)) throw new LoaderException("Description can't be empty");
        if (StringUtils.isEmpty(param)) throw new LoaderException("Parameter can't be empty");

        // URL processing
        if (StringUtils.isEmpty(url)) throw new LoaderException("URL can't be empty");
        url = preprocessURL(url);

        // ID check
        if (StringUtils.isEmpty(id)) throw new LoaderException("ID can't be empty");
        if (!StringUtils.isNumeric(id)) throw new LoaderException("ID must be numeric");
        int idI = Integer.parseInt(id);
        if (idI < QueryType.RESERVED_IDS) throw new LoaderException("ID must be larger than " +
            QueryType.RESERVED_IDS + " (the last reserved)");

        // Type check
        if (StringUtils.isEmpty(type)) type = "text";
        FeedType typeI = TYPES.get(type.trim().toLowerCase());
        if (typeI == null) throw new LoaderException("Type can be: text, link, image");

        // Mode check
        if (StringUtils.isEmpty(mode)) mode = "full";
        Integer modeI = MODES.get(mode.trim().toLowerCase());
        if (modeI == null) throw new LoaderException("Mode can be: mini, brief or full");

        // Icon check
        if (StringUtils.isEmpty(icon)) throw new LoaderException("Icon can't be empty");
        InputStream is = loader.getResourceAsStream(icon);
        if (is == null) throw new LoaderException("Icon file cannot be found"); else
        {
            try
            {
                is.close();
            } catch (IOException e)
            {
                // Fall through
            }
        }

        return new SmartFeedPlugin(idI, typeI, name, icon, url, param, descr, modeI, loader);
    }

    /**
     * Preprocesses URL by replacing human-readable tags with those for query engine.
     *
     * @param url   URL.
     *
     * @return finished URL.
     */
    static String preprocessURL(String url)
    {
        url = url.replaceAll("\\{(keys|query)\\}", "{0}");
        url = url.replaceAll("\\{(max|limit)\\}", "{1}");
        
        return url;
    }
}
