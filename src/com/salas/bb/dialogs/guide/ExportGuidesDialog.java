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
// $Id: ExportGuidesDialog.java,v 1.6 2008/03/17 15:12:47 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.core.actions.guide.OPMLSelectionAction;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Dialog for exporting of Channel Guide.
 */
public class ExportGuidesDialog extends AbstractDialog
{
    private static final String FORM_COLUMNS = "7dlu, 7dlu, pref:grow, 2dlu, 150dlu, 1dlu, pref";

    private final JTextField tfFilename = new JTextField();
    private final JButton btnBrowse = new JButton("\u2026");
    private JRadioButton rbAllGuides;
    private JRadioButton rbSelectedGuide;
    private JCheckBox    chExtended;

    private boolean allowSelectedGuide = true;

    /**
     * Creates dialog box for entering properties of new guide.
     *
     * @param owner owner-frame.
     */
    public ExportGuidesDialog(final Frame owner)
    {
        super(owner, Strings.message("export.guides.dialog.title"));
    }

    /**
     * Build header panel.
     *
     * @return header panel.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("export.guides.dialog.title"),
            Strings.message("export.guides.dialog.header"));
    }

    /**
     * Returns content of the dialog box.
     *
     * @return content component.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Main dialog body.
     *
     * @return body.
     */
    private JComponent buildBody()
    {
        initComponents();

        BBFormBuilder builder = new BBFormBuilder(FORM_COLUMNS);

        builder.append(Strings.message("export.guides.opml.file"), 3, tfFilename, btnBrowse);
        builder.appendRelatedComponentsGapRow(2);
        builder.setLeadingColumnOffset(1);
        builder.append(rbAllGuides, 6);
        builder.append(rbSelectedGuide, 6);

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chExtended, 6);

        return builder.getPanel();
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        rbAllGuides = ComponentsFactory.createRadioButton(Strings.message("export.guides.all.guides"));
        rbSelectedGuide = ComponentsFactory.createRadioButton(Strings.message("export.guides.selected.guides"));

        chExtended = ComponentsFactory.createCheckBox(Strings.message("export.guides.extended"));
        btnBrowse.setAction(new OPMLSelectionAction(OPMLSelectionAction.MODE_SAVE, tfFilename));
        btnBrowse.setMargin(Constants.INSETS_NONE);
        btnBrowse.setPreferredSize(new Dimension(20, 20));
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbSelectedGuide);
        bg.add(rbAllGuides);

        rbAllGuides.setSelected(true);
        rbSelectedGuide.setEnabled(allowSelectedGuide);
    }

    /**
     * Returns entered string in OPML-URL field.
     *
     * @return entered URL.
     */
    public String getFilename()
    {
        return tfFilename.getText();
    }

    /**
     * Returns TRUE if export should be performed in the extended mode.
     *
     * @return TRUE if export should be performed in the extended mode.
     */
    public boolean isExtendedMode()
    {
        return chExtended.isSelected();
    }

    /**
     * Verify the presence of file on disk and if it is present then display the file
     * overwriting warning. Accept the entry only if file does not exist or allowed to be
     * overwritten.
     */
    public void doAccept()
    {
        boolean allowToAccept = true;

        if (new File(getFilename()).exists())
        {
            int result = JOptionPane.showConfirmDialog(
                getParent(),
                Strings.message("export.guides.file.exists"),
                Strings.message("export.guides.dialog.title"), JOptionPane.YES_NO_OPTION);

            allowToAccept = result == JOptionPane.YES_OPTION;
        }

        if (allowToAccept) super.doAccept();
    }

    /**
     * Enables / disables "Selected guide" option.
     *
     * @param allow <code>TRUE</code> to enable.
     */
    public void setAllowSelectedGuide(boolean allow)
    {
        allowSelectedGuide = allow;
    }
    
    /**
     * Returns TRUE if user selected 'selected guide only' mode.
     *
     * @return TRUE if selected-guide-only option chosen.
     */
    public boolean isSelectedGuideMode()
    {
        return rbSelectedGuide.isSelected();
    }
}

