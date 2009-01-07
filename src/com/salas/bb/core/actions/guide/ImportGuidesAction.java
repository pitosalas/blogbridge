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
// $Id: ImportGuidesAction.java,v 1.67 2008/03/17 16:04:50 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.guide.ImportGuidesDialog;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.utils.GuideIcons;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.opml.BloglinesImporter;
import com.salas.bb.utils.opml.Helper;
import com.salas.bb.utils.opml.ImporterAdv;
import com.salas.bb.utils.xml.XmlReaderFactory;
import com.salas.bbutilities.opml.Importer;
import com.salas.bbutilities.opml.ImporterException;
import com.salas.bbutilities.opml.objects.OPMLGuide;
import com.salas.bbutilities.opml.objects.OPMLGuideSet;
import com.salas.bbutilities.opml.objects.OPMLReadingList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Import guide from OPML resource to the list.
 *
 * SHOULD ALWAYS BE EXCUTED FROM EDT!
 */
public final class ImportGuidesAction extends AbstractAction
{
    private static final Logger LOG = Logger.getLogger(ImportGuidesAction.class.getName());

    /** @noinspection HardCodedStringLiteral*/
    private static final String HTTP_REQUEST_AUTHORIZATION = "Authorization";
    /** @noinspection HardCodedStringLiteral*/
    private static final String BLOGLINES_SERVICE_URL = "http://rpc.bloglines.com/listsubs";

    private static ImportGuidesAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private ImportGuidesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ImportGuidesAction getInstance()
    {
        if (instance == null) instance = new ImportGuidesAction();
        return instance;
    }


    /**
     * Actual action.
     *
     * @param event original event object.
     */
    public void actionPerformed(ActionEvent event)
    {
        if (GlobalController.SINGLETON.checkForNewSubscription()) return;

        final ImportGuidesDialog dialog =
                new ImportGuidesDialog(GlobalController.SINGLETON.getMainFrame());

        dialog.open();

        if (!dialog.hasBeenCanceled())
        {
            setEnabled(false);
            try
            {
                processImport(dialog);
            } catch (Exception e1)
            {
                // Just do nothing in this case. We need to be sure that this action
                // will get enabled in any case.
                e1.printStackTrace();
            } finally
            {
                setEnabled(true);
            }
        }
    }

    /**
     * Processes import operation basing on user entry.
     *
     * @param dialog dialog box.
     */
    private void processImport(ImportGuidesDialog dialog)
    {
        final Importer importer;
        final String url;

        boolean fromURL = dialog.isFromURL();
        final boolean isSingle = dialog.isSingleMode();
        final boolean isAppending = dialog.isAppendingMode();

        if (fromURL)
        {
            url = dialog.getUrlString();
            importer = new ImporterAdv();
        } else
        {
            String email = dialog.getBloglinesEmail();
            String password = dialog.getBloglinesPassword();

            url = BLOGLINES_SERVICE_URL;
            importer = createBloglinesImporter(email, password);
        }

        // Span thread to read data separately
        Thread thread = new Thread()
        {
            public void run()
            {
                doImport(importer, url, isSingle, isAppending, GlobalModel.SINGLETON, true);
            }
        };

        thread.start();
    }

    /**
     * Fetches data and adds it in EDT thread.
     *
     * @param aImporter     importer to use to read data.
     * @param aUrl          URL to take data from.
     * @param aSingle       <code>TRUE</code> to fetch in single mode.
     * @param aAppending    <code>TRUE</code> to append.
     * @param aModel        data model.
     * @param isConfirmationRequired <code>TRUE</code> to ask for the confirmation before importing data.
     */
    public static void doImport(Importer aImporter, String aUrl, final boolean aSingle,
                                 final boolean aAppending, final GlobalModel aModel,
                                 final boolean isConfirmationRequired)
    {
        OPMLGuideSet guideSet = null;
        URL baseUrl = null;
        try
        {
            try
            {
                baseUrl = new URL(aUrl);
            } catch (MalformedURLException e)
            {
                throw ImporterException.malformedUrl(e.getMessage());
            }

            guideSet = aImporter.process(baseUrl, aSingle);
        } catch (ImporterException e)
        {
            processException(e);
        }

        if (guideSet != null)
        {
            final OPMLGuide[] aGuides = guideSet.getGuides();
            final URL aBaseUrl = baseUrl;

            // Do actual addition of data in EDT
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (aGuides.length > 0)
                    {
                        processImportedGuides(aModel, aBaseUrl, aGuides, aSingle || aAppending, isConfirmationRequired);
                    } else
                    {
                        JOptionPane.showMessageDialog(GlobalController.SINGLETON.getMainFrame(),
                            Strings.message("import.guides.nothing.to.import"));
                    }
                }
            });
        }
    }

    /**
     * Imports guides from a URL in append mode.
     *
     * @param model data model.
     * @param url   OPML URL.
     */
    public static void importAndAppend(GlobalModel model, String url)
    {
        doImport(new ImporterAdv(), url, false, true, model, false);
    }

    /**
     * Creates customized importer (handling unicode signatures) for dealing
     * with foreign OPML resources. Our own OPML's do not require such processing
     * and this is why this is the only place we do the following.
     *
     * @param username  name of user to authenticate (optional).
     * @param password  password to user for user to authenticate (optional).
     *
     * @return importer object.
     */
    private static Importer createBloglinesImporter(final String username, final String password)
    {
        return new BloglinesImporter()
        {
            /**
             * Creates reader to use for reading data from stream.
             *
             * @param url URL to read data from.
             *
             * @return reader object.
             *
             * @throws java.io.IOException if opening of stream for given URL fails.
             */
            public Reader createReaderForURL(URL url)
                throws IOException
            {
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                if (username != null && password != null)
                {
                    con.setRequestProperty(HTTP_REQUEST_AUTHORIZATION,
                        StringUtils.createBasicAuthToken(username, password));
                }

                return XmlReaderFactory.create(con.getInputStream());
            }
        };
    }

    /**
     * Shows confirmation dialog box with information about numbers and on confirmation
     * appends or replaces the guides.
     *
     * @param model       data model.
     * @param baseURL     base URL of OPML resource.
     * @param guides      guides.
     * @param isAppending TRUE if user decided to append new guides.
     * @param isConfirmationRequired TRUE to ask for confirmation before importing feeds.
     */
    private static void processImportedGuides(GlobalModel model, URL baseURL, OPMLGuide[] guides,
                                              boolean isAppending, boolean isConfirmationRequired)
    {
        int result = JOptionPane.YES_OPTION;

        if (isConfirmationRequired)
        {
            int feedsCount = countFeeds(guides);

            String message;

            if (guides.length == 1)
            {
                message = MessageFormat.format(Strings.message("import.guides.ready.to.import.0.feeds"), feedsCount);
            } else
            {
                message = MessageFormat.format(Strings.message("import.guides.ready.to.import.0.guides.with.1.feeds"),
                    guides.length, feedsCount);
            }

            message += Strings.message("import.guides.continue");

            result = JOptionPane.showConfirmDialog(GlobalController.SINGLETON.getMainFrame(),
                    message, Strings.message("import.guides.dialog.title"), JOptionPane.YES_NO_OPTION);
        }

        if (result == JOptionPane.YES_OPTION)
        {
            final GuidesSet cgs = model.getGuidesSet();
            if (isAppending)
            {
                appendGuides(baseURL, guides, cgs);
            } else
            {
                replaceGuides(baseURL, guides, cgs);
            }
        }
    }

    /**
     * Calculates total number of channels in guides in list.
     *
     * Note: package local visibility chosen to allow testing of method.
     *
     * @param guides list of guides.
     *
     * @return total number of channels.
     */
    static int countFeeds(OPMLGuide[] guides)
    {
        int count = 0;

        for (int i = 0; i < guides.length; i++)
        {
            OPMLGuide guide = guides[i];
            count += guide.getFeeds().size();

            OPMLReadingList[] lists = guide.getReadingLists();
            for (int j = 0; j < lists.length; j++)
            {
                OPMLReadingList list = lists[j];
                count += list.getFeeds().size();
            }
        }

        return count;
    }

    /**
     * Creates <code>ChannelGuide</code>'s, appends them to the set
     * and fills with <code>ChannelGuideEntry</code>'s.
     *
     * Note: package local visibility chosen to allow testing of method.
     *
     * @param baseURL   base URL of OPML resource.
     * @param guides    list of guides to append.
     * @param set       guides set to append guides to.
     */
    static void appendGuides(URL baseURL, OPMLGuide[] guides, GuidesSet set)
    {
        // Load array list with titles already present in CGS
        final Set<String> titles = set.getGuidesTitles();

        for (final OPMLGuide guide : guides)
        {
            String title = getUniqueTitle(guide.getTitle(), titles);
            appendGuide(baseURL, guide, title, set);
            titles.add(title);
        }
    }

    /**
     * Appends single guide to the list.
     *
     * @param baseURL       base URL of OPML resource.
     * @param opmlGuide     guide to append.
     * @param uniqueTitle   unique title created on basis of original title.
     * @param guidesSet     guide set to append guides to.
     *
     * @return number of feeds were actually added.
     */
    static int appendGuide(URL baseURL, OPMLGuide opmlGuide, String uniqueTitle,
                           GuidesSet guidesSet)
    {
        IGuide guide = Helper.createGuide(baseURL, opmlGuide, null);
        guide.setTitle(uniqueTitle);
        String icon = guide.getIconKey();
        if (StringUtils.isEmpty(icon))
        {
            icon = getUnusedIcon(guidesSet);
            guide.setIconKey(icon);
        }

        // Replace feeds with existing -- sharing
        replaceFeedsWithShares(guidesSet, guide);

        // Finally add the guide
        guidesSet.add(guide);

        GlobalController.SINGLETON.getPoller().update(guide);

        return guide.getFeedsCount();
    }

    /**
     * Checks if the guide set contains feeds we are going to add and
     * replaces them with existing.
     *
     * @param set   set to operate.
     * @param guide guide to check.
     */
    static void replaceFeedsWithShares(GuidesSet set, IGuide guide)
    {
        IFeed[] feeds = guide.getFeeds();
        for (IFeed feed : feeds)
        {
            IFeed existing = set.findFeed(feed);
            if (existing != null && existing != feed)
            {
                GuidesSet.replaceFeed(feed, existing);
            }
        }
    }

    /**
     * Returns first unused icon.
     *
     * @param set   guides set to check for used icons.
     *
     * @return icon key.
     */
    static String getUnusedIcon(GuidesSet set)
    {
        String icon = null;

        int unusedIconIndex = GuideIcons.findUnusedIconName(set.getGuidesIconKeys());
        if (unusedIconIndex != -1) icon = GuideIcons.getIconsNames()[unusedIconIndex];

        return icon;
    }

    /**
     * Checks if title is unique and if it's not then generates new one by adding
     * suffix '_x' where 'x' number from 2.
     *
     * @param title     title to check uniqueness of.
     * @param titles    set of already present titles.
     *
     * @return unique title.
     */
    static String getUniqueTitle(String title, Set titles)
    {
        String uniqueTitle = title;

        int i = 2;
        while (titles.contains(uniqueTitle))
        {
            uniqueTitle = title + "_" + i;
            i++;
        }

        return uniqueTitle;
    }

    /**
     * Creates <code>ChannelGuide</code>'s, replaces with them current set
     * of guides and fills with <code>ChannelGuideEntry</code>'s.
     *
     * Note: package local visibility chosen to allow testing of method.
     *
     * @param baseURL   base URL of OPML resource.
     * @param guides    list of guides to replace with.
     * @param set       guides set to append guides to.
     */
    public static void replaceGuides(URL baseURL, OPMLGuide[] guides, GuidesSet set)
    {
        set.clear();

        appendGuides(baseURL, guides, set);
    }

    /**
     * Shows appropriate warning / error dialog.
     *
     * @param e exception object.
     */
    private static void processException(ImporterException e)
    {
        JOptionPane.showMessageDialog(GlobalController.SINGLETON.getMainFrame(),
            ImporterException.getStringType(e),
            Strings.message("import.guides.dialog.title"), JOptionPane.ERROR_MESSAGE);
    }
}
