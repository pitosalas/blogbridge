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
// $Id: ExportGuidesAction.java,v 1.9 2008/03/17 15:12:47 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.guide.ExportGuidesDialog;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.opml.Converter;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bbutilities.opml.export.Exporter;
import com.salas.bbutilities.opml.objects.OPMLGuide;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Export of selected or all guides to OPML file.
 */
public final class ExportGuidesAction extends ThreadedAction
{
    private static final Logger LOG = Logger.getLogger(ExportGuidesAction.class.getName());

    private static final String OPML_TITLE = "BlogBridge Feeds";

    private static ExportGuidesAction instance;

    /**
     * Hidden singleton constructor.
     */
    private ExportGuidesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ExportGuidesAction getInstance()
    {
        if (instance == null) instance = new ExportGuidesAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();
        boolean isGuideSelected = GlobalModel.SINGLETON.getSelectedGuide() != null;

        ExportGuidesDialog dialog = new ExportGuidesDialog(mainFrame);
        dialog.setAllowSelectedGuide(isGuideSelected);

        dialog.open();

        if (!dialog.hasBeenCanceled())
        {
            String filename = dialog.getFilename();
            boolean extended = dialog.isExtendedMode();

            if (dialog.isSelectedGuideMode())
            {
                processExportSelected(filename, extended);
            } else
            {
                processExportAll(filename, extended);
            }
        }
    }

    /**
     * Writes information to the specified file name.
     *
     * @param filename filename to use for writing.
     * @param extended TRUE to perform export in the extended mode.
     */
    void processExportSelected(String filename, boolean extended)
    {
        IGuide[] guides = GlobalController.SINGLETON.getSelectedGuides();
        
        if (guides.length > 0)
        {
            List<OPMLGuide> opmlGuides = new ArrayList<OPMLGuide>(guides.length);
            for (IGuide guide : guides)
            {
                if (guide instanceof StandardGuide)
                {
                    opmlGuides.add(Converter.convertToOPML((StandardGuide)guide));
                }
            }

            final OPMLGuide[] opmlGuidesA = opmlGuides.toArray(new OPMLGuide[opmlGuides.size()]);
            processExport(opmlGuidesA, filename, extended);
        }
    }
    
    /**
     * Writes information to the specified file name.
     *
     * @param filename filename to use for writing.
     * @param extended TRUE to perform export in the extended mode.
     */
    void processExportAll(String filename, boolean extended)
    {
        GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();
        processExport(Converter.convertToOPML(set.getStandardGuides(null)), filename, extended);
    }
    
    /**
     * Exports guides to the specified file name.
     * 
     * @param opmlGuides    guide(s) to export
     * @param filename      filename to use for writing.
     * @param extended      TRUE to perform export in the extended mode.
     */
    void processExport(OPMLGuide[] opmlGuides, String filename, boolean extended)
    {
        Exporter exporter = new Exporter(extended);
        Document doc = exporter.export(Converter.convertToOPML(opmlGuides, OPML_TITLE));

        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            XMLOutputter xo = new XMLOutputter();
            xo.output(doc, fos);
            fos.close();
        } catch (IOException e)
        {
            processException(e);
        }
    }
    
    /**
     * Shows appropriate warning / error dialog.
     *
     * @param e exception object.
     */
    private void processException(IOException e)
    {
        LOG.log(Level.SEVERE, Strings.error("failed.to.export.guide.data"), e);
        JOptionPane.showMessageDialog(GlobalController.SINGLETON.getMainFrame(),
            MessageFormat.format(Strings.message("export.guides.dialog.text.error"), e.getMessage()),
            Strings.message("export.guides.dialog.title"), JOptionPane.ERROR_MESSAGE);
    }
}

