package com.salas.bb.networking.manager;

import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.treetable.AbstractTreeTableModel;
import com.salas.bb.utils.uif.treetable.TreeTableModel;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for displaying activity if groups of tasks.
 *
 * This model has a trick which allows users of it to get the <code>NetTask</code> object itself
 * instead of it's fields broken into columns. In order to do that you need to ask a value for
 * a column with index -1.
 */
public class NetTasksModel extends AbstractTreeTableModel
{
    /** Index of Task Name column. */
    public static final int COL_TASK_NAME   = 0;
    /** Index of column with progress indicator. */
    public static final int COL_PROGRESS    = 1;
    /** Index of column with commands. */
    public static final int COL_COMMANDS    = 2;

    private final static String[] COLUMN_NAME =
        { Strings.message("activity.taskstable.name"), Strings.message("activity.taskstable.progress"), "_" };

    private final static Class[] COLUMN_CLASS =
        { TreeTableModel.class, Double.class, Integer.class };

    private ModelChangeListener rootListener;

    /**
     * Creates model.
     */
    public NetTasksModel(NetTaskGroup aRoot)
    {
        super(aRoot);

        rootListener = new ModelChangeListener();
        aRoot.addPropertyChangeListener(rootListener);
        aRoot.addListener(rootListener);
    }

    /**
     * Returns the number of availible columns.
     *
     * @return number of columns.
     */
    public int getColumnCount()
    {
        return COLUMN_NAME.length;
    }

    /**
     * Returns the name for column number <code>column</code>.
     *
     * @param column column index.
     * @return name of column.
     */
    public String getColumnName(int column)
    {
        return COLUMN_NAME[column];
    }

    /**
     * Returns the type for column number <code>column</code>.
     *
     * @return class.
     */
    public Class getColumnClass(int column)
    {
        return COLUMN_CLASS[column];
    }

    /**
     * Returns the value to be displayed for node <code>node</code>,
     * at column number <code>column</code>.
     *
     * @param node   node to get value of.
     * @param column column index.
     * @return value.
     */
    public Object getValueAt(Object node, int column)
    {
        NetTask task = (NetTask)node;
        Object value;

        switch (column)
        {
            case COL_TASK_NAME:
                value = task.getTitle();
                break;
            case COL_PROGRESS:
                float progress = task.getProgress();
                value = new Double(progress);
                break;
            case COL_COMMANDS:
                value = new Integer(task.getStatus());
                break;
            case -1:
                // Small cheat to provide a fast path for gitting node from tree-table.
                value = task;
                break;
            default:
                value = null;
        }

        return value;
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code>
     * in the parent's
     * child array.  <code>parent</code> must be a node previously obtained
     * from this data source. This should not return <code>null</code>
     * if <code>index</code>
     * is a valid index for <code>parent</code> (that is <code>index >= 0 &&
     * index < getChildCount(parent</code>)).
     *
     * @param parent a node in the tree, obtained from this data source
     * @return the child of <code>parent</code> at index <code>index</code>
     */
    public Object getChild(Object parent, int index)
    {
        return (parent instanceof NetTaskGroup)
            ? ((NetTaskGroup)parent).getTask(index)
            : null;
    }

    /**
     * Returns the number of children of <code>parent</code>.
     * Returns 0 if the node
     * is a leaf or if it has no children.  <code>parent</code> must be a node
     * previously obtained from this data source.
     *
     * @param parent a node in the tree, obtained from this data source
     * @return the number of children of the node <code>parent</code>
     */
    public int getChildCount(Object parent)
    {
        return (parent instanceof NetTaskGroup)
            ? ((NetTaskGroup)parent).getTaskCount()
            : 0;
    }

    /**
     * Returns TRUE if nonde is leaf.
     *
     * @param node node to check.
     * @return TRUE if node is leaf.
     */
    public boolean isLeaf(Object node)
    {
        return !(node instanceof NetTaskGroup);
    }

    /**
     * Returns coordinates package for the task. If the task isn't put into tree or it's a
     * top level node (root) then the result will be undefined (NULL).
     *
     * @param group group the task belongs to.
     * @param task  task to get coordinates for.
     *
     * @return coordinates or NULL.
     */
    private Coordinates getTaskCoordinates(NetTaskGroup group, NetTask task)
    {
        Coordinates coords = null;

        if (group != null)
        {
            List pathL = new ArrayList();
            NetTaskGroup parent = group;
            while (parent != null)
            {
                pathL.add(0, parent);
                parent = parent.getParent();
            }

            coords = new Coordinates();
            coords.path = pathL.toArray();
            coords.index = group.indexOf(task);
        }

        return coords;
    }

    /**
     * Coordinates of single task.
     */
    private static class Coordinates
    {
        private Object[]    path;
        private int         index;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adapter between changes in model and tree-table.
     */
    private class ModelChangeListener implements PropertyChangeListener, INetTaskGroupListener
    {
        /**
         * Called when some task property changes.
         *
         * @param evt property change event.
         */
        public void propertyChange(final PropertyChangeEvent evt)
        {
            if (UifUtilities.isEDT())
            {
                propertyChangeEDT(evt);
            } else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        propertyChangeEDT(evt);
                    }
                });
            }
        }

        /**
         * Called when some task property changes.
         *
         * @param evt property change event.
         */
        private void propertyChangeEDT(PropertyChangeEvent evt)
        {
            NetTask task = (NetTask)evt.getSource();
            Coordinates coords = getTaskCoordinates(task.getParent(), task);

            // If coordinates aren't set then it's root
            if (coords != null)
            {
                int[] indicies = new int[] { coords.index };
                Object[] children = new Object[] { task };

                fireTreeNodesChanged(this, coords.path, indicies, children);
            }
        }

        /**
         * Fired when new task is added to the group.
         *
         * @param group group to which the task was added.
         * @param task  added task.
         */
        public void taskAdded(final NetTaskGroup group, final NetTask task)
        {
            if (UifUtilities.isEDT())
            {
                taskAddedEDT(group, task);
            } else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        taskAddedEDT(group, task);
                    }
                });
            }
        }

        /**
         * Fired when new task is added to the group.
         *
         * @param group group to which the task was added.
         * @param task  added task.
         */
        private void taskAddedEDT(NetTaskGroup group, NetTask task)
        {
            Coordinates coords = getTaskCoordinates(group, task);
            if (coords != null && coords.index != -1)
            {
                int[] indicies = new int[] { coords.index };
                Object[] children = new Object[] { task };

                fireTreeNodesInserted(this, coords.path, indicies, children);
            }
        }

        /**
         * Fired when task is removed from the group.
         *
         * @param group group the task was removed from.
         * @param task  removed task.
         * @param index index of removed task.
         */
        public void taskRemoved(final NetTaskGroup group, final NetTask task, final int index)
        {
            if (UifUtilities.isEDT())
            {
                taskRemovedEDT(group, task, index);
            } else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        taskRemovedEDT(group, task, index);
                    }
                });
            }
        }

        /**
         * Fired when task is removed from the group.
         *
         * @param group group the task was removed from.
         * @param task  removed task.
         * @param index index of removed task.
         */
        private void taskRemovedEDT(NetTaskGroup group, NetTask task, int index)
        {
            Coordinates coords = getTaskCoordinates(group, task);
            if (coords != null)
            {
                int[] indicies = new int[] { index };
                Object[] children = new Object[] { task };

                fireTreeNodesRemoved(this, coords.path, indicies, children);
            }
        }
    }
}
