package com.salas.bb.imageblocker;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.views.mainframe.MainFrame;

import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Image blocker configuration screen.
 */
public class ImageBlockerConfigAction extends ThreadedAction
{
    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        List<String> oldExpr = ImageBlocker.getExpressions();

        MainFrame frame = GlobalController.SINGLETON.getMainFrame();
        ImageBlockerDialog dialog = new ImageBlockerDialog(frame, null);
        dialog.open();

        if (!oldExpr.equals(ImageBlocker.getExpressions()))
        {
            GlobalModel.touchPreferences();
        }
    }
}
