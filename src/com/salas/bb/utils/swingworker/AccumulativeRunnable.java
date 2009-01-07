// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: AccumulativeRunnable.java,v 1.1 2007/06/12 16:11:32 spyromus Exp $
//

package com.salas.bb.utils.swingworker;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Accumulative runnable.
 *
 * Note: This is a backport from Java 1.6
 */
public abstract class AccumulativeRunnable<E> implements Runnable
{
    private List<E> arguments;
    private Class componentType;

    /**
     * Creates a runnable.
     */
    public AccumulativeRunnable()
    {
        arguments = null;
        componentType = null;
    }

    /**
     * Calls a runnable with given parameters.
     *
     * @param params parameters.
     */
    protected abstract void run(E params[]);

    /**
     * Invoked when the task is executed.
     */
    public final void run()
    {
        run(flush());
    }

    /**
     * Adds arguments.
     *
     * @param args arguments.
     */
    public final synchronized void add(E ... args)
    {
        if (componentType == null) componentType = args.getClass().getComponentType();

        boolean flag = true;
        if (arguments == null)
        {
            flag = false;
            arguments = new ArrayList<E>();
        }
        
        Collections.addAll(arguments, args);

        if (!flag) submit();
    }

    /**
     * Submits the task.
     */
    protected void submit()
    {
        SwingUtilities.invokeLater(this);
    }

    /**
     * Flushes arguments in the array.
     *
     * @return array.
     */
    private synchronized E[] flush()
    {
        List<E> list = arguments;
        arguments = null;

        if(componentType == null) componentType = Object.class;
        E aobj[] = (E[])Array.newInstance(componentType, list.size());
        list.toArray(aobj);
        return aobj;
    }
}
