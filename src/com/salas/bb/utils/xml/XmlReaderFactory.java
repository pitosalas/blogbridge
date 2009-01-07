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
// $Id: XmlReaderFactory.java,v 1.5 2006/05/29 12:50:07 spyromus Exp $
//

package com.salas.bb.utils.xml;

import org.apache.commons.lang.StringUtils;

import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;

import com.salas.bb.utils.i18n.Strings;

/**
 * Factory for creation of readers for XML.
 */
public final class XmlReaderFactory
{
    private static final Logger LOG = Logger.getLogger(XmlReaderFactory.class.getName());

    private static Map readers;
    private static Map aliases;

    static
    {
        String pckg = XmlReaderFactory.class.getPackage().getName().replaceAll("\\.", "/") + "/";

        try
        {
            readers = readPropsToMap(pckg + "mapping.properties");
            aliases = readPropsToMap(pckg + "aliases.properties");
        } catch (IOException e)
        {
            LOG.log(Level.SEVERE, Strings.error("failed.to.initialize.maps"), e);
        }
    }

    /**
     * Hidden utility class constructor.
     */
    private XmlReaderFactory()
    {
    }

    /**
     * Creates reader for given encoding.
     *
     * @param is        input stream to wrap.
     * @param encoding  encoding to use.
     *
     * @return reader or NULL if not possible to create one.
     *
     * @throws IOException in case of I/O error.
     */
    static Reader createReaderForEncoding(InputStream is, String encoding)
        throws IOException
    {
        if (encoding == null || is == null) return null;

        // First try to get reader from mapping
        Reader reader = createReaderForMappedEncoding(is, encoding);

        // If reader wasn't found, create standard one for given encoding
        if (reader == null) reader = new InputStreamReader(is, convertEncodingAlias(encoding));

        return reader;
    }

    /**
     * Create reader for mapped encoding. If the encoding is present in
     * <code>mapping.properties</code> then the reader will be created.
     *
     * @param is        input stream to wrap.
     * @param encoding  encoding to use.
     *
     * @return reader or NULL if failed or not found.
     */
    static Reader createReaderForMappedEncoding(InputStream is, String encoding)
    {
        if (encoding == null || is == null) return null;

        Reader reader = null;

        String className = (String)readers.get(encoding.toLowerCase());
        if (className != null)
        {
            try
            {
                String pckg = XmlReaderFactory.class.getPackage().getName() + ".";
                Class readerClass = Class.forName(pckg + className);
                Constructor constructor = readerClass.getConstructor(new Class[]
                {
                    InputStream.class
                });
                reader = (Reader)constructor.newInstance(new Object[] { is });
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("failed.to.initialize.reader"), e);
            }
        }

        return reader;
    }

    /**
     * Converts encoding to Java-understood name if we have corresponding alias registered.
     *
     * @param encoding encoding name or alias.
     *
     * @return de-aliased name or original.
     */
    static String convertEncodingAlias(String encoding)
    {
        if (encoding != null)
        {
            String deAliased = (String)aliases.get(encoding.toLowerCase());
            if (deAliased != null) encoding = deAliased;
        }

        return encoding;
    }

    /**
     * Reads properties from the resource into the map. Each key in properties resource
     * is a value for comma-delimetered list of keys.
     *
     * @param propertiesName    name of properties resource.
     *
     * @return map.
     *
     * @throws IOException in case of I/O error.
     */
    static Map readPropsToMap(String propertiesName)
        throws IOException
    {
        Properties props = new Properties();
        props.load(XmlReaderFactory.class.getClassLoader().getResourceAsStream(propertiesName));

        Map map = new HashMap();

        Enumeration propEnumeration = props.propertyNames();
        while (propEnumeration.hasMoreElements())
        {
            String readerClassName = ((String)propEnumeration.nextElement()).trim();
            String[] encodings = StringUtils.split(props.getProperty(readerClassName), ",");

            putKeysInMap(map, encodings, readerClassName);
        }

        return map;
    }

    /**
     * Puts the same value for multiple keys in the map. Keys and value will be trimmed.
     *
     * @param map   map to populate.
     * @param keys  list of keys.
     * @param value value.
     */
    static void putKeysInMap(Map map, String[] keys, String value)
    {
        if (map == null || keys == null || value == null) return;

        value = value.trim();

        for (int i = 0; i < keys.length; i++)
        {
            String key = keys[i].trim();
            map.put(key, value);
        }
    }

    /**
     * Creates reader for a given stream. Makes all possible to detect encoding and use
     * this information to correctly decode stream bytes into characters.
     *
     * @param is    input stream to wrap.
     *
     * @return reader or NULL if failed to create one.
     *
     * @throws IOException in case of I/O errors.
     */
    public static Reader create(InputStream is)
        throws IOException
    {
        if (is == null) return null;

        EncodingDetector.DetectionResult result = EncodingDetector.detectEncoding(is);
        String encoding = result.getEncoding();

        return createReaderForEncoding(result.getStream(), encoding);
    }
}
