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
// $Id: CustomHTMLDocument.java,v 1.3 2006/01/08 05:10:10 kyank Exp $
//

package com.salas.bb.utils.swinghtml;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.*;

/**
 * Custom model for HTML documents which represents images internally as question mark characters
 * to ensure correct word wrapping with inline images. This is accomplished by subclassing
 * <code>HTMLDocument.HTMLReader</code>, and using it to read in new HTML documents. Unfortunately,
 * the Swing API does not permit us to use our subclass under all circumstances (one version of
 * <code>getReader</code> in <code>HTMLDocument</code> is package-private), so this will only work
 * for documents that are read in as a whole and not modified.
 */
public class CustomHTMLDocument extends HTMLDocument
{
    /**
     * Constructs an HTML document using the default buffer size
     * and a default <code>StyleSheet</code>.  This is a convenience
     * method for the constructor
     * <code>HTMLDocument(Content, StyleSheet)</code>.
     */
    public CustomHTMLDocument()
    {
        super();
    }

    /**
     * Constructs an HTML document with the default content
     * storage implementation and the specified style/attribute
     * storage mechanism.  This is a convenience method for the
     * constructor
     * <code>HTMLDocument(Content, StyleSheet)</code>.
     *
     * @param styles the styles
     */
    public CustomHTMLDocument(StyleSheet styles)
    {
        super(styles);
    }

    /**
     * Constructs an HTML document with the given content
     * storage implementation and the given style/attribute
     * storage mechanism.
     *
     * @param c      the container for the content
     * @param styles the styles
     */
    public CustomHTMLDocument(Content c, StyleSheet styles)
    {
        super(c, styles);
    }

    /**
     * Fetches the reader for the parser to use when loading the document
     * with HTML.  This is implemented to return an instance of
     * <code>HTMLDocument.HTMLReader</code>.
     * Subclasses can reimplement this
     * method to change how the document gets structured if desired.
     * (For example, to handle custom tags, or structurally represent character
     * style elements.)
     *
     * @param pos the starting position
     * @return the reader used by the parser to load the document
     */
    public HTMLEditorKit.ParserCallback getReader(int pos)
    {
        // Calls superclass for the side effects (base URL is set from stream)
        super.getReader(pos);

        return new HTMLReader(pos);
    }

    /**
     * Modifies built-in HTML reading functionality to represent image elements as question marks
     * in the internal text data.
     */
    public class HTMLReader extends HTMLDocument.HTMLReader
    {
        /**
         * Creates an HTMLReader.
         * @param offset The document offset to read HTML content into
         */
        public HTMLReader(int offset)
        {
            super(offset);
        }

        /**
         * Adds content that is basically specified entirely
         * in the attribute set.
         * @param t The HTML tag to insert
         * @param a The attributes for the tag
         */
        protected void addSpecialElement(HTML.Tag t, MutableAttributeSet a)
        {
            super.addSpecialElement(t, a);

            ElementSpec es = (ElementSpec)parseBuffer.lastElement();
            if (es.getType() == ElementSpec.ContentType &&
                    es.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMG)
            {
                // Patch image content to use a question mark
                es.getArray()[0] = '?';
            }
        }
    }
}
