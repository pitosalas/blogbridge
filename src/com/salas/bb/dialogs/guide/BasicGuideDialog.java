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
// $Id: BasicGuideDialog.java,v 1.54 2008/04/03 08:53:25 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.guide.AbstractReadingListDialog;
import com.salas.bb.dialogs.CollectionItemsSelectionDialog;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.utils.GuideIcons;
import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;
import com.salas.bb.utils.uif.table.TooltipTableCellRenderer;
import com.salas.bb.views.mainframe.StarsSelectionComponent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Basic stage for both (add / edit) guide dialogs.
 */
public abstract class BasicGuideDialog extends AbstractDialog
{
    private static final String SERVICE_LINK = "http://www.blogbridge.com/";

    /** Width of dialog box in pixels. */
    private static final int DIALOG_WIDTH = 500;

    private final boolean           publishingAvailable;
    private int                     publishingLimit;
    private boolean                 publishingLimitReached;

    private final JTable            tblReadingLists;
    private final ReadingListsTableModel readingListsModel;
    private final JButton           btnAddReadingList;
    private final JButton           btnRemoveList;

    protected JTextField            tfTitle = new JTextField();
    protected String                originalTitle = null;

    protected Collection            presentTitles;
    protected ComboBoxModel         model;
    protected IconListCellRenderer  renderer;

    protected JCheckBox             chPublishingEnabled;
    protected JLabel                lbPublishingTitle;
    protected JLabel                lbPublishingTags;
    protected JLabel                lbPublishingURL;
    protected JLabel                lbPublishingPublic;
    protected LinkLabel             lnkPublishingURL;
    protected JLabel                lbLastPublishingDate;
    protected JLabel                tfLastPublishingDate;
    protected JTextField            tfPublishingTitle;
    protected JTextField            tfPublishingTags;
    protected JCheckBox             chPublishingPublic;
    protected JLabel                lbPublishingRating;
    protected StarsSelectionComponent sscPublishingRating;
    protected ValueHolder           vhPublishingRating;
    protected JButton               btnCopyToClipboard;
    protected JCheckBox             chAllowNotifications;

    protected GuidesSet             guidesSet;

    /**
     * Creates dialog.
     *
     * @param aFrame                    parent frame.
     * @param aTitle                    dialog title.
     * @param aPublishingAvailable      <code>TRUE</code> if publishing is available.
     * @param aPublishingLimit          the number of guides the user can have published.
     * @param aPublishingLimitReached   <code>TRUE</code> if the limit is reached.
     */
    public BasicGuideDialog(Frame aFrame, String aTitle, boolean aPublishingAvailable,
                            int aPublishingLimit, boolean aPublishingLimitReached)
    {
        super(aFrame, aTitle);

        publishingAvailable = aPublishingAvailable;
        publishingLimit = aPublishingLimit;
        publishingLimitReached = aPublishingLimitReached;

        presentTitles = Collections.EMPTY_SET;
        model = new GuideIcons.ComboBoxModel();
        renderer = new IconListCellRenderer();
        readingListsModel = new ReadingListsTableModel();

        tblReadingLists = new JTable(readingListsModel);
        tblReadingLists.setDefaultRenderer(String.class, new ReadingListsTableCellRenderer(readingListsModel));
        UifUtilities.setTableColWidth(tblReadingLists, 2, 90);

        btnAddReadingList = new JButton(null, ResourceUtils.getIcon("add.icon"));
        btnAddReadingList.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onAddReadingList();
            }
        });
        btnRemoveList = new JButton(null, ResourceUtils.getIcon("delete.icon"));
        btnRemoveList.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onRemoveReadingList();
            }
        });

        // Publishing components
        chPublishingEnabled = ComponentsFactory.createCheckBox(Strings.message("guide.dialog.enable.publishing"));
        lbPublishingPublic = new JLabel(Strings.message("guide.dialog.public.visibility"));
        chPublishingPublic = new JCheckBox();
        lbPublishingTitle = ComponentsFactory.createLabel(Strings.message("guide.dialog.reading.list.title"));
        lbPublishingTags = ComponentsFactory.createLabel(Strings.message("guide.dialog.tags"));
        lbPublishingURL = new JLabel(Strings.message("guide.dialog.publicationurl"));
        lnkPublishingURL = new LinkLabel(Strings.message("guide.dialog.not.published.yet"));
        lbLastPublishingDate = new JLabel(Strings.message("guide.dialog.last.update.date"));
        tfLastPublishingDate = new JLabel(Strings.message("guide.dialog.never.updated"));
        tfPublishingTitle = new JTextField();
        lbPublishingTitle.setLabelFor(tfPublishingTitle);
        tfPublishingTags = new JTextField();
        lbPublishingTags.setLabelFor(tfPublishingTags);

        vhPublishingRating = new ValueHolder(1);
        sscPublishingRating = new StarsSelectionComponent(new BoundedRangeAdapter(vhPublishingRating, 0, 1, 5));
        lbPublishingRating = new JLabel(Strings.message("guide.dialog.rating"));

        btnCopyToClipboard = new JButton(Strings.message("guide.dialog.copy"));
        btnCopyToClipboard.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                CommonUtils.copyTextToClipboard(lnkPublishingURL.getText());
            }
        });

        onPublishingEnabled();
        chPublishingEnabled.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                onPublishingEnabled();
            }
        });

        chAllowNotifications = ComponentsFactory.createCheckBox(Strings.message("guide.dialog.allow.notifications"));
    }

    /**
     * Enabled / disable publishing controls depending on the state of the
     * "Enable Publishing" mark.
     */
    private void onPublishingEnabled()
    {
        boolean enabled = chPublishingEnabled.isSelected();

        lbPublishingPublic.setEnabled(enabled);
        chPublishingPublic.setEnabled(enabled);
        lbLastPublishingDate.setEnabled(enabled);
        tfLastPublishingDate.setEnabled(enabled);
        lbPublishingTags.setEnabled(enabled);
        lbPublishingTitle.setEnabled(enabled);
        lbPublishingURL.setEnabled(enabled);
        lnkPublishingURL.setEnabled(enabled);
        btnCopyToClipboard.setEnabled(enabled);
        tfPublishingTags.setEnabled(enabled);
        tfPublishingTitle.setEnabled(enabled);
        lbPublishingRating.setEnabled(enabled);
        sscPublishingRating.setEnabled(enabled);

        if (enabled && StringUtils.isEmpty(getPublishingTitle()))
        {
            tfPublishingTitle.setText(getGuideTitle());
        }
    }

    /**
     * Invoked when adding new reading list is required.
     */
    private void onAddReadingList()
    {
        if (GlobalController.SINGLETON.checkForNewSubscription()) return;

        URL[] newListURLs = queryForURL();
        if (newListURLs != null)
        {
            for (URL url : newListURLs) readingListsModel.addList(url);
        }
    }

    private URL[] queryForURL()
    {
        NewReadingListDialog dialog = new NewReadingListDialog(this);
        dialog.open();

        String urlsS = !dialog.hasBeenCanceled() ? dialog.getURLs() : null;

        return StringUtils.strToURLs(urlsS);
    }

    /**
     * Invoked when removing selected reading list is required.
     */
    private void onRemoveReadingList()
    {
        int[] rows = tblReadingLists.getSelectedRows();
        boolean haveFeeds = false;
        for (int i = 0; !haveFeeds && i < rows.length; i++)
        {
            int row = rows[i];
            haveFeeds = readingListsModel.getLists()[row].getFeeds().length > 0;
        }

        boolean delete = true;
        if (haveFeeds)
        {
            String msg = rows.length == 1
                ? Strings.message("guide.dialog.readinglists.has.feeds")
                : Strings.message("guide.dialog.readinglists.have.feeds");

            delete = JOptionPane.showConfirmDialog(this, msg, Strings.message("guide.dialog.delete.readinglist"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        if (delete) readingListsModel.removeRows(rows);
    }

    /**
     * Returns title entered by user.
     *
     * @return title.
     */
    public String getGuideTitle()
    {
        return tfTitle.getText();
    }

    /**
     * Returns <code>TRUE</code> if publishing is enabled.
     *
     * @return <code>TRUE</code> when publishing is enabled.
     */
    public boolean isPublishingEnabled()
    {
        return chPublishingEnabled.isSelected();
    }

    /**
     * Returns the title of the reading list the user is going to publish.
     *
     * @return the title.
     */
    public String getPublishingTitle()
    {
        return tfPublishingTitle.getText().trim();
    }

    /**
     * Returns the tags of the reading list the user is going to publish.
     *
     * @return the tags.
     */
    public String getPublishingTags()
    {
        return tfPublishingTags.getText().trim();
    }

    /**
     * Returns the state of public publishing flag.
     *
     * @return the state of the flag.
     */
    public boolean isPublishingPublic()
    {
        return chPublishingPublic.isSelected();
    }

    /**
     * Returns minimum rating of feed to be published.
     *
     * @return minimum rating.
     */
    public int getPublishingRating()
    {
        return (Integer)vhPublishingRating.getValue();
    }

    /**
     * Returns resource key of selected icon.
     *
     * @return resource key.
     */
    public abstract String getIconKey();

    /**
     * Registers the list of titles which are not allowed.
     *
     * @param presTitles titles which are not allowed for entry.
     */
    protected void setPresentTitles(Collection presTitles)
    {
        this.presentTitles = presTitles;
    }

    /**
     * Returns <code>TRUE</code> when guide notifications are allowed.
     *
     * @return <code>TRUE</code> when guide notifications are allowed.
     */
    public boolean isNotificationsAllowed()
    {
        return chAllowNotifications.isSelected();
    }

    /**
     * Checks if information is valid.
     *
     * @return error message or NULL if everything is OK.
     */
    protected String validateInformation()
    {
        String msg = validateTitle();
        if (msg == null) msg = validatePublishing();

        return msg;
    }

    /**
     * Validates the publishing information entered.
     *
     * @return error message or <code>NULL</code> if everything is OK.
     */
    protected String validatePublishing()
    {
        String msg = null;

        if (isPublishingEnabled())
        {
            String publishingTitle = getPublishingTitle();
            if (StringUtils.isEmpty(publishingTitle))
            {
                msg = Strings.message("guide.dialog.validation.publishing.empty.title");
            } else if (StringUtils.indexOfAny(publishingTitle, new String[] { "/", "\"" }) > -1)
            {
                msg = Strings.message("guide.dialog.validation.publishing.invalid.title");
            } else
            {
                IGuide currentGuide = getGuide();
                IGuide otherGuide = guidesSet.getGuideByPublishingTitle(publishingTitle);

                if (currentGuide == null
                    ? otherGuide != null
                    : otherGuide != null && otherGuide != currentGuide)
                {
                    msg = MessageFormat.format(Strings.message("guide.dialog.validation.publishing.existing.title"),
                        getPublishingTitle());
                }
            }
        }

        return msg;
    }

    /**
     * The guide we are looking at.
     *
     * @return the guide.
     */
    protected abstract IGuide getGuide();

    /**
     * Checks if title is valid.
     *
     * @return error message or NULL.
     */
    protected String validateTitle()
    {
        String message = null;

        final String title = tfTitle.getText();
        if (title == null || title.trim().length() == 0)
        {
            message = Strings.message("guide.dialog.validation.empty.title");
        } else if (CommonUtils.areDifferent(originalTitle, title) && presentTitles.contains(title))
        {
            message = Strings.message("guide.dialog.validation.already.present");
        }
        return message;
    }

    /**
     * Returns test probe to the caller.
     *
     * @return test probe.
     */
    public Probe getProbe()
    {
        return new Probe();
    }

    /**
     * Called when user confirms the information.
     */
    public void doAccept()
    {
        String validationMessage = validateInformation();
        if (validationMessage == null)
        {
            super.doAccept();
        } else
        {
            JOptionPane.showMessageDialog(this, validationMessage,
                Strings.message("guide.dialog.error"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates Publishing tab.
     *
     * @return component.
     */
    protected JComponent buildPublishingTab()
    {
        JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("guide.dialog.publishing.publish"));

        return publishingAvailable
            ? (isReachedPublishingLimit()
                ? buildPublishingTabLimitReached(wording)
                : buildPublishingTabAvailable(wording))
            : buildPublishingTabUnavailable(wording);
    }

    /**
     * Tests if the publishing limit is reached.
     *
     * @return <code>TRUE</code> if the limit has been reached and it's not the already published guide.
     */
    private boolean isReachedPublishingLimit()
    {
        return publishingLimitReached && !chPublishingEnabled.isSelected();
    }

    /**
     * Builds the panel for publishing tab when too many guides are already published.
     *
     * @param wording wording to put on the page.
     *
     * @return component.
     */
    private JPanel buildPublishingTabLimitReached(JComponent wording)
    {
        BBFormBuilder builder = new BBFormBuilder("0:grow");
        builder.setDefaultDialogBorder();

        builder.append(wording);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(ComponentsFactory.createWrappedMultilineLabel(
            MessageFormat.format(Strings.message("guide.dialog.publishing.limit.reached"), publishingLimit)));
        builder.appendUnrelatedComponentsGapRow(2);

        LinkLabel link = new LinkLabel(Strings.message("guide.dialog.publishing.limit.reached.link"), SERVICE_LINK);
        builder.append(link);

        return builder.getPanel();
    }

    /**
     * Builds panel for publishing tab when publishing is unavailable.
     *
     * @param aWording wording to put on the page.
     *
     * @return component.
     */
    private JPanel buildPublishingTabUnavailable(JComponent aWording)
    {
        // Panel
        BBFormBuilder builder = new BBFormBuilder("0:grow");
        builder.setDefaultDialogBorder();

        builder.append(aWording);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("guide.dialog.publishing.you.need.bbservice.account")));

        return builder.getPanel();
    }

    /**
     * Builds panel for publishing tab when publishing is available.
     *
     * @param aWording wording to put on the page.
     *
     * @return component.
     */
    private JPanel buildPublishingTabAvailable(JComponent aWording)
    {
        JPanel sscPanel = new JPanel(new BorderLayout());
        sscPanel.add(sscPublishingRating, BorderLayout.WEST);

        // Panel
        BBFormBuilder builder = new BBFormBuilder("7dlu, p, 2dlu, 100dlu, 0:grow, 2dlu, p");
        builder.setDefaultDialogBorder();

        builder.append(aWording, 7);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(chPublishingEnabled, 7);
        builder.setLeadingColumnOffset(1);
        builder.append(lbPublishingTitle, tfPublishingTitle);
        builder.nextLine();
        builder.append(lbPublishingTags, tfPublishingTags);
        builder.nextLine();
        builder.append(lbPublishingPublic, chPublishingPublic);
        builder.nextLine();
        builder.append(lbPublishingRating, sscPanel);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(lbPublishingURL);
        builder.append(lnkPublishingURL, 2);
        builder.append(btnCopyToClipboard);
        builder.append(lbLastPublishingDate, tfLastPublishingDate);

        builder.setLeadingColumnOffset(0);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("min:grow");

        JComponent instructionsBox = ComponentsFactory.createInstructionsBox(
            Strings.message("guide.dialog.publishing.instructions"));
        builder.append(instructionsBox, 7, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    private JComponent msg(String msg)
    {
        return ComponentsFactory.createWrappedMultilineLabel(msg);
    }

    /**
     * Creates notifications panel.
     *
     * @return panel.
     */
    protected JComponent buildNotificationsTab()
    {
        // Wording
        JComponent wording = msg(Strings.message("guide.dialog.notifications.wording"));

        // Panel
        BBFormBuilder builder = new BBFormBuilder("0:grow");
        builder.setDefaultDialogBorder();

        builder.append(wording);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chAllowNotifications);

        return builder.getPanel();
    }

    /**
     * Builds reading lists tab.
     *
     * @return component.
     */
    protected JComponent buildReadingListsTab()
    {
        // Wording
        JComponent wording = msg(Strings.message("guide.dialog.readinglists.wording"));

        // Buttons
        Dimension btnSize = new Dimension(20, 20);
        btnAddReadingList.setPreferredSize(btnSize);
        btnRemoveList.setPreferredSize(btnSize);
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        JPanel bbar = new JPanel(layout);
        bbar.add(btnAddReadingList);
        bbar.add(btnRemoveList);
        layout.setHgap(0);
        layout.setVgap(0);

        // Panel
        BBFormBuilder builder = new BBFormBuilder("0:grow");
        builder.setDefaultDialogBorder();

        builder.append(wording);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("min:grow");
        builder.append(new JScrollPane(tblReadingLists), 1,
            CellConstraints.FILL, CellConstraints.FILL);
        builder.append(bbar);

        return builder.getPanel();
    }

    /**
     * Shows given list of reading lists.
     *
     * @param lists lists.
     */
    protected void setReadingLists(ReadingList[] lists)
    {
        tblReadingLists.setEnabled(lists != null);
        btnAddReadingList.setEnabled(lists != null);
        btnRemoveList.setEnabled(lists != null);

        readingListsModel.setLists(lists);
    }

    /**
     * Returns currently displayed list of reading lists.
     *
     * @return list which is currently displayed.
     */
    public ReadingList[] getReadingLists()
    {
        return readingListsModel.getLists();
    }

    protected void resizeHook(JComponent content)
    {
        int height = (int)(DIALOG_WIDTH * 1.1);
        content.setPreferredSize(new Dimension(DIALOG_WIDTH, height));
    }

    /**
     * Opens a dialog.
     *
     * @param set guides set.
     */
    public void openDialog(GuidesSet set)
    {
        guidesSet = set;
        super.open();
    }

    /**
     * Renders simple icon element basing on the key of resource passed in <code>value</code>
     * parameter as a String.
     */
    protected static class IconListCellRenderer extends JPanel
            implements ListCellRenderer
    {
        private JLabel iconLabel = new JLabel();

        /**
         * Constructs new cell renderer.
         */
        public IconListCellRenderer()
        {
            add(iconLabel);
        }

        /**
         * Return a component that has been configured to display the specified
         * value.
         *
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
         *      Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus)
        {
            final String key = (String)value;
            final Icon icon = IconSource.getIcon(key);

            final Color back = isSelected ? list.getSelectionBackground() : list.getBackground();
            setBackground(back);
            iconLabel.setBackground(back);

            iconLabel.setIcon(icon);

            return this;
        }
    }

    /**
     * Cell renderer that makes missing reading lists appear gray.
     */
    private static class ReadingListsTableCellRenderer extends TooltipTableCellRenderer
    {
        private final ReadingListsTableModel model;

        /**
         * Creates cell renderer.
         *
         * @param model model.
         */
        public ReadingListsTableCellRenderer(ReadingListsTableModel model)
        {
            this.model = model;
        }

        /**
         * Returns the default table cell renderer.
         *
         * @param table      the <code>JTable</code>
         * @param value      the value to assign to the cell at
         *                   <code>[row, column]</code>
         * @param isSelected true if cell is selected
         * @param hasFocus   true if cell has focus
         * @param row        the row of the cell to render
         * @param column     the column of the cell to render
         * @return the default table cell renderer
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column)
        {
            ReadingList list = model.getLists()[row];
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setForeground(list.isMissing() ? Color.GRAY : table.getForeground());
            return comp;
        }
    }

    /**
     * Reading lists table model.
     */
    private static class ReadingListsTableModel extends DefaultTableModel
    {
        // Format for dates
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd, yy");

        /** List name. */
        public static final int COL_NAME    = 0;
        /** List URL. */
        public static final int COL_URL     = 1;
        /** List latest update time. */
        public static final int COL_LATEST  = 2;

        private static final String[] COLUMNS = {
            Strings.message("guide.dialog.readinglists.table.name"),
            Strings.message("guide.dialog.readinglists.table.url"),
            Strings.message("guide.dialog.readinglists.table.latest")
        };
        private static final Class[] CLASSES = { String.class, String.class, String.class };

        private ReadingList[] lists;

        /**
         * Returns current version of reading lists.
         *
         * @return current version.
         */
        public ReadingList[] getLists()
        {
            return lists;
        }

        /**
         * Sets the version of reading lists.
         *
         * @param aLists lists.
         */
        public void setLists(ReadingList[] aLists)
        {
            lists = aLists;
            fireTableDataChanged();
        }

        /**
         * Returns number of columns in this table.
         *
         * @return columns
         */
        public int getColumnCount()
        {
            return COLUMNS.length;
        }

        /**
         * Returns number of rows in the table.
         *
         * @return rows.
         */
        public int getRowCount()
        {
            return lists == null ? 0 : lists.length;
        }

        /**
         * Returns value of a cell.
         *
         * @param row       row.
         * @param column    column.
         *
         * @return value.
         */
        public Object getValueAt(int row, int column)
        {
            Object value;
            ReadingList list = lists[row];

            switch (column)
            {
                case COL_NAME:
                    value = list.getTitle();
                    break;
                case COL_URL:
                    value = list.getURL().toString();
                    break;
                case COL_LATEST:
                    long lastPollTime = list.getLastPollTime();
                    value = pollTimeToString(lastPollTime);
                    break;
                default:
                    value = null;
            }
            return value;
        }

        /**
         * Converts timestamp into some string representation.
         *
         * @param aTime time to convert.
         *
         * @return string.
         */
        private String pollTimeToString(long aTime)
        {
            String timeS;

            if (aTime == -1)
            {
                timeS = Strings.message("guide.dialog.readinglists.table.latest.never");
            } else
            {
                TimeRange range = findTimeRange(aTime);
                if (range == TimeRange.TR_FUTURE)
                {
                    timeS = Strings.message("guide.dialog.readinglists.table.latest.never");
                } else if (range == TimeRange.TR_TODAY)
                {
                    DateFormat fmt = DateFormat.getTimeInstance(DateFormat.SHORT);
                    timeS = MessageFormat.format(Strings.message("guide.dialog.readinglists.table.latest.today.0"),
                        fmt.format(new Date(aTime)));
                } else if (range == TimeRange.TR_YESTERDAY)
                {
                    DateFormat fmt = DateFormat.getTimeInstance(DateFormat.SHORT);
                    timeS = MessageFormat.format(Strings.message("guide.dialog.readinglists.table.latest.yesterday.0"),
                        fmt.format(new Date(aTime)));
                } else
                {
                    timeS = DATE_FORMAT.format(new Date(aTime));
                }
            }

            return timeS;
        }

        /**
         * Finds range matching this time.
         *
         * @param aTime time.
         *
         * @return range.
         */
        private TimeRange findTimeRange(long aTime)
        {
            TimeRange range = null;

            for (int i = 0; range == null && i < TimeRange.TIME_RANGES.length; i++)
            {
                TimeRange timeRange = TimeRange.TIME_RANGES[i];
                if (timeRange.isInRange(aTime)) range = timeRange;
            }

            return range;
        }

        /**
         * None of the cells editable.
         *
         * @param row       row.
         * @param column    column.
         *
         * @return <code>FALSE</code>.
         */
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        /**
         * Returns the title of the column.
         *
         * @param column column.
         *
         * @return title.
         */
        public String getColumnName(int column)
        {
            return COLUMNS[column];
        }

        /**
         * Returns data class of the column.
         *
         * @param column column.
         *
         * @return class.
         */
        public Class getColumnClass(int column)
        {
            return CLASSES[column];
        }

        /**
         * Removes specific rows from the list of reading lists.
         *
         * @param aRows rows to remove.
         */
        public void removeRows(int[] aRows)
        {
            Arrays.sort(aRows);

            java.util.List<ReadingList> newLists = new ArrayList<ReadingList>(Arrays.asList(lists));
            for (int i = aRows.length - 1; i >= 0; i--)
            {
                newLists.remove(aRows[i]);
            }

            setLists(newLists.toArray(new ReadingList[newLists.size()]));
        }

        /**
         * Adds new reading list URL.
         *
         * @param aNewListURL new reading list.
         */
        public void addList(URL aNewListURL)
        {
            ReadingList[] newList = new ReadingList[lists.length + 1];
            System.arraycopy(lists, 0, newList, 0, lists.length);
            newList[lists.length] = new ReadingList(aNewListURL);

            setLists(newList);
        }
    }

    /**
     * New reading list addition dialog with verification of URL.
     */
    private class NewReadingListDialog extends AbstractReadingListDialog
    {
        private final JButton btnSuggest;

        /**
         * Creates dialog.
         *
         * @param parent parent dialog.
         */
        public NewReadingListDialog(Dialog parent)
        {
            super(parent, Strings.message("guide.dialog.add.readinglist"));

            btnSuggest = new JButton(new SuggestAction());
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        }

        /**
         * Repacks the window each time it is displayed to adjust size to fit
         * wording and buttons.
         *
         * @param e event.
         */
        protected void processWindowEvent(WindowEvent e)
        {
            super.processWindowEvent(e);

            if (e.getID() == WindowEvent.WINDOW_OPENED) pack();
        }

        /**
         * Builds main part.
         *
         * @return main part.
         */
        protected JComponent buildMain()
        {
            BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, max(pref;200px):grow, 4dlu, p");
            builder.setDefaultDialogBorder();

            JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
                Strings.message("guide.dialog.readinglists.add.wording"));

            builder.append(wording, 3);
            builder.appendUnrelatedComponentsGapRow(2);

            builder.append(Strings.message("guide.dialog.readinglists.add.address"), tfAddress);
            builder.append(btnSuggest);
            builder.append(Strings.message("guide.dialog.readinglists.add.status"), lbStatus);

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
                        new CollectionItemsSelectionDialog(BasicGuideDialog.this);

                tfAddress.setText(dialog.open("", new ArrayList(), true));
                if (!dialog.hasBeenCanceled()) NewReadingListDialog.super.doAccept();
            }
        }
    }

    /**
     * Testing agent used in tests.
     */
    public class Probe
    {
        /**
         * Initializes structured and values necessary for title validation.
         *
         * @param orgTitle original title (NULL for new guide).
         * @param prTitles list of present titles.
         * @param title    title entered by user.
         *
         * @return TRUE if validation was successful.
         */
        public boolean validate(String orgTitle, Collection prTitles, String title)
        {
            originalTitle = orgTitle;
            setPresentTitles(prTitles);
            tfTitle.setText(title);

            return validateInformation() == null;
        }
    }
}
