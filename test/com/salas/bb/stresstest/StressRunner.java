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
// $Id: StressRunner.java,v 1.5 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.stresstest;

import com.salas.bb.stresstest.scripts.IStressScript;
import com.salas.bb.utils.DateUtils;

/**
 * Runner of stress test.
 */
public class StressRunner extends Thread
{
    private int             maxLoops;
    private IStressScript   script;

    private boolean         terminating;
    private int             loop;

    /**
     * Creates runner.
     *
     * @param aMaxLoops     maximum number of loops to do.
     * @param aScript       script to run.
     */
    public StressRunner(int aMaxLoops, IStressScript aScript)
    {
        super("Stress Runner");

        maxLoops = aMaxLoops;
        script = aScript;
    }

    /**
     * Main loop.
     */
    public void run()
    {
        long testStart = System.currentTimeMillis();

        script.init();

        loop = 0;
        int loopReported = 0;
        while (!isTerminating() && loop < maxLoops)
        {
            try
            {
                script.run(loop);
                loop++;
                int loopToReport = loop / 50;
                if (loopToReport > loopReported)
                {
                    System.out.println("Loop: " + loop);
                    loopReported = loopToReport;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                setTerminating(true);
            }
        }

        long testEnd = System.currentTimeMillis();
        int loops = loop;
        boolean passed = maxLoops == loops;

        // Output stats and terminate
        System.out.println("\n\n----------------------------");
        System.out.println("Test time  : " + DateUtils.millisToString(testEnd - testStart));
        System.out.println("Loops made : " + loops);
        System.out.println("Result     : " + (passed ? "Passed" : "Failed"));

        System.exit(1);
    }

    /**
     * Returns TRUE if the script is terminating.
     *
     * @return TRUE if the script is terminating.
     */
    synchronized boolean isTerminating()
    {
        return terminating;
    }

    /**
     * Sets the value of terminating flag.
     *
     * @param value TRUE to terminate testing after the current loop end.
     */
    public synchronized void setTerminating(boolean value)
    {
        terminating = value;
    }

    /**
     * Returns the loop number.
     *
     * @return loop number.
     */
    public int getLoop()
    {
        return loop;
    }
}
