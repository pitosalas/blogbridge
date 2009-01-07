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
// $Id: GuidesListModel.java,v 1.12 2007/10/04 09:55:07 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.IdentityList;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Adapter between guides set and the guides list model.
 * Guides set is the source of guides. This model monitors
 * the set it's assigned to for changes in the guides list
 * and guides themselves. Changes in guides aren't monitored
 * (so far) and must be delievered externally.
 */
public class GuidesListModel extends AbstractListModel
    implements IGuidesSetListener, IDisplayModeManagerListener
{
    /** Set this to <code>TRUE</code> when unit-testing. */
    public boolean testing;

    private final List<IGuide> guides;
    private final GuideDisplayModeManager dmm;

    private final DomainListener domainListener;
    private final UserPreferencesListener userPreferencesListener;
    private final ControllerListener controllerListener;

    private GuidesSet set;

    /**
     * When a batch of new guides is added, before the last one, this property
     * contains <code>TRUE</code> if there were any visible guides.
     */
    private boolean addedGuidesBatchHasVisible;

    /**
     * Adapts the set to list model.
     */
    public GuidesListModel()
    {
        guides = new IdentityList<IGuide>();
        dmm = GuideDisplayModeManager.getInstance();
        dmm.addListener(this);

        domainListener = new DomainListener();
        userPreferencesListener = new UserPreferencesListener();
        controllerListener = new ControllerListener();
    }

    /**
     * Registers the guide set to monitor and reflect.
     *
     * @param set set.
     */
    public void setGuidesSet(GuidesSet set)
    {
        if (this.set != null)
        {
            set.removeListener(this);
            int gc = guides.size();

            guides.clear();
            fireIntervalRemoved(this, 0, gc);
        }

        this.set = set;

        if (set != null)
        {
            reloadGuidesSet();
            set.addListener(this);
        }
    }

    /**
     * Returns the domain listener for subscriptions.
     *
     * @return listener.
     */
    public DomainListener getDomainListener()
    {
        return domainListener;
    }

    /**
     * Returns user preferences listener object.
     *
     * @return listener.
     */
    public UserPreferencesListener getUserPreferencesListener()
    {
        return userPreferencesListener;
    }

    /**
     * Returns the controller listener.
     *
     * @return listener.
     */
    public ControllerListener getControllerListener()
    {
        return controllerListener;
    }

    private void reloadGuidesSet()
    {
        int lpos = 0;
        int size = getSize();
        if (set != null)
        {
            IGuide selected = GlobalModel.SINGLETON == null ? null : GlobalModel.SINGLETON.getSelectedGuide();
            for (int i = 0; i < set.getGuidesCount(); i++)
            {
                IGuide guide = set.getGuideAt(i);

                if (selected == guide || dmm.isVisible(guide))
                {
                    if (lpos < size)
                    {
                        setGuide(lpos, guide);
                    } else
                    {
                        addGuide(guide);
                    }

                    lpos++;
                } else
                {
                    removeGuide(guide);
                }
            }
        }

        // Remove the remainder
        int from = lpos;
        int to = guides.size() - 1;
        if (to - from >= 0)
        {
            for (int i = guides.size() - 1; i >= lpos; i--)
            {
                guides.remove(i);
            }

            fireIntervalRemoved(this, from, to);
        }
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     *
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index)
    {
        return guides.get(index);
    }

    /**
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    public int getSize()
    {
        return guides.size();
    }

    // ------------------------------------------------------------------------
    // Model review methods
    // ------------------------------------------------------------------------

    /**
     * Invoked when a new guide is added.
     *
     * @param batchHasVisibleGuides <code>TRUE</code> if batch had visible guides.
     */
    public void onGuideAdded(boolean batchHasVisibleGuides)
    {
        if (batchHasVisibleGuides) reloadGuidesSet();
    }

    /**
     * Invoked when a guide is removed.
     *
     * @param guide     guide.
     */
    public void onGuideRemoved(IGuide guide)
    {
        if (guides.contains(guide)) removeGuide(guide);
    }

    /**
     * Invoked when a guide is moved.
     *
     * @param guide     guide.
     * @param oldIndex  old index.
     * @param newIndex  new index.
     */
    public void onGuideMoved(IGuide guide, int oldIndex, int newIndex)
    {
        if (guides.contains(guide))
        {
            reloadGuidesSet();
        }
    }

    /**
     * Invoked when a guide is updated.
     *
     * @param guide     guide.
     */
    public void onGuideUpdated(IGuide guide)
    {
    }

    /**
     * Invoked when guide contents are updated and the guide has to
     * be reviewed.
     *
     * @param guide     guide.
     */
    public void onGuideContentsUpdated(IGuide guide)
    {
        boolean curVisible = guides.contains(guide);
        boolean shoVisible = GlobalModel.SINGLETON.getSelectedGuide() == guide || dmm.isVisible(guide);

        if (curVisible && !shoVisible) removeGuide(guide);
        else if (!curVisible && shoVisible) reloadGuidesSet();
    }

    /**
     * Invoked when color of some class changes.
     *
     * @param cl       class.
     * @param oldColor old color value.
     * @param newColor new color value.
     */
    public void onClassColorChanged(int cl, Color oldColor, Color newColor)
    {
        // Ignore options change if visibility isn't affected (nothing has changed
        // from or to hidden state)
        if (oldColor != null && newColor != null) return;

        reloadGuidesSet();
    }

    // ------------------------------------------------------------------------
    // Supplementary functions
    // ------------------------------------------------------------------------

    /**
     * Returns index of the guide.
     *
     * @param guide guide.
     *
     * @return index.
     */
    public int indexOf(IGuide guide)
    {
        return guides.indexOf(guide);
    }

    private void setGuide(int pos, IGuide guide)
    {
        if (guides.get(pos) != guide)
        {
            guides.set(pos, guide);
            fireContentsChanged(this, pos, pos);
        }
    }

    private void addGuide(IGuide guide)
    {
        int index = guides.size();
        guides.add(guide);
        fireIntervalAdded(this, index, index);
    }

    private void removeGuide(IGuide guide)
    {
        int index = guides.indexOf(guide);
        if (index > -1)
        {
            guides.remove(guide);
            fireIntervalRemoved(this, index, index);
        }
    }

    // ------------------------------------------------------------------------
    // Guide Set listener
    // ------------------------------------------------------------------------

    /**
     * Invoked when new guide has been added to the set.
     *
     * @param set           guides set.
     * @param guide         added guide.
     * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
     */
    public void guideAdded(GuidesSet set, final IGuide guide, boolean lastInBatch)
    {
        addedGuidesBatchHasVisible |= dmm.isVisible(guide);
        if (lastInBatch)
        {
            // Save the state and reset.
            final boolean batchHasVisible = addedGuidesBatchHasVisible;
            addedGuidesBatchHasVisible = false;

            if (testing || UifUtilities.isEDT())
            {
                onGuideAdded(batchHasVisible);
            } else SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onGuideAdded(batchHasVisible);
                }
            });
        }
    }

    /**
     * Invoked when the guide has been removed from the set.
     *
     * @param set   guides set.
     * @param guide removed guide.
     * @param index old guide index.
     */
    public void guideRemoved(GuidesSet set, final IGuide guide, int index)
    {
        if (testing || UifUtilities.isEDT())
        {
            onGuideRemoved(guide);
        } else SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                onGuideRemoved(guide);
            }
        });
    }

    /**
     * Invoked when the guide has been moved to a new location in list.
     *
     * @param set      guides set.
     * @param guide    guide which has been removed.
     * @param oldIndex old guide index.
     * @param newIndex new guide index.
     */
    public void guideMoved(GuidesSet set, final IGuide guide,
                           final int oldIndex, final int newIndex)
    {
        if (testing || UifUtilities.isEDT())
        {
            onGuideMoved(guide, oldIndex, newIndex);
        } else SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                onGuideMoved(guide, oldIndex, newIndex);
            }
        });
    }

    /**
     * Listens to domain changes.
     */
    private class DomainListener extends DomainAdapter
    {
        @Override
        public void propertyChanged(final IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (IFeed.PROP_UNREAD_ARTICLES_COUNT.equals(property) ||
                DirectFeed.PROP_DISABLED.equals(property))
            {
                if (UifUtilities.isEDT())
                {
                    reviewAllParentGuides(feed);
                } else SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        reviewAllParentGuides(feed);
                    }
                });
            }
        }

        private void reviewAllParentGuides(IFeed feed)
        {
            IGuide[] pguides = feed.getParentGuides();
            for (IGuide guide : pguides) onGuideContentsUpdated(guide);
        }
    }

    /**
     * Listens to important user preferences properties changes.
     */
    private class UserPreferencesListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (!UserPreferences.FEED_VISIBILITY_PROPERTIES.contains(evt.getPropertyName())) return;

            reloadGuidesSet();
        }
    }

    /**
     * Listens to controller events.
     */
    private class ControllerListener extends ControllerAdapter
    {
        private IGuide selection;

        @Override
        public void guideSelected(IGuide guide)
        {
            if (selection != null)
            {
                onGuideContentsUpdated(guide);
            }

            selection = guide;
        }
    }
}
