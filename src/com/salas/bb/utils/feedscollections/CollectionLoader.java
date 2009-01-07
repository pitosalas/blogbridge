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
// $Id $
//

package com.salas.bb.utils.feedscollections;

import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.URLInputStream;
import com.salas.bb.utils.xml.XmlReaderFactory;
import com.salas.bbutilities.opml.objects.FormatConstants;
import com.salas.bbutilities.opml.utils.EmptyEntityResolver;
import com.salas.bbutilities.opml.utils.Transformation;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import javax.swing.tree.TreeNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collections loading and parsing utility.
 */
class CollectionLoader
{
    private static final Logger LOG = Logger.getLogger(CollectionLoader.class.getName());

    /**
     * Loads collection by the given URL and parses it as the list of reading lists or feeds.
     *
     * @param collection        collection to populate with information.
     * @param indexURL          URL of the collection.
     * @param loadReadingLists  <code>TRUE</code> to return the collection of reading lists only.
     * @param skipLevel         <code>TRUE</code> to skip first level of folders and treat them as organization
     *                          level fore reading list.
     * @param listener          listener to notify of the progress.
     */
    public static void load(CollectionFolder collection, URL indexURL, boolean loadReadingLists,
                            boolean skipLevel, IProgressListener listener)
    {
        String error = null;

        if (listener != null) listener.started();
        try
        {
            Document doc = parseDocument(indexURL);
            if (listener != null) listener.progress(50);

            if (!isValid(doc))
            {
                throw new LoaderException(Strings.error("failed.to.load.the.collection"));
            } else
            {
                // Parse
                processCollection(collection, doc, loadReadingLists, skipLevel, indexURL, listener);
            }

            if (listener != null) listener.progress(100);
        } catch (LoaderException e)
        {
            error = e.getMessage();
        } finally
        {
            if (listener != null) listener.finished(error);
        }
    }

    /**
     * Loads whole collection from the document into the folder node.
     *
     * @param col       collection folder-node.
     * @param doc       document.
     * @param loadReadingLists <code>TRUE</code> if we currently loading reading lists.
     * @param skipLevel <code>TRUE</code> to skip first level of folders and treat them as organization
     *                  level fore reading list.
     * @param baseURL   base URL of the collection for correct relative links resolution.
     * @param listener  progress listener.
     */
    private static void processCollection(CollectionFolder col, Document doc, boolean loadReadingLists,
                                          boolean skipLevel, URL baseURL, IProgressListener listener)
    {
        Element root = doc.getRootElement();
        Namespace bbNS = root.getNamespace(FormatConstants.BB_NS_PREFIX);

        Element body = root.getChild("body");
        loadToNode(col, bbNS, body, loadReadingLists, skipLevel, baseURL, listener);
    }

    /**
     * Loads information to node.
     *
     * @param node      node of the collections hierarchy.
     * @param bbNS      BlogBridge specific namespace.
     * @param element   element.
     * @param loadReadingLists <code>TRUE</code> if we currently loading reading lists.
     * @param skipLevel <code>TRUE</code> to skip first level of folders and treat them as organization
     *                  level fore reading list.
     * @param baseURL   base URL of the collection for correct relative links resolution.
     * @param listener  progress listener.
     */
    private static void loadToNode(CollectionFolder node, Namespace bbNS, Element element, boolean loadReadingLists,
                                   boolean skipLevel, URL baseURL, IProgressListener listener)
    {
        List outlines = element.getChildren("outline");
        for (int i = 0; i < outlines.size(); i++)
        {
            Element outline = (Element)outlines.get(i);

            Transformation.lowercaseAttributes(outline);
            String tagsS = bbNS == null ? outline.getAttributeValue("tags") : outline.getAttributeValue("tags", bbNS);
            String[] tags = new String[0];
            if (!StringUtils.isEmpty(tagsS)) tags = StringUtils.split(tagsS, ",");
            String type = outline.getAttributeValue("type");
            String title = outline.getAttributeValue("title");
            String text = outline.getAttributeValue("text");
            String htmlURL = outline.getAttributeValue("htmlurl");
            String xmlURLS = outline.getAttributeValue("xmlurl");
            if (xmlURLS == null) xmlURLS = outline.getAttributeValue("url");
            URL xmlURL = null;
            try
            {
                if (xmlURLS != null) xmlURL = new URL(baseURL, xmlURLS);
            } catch (MalformedURLException e)
            {
                LOG.log(Level.WARNING, MessageFormat.format(
                    Strings.error("invalid.url"), new Object[] { xmlURL }), e);
            }

            if (title == null) title = text;
            if (title == null) continue;

            if (type == null)
            {
                // Folder or reading list
                handleFolder(node, outline, bbNS, baseURL, title, text, tags,
                    htmlURL, xmlURLS, xmlURL, loadReadingLists, skipLevel, listener);
            } else if ("list".equals(type))
            {
                // Reading list. We don't need it right now.
            } else if ("link".equals(type))
            {
                // Possible reading list. We don't need it right now.
                if (xmlURL != null && (xmlURLS.endsWith(".opml") ||
                    xmlURL.getPath().endsWith(".opml")))
                {
                    handleFolder(node, outline, bbNS, baseURL, title, text, tags,
                        htmlURL, xmlURLS, xmlURL, loadReadingLists, skipLevel, listener);
                }
            } else if ("rss".equals(type) && xmlURL != null && !loadReadingLists)
            {
                // Feed
                CollectionItem feed = new CollectionItem(title, text, tags, htmlURL, xmlURLS);
                node.addNode(feed);
            }
        }

        // Compress the view. If the only element is a folder with the same name as in this
        // node, it has to be removed as a redundant level.
        CollectionFolder fldr = hasDuplicateLevelFolder(node);
        if (fldr != null)
        {
            node.nodes.clear();
            Iterator en = fldr.nodes.iterator();
            while (en.hasNext()) node.addNode((CollectionNode)en.next());
        }
    }

    /**
     * Returns a folder if the only child is that folder with the same name.
     *
     * @param node tree folder node.
     *
     * @return folder if the only child is that folder with the same name.
     */
    private static CollectionFolder hasDuplicateLevelFolder(CollectionFolder node)
    {
        CollectionFolder theOnlyFolder = null;

        if (node instanceof LazyCollectionFolder)
        {
            // Lazy collection folder needs special treatment because if
            // we currently are loading it, it will go into endless recursion
            // if asked for a child count or child object directly.
            LazyCollectionFolder lcf = (LazyCollectionFolder)node;
            if (lcf.getChildCountNoCheck() == 1 && lcf.getChildAtNoCheck(0) instanceof CollectionFolder)
            {
                theOnlyFolder = (CollectionFolder)lcf.getChildAtNoCheck(0);
            }
        } else if (node.getChildCount() == 1 && node.getChildAt(0) instanceof CollectionFolder)
        {
            theOnlyFolder = (CollectionFolder)node.getChildAt(0);
        }

        return theOnlyFolder != null &&
            node.getTitle() != null &&
            node.getTitle().equals(theOnlyFolder.getTitle()) ? theOnlyFolder : null;
    }

    /**
     * Loads folder or sub-collection depending on the settings.
     *
     * @param node      collections tree node.
     * @param outline   outline element.
     * @param bbNS      BB namespace.
     * @param baseURL   base URL.
     * @param title     title of the folder.
     * @param text      associated text.
     * @param tags      tags.
     * @param htmlURL   HTML URL.
     * @param xmlURLS   XML URL text.
     * @param xmlURL    XML URL.
     * @param loadReadingLists <code>TRUE</code> if we currently loading reading lists.
     * @param skipLevel <code>TRUE</code> to skip first level of folders and treat them as organization
     *                  level fore reading list.
     * @param listener  progress listener.
     */
    private static void handleFolder(CollectionFolder node, Element outline, Namespace bbNS, URL baseURL, String title,
                                     String text, String[] tags, String htmlURL, String xmlURLS, URL xmlURL,
                                     boolean loadReadingLists, boolean skipLevel, IProgressListener listener)
    {
        if (xmlURL != null)
        {
            // Reading list
            if (loadReadingLists && (!skipLevel || !(node instanceof Collection)))
            {
                CollectionItem rl = new CollectionItem(title, text, tags, htmlURL, xmlURLS);
                node.addNode(rl);
            } else
            {
                // Sub-folder
                node.addNode(new LazyCollectionFolder(title, text, tags, xmlURL, loadReadingLists));
            }
        } else
        {
            // Folder
            CollectionFolder outlineFolder = new CollectionFolder(title, text, tags);
            loadToNode(outlineFolder, bbNS, outline, loadReadingLists, false, baseURL, listener);
            if (outlineFolder.getChildCount() > 0) node.addNode(outlineFolder);
        }
    }

    /**
     * Lazy folder loading its contents on demand.
     */
    private static class LazyCollectionFolder extends CollectionFolder
    {
        private URL xmlURL;
        private boolean loaded;
        private boolean loadReadingLists;

        /**
         * Creates lazy folder.
         *
         * @param title         title.
         * @param description   description.
         * @param tags          tags list.
         * @param xmlURL        XML URL of the contents.
         * @param loadReadingLists <code>TRUE</code> if this folder was loaded in reading lists mode and we
         */
        public LazyCollectionFolder(String title, String description, String[] tags, URL xmlURL, boolean loadReadingLists)
        {
            super(title, description, tags);

            this.xmlURL = xmlURL;
            this.loadReadingLists = loadReadingLists;

            loaded = false;
        }

        /**
         * Loads items if not loaded yet.
         */
        private synchronized void loadItems()
        {
            if (!loaded)
            {
                loadSubCollection(this, xmlURL, loadReadingLists);
                loaded = true;
            }
        }

        /**
         * Returns the number of children <code>TreeNode</code>s the receiver
         * contains.
         */
        public int getChildCount()
        {
            loadItems();
            return super.getChildCount();
        }

        /**
         * Returns the child <code>TreeNode</code> at index
         * <code>childIndex</code>.
         */
        public TreeNode getChildAt(int childIndex)
        {
            loadItems();
            return super.getChildAt(childIndex);
        }

        /**
         * Returns the index of <code>node</code> in the receivers children.
         * If the receiver does not contain <code>node</code>, -1 will be
         * returned.
         */
        public int getIndex(TreeNode node)
        {
            loadItems();
            return super.getIndex(node);
        }

        /**
         * Returns true if the receiver is a leaf.
         */
        public boolean isLeaf()
        {
            return loaded && super.isLeaf();
        }

        /**
         * Returns the number of children without a loading check.
         *
         * @return the number of children.
         */
        int getChildCountNoCheck()
        {
            return super.getChildCount();
        }

        /**
         * Returns a child without a check.
         *
         * @param i index.
         *
         * @return a child.
         */
        TreeNode getChildAtNoCheck(int i)
        {
            return super.getChildAt(i);
        }
    }

    /**
     * Loads sub-collection of items.
     *
     * @param node      node.
     * @param xmlURL    XML URL of the collection.
     * @param loadReadingLists <code>TRUE</code> if we currently loading reading lists.
     */
    private static void loadSubCollection(CollectionFolder node, URL xmlURL, boolean loadReadingLists)
    {
        load(node, xmlURL, loadReadingLists, false, null);
    }

    /**
     * Returns <code>TRUE</code> if the document is valid OPML.
     *
     * @param doc document to check.
     *
     * @return <code>TRUE</code> if the document is valid OPML.
     */
    private static boolean isValid(Document doc)
    {
        Element root = doc.getRootElement();
        if (!"opml".equalsIgnoreCase(root.getName())) return false;

        Element body = root.getChild("body");
        return body != null;
    }

    /**
     * Reads OPML and parses it into the JDOM document.
     *
     * @param url   URL to grab OPML from.
     *
     * @return JDOM document.
     *
     * @throws LoaderException if loading or parsing failed.
     */
    private static Document parseDocument(URL url) throws LoaderException
    {
        SAXBuilder builder = new SAXBuilder(false);

        // Turn off DTD loading
        builder.setEntityResolver(EmptyEntityResolver.INSTANCE);

        Document doc;
        try
        {
            doc = builder.build(XmlReaderFactory.create(new URLInputStream(url)));
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("there.was.a.problem.reading.a.collection.0"), new Object[] { url }), e);
            throw new LoaderException(Strings.error("there.was.a.problem.reading.a.collection"));
        }

        return doc;
    }

    /**
     * Internal loader exception.
     */
    private static class LoaderException extends Exception
    {
        /**
         * Constructs a new exception with the specified detail message.  The
         * cause is not initialized, and may subsequently be initialized by
         * a call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public LoaderException(String message)
        {
            super(message);
        }
    }

}
