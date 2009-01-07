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
// $Id: FeedsPanel.java,v 1.99 2007/09/19 15:55:01 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.SystemUtils;
import com.jgoodies.uifextras.util.UIFactory;
import com.salas.bb.core.*;
import com.salas.bb.domain.*;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.dnd.DNDList;
import com.salas.bb.utils.dnd.DNDListContext;
import com.salas.bb.utils.dnd.IDNDObject;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;
import com.salas.bb.views.settings.RenderingManager;
import com.salas.bb.views.settings.RenderingSettingsNames;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Display information about all the Channels that are part of the selected ChannelGuide.
 */
public class FeedsPanel extends CoolInternalFrame
{
    private static final Logger LOG = Logger.getLogger(FeedsPanel.class.getName());

    protected static final int INITIAL_VISIBLE_ROWS     = 15;
    protected static final int FIXED_CHAN_CELL_WIDTH    = 185;
    protected static final int FIXED_CHAN_CELL_HEIGHT   = 35;

    /** Pause in ms between progress icon frames. */
    private static final int PROGRESS_ICON_FRAME_PAUSE = 750;

    protected DNDList               feedsList;
    protected JScrollPane           scrollPane;
    private FeedsListCellRenderer   cellRenderer;
    private UnreadActivityController    activityController;
    private JLabel lbNoGuideSelected;

    private Action onDoubleClickAction;

    /**
     * Constructor.
     */
    public FeedsPanel()
    {
        super(Strings.message("panel.feeds"));

        lbNoGuideSelected = new JLabel(Strings.message("panel.feeds.no.guide.selected"));
        lbNoGuideSelected.setHorizontalAlignment(SwingUtilities.CENTER);

        // Create and register toolbar
        setHeaderControl(createSubtoolbar());

        GlobalModel globalModel = GlobalModel.SINGLETON;

        GuideModel model = globalModel.getGuideModel();
        feedsList = new DNDList(model);
        model.setListComponent(feedsList);

        // Set the background
        setBackground(feedsList.getBackground());

        // Register own controller listener
        final ControllerListener l = new ControllerListener();
        GlobalController.SINGLETON.addControllerListener(l);

        setPreferredSize(new Dimension(FIXED_CHAN_CELL_WIDTH, FIXED_CHAN_CELL_HEIGHT));

        UserPreferences prefs = globalModel.getUserPreferences();
        long delay = prefs.getFeedSelectionDelay();
        FeedSelectionListener selListener = new FeedSelectionListener(delay);
        prefs.addPropertyChangeListener(UserPreferences.PROP_FEED_SELECTION_DELAY, selListener);
        feedsList.addListSelectionListener(selListener);
        feedsList.addMouseListener(selListener);

        // Subscribe to theme and layout changes notifications
        RenderingManager.addPropertyChangeListener(new RenderSettingsChangeListener());

        new LoadingIconRepainter(feedsList, PROGRESS_ICON_FRAME_PAUSE).start();
        cellRenderer = new FeedsListCellRenderer();
        onListColorsUpdate();

        feedsList.setCellRenderer(cellRenderer);
        feedsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        feedsList.setVisibleRowCount(INITIAL_VISIBLE_ROWS);
//        feedsList.setBorder(new EmptyBorder(2, 2, 2, 2));
        Dimension cellSize = cellRenderer.getFixedCellSize();
        feedsList.setFixedCellWidth(cellSize.width);
        feedsList.setFixedCellHeight(cellSize.height);

        scrollPane = UIFactory.createStrippedScrollPane(feedsList);
        scrollPane.setMinimumSize(new Dimension(cellSize.width + 55, cellSize.height));

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(feedsList);
        add(scrollPane);

        // For the Channel List itself
        final MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();
        feedsList.addMouseListener(mainFrame.getFeedsListPopupAdapter());

        // For the header of the ChannelListScrollarea
        addMouseListener(mainFrame.getFeedsListPopupAdapter());

        final FeedsListListener listener = new FeedsListListener(this);

        feedsList.addMouseListener(listener);
        feedsList.addMouseMotionListener(listener);

        // Enable drag'n'drop
        feedsList.addPropertyChangeListener(DNDList.PROP_DRAGGING, new DraggingListListener());
        feedsList.setDropTarget(new URLDropTarget(new URLDropListener()));

        activityController = new UnreadActivityController(this);

        FeedDisplayModeManager.getInstance().addListener(new IDisplayModeManagerListener()
        {
            public void onClassColorChanged(int feedClass, Color oldColor, Color newColor)
            {
                feedsList.repaint();
            }
        });

        // Select no guide initially
        l.guideSelected(null);
    }

    /**
     * Sets on-double-click action.
     *
     * @param action action.
     */
    public void setOnDoubleClickAction(Action action)
    {
        this.onDoubleClickAction = action;
    }

    /**
     * Creates sub-toolbar component.
     *
     * @return component.
     */
    private JComponent createSubtoolbar()
    {
        UserPreferences uPrefs = GlobalModel.SINGLETON.getUserPreferences();
        String propName = UserPreferences.PROP_GOOD_CHANNEL_STARZ;
        PropertyAdapter propertyAdapter = new PropertyAdapter(uPrefs, propName, true);
        BoundedRangeAdapter model = new BoundedRangeAdapter(propertyAdapter, 0, 1, 5);

        StarsSelectionComponent starsSelector = new StarsSelectionComponent(model);
        starsSelector.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Install Starz Selector and its proactive tip
        MouseListener tipAdapter = new TipOfTheDay.TipMouseAdapter(TipOfTheDay.TIP_STARZ_FILTER, true);
        starsSelector.addMouseListener(tipAdapter);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(starsSelector, BorderLayout.CENTER);

        return pnl;
    }

    /**
     * Shows the list of feeds or the "No Guide Selected" message depending on
     * current guide selection.
     *
     * @param selectedGuide current guide selection.
     */
    private void updateMainListArea(IGuide selectedGuide)
    {
        if (selectedGuide == null)
        {
            remove(scrollPane);
            add(lbNoGuideSelected);
        } else
        {
            remove(lbNoGuideSelected);
            add(scrollPane);
        }

        revalidate();
        repaint();
    }

    /**
     * Selects item in list.
     *
     * @param feed feed to select.
     */
    public void selectListItem(final IFeed feed)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            selectListItem0(feed);
        } else
        {
            SwingUtilities.invokeLater(new SelectFeed(feed));
        }
    }

    /**
     * Selects item in list.
     *
     * @param feed to select.
     */
    private void selectListItem0(IFeed feed)
    {
        GuideModel model = (GuideModel)feedsList.getModel();
        synchronized (model)
        {
            int index = model.indexOf(feed);
            if (index > -1)
            {
                // Select item only if it isn't already selected (multi-selections support)
                if (!feedsList.isSelectedIndex(index))
                {
                    feedsList.setSelectedIndex(index);
                    feedsList.ensureIndexIsVisible(index);
                }
            } else
            {
                feedsList.clearSelection();
            }
        }
    }

    /**
     * Returns feeds list component.
     *
     * @return feeds list component.
     */
    public JList getFeedsList()
    {
        return feedsList;
    }

    /**
     * Returns the monitor which manages the appearance of the freshness button.
     *
     * @return freshness button monitor
     */
    public UnreadActivityController getUnreadActivityController()
    {
        return activityController;
    }

    /**
     * Returns focusable component of the list.
     *
     * @return component.
     */
    public Component returnFocusableComponent()
    {
        return feedsList;
    }

    /**
     * Updates the list color according to the theme and starts list repainting.
     */
    private void onListColorsUpdate()
    {
        Color background = RenderingManager.getFeedsListBackground(false);
        feedsList.setBackground(background);
        feedsList.repaint();
    }

    /**
     * Update the layout of the feeds list cells.
     */
    private void updateFeedsListLayout()
    {
        cellRenderer.initLayout();
        Dimension size = cellRenderer.getFixedCellSize();
        feedsList.setFixedCellWidth(size.width);
        feedsList.setFixedCellHeight(size.height);
        feedsList.repaint();
        activityController.resetAttachment();
    }

    /**
     * Listens for URL drops and invokes feeds subscriptions.
     */
    private class URLDropListener implements IURLDropTargetListener
    {
        /**
         * Called when valid URL is dropped to the target.
         *
         * @param url       URL dropped.
         * @param location  mouse pointer location.
         */
        public void urlDropped(URL url, Point location)
        {
            if (GlobalController.SINGLETON.checkForNewSubscription()) return;

            final GlobalController controller = GlobalController.SINGLETON;
            final IFeed feed = controller.createDirectFeed(url.toString(), false);

            if (feed != null)
            {
                // We do this to be sure that all events connected to addition of
                // the feed to the guide are successfully processed.
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        controller.selectFeed(feed);
                    }
                });
            }
        }
    }

    /**
     * Listens for dragging mode changes of the list and updates global context.
     */
    private class DraggingListListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property that
         *            has changed.
         */
        public void propertyChange(final PropertyChangeEvent evt)
        {
            final IGuide sourceGuide = GlobalModel.SINGLETON.getSelectedGuide();

            boolean isDraggingFinished = !(Boolean)evt.getNewValue();
            if (isDraggingFinished && sourceGuide instanceof StandardGuide)
            {
                final DNDList source = DNDListContext.getSource();
                IDNDObject object = DNDListContext.getObject();
                int insertPosition = source.getInsertPosition();
                final Object[] feedsI = object.getItems();
                StandardGuide guide = (StandardGuide)sourceGuide;

                if (feedsList.isDraggingInternal())
                {
                    final GuideModel model = (GuideModel)feedsList.getModel();

                    // Dragging operation finished within the same list
                    final IFeed currentSelection = GlobalModel.SINGLETON.getSelectedFeed();

                    int index = insertPosition;
                    for (int i = 0; i < feedsI.length; i++)
                    {
                        IFeed feed = (IFeed)feedsI[feedsI.length - i - 1];

                        int currentIndex = model.indexOf(feed);
                        if (currentIndex < index) index--;

                        GlobalController.SINGLETON.moveFeed(feed, guide, guide, index);
                    }

                    // We call it in new EDT task as the model will be updated
                    // in the next event only, so we have to schedule ourselves
                    // after that update to get correct indexes.
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            int curSelNewIndex = model.indexOf(currentSelection);

                            boolean curSelIsOnTheList = false;
                            int[] newIndices = new int[feedsI.length];
                            for (int i = 0; i < feedsI.length; i++)
                            {
                                newIndices[i] = model.indexOf((IFeed)feedsI[i]);
                                curSelIsOnTheList |= newIndices[i] == curSelNewIndex;
                            }

                            ListSelectionModel selModel = source.getSelectionModel();
                            if (!curSelIsOnTheList)
                            {
                                selModel.setSelectionInterval(curSelNewIndex, curSelNewIndex);
                            } else
                            {
                                source.setSelectedIndices(newIndices);
                            }

                            selModel.setLeadSelectionIndex(curSelNewIndex);

                            // Return focus to the guide
                            GlobalController.SINGLETON.fireGuideSelected(sourceGuide);
                        }
                    });
                } else
                {
                    Object destination = DNDListContext.getDestination();
                    boolean isCopying = DNDListContext.isFinishedCopying();

                    if (destination instanceof StandardGuide &&
                        destination != sourceGuide)
                    {
                        StandardGuide destGuide = (StandardGuide)destination;

                        // Feeds should be moved to the new guide.
                        for (Object f : feedsI)
                        {
                            IFeed feed = (IFeed)f;
                            if (isCopying)
                            {
                                destGuide.add(feed);
                            } else
                            {
                                GlobalController.SINGLETON.moveFeed(feed,
                                    guide, destGuide, destGuide.getFeedsCount());
                            }
                        }

                        // EDT !!!
                        GlobalController.SINGLETON.fireGuideSelected(guide);
                        if (guide.getFeedsCount() > 0)
                        {
                            GlobalController.SINGLETON.selectFeed(guide.getFeedAt(0));
                        }
                    }
                }
            }
        }
    }

    /**
     * The UnreadActivityController manages the unread button and activity meter. There is a single
     * "live" instance of each, which is moved into place on whatever row the user has moused on.
     * We take care to remove or reset the buttons if the table contents changes, so that they're
     * not left in an obsolete position in the list.
     */
    static class UnreadActivityController extends ComponentAdapter
        implements ListDataListener, ActionListener
    {
        private final static String TOOLTIP_MSG_SINGLE = Strings.message("panel.feeds.unread.one");
        private final static String TOOLTIP_MSG_MANY = Strings.message("panel.feeds.unread.many");

        private JList feedsList;
        private ArticleActivityMeter activityMeter;
        private UnreadButton unreadButton;
        private int attachedRow;
        private IFeed attachedFeed;

        /**
         * Constructs as on the given FeedsPanel.
         * @param thePanel Panel we're attached to
         */
        UnreadActivityController(FeedsPanel thePanel)
        {
            feedsList = thePanel.getFeedsList();
            activityMeter = new ArticleActivityMeter();
            unreadButton = new UnreadButton();
            unreadButton.initToolTipMessage(TOOLTIP_MSG_SINGLE, TOOLTIP_MSG_MANY);
            attachedRow = -1;

            attachListeners();
        }

        /**
         * Adds listeners so that we are notified of changes to the list, and can track the button
         * press on the unread button.
         */
        void attachListeners()
        {
            GlobalModel.SINGLETON.getGuideModel().addListDataListener(this);
            unreadButton.addActionListener(this);
            feedsList.addComponentListener(this);
        }

        /**
         * Compute the unread statistics for the given feed, i.e. the number of read/unread
         * articles over the last X days.
         * @param feed The feed to calculate.
         * @return the UnreadStats for that feed.
         */
        static UnreadStats calcUnreadStats(IFeed feed)
        {
            UnreadStats stats = new UnreadStats();

            IArticle[] articles = feed.getArticles();
            for (IArticle art : articles)
            {
                stats.increment(art.getPublicationDate(), art.isRead());
            }
            return stats;
        }

        /**
         * Move the buttons into place on the given row of the list. Does nothing if we're already
         * attached at that position.
         * @param row index of row to attach to
         * @param forceUpdate <code>TRUE</code> to force button update even if already in place
         */
        void attachButtons(int row, boolean forceUpdate)
        {
            boolean showUnread = RenderingManager.isShowUnreadInFeeds();
            boolean showActivity = RenderingManager.isShowActivityChart();
            boolean showStarz = RenderingManager.isShowStarz();
            boolean showOneRow = !(showActivity || showStarz);

            IFeed feed = (IFeed) feedsList.getModel().getElementAt(row);

            boolean sameButton = (row == attachedRow && feed == attachedFeed);

            if (sameButton && !forceUpdate) return;

            attachedFeed = feed;
            attachedRow = row;
            UnreadStats stats = calcUnreadStats(attachedFeed);
            activityMeter.init(stats);

            Rectangle cellBounds  = feedsList.getCellBounds(row, row);
            Rectangle r = new Rectangle(cellBounds);

            r.x = r.width - 2;
            // Cell layout is slightly different on Mac -- see FeedListCellRender
            r.y += SystemUtils.IS_OS_MAC ? 3 : 1;

            if (showActivity)
            {
                r.x -= activityMeter.getSize().width;

                r.setSize(activityMeter.getSize());
                activityMeter.setBounds(r);
                feedsList.add(activityMeter);
            } else
            {
                feedsList.remove(activityMeter);

                if (showOneRow) r.x += 1;
            }

            if (showUnread)
            {
                // unread button is immediately to the left of the activity
                // meter, centered vertically in the row
                Dimension unreadButtonSize = unreadButton.getSize();
                r.x -= unreadButtonSize.width;
                r.setSize(unreadButtonSize);
                r.y = cellBounds.y + (SystemUtils.IS_OS_MAC ? 3 : 1);
                if (showOneRow) r.y += 1;

                int unreadCount = stats.getTotalCount().getUnread();
                Rectangle oldBounds = unreadButton.getBounds();

                // If it's the same button at the same position, then update it;
                // this preserves the mouse state of the button.  Otherwise,
                // reset it.
                if (sameButton && oldBounds.equals(r))
                {
                    unreadButton.update(unreadCount);
                } else
                {
                    unreadButton.setBounds(r);
                    unreadButton.init(stats.getTotalCount().getUnread());
                }
                feedsList.add(unreadButton);

                // Register the object this button is attached to (for event)
                unreadButton.setAttachedToObject(attachedFeed);
            } else
            {
                feedsList.remove(unreadButton);
            }
        }

        /**
         * Detach the buttons from the component hierarchy, effectively hiding them.
         */
        void detachButtons()
        {
            if (activityMeter.getParent() != null)
            {
                // For some reason an explicit repaint is required to
                // paint over the old button. (Problem is only noticable when
                // button had been displayed in a raised state when detached.)
                Rectangle r = activityMeter.getBounds();
                feedsList.remove(activityMeter);
                feedsList.repaint(r);
            }

            if (unreadButton.getParent() != null)
            {
                Rectangle r = unreadButton.getBounds();
                feedsList.remove(unreadButton);
                feedsList.repaint(r);
            }
            attachedFeed = null;
            attachedRow = -1;
        }

        /**
         * Validate that the buttons are attached in the proper position and show up-to-date
         * article read/unread info.
         */
        void resetAttachment()
        {
            if (attachedRow >= 0)
            {
                int row = attachedRow;
                IFeed feed = attachedFeed;

                // reattach to update them if row is still valid for feed
                ListModel model = feedsList.getModel();
                if (model.getSize() > row && model.getElementAt(row) == feed)
                    attachButtons(row, true);
                else
                    detachButtons();
            }
        }

        /**
         * Notifies us that contents of list have changed. Update button -- unread counts could
         * have changed.
         * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
         */
        public void contentsChanged(ListDataEvent e)
        {
            resetAttachment();
        }

        /**
         * Elements have been added to the list - reset button attachment.
         * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
         */
        public void intervalAdded(ListDataEvent e)
        {
            resetAttachment();
        }

        /**
         * Elements have been removed from the list - reset button attachment.
         * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
         */
        public void intervalRemoved(ListDataEvent e)
        {
            resetAttachment();
        }

        /**
         * The list has been resized - - reset button attachment.
         * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
         */
        public void componentResized(ComponentEvent e)
        {
            resetAttachment();
        }

        /**
         * Handle a press of the unread button. (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            GlobalModel model = GlobalModel.SINGLETON;

            IFeed feed = (IFeed)e.getSource();
            GlobalController.readFeeds(true, model.getSelectedGuide(), feed);
        }
    }

    /**
     * Simple listener for FeedsList to make sure that a right click menu also has the effect of selecting the indicated
     * item.
     */
    class FeedsListListener extends MouseAdapter implements MouseMotionListener
    {
        private static final int ICON_STARS_WIDTH = 64;

        private FeedsPanel feedsPanel;
        private JList feedsList;
        private GuideModel model;
        private MouseListener starzSettingTipAdapter;

        private int insetsTop;

        FeedsListListener(final FeedsPanel thePanel)
        {
            feedsPanel = thePanel;
            feedsList = thePanel.getFeedsList();

            model = (GuideModel)feedsList.getModel();
            insetsTop = feedsList.getInsets().top;

            starzSettingTipAdapter =
                new TipOfTheDay.TipMouseAdapter(TipOfTheDay.TIP_STARZ_SETTINGS, true);
        }

        /**
         * If this is a right click then select the corresponding row in the list.
         *
         * @param e event object.
         */
        public void mousePressed(final MouseEvent e)
        {
            Point point = e.getPoint();
            int row = feedsList.locationToIndex(point);
            if (row != -1 && feedsList.getCellBounds(row, row).contains(point))
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    if (!feedsList.isSelectedIndex(row)) feedsList.setSelectedIndex(row);
                } else if (SwingUtilities.isLeftMouseButton(e))
                {
                    if (FeedsListCellRenderer.getHoveredStar() != -1)
                    {
                        starzSettingTipAdapter.mousePressed(e);

                        IFeed feed = (IFeed)model.getElementAt(row);
                        int rating = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0 ? -1
                            : FeedsListCellRenderer.getHoveredStar();

                        if (feed.getRating() != rating)
                        {
                            feed.setRating(rating);

                            // If channel is no longer selectable then reset selection
                            if (!model.isPresent(feed))
                            {
                                GlobalController.SINGLETON.selectFeed(null);
                            }
                        }
                    }
                }
            }
        }

        private Point convertToCellCoords(Point point)
        {
            final Rectangle rect = feedsList.getCellBounds(0, 0);
            int y = (point.y - insetsTop) % rect.height;
            int x = point.x - feedsList.getInsets().left;

            return new Point(x, y);
        }

        private int locationToStar(Point point)
        {
            return (int)((point.x - 3) / (ICON_STARS_WIDTH / 5.0));
        }

        /**
         * Invoked when the mouse enters a component.
         */
        public void mouseEntered(final MouseEvent e)
        {
            checkHover(e.getPoint());
        }

        /**
         * Invoked when a mouse button is pressed on a component and then dragged.
         * <code>MOUSE_DRAGGED</code> events will continue to be delivered to the component where
         * the drag originated until the mouse button is released (regardless of whether the mouse
         * position is within the bounds of the component). <p/>Due to platform-dependent Drag&Drop
         * implementations, <code>MOUSE_DRAGGED</code> events may not be delivered during a native
         * Drag&Drop operation.
         */
        public void mouseDragged(final MouseEvent e)
        {
            checkHover(e.getPoint());
        }

        /**
         * Invoked when the mouse cursor has been moved onto a component but no buttons have been
         * pushed.
         */
        public void mouseMoved(final MouseEvent e)
        {
            checkHover(e.getPoint());
        }

        /**
         * Check if we require to mark some hover and to unmark some.
         *
         * @param point mouse pointer position.
         */
        private void checkHover(final Point point)
        {
            int cursor = Cursor.DEFAULT_CURSOR;
            int row = feedsList.locationToIndex(point);

            int star = -1;
            IFeed hoveredFeed = null;

            // Check if pointer over the rating icon
            if (row > -1 && feedsList.getCellBounds(row, row).contains(point))
            {
                // hover new rating icon
                hoveredFeed = (IFeed)feedsList.getModel().getElementAt(row);

                final Point convertedPoint = convertToCellCoords(point);
                if (cellRenderer.isStarzHovered(convertedPoint) &&
                    ((hoveredFeed instanceof DataFeed && ((DataFeed)hoveredFeed).isInitialized()) ||
                    (hoveredFeed instanceof SearchFeed)))
                {
                    boolean selectedCell = feedsList.getSelectedIndex() == row;
                    if (selectedCell)
                    {
                        star = locationToStar(convertedPoint);
                        cursor = Cursor.HAND_CURSOR;
                    }
                }

                feedsPanel.getUnreadActivityController().attachButtons(row, false);
            }

            FeedsListCellRenderer.setHoveredStar(star);
            FeedsListCellRenderer.setHoveredFeed(hoveredFeed);

            if (feedsList.getCursor().getType() != cursor)
            {
                feedsList.setCursor(Cursor.getPredefinedCursor(cursor));
            }
        }


        /**
         * Invoked when mouse clicks over the list.
         *
         * @param e event.
         */
        public void mouseClicked(MouseEvent e)
        {
            if (onDoubleClickAction != null && e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
            {
                onDoubleClickAction.actionPerformed(new ActionEvent(feedsList, 0, null));
            }
        }
    }

    /**
     * Listens for <code>GlobalController</code> events in order to get when channel selected. This
     * information is necessary to set correct articles list title.
     */
    private final class ControllerListener extends ControllerAdapter
    {
        /**
         * Invoked after application changes the channel.
         *
         * @param guide guide to which we are switching.
         */
        public void guideSelected(final IGuide guide)
        {
            final String text = (guide == null ? Strings.message("panel.feeds.no.guide.selected") : guide.getTitle());

            setSubtitle(MessageFormat.format(Strings.message("panel.in"), text));
            updateMainListArea(guide);
        }
    }

    /**
     * Calls model to fire updates of the cells once in specified interval. Cells are registered
     * during the waiting time and cleared once fired.
     */
    private static class LoadingIconRepainter extends Thread
    {
        private JList       list;
        private long        intervals;

        /**
         * Creates thread for repainting of loading icons.
         *
         * @param aList         list to monitor.
         * @param aIntervals    intervals of updates.
         */
        public LoadingIconRepainter(JList aList, long aIntervals)
        {
            super(LoadingIconRepainter.class.getName());
            setDaemon(true);

            list = aList;
            intervals = aIntervals;
        }

        /**
         * Repaint all feeds which require repainting.
         */
        private synchronized void repaintFeedsRows()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    ListModel model = list.getModel();
                    int firstVisibleIndex = list.getFirstVisibleIndex();
                    int lastVisibleIndex = list.getLastVisibleIndex();

                    if (firstVisibleIndex >= 0 && lastVisibleIndex >= 0)
                    {
                        for (int i = firstVisibleIndex; i <= lastVisibleIndex; i++)
                        {
                            IFeed feed = (IFeed)model.getElementAt(i);
                            if (FeedsListCellRenderer.needsProgressIcon(feed))
                            {
                                list.repaint(list.getCellBounds(i, i));

                                // The commented method of cell repainting is too dirty and expensive because
                                // it involves calls within the model and many blocks happen
//                                ((GuideModel)model).fireContentsChanged(model, i, i);
                            }
                        }
                    }
                }
            });
        }

        /**
         * Main thread cycle.
         */
        public void run()
        {
            while (true)
            {
                try
                {
                    repaintFeedsRows();
                    try
                    {
                        Thread.sleep(intervals);
                    } catch (InterruptedException e)
                    {
                        LOG.log(Level.WARNING, Strings.error("interrupted"), e);
                    }
                } catch (Exception e)
                {
                    LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
                }
            }
        }
    }

    /**
     * Custom Renderer for entries in the channelList.
     */
    private static class FeedsListCellRenderer extends JPanel implements ListCellRenderer
    {
        private static final Logger LOG = Logger.getLogger(FeedsListCellRenderer.class.getName());

        private static final Color COLOR_DRAG_SOURCE = Color.LIGHT_GRAY;
        private static final Border BORDER_NO_FOCUS = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        private JLabel lbStars;     // Icon for overall 'stars' ranking
        private JLabel lbLoading;   // Icon for loading process indication
        private JLabel lbTitle;     // Text for Title
        private JLabel lbIcon;      // Icon
        private ArticleActivityMeter activityMeter;
        private UnreadButton        unreadButton;

        private static int hoveredStar = -1;

        private static IFeed hoveredFeed;

        /**
         * Creates new cell renderer.
         */
        public FeedsListCellRenderer()
        {
            super();

            lbTitle = new JLabel();
            activityMeter = new ArticleActivityMeter();
            unreadButton = new UnreadButton();
            lbStars = new JLabel();
            lbLoading = new JLabel();
            lbIcon = new JLabel();
            lbIcon.setHorizontalAlignment(SwingConstants.CENTER);
            lbIcon.setPreferredSize(new Dimension(18, 16));

            setOpaque(true);
            setBorder(BORDER_NO_FOCUS);

            initLayout();

            if (LOG.isLoggable(Level.FINE)) LOG.fine("Completed construction");
        }

        /**
         * Initialize the layout of the list based on current
         * rendering options.
         */
        public void initLayout()
        {
            boolean showStarz = RenderingManager.isShowStarz();
            boolean showUnread = RenderingManager.isShowUnreadInFeeds();
            boolean showActivity = RenderingManager.isShowActivityChart();
            boolean showOneRow = !(showStarz || showActivity);
            boolean mac = SystemUtils.IS_OS_MAC;

            CellConstraints cc = new CellConstraints();
            String spacing = mac ? "2px" : "0";
            if (showOneRow)
            {
                // - title - spin - antenna - unread
                String cols = "1px, left:64px:grow, 1px, 12px, 2px, p, 1px, center:21px, 1px";
                String rows = spacing + ", max(16px;pref), " + spacing;
                setLayout(new FormLayout(cols, rows));

                add(lbTitle, cc.xy(2, 2));
                add(lbLoading, cc.xy(4, 2));
                add(lbIcon, cc.xy(6, 2));
                manageComponent(unreadButton, showUnread, cc.xy(8, 2));
                remove(lbStars);
                remove(activityMeter);
            } else
            {
                // - starz/title - spin - unread/untenna - activity
                String cols = "1px, left:64px:grow, 1px, 12px, 2px, center:21px, 1px, pref, 1px";
                String rows = spacing + ", max(12px;p), 1px, max(16px;pref), " + spacing;
                setLayout(new FormLayout(cols, rows));

                add(lbTitle, cc.xyw(2, 4, 3));
                add(lbLoading, cc.xy(4, 2));
                add(lbIcon, cc.xy(6, 4));
                manageComponent(unreadButton, showUnread, cc.xy(6, 2));
                manageComponent(lbStars, showStarz, cc.xy(2, 2));
                manageComponent(activityMeter, showActivity, cc.xywh(8, 2, 1, 3, "right, top"));
            }

            Font textFont = mac
                ? new Font("Lucida Grande", Font.BOLD, 10)
                : lbTitle.getFont().deriveFont(Font.BOLD);
            lbTitle.setFont(textFont);
        }

        /**
         * Adds or removes a component depending on its show-state.
         *
         * @param component component.
         * @param show      state.
         * @param constr    constraints.
         */
        private void manageComponent(JComponent component, boolean show, Object constr)
        {
            if (show)
            {
                add(component, constr);
            } else
            {
                remove(component);
            }
        }

        /*
        * Returns the fixed cell size.
        * If the cell renderer gets more complicated, we may have to create a
        * more elaborate cell mock-up in order to measure the size.
        */
        public Dimension getFixedCellSize()
        {
            lbTitle.setText("<dummy>"); // so title row has some height
            validate();
            return getPreferredSize();
        }

        /**
         * Sets the hovered feed.
         *
         * @param aHoveredFeed hovered feed.
         */
        public static void setHoveredFeed(IFeed aHoveredFeed)
        {
            hoveredFeed = aHoveredFeed;
        }

        /**
         * Sets the hovered star.
         *
         * @param aStar star.
         */
        public static void setHoveredStar(int aStar)
        {
            hoveredStar = aStar;
        }

        /**
         * Returns the hovered star.
         *
         * @return star.
         */
        public static int getHoveredStar()
        {
            return hoveredStar;
        }

        /**
         * Return a component that has been configured to display the specified value. That
         * component's <code>paint</code> method is then called to "render" the cell. If it is
         * necessary to compute the dimensions of a list because the list cells do not have a fixed
         * size, this method is called to generate a component on which
         * <code>getPreferredSize</code> can be invoked.
         *
         * @param list         The JList we're painting.
         * @param value        The value returned by list.getModel().getElementAt(index).
         * @param index        The cells index.
         * @param isSelected   True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         *
         * @return A component whose paint() method will render the specified value.
         *
         * @see javax.swing.JList
         * @see javax.swing.ListSelectionModel
         * @see javax.swing.ListModel
         */
        public Component getListCellRendererComponent(final JList list, final Object value,
                                                      final int index, final boolean isSelected, final boolean cellHasFocus)
        {
            // Setup the values
            IFeed currentFeed = (IFeed)value;

            if (currentFeed == null) return null;

            // Based on selection, choose foreground and background colors.
            Color backround = (index != -1 && isBeingDragged(value))
                ? COLOR_DRAG_SOURCE
                : isSelected
                    ? RenderingManager.getFeedsListSelectedBackground()
                    : RenderingManager.getFeedsListBackground(index % 2 == 0);

            Color foreground = FeedDisplayModeManager.getInstance().getColor(currentFeed);
            if (foreground == null || isSelected)
            {
                foreground = RenderingManager.getFeedsListForeground(isSelected);
            }

            setForeground(foreground);
            setBackground(backround);
            lbTitle.setForeground(foreground);
            lbTitle.setBackground(backround);
            lbStars.setForeground(Color.RED);
            lbStars.setBackground(Color.RED);

            // Indicate focus with a border
            setBorder((cellHasFocus)
                ? UIManager.getBorder("List.focusCellHighlightBorder")
                : BORDER_NO_FOCUS);

            FeedFormatter formatter = new FeedFormatter(currentFeed);

            String title = currentFeed.getTitle();

            // Find appropriate icon
            String type = null;
            if (currentFeed.isDynamic())
            {
                type = "feed.from.reading.list.icon";
            } else if (currentFeed instanceof QueryFeed)
            {
                type = "feed.query.icon";
            } else if (currentFeed instanceof SearchFeed)
            {
                type = "feed.search.icon";
            }
            Icon icon = type == null ? null : IconSource.getIcon(type);

            // Don't let the title string be empty, or FormLayout will consider
            // it 0 height and collapse ALL the title rows, in the case that this is the
            // first title in the list.
            if (title.length() == 0) title = Strings.message("panel.feeds.no.title");
            lbTitle.setText(title);
            lbIcon.setIcon(icon);

            UnreadStats stats = UnreadActivityController.calcUnreadStats(currentFeed);

            activityMeter.init(stats);
            unreadButton.init(stats.getTotalCount().getUnread());

            Icon iconStars = null;
            Icon iconLoading = null;

            if ((currentFeed instanceof DataFeed && ((DataFeed)currentFeed).isInitialized()) ||
                 currentFeed instanceof SearchFeed)
            {
                iconStars = formatter.getStarsIcon();
            }

            if (needsProgressIcon(currentFeed))
            {
                long time = System.currentTimeMillis();
                int frames = FeedFormatter.getLoadingIconFrames();
                int frame = (int)((time / PROGRESS_ICON_FRAME_PAUSE) % frames);
                iconLoading = FeedFormatter.getLoadingIcon(frame);
            }

            lbStars.setIcon(iconStars);
            lbLoading.setIcon(iconLoading);

            // Finally make the title bold only some articles have not yet been read.
            Font fnt = this.lbTitle.getFont();
            int style = currentFeed.isRead() ? Font.PLAIN : Font.BOLD;
            this.lbTitle.setFont(fnt.deriveFont(style));

            return this;
        }

        /**
         * Returns <code>TRUE</code> if value in the list of items being dragged at the moment.
         *
         * @param aValue value to check.
         *
         * @return <code>TRUE</code> if value in the list of items being dragged at the moment.
         */
        private boolean isBeingDragged(Object aValue)
        {
            boolean found = false;

            if (DNDListContext.isDragging())
            {
                Object[] items = DNDListContext.getObject().getItems();
                for (int i = 0; !found && i < items.length; i++)
                {
                    found = aValue == items[i];
                }
            }

            return found;
        }

        /**
         * Returns <code>TRUE</code> if feed needs progress indicator icon to be displayed.
         *
         * @param feed  feed to check.
         *
         * @return <code>TRUE</code> if feed needs progress indicator icon to be displayed.
         */
        static boolean needsProgressIcon(IFeed feed)
        {
            if (!GlobalController.getConnectionState().isOnline()) return false;

            boolean repaint = feed.isProcessing();

            if (!repaint && feed instanceof DirectFeed)
            {
                FeedMetaDataHolder holder = ((DirectFeed)feed).getMetaDataHolder();
                repaint = holder == null || !holder.isComplete();
            }

            return repaint;
        }

        /**
         * Returns the string to be used as the tooltip for <i>event </i>. By default this returns
         * any string set using <code>setToolTipText</code>. If a component provides more extensive
         * API to support differing tooltips at different locations, this method should be
         * overridden.
         *
         * @param event event object.
         * @return the tooltip message string
         */
        public String getToolTipText(final MouseEvent event)
        {
            if (hoveredFeed == null) return null;

            String text = null;

            Rectangle bounds = getBounds();
            setSize(-bounds.x, -bounds.y);

            Component comp = getComponentAt(event.getPoint());
            if (comp == lbStars)
            {
                GlobalModel model = GlobalModel.SINGLETON;

                int rating = hoveredFeed.getRating();
                int score = model.getScoreCalculator().calcBlogStarzScore(hoveredFeed);

                String name = covertToResources(FeedFormatter.getStarzFileName(score, true));
                String userRatingName = null;

                boolean userRatingSet = rating != -1;

                if (userRatingSet)
                {
                    userRatingName = FeedFormatter.getStarzFileName(rating, false);
                    userRatingName = covertToResources(userRatingName);
                }

                text = "<html><table border='0'><tr>" +
                    "<td>" + Strings.message("panel.feeds.starz.recommendation") + "</td>" +
                    "<td><img src='" + name + "'></td></tr>" +
                    "<tr><td>" + Strings.message("panel.feeds.starz.your.rating") + "</td>" +
                    "<td>" + (userRatingSet ? "<img src='" + userRatingName + "'>"
                        : Strings.message("panel.feeds.starz.not.set")) +
                    "</td></tr></table></html>";
            } else if (comp == lbIcon && hoveredFeed.isDynamic())
            {
                DirectFeed dFeed = (DirectFeed)hoveredFeed;
                ReadingList[] readingLists = dFeed.getReadingLists();
                String[] names = new String[readingLists.length];
                for (int i = 0; i < readingLists.length; i++)
                {
                    ReadingList list = readingLists[i];
                    names[i] = list.getTitle();
                    if (names[i] == null) names[i] = list.getURL().toString();
                    names[i] += " (" + list.getParentGuide().getTitle() + ")";
                }
                text = MessageFormat.format(Strings.message("panel.feeds.readinglists"),
                    StringUtils.join(names, ","));
            } else if (comp == lbTitle)
            {
                text = hoveredFeed.getTitle();
            } else
            {
                if (hoveredFeed != null && hoveredFeed.isInvalid())
                {
                    text = MessageFormat.format(Strings.message("panel.feeds.error"),
                        hoveredFeed.getInvalidnessReason());
                }
            }

            return text;
        }

        private String covertToResources(String path)
        {
            if (path == null) return null;

            return path.startsWith(File.separator) ? "/" + path : path;
        }

        /**
         * Returns <code>TRUE</code> if the starz component is hovered.
         *
         * @param aPoint point in the coordinates of the cell.
         *
         * @return <code>TRUE</code> if hovered.
         */
        public boolean isStarzHovered(Point aPoint)
        {
            return lbStars.contains(aPoint);
        }
    }

    /**
     * Listens for  changes to render setting.
     */
    private class RenderSettingsChangeListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            String prop = evt.getPropertyName();
            if (prop.equals(RenderingSettingsNames.THEME))
            {
                onListColorsUpdate();
            } else if (prop.equals(RenderingSettingsNames.IS_STARZ_SHOWING) ||
                    prop.equals(RenderingSettingsNames.IS_UNREAD_IN_FEEDS_SHOWING) ||
                    prop.equals(RenderingSettingsNames.IS_ACTIVITY_CHART_SHOWING))
            {
                updateFeedsListLayout();
            }
        }
    }

    /**
     * Simple feed selector.
     */
    private class SelectFeed implements Runnable
    {
        private final IFeed feed;

        public SelectFeed(IFeed aFeed)
        {
            feed = aFeed;
        }

        public void run()
        {
            selectListItem0(feed);
        }
    }
}

/**
 * Takes care of handling selection gestures on the feeds list.
 */
class FeedSelectionListener extends MouseAdapter
    implements ListSelectionListener, PropertyChangeListener
{
    private java.util.Timer     timer;
    private FeedSelector        task;

    private final Object        eventLock;
    private volatile IFeed      eventFeed;
    private volatile long       eventTime;
    private volatile int        eventIndex;

    private boolean             feedSelectionDelayed;

    public FeedSelectionListener(long aFeedSelectionDelay)
    {
        eventLock = new Object();
        timer = new java.util.Timer(true);
        setFeedSelectionDelay(aFeedSelectionDelay);
    }

    // Sets the delay and reschedules the timer.
    private void setFeedSelectionDelay(long aDelay)
    {
        if (task != null) task.cancel();

        feedSelectionDelayed = (aDelay != 0);

        if (feedSelectionDelayed)
        {
            task = new FeedSelector(aDelay);
            timer.schedule(task, 1, aDelay);
        }
    }

    /**
     * Called when feed selection delay property changes.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        Integer value = (Integer)evt.getNewValue();
        setFeedSelectionDelay(value.longValue());
    }

    /**
     * Call this whenever user clicks on one of the Channels in the ChannelList.
     *
     * @param e event object.
     */
    public void valueChanged(final ListSelectionEvent e)
    {
        // If either of these is true, the event can safely be ignored

        if (e.getValueIsAdjusting()) return;

        JList list = (JList)e.getSource();
        ListModel model = list.getModel();

        GlobalModel globalModel = GlobalModel.SINGLETON;
        IFeed prevFeed = globalModel == null ? null : globalModel.getSelectedFeed();
        int oldIndex = prevFeed == null ? -1 : ((GuideModel)model).indexOf(prevFeed);

        // Find out new selection index
        int selIndex = ListSelectionManager.evaluateSelectionIndex(list, oldIndex);

        final IFeed feed;
        feed = (selIndex == -1) ? null : (IFeed)model.getElementAt(selIndex);

        if (feedSelectionDelayed)
        {
            // We should always update selection event values for delayed selection
            // as we have invalid information of currently selected feed as the next
            // second it can become different.
            synchronized (eventLock)
            {
                eventTime = System.currentTimeMillis();
                eventFeed = feed;
                eventIndex = selIndex;
            }
        } else if (selIndex != oldIndex)
        {
            // We should update the feed directly only if the index has changed.
            selectFeed(feed);
        }
    }

    /**
     * Invoked when someone clicks over the feed in list.
     *
     * @param e event.
     */
    public void mousePressed(MouseEvent e)
    {
        // Every mouse press changes UI and every release sends event to the code.
        // We have to disarm a delay to avoid selection of the feed while the mouse
        // button is pressed. It causes problems when user quickly selects and deselect
        // the feed by doing press-release-press-... and at this moment feed becomes
        // selected again because of a triggered delayed feed selection as the result
        // of the first press, thereby frustrating the user.
        synchronized (eventLock)
        {
            Point point = e.getPoint();
            JList list = (JList)e.getSource();

            int index = list.locationToIndex(point);
            if (index != eventIndex)
            {
                eventIndex = -1;
                eventTime = -1;
                eventFeed = null;
            }
        }
    }

    /**
     * Select feed if it's not currently selected.
     * 
     * @param feed feed to select.
     */
    protected void selectFeed(final IFeed feed)
    {
        if (feed != GlobalModel.SINGLETON.getSelectedFeed() && feed != null)
        {
            if (UifUtilities.isEDT())
            {
                GlobalController.SINGLETON.selectFeed(feed);
            } else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        GlobalController.SINGLETON.selectFeed(feed);
                    }
                });
            }
        }
    }

    /** Feed selector with delay. */
    private class FeedSelector extends TimerTask
    {
        private long lastProcessedTime;
        private long feedSelectionDelay;

        public FeedSelector(long aFeedSelectionDelay)
        {
            lastProcessedTime = 0;
            feedSelectionDelay = aFeedSelectionDelay;
        }

        /**
         * Called periodically to check if some feed should be selected.
         */
        public void run()
        {
            long time;
            IFeed feed;

            synchronized (eventLock)
            {
                time = eventTime;
                feed = eventFeed;
            }

            if (time > lastProcessedTime &&
                System.currentTimeMillis() - time > feedSelectionDelay)
            {
                selectFeed(feed);
                lastProcessedTime = time;
            }
        }
    }
}
