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
// $Id: ImageBlockerDialog.java,v 1.2 2008/02/22 12:23:37 spyromus Exp $
//

package com.salas.bb.imageblocker;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.AbstractFrame;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.IconSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Dialog for Image blocking setup.
 *
 * TODO: ResourceID.ICON_PREFERENCES
 */
public final class ImageBlockerDialog extends AbstractDialog
{
    private JTextArea taPatterns;
    private URL url;
    private JTextField tfURL;

    /**
     * Creates image blocker dialog.
     *
     * @param owner     owning frame.
     * @param url       URL to offer for blocking.
     */
    public ImageBlockerDialog(AbstractFrame owner, URL url)
    {
        super(owner, Strings.message("imageblocker.dialog.title"));
        this.url = url;
    }

    /**
     * Builds and answers the preference's header.
     *
     * @return JComponent header of dialog box
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("imageblocker.dialog.title"),
            Strings.message("imageblocker.dialog.header"),
            IconSource.getIcon(ResourceID.ICON_PREFERENCES));
    }

    /**
     * Builds and answers the preference's content pane.
     *
     * @return JComponent of content part of the dialog box
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildMainPanel(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Builds content component.
     *
     * @return main content component.
     */
    protected JComponent buildMainPanel()
    {
        // Configure patterns section
        taPatterns = new JTextArea();
        taPatterns.setLineWrap(false);
        taPatterns.setText(getCurrentPatternsText());

        BBFormBuilder builder = new BBFormBuilder("min:grow, 4dlu, p");

        // Configure offer section
        if (url != null)
        {
            tfURL = new JTextField(url.toString());
            tfURL.setCaretPosition(0);

            builder.append(new JLabel(Strings.message("imageblocker.dialog.link")), 3);
            builder.append(tfURL);
            builder.append(new JButton(new BlockAction()));
            builder.appendRelatedComponentsGapRow(2);
        }

        builder.appendRow("min:grow");
        builder.append(new JScrollPane(taPatterns), 3, CellConstraints.FILL, CellConstraints.FILL);

        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("min");
        builder.append(ComponentsFactory.createWrappedMultilineLabel(Strings.message("imageblocker.dialog.disclaimer")));

        return builder.getPanel();
    }

    /**
     * Returns current patterns text.
     *
     * @return text.
     */
    private static String getCurrentPatternsText()
    {
        java.util.List<String> pats = ImageBlocker.getExpressions();
        return StringUtils.join(pats.iterator(), "\n");
    }

    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.DEFAULT.fromWidth(500));
    }

    /**
     * Closes the window.
     */
    protected void doCloseWindow()
    {
        doCancel();
    }

    /**
     * Invoked when changes are applied.
     */
    public void doApply()
    {
        super.doApply();

        ImageBlocker.setExpressions(taPatterns.getText());
    }

    /**
     * Opens the dialog.
     *
     * @return returns TRUE if patterns have changed.
     */
    public boolean openDialog()
    {
        String oldPatterns = getCurrentPatternsText();
        open();
        String newPatterns = getCurrentPatternsText();

        return !oldPatterns.equals(newPatterns);
    }

    /**
     * Block action is invoked when a user presses the "Block" button.
     */
    private class BlockAction extends AbstractAction
    {
        /** Defines an <code>Action</code> object with a default description string and default icon. */
        private BlockAction()
        {
            super(Strings.message("imageblocker.dialog.block"));
        }

        /** Invoked when an action occurs. */
        public void actionPerformed(ActionEvent e)
        {
            String link = tfURL.getText();
            if (StringUtils.isNotEmpty(link))
            {
                taPatterns.setText((taPatterns.getText().trim() + "\n" + link).trim());
            }

            tfURL.setText("");
        }
    }
}