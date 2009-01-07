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
// $Id: CustomHTMLEditorKit.java,v 1.9 2007/07/06 12:14:04 spyromus Exp $
//

package com.salas.bb.utils.uif.html;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.swinghtml.CustomHTMLDocument;

import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Custom HTML editor kit giving us custom view factory and document model.
 */
public class CustomHTMLEditorKit extends LinkTrackHTMLEditorKit
{
    /**
     * A factory that will return our custom <code>View</code> classes for relevant elements.
     */
    private CustomHTMLFactory factory;

    /**
     * The custom styles for displaying HTML articles.
     */
    private static StyleSheet defaultStyles;

    /** Resource ID of the stylesheet. */
    private String stylesheetResourceID;

    /** TRUE to use default styles. */
    private final boolean useDefaultStyles;
    /** Individual styles used when useDefaultStyles is FALSE. */
    private static StyleSheet styles;

    /**
     * Creates HTML editor kit.
     */
    public CustomHTMLEditorKit()
    {
        this(ResourceID.URL_DEFAULT_STYLESHEET);
    }

    /**
     * Creates HTML editor kit.
     *
     * @param stylesheetResourceID stylesheet resource ID.
     */
    public CustomHTMLEditorKit(String stylesheetResourceID)
    {
        this(stylesheetResourceID, true);
    }

    /**
     * Creates HTML editor kit.
     *
     * @param stylesheetResourceID stylesheet resource ID.
     * @param useDefaultStyles <code>TRUE</code> to use default article styles.
     */
    public CustomHTMLEditorKit(String stylesheetResourceID, boolean useDefaultStyles)
    {
        this.useDefaultStyles = useDefaultStyles;
        this.stylesheetResourceID = stylesheetResourceID;
        factory = new CustomHTMLFactory();
    }

    /**
     * Fetch a factory that is suitable for producing
     * views of any models that are produced by this
     * kit.
     *
     * @return the factory
     */
    public ViewFactory getViewFactory()
    {
        return factory;
    }

    /**
     * Create an uninitialized text storage model
     * that is appropriate for this type of editor.
     *
     * @return the model
     */
    public Document createDefaultDocument()
    {
        StyleSheet styles = getStyleSheet();
        StyleSheet ss = new StyleSheet();

        ss.addStyleSheet(styles);

        HTMLDocument doc = new CustomHTMLDocument(ss);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }

    /**
     * Gets the set of styles to be used to render HTML elements.
     *
     * @return A <code>StyleSheet</code> containing the default HTML element styles.
     */
    public StyleSheet getStyleSheet()
    {
        StyleSheet style;

        if (useDefaultStyles)
        {
            if (defaultStyles == null) defaultStyles = loadStyles();
            style = defaultStyles;
        } else
        {
            if (styles == null) styles = loadStyles();
            style = styles;
        }

        return style;
    }

    /**
     * Loads styles.
     *
     * @return styles.
     */
    protected StyleSheet loadStyles()
    {
        StyleSheet styles = new StyleSheet();
        styles.addStyleSheet(super.getStyleSheet());

        try
        {
            String path = ResourceUtils.getString(stylesheetResourceID);
            InputStream is = ResourceUtils.getInputStream(path);
            Reader r = new BufferedReader(new InputStreamReader(is));
            styles.loadRules(r, null);
            r.close();
        } catch (Throwable e)
        {
            // on error we simply have no styles... the html
            // will look mighty wrong but still function.
        }
        return styles;
    }
}
