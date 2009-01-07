package com.salas.bb.networking.manager;

import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.treetable.JTreeTable;
import com.salas.bb.utils.uif.treetable.TreeTableModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;

/**
 * Networking manager window.
 */
public final class ActivityWindow extends JDialog
{
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;

    // Number of points the font should be smaller for whole content of the window
    private static final int FONT_SIZE_DECREMENT = 1;

    private static ActivityWindow instance;

    /**
     * Returns instance of window. If not created yet then it will be created using parent frame
     * specified.
     *
     * @param parent parent frame.
     *
     * @return instance of window.
     */
    public synchronized static ActivityWindow getInstance(JFrame parent)
    {
        if (instance == null) instance = new ActivityWindow(parent);
        return instance;
    }

    /**
     * Creates a window and connects it to the frame.
     *
     * @param parent parent frame.
     */
    private ActivityWindow(JFrame parent)
    {
        super(parent, Strings.message("activity.title"), false);
        initGUI();
    }

    // Initializes GUI
    private void initGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(createMainArea(), BorderLayout.CENTER);

        // center
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int)(screenSize.getWidth() - DEFAULT_WIDTH) / 2;
        int y = (int)(screenSize.getHeight() - DEFAULT_HEIGHT) / 2;
        setBounds(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // Creates main area
    private JComponent createMainArea()
    {
        JScrollPane sp = createTasksTreeTable();

        BBFormBuilder builder = new BBFormBuilder("2dlu, pref:grow, 7dlu");

        builder.appendRow("min:grow");
        builder.append(sp, 3, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    // Creates tree table in main area
    private JScrollPane createTasksTreeTable()
    {
        NetTasksModel model = new NetTasksModel(NetManager.getRootTasksGroup());
        JTreeTable treeTable = new NetTasksTreeTable(model);

        JScrollPane sp = new JScrollPane(treeTable);
        sp.getViewport().setBackground(treeTable.getBackground());

        return sp;
    }

    /**
     * Makes an operation over the given task. The operation will be performed depends on
     * current status of the task.
     *
     * @param aTask task.
     */
    private void doTaskOperation(NetTask aTask)
    {
        if (aTask == null) return;

        int status = aTask.getStatus();
        switch (status)
        {
            case NetTask.STATUS_ABORTED:
            case NetTask.STATUS_COMPLETED:
            case NetTask.STATUS_ERRORED:
                // Do nothing
                break;
            case NetTask.STATUS_PAUSED:
                aTask.resume();
                break;
            case NetTask.STATUS_CONNECTING:
            case NetTask.STATUS_RUNNING:
                aTask.pause();
                break;
            default:
                break;
        }
    }

    /**
     * Custom component for net tasks displaying. It is capable of telling the correct tool-tip
     * text for tasks and groups.
     */
    private class NetTasksTreeTable extends JTreeTable
    {
        public NetTasksTreeTable(TreeTableModel model)
        {
            super(model);

            setSelectionBackground(getBackground());

            // Fonts setup
            Font font = getFont();
            font = font.deriveFont((float)font.getSize() - FONT_SIZE_DECREMENT);
            Font fontGroup = font.deriveFont(Font.BOLD);

            // Remove icons from leaves
            DefaultTreeCellRenderer renderer = new CustomTreeCellRenderer(fontGroup, font);
            renderer.setLeafIcon(null);
            renderer.setClosedIcon(null);
            renderer.setOpenIcon(null);
            setTreeCellRenderer(renderer);

            setRootVisible(false);
            setShowsRootHandles(true);

            // Setup rendering of cells
            ProgressCellRenderer progressRenderer = new ProgressCellRenderer();
            setDefaultRenderer(Integer.class, new CommandCellRenderer());
            setDefaultRenderer(Double.class, progressRenderer);

            addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    Point point = e.getPoint();
                    int col = columnAtPoint(point);
                    if (col == NetTasksModel.COL_COMMANDS)
                    {
                        int row = rowAtPoint(point);
                        NetTask task = (NetTask)getValueAt(row, -1);
                        doTaskOperation(task);
                    }
                }
            });

            // Table setup
            setColumnWidth(NetTasksModel.COL_COMMANDS, 20, 20);
            setColumnWidth(NetTasksModel.COL_TASK_NAME, 150, -1);
            setColumnWidth(NetTasksModel.COL_PROGRESS, 75, -1);

            setFont(font);
            progressRenderer.setFont(font);

            // Set horizontal division lines
            JTree treeRenderer = getTreeRenderer();
            treeRenderer.putClientProperty("JTree.lineStyle", "None");
        }

        // Sets the widths of selected column
        private void setColumnWidth(int columnIndex, int minWidth, int maxWidth)
        {
            TableColumn column = getColumnModel().getColumn(columnIndex);
            if (minWidth != -1) column.setMinWidth(minWidth);
            if (maxWidth != -1) column.setMaxWidth(maxWidth);
        }

        /**
         * Returns tip text for a given mouse event.
         *
         * @param event event.
         *
         * @return text.
         */
        public String getToolTipText(MouseEvent event)
        {
            String text = null;

            int row = rowAtPoint(event.getPoint());
            if (row != -1)
            {
                NetTask task = (NetTask)getValueAt(row, -1);
                text = getToolTip(task);
            }

            return text;
        }

        /**
         * Returns tool-tip text for a task.
         *
         * @param aTask task.
         *
         * @return text.
         */
        private String getToolTip(NetTask aTask)
        {
            String text;

            if (aTask == null) return null;

            if (aTask instanceof NetTaskGroup)
            {
                text = aTask.getTitle();
            } else
            {
                String feed = aTask.getFeed();
                if (feed == null) feed = Strings.message("activity.feed.unknown");
                String size = StringUtils.sizeToString(aTask.getSize());
                String sourceURL = aTask.getSourceURL().toString();
                String startTime = Constants.DATE_TIME_FORMAT.format(aTask.getStartTime());

                text = MessageFormat.format(Strings.message("activity.status"),
                    new Object[] { StringUtils.escape(feed), StringUtils.escape(sourceURL), size, startTime });
            }

            return text;
        }

        /**
         * Custom renderer with separate fonts for group and task.
         */
        private class CustomTreeCellRenderer extends DefaultTreeCellRenderer
        {
            private Font groupFont;
            private Font taskFont;

            /**
             * Creates renderer with specific fonts for group and task.
             *
             * @param aGroupFont    group font.
             * @param aTaskFont     task font.
             */
            public CustomTreeCellRenderer(Font aGroupFont, Font aTaskFont)
            {
                groupFont = aGroupFont;
                taskFont = aTaskFont;
            }

            /**
             * Configures the renderer based on the passed in components. The value is set
             * from messaging the tree with <code>convertValueToText</code>, which ultimately
             * invokes <code>toString</code> on <code>value</code>. The foreground color is
             * set based on the selection and the icon is set based on on leaf and expanded.
             */
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                          boolean expanded, boolean leaf, int row, boolean hasFocus)
            {
                Component comp = super.getTreeCellRendererComponent(tree,
                    value, sel, expanded, leaf, row, hasFocus);

                Font fnt = (value instanceof NetTaskGroup) ? groupFont : taskFont;
                comp.setFont(fnt);

                return comp;
            }
        }
    }
}