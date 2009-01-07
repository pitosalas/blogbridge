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

package com.salas.bb.core.actions.guide;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.CollectionItemsSelectionDialog;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

/**
 * Shows the dialog and subscribes to a reading list.
 */
public class SubscribeToReadingListAction extends AbstractAction
{
    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (GlobalController.SINGLETON.checkForNewSubscription()) return;

        subscribe(null);
    }

    /**
     * Opens the dialog with subscription information.
     *
     * @param listURL URL of the list or <code>NULL</code> for no URL.
     */
    public static void subscribe(URL listURL)
    {
        GlobalModel model = GlobalController.SINGLETON.getModel();
        IGuide selectedGuide = model.getSelectedGuide();
        StandardGuide[] guides = model.getGuidesSet().getStandardGuides(null);

        SubscribeToReadingListDialog dialog = new SubscribeToReadingListDialog(
            GlobalController.SINGLETON.getMainFrame());
        dialog.open(guides, selectedGuide, listURL == null ? null : listURL.toString());
        if (!dialog.hasBeenCanceled())
        {
            URL[] rls = StringUtils.strToURLs(dialog.getURLs());

            if (dialog.isNewGuide())
            {
                createGuideAndSubscribe(dialog.getNewGuideTitle(), rls);
            } else
            {
                subscribe(dialog.getSelectedExistingGuide(), rls);
            }
        }
    }

    /**
     * Creates new guide with a title and random unused icon and subscribes to reading lists.
     *
     * @param title title.
     * @param urls  links.
     */
    public static void createGuideAndSubscribe(String title, URL[] urls)
    {
        // Get unique title
        GuidesSet set = GlobalController.SINGLETON.getModel().getGuidesSet();
        Set titles = set.getGuidesTitles();
        String unique = ImportGuidesAction.getUniqueTitle(title, titles);

        // Create new guide
        StandardGuide guide = new StandardGuide();
        guide.setTitle(unique);
        guide.setIconKey(ImportGuidesAction.getUnusedIcon(set));

        // Add guide
        set.add(guide);

        subscribe(guide, urls);
    }

    /**
     * Subscribes a guide to reading list links.
     *
     * @param guide guide.
     * @param urls  links.
     */
    public static void subscribe(StandardGuide guide, URL[] urls)
    {
        for (URL url : urls) guide.add(new ReadingList(url));
    }

    /**
     * Reading list dialog.
     */
    private static class SubscribeToReadingListDialog extends AbstractReadingListDialog
    {
        private JComboBox cbGuides;
        private JTextField tfGuideTitle;
        private JRadioButton rbExistingGuide;
        private JRadioButton rbNewGuide;

        /**
         * Creates dialog.
         *
         * @param parent parent.
         */
        public SubscribeToReadingListDialog(Frame parent)
        {
            super(parent, Strings.message("subscribe.to.readinglist.dialog.title"));

            cbGuides = new JComboBox();
            tfGuideTitle = new JTextField();

            RadioListener listener = new RadioListener();
            rbExistingGuide = ComponentsFactory.createRadioButton(
                Strings.message("subscribe.to.readinglist.dialog.add.to.existing.guide"));
            rbExistingGuide.addActionListener(listener);
            rbNewGuide = ComponentsFactory.createRadioButton(
                Strings.message("subscribe.to.readinglist.dialog.add.to.new.guide"));
            rbNewGuide.addActionListener(listener);
            ButtonGroup bg = new ButtonGroup();
            bg.add(rbExistingGuide);
            bg.add(rbNewGuide);
        }

        /**
         * Builds header.
         *
         * @return header.
         */
        protected JComponent buildHeader()
        {
            return new HeaderPanelExt(
                Strings.message("subscribe.to.readinglist.dialog.title"),
                Strings.message("subscribe.to.readinglist.dialog.header"));
        }

        /**
         * Builds main pane.
         *
         * @return pane.
         */
        protected JComponent buildMain()
        {
            BBFormBuilder builder = new BBFormBuilder("p, 4dlu, max(50dlu;p), 4dlu, max(100dlu;p):grow, 4dlu, p");
            builder.setDefaultDialogBorder();

            builder.append(Strings.message("guide.dialog.readinglists.add.address"), 1, tfAddress, 3);
            builder.append(new JButton(new SuggestAction()));
            builder.setLeadingColumnOffset(2);
            builder.append(Strings.message("guide.dialog.readinglists.add.status"), lbStatus);
            builder.appendUnrelatedComponentsGapRow(2);

            builder.setLeadingColumnOffset(0);
            builder.append(rbExistingGuide, 3);
            builder.append(cbGuides);
            builder.nextLine();

            builder.setLeadingColumnOffset(0);
            builder.append(rbNewGuide, 3);
            builder.append(tfGuideTitle);
            builder.nextLine();

            updateRadioStatus();

            return builder.getPanel();
        }

        /**
         * Builds buttons.
         *
         * @return buttons.
         */
        protected JComponent buildButtons()
        {
            return ButtonBarFactory.buildOKCancelBar(btnCheckAndAdd, createCancelButton());
        }

        /**
         * Returns <code>TRUE</code> if new guide should be created.
         *
         * @return <code>TRUE</code> if new guide should be created.
         */
        public boolean isNewGuide()
        {
            return rbNewGuide.isSelected();
        }

        /**
         * Returns new guide title.
         *
         * @return title.
         */
        public String getNewGuideTitle()
        {
            return tfGuideTitle.getText();
        }

        /**
         * Returns existing guide which is currently selected.
         *
         * @return existing guide.
         */
        public StandardGuide getSelectedExistingGuide()
        {
            StandardGuide guide = null;

            Object item = cbGuides.getSelectedItem();
            if (item instanceof GuideHolder)
            {
                guide = ((GuideHolder)item).getGuide();
            }

            return guide;
        }

        /**
         * Opens the dialog.
         *
         * @param guides        guides.
         * @param selectedGuide selected guide.
         * @param defaultURL    the link to put in the address line.
         */
        public void open(StandardGuide[] guides, IGuide selectedGuide, String defaultURL)
        {
            tfAddress.setText(defaultURL);
            initExistingGuides(guides, selectedGuide);

            super.open();
        }

        /**
         * Does some verifications before apply.
         */
        public void doAccept()
        {
            String msgNewTitle = Strings.message("subscribe.to.readinglist.enter.title.please");

            if (isNewGuide() &&
                (StringUtils.isEmpty(getNewGuideTitle()) ||
                 msgNewTitle.equals(getNewGuideTitle())))
            {
                tfGuideTitle.requestFocus();
                tfGuideTitle.setText(msgNewTitle);
                tfGuideTitle.selectAll();
            } else super.doAccept();
        }

        /**
         * Initializes existing guides list and selects appropriate radio button.
         *
         * @param guides        guides.
         * @param selectedGuide currently selected guide.
         */
        private void initExistingGuides(StandardGuide[] guides, IGuide selectedGuide)
        {
            cbGuides.removeAllItems();

            if (guides == null || guides.length == 0)
            {
                // No Guides
                rbExistingGuide.setEnabled(false);
                rbNewGuide.setSelected(true);
            } else
            {
                // Fill guides box
                int selectedGuideIndex = -1;
                for (int i = 0; i < guides.length; i++)
                {
                    StandardGuide guide = guides[i];
                    GuideHolder holder = new GuideHolder(guide);
                    cbGuides.addItem(holder);

                    if (guide == selectedGuide) selectedGuideIndex = i;
                }

                if (selectedGuideIndex != -1) cbGuides.setSelectedIndex(selectedGuideIndex);

                rbExistingGuide.setEnabled(true);
                rbExistingGuide.setSelected(true);
            }
        }

        /**
         * Updates status of components depending on the radio button status.
         */
        private void updateRadioStatus()
        {
            boolean bEx = rbExistingGuide.isSelected();

            cbGuides.setEnabled(bEx);
            tfGuideTitle.setEnabled(!bEx);
        }

        /**
         * Radio button change listener.
         */
        private class RadioListener implements ActionListener
        {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e)
            {
                updateRadioStatus();
            }
        }

        /**
         * Action to call when a user asks for suggestion.
         */
        private class SuggestAction extends AbstractAction
        {
            /**
             * Defines an <code>Action</code> object with a default
             * description string and default icon.
             */
            public SuggestAction()
            {
                super(Strings.message("guide.dialog.readinglists.add.suggest"));
            }

            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e)
            {
                CollectionItemsSelectionDialog dialog =
                        new CollectionItemsSelectionDialog(SubscribeToReadingListDialog.this);

                tfAddress.setText(dialog.open("", new ArrayList(), true));
            }
        }

        /**
         * Holder.
         */
        private static class GuideHolder
        {
            private final StandardGuide guide;

            /**
             * Creates a holder.
             *
             * @param guide guide.
             */
            public GuideHolder(StandardGuide guide)
            {
                this.guide = guide;
            }

            /**
             * Returns associated guide.
             *
             * @return guide.
             */
            public StandardGuide getGuide()
            {
                return guide;
            }

            /**
             * Returns a string representation of the object.
             *
             * @return a string representation of the object.
             */
            public String toString()
            {
                return guide.getTitle();
            }
        }
    }
}
