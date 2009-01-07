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
// $Id: IProgressListener.java,v 1.4 2006/01/25 08:15:19 spyromus Exp $
//

package com.salas.bb.service.sync;

/**
 * Listener of progress.
 */
interface IProgressListener
{
    /**
     * Invoked when step of overall process started.
     *
     * @param stepName step name.
     */
    void processStep(String stepName);

    /**
     * Invoked when started step is completed.
     */
    void processStepCompleted();

    /**
     * Invoked when process started.
     *
     * @param information   information to display.
     * @param steps         steps in the process.
     */
    void processStarted(String information, int steps);

    /**
     * Invoked when process finished.
     *
     * @param information   information to display.
     */
    void processFinished(String information);
}
