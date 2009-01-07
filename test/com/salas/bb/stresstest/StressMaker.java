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
// $Id: StressMaker.java,v 1.4 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.stresstest;

import com.salas.bb.core.ControllerAdapter;
import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.stresstest.scripts.IStressScript;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stress maker listens to initialization finish and starts to perform tests. But before
 * that it registers listener for SEVERE log messages to terminate when first appears.
 */
public class StressMaker extends ControllerAdapter
{
    private ThreadTerminatingHandler    handler;
    private StressRunner                runner;
    private int                         maxRuns;

    /**
     * Creates and subscribes to initialization finish event.
     */
    public StressMaker() throws Exception
    {
        maxRuns = getMaxRuns();
        IStressScript script = getStressScript();

        runner = new StressRunner(maxRuns, script);
        handler = new ThreadTerminatingHandler(runner, Level.SEVERE);
        GlobalController controller = GlobalController.SINGLETON;
        controller.addControllerListener(this);
    }

    private static IStressScript getStressScript() throws Exception
    {
        String scriptClassProp = System.getProperty("stress.script.class");
        if (scriptClassProp == null)
        {
            throw new IllegalArgumentException("Property 'stress.script.class' isn't set.");
        }
        Class clazz = Class.forName(scriptClassProp);
        Object obj = clazz.newInstance();

        if (!(obj instanceof IStressScript))
        {
            throw new IllegalArgumentException("Property 'stress.script.class' points to class, " +
                "non-implementing IStressScript interface.");
        }

        return (IStressScript)obj;
    }

    private static int getMaxRuns()
    {
        int maxRuns = -1;

        String maxRunsProp = System.getProperty("stress.max.runs");
        if (maxRunsProp == null)
        {
            throw new IllegalArgumentException("Property 'stress.max.runs' isn't set.");
        }
        maxRuns = Integer.parseInt(maxRunsProp);

        return maxRuns;
    }

    /**
     * Invoked after application finishes initialization of data.
     */
    public void initializationFinished()
    {
        disableInterface();
        installLogHandler();
        startTest();
    }

    private void disableInterface()
    {
        // TODO implement
    }

    private void installLogHandler()
    {
        Logger logger = Logger.getLogger("");
        logger.addHandler(handler);
    }

    private void startTest()
    {
        runner.start();
    }
}
