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
// $Id: IPC.java,v 1.6 2007/02/14 09:46:22 spyromus Exp $
//

package com.salas.bb.utils.ipc;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;

import com.salas.bb.utils.StringUtils;

/**
 * File-based IPC implementation.
 */
public class IPC
{
    private final static Logger LOG = Logger.getLogger(IPC.class.getName());

    private final File socketFile;
    private final FileChannel socketChannel;
    private final List listeners;

    private SocketListener socketListener;

    /**
     * Creates IPC channel for a given socket file. All the data in the file
     * is removed.
     *
     * @param socketFile socket file.
     *
     * @throws java.io.IOException in case when it's not possible to create a file.
     */
    public IPC(File socketFile)
        throws IOException
    {
        if (socketFile == null) throw new IllegalArgumentException("Socket file should be specified.");

        this.socketFile = socketFile;
        this.listeners = new CopyOnWriteArrayList();

        configureSocketFile();

        socketChannel = new FileInputStream(socketFile).getChannel();

        configureSocketListener();
    }

    /**
     * Initializes socket file.
     *
     * @throws IOException if failed to create the socket file.
     */
    private void configureSocketFile()
        throws IOException
    {
        socketFile.delete();
        socketFile.createNewFile();
        socketFile.deleteOnExit();
    }

    /**
     * Creates socket listener thread.
     */
    private void configureSocketListener()
    {
        socketListener = new SocketListener();
        socketListener.start();
    }

    /**
     * Closes active socket channel which lets the listener thread terminate.
     */
    public void close()
    {
        try
        {
            if (socketChannel != null && socketChannel.isOpen())
            {
                socketChannel.close();
                socketListener = null;
            }
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Failed to close socket channel.", e);
        }
    }

    /**
     * Sends command using the given socket file.
     *
     * @param socketFile socket file.
     * @param cmd        command.
     * @param args       command arguments.
     *
     * @return <code>TRUE</code> if sent.
     */
    public static boolean sendCommand(File socketFile, String cmd, String[] args)
    {
        boolean sent = false;

        if (socketFile.exists() && !StringUtils.isEmpty(cmd) && args != null)
        {
            String command = cmd + " " + StringUtils.join(args, " ") + "\n";
            try
            {
                RandomAccessFile raf = new RandomAccessFile(socketFile, "rws");
                raf.seek(raf.length());
                raf.getChannel().write(ByteBuffer.wrap(command.getBytes()));
                raf.close();

                sent = true;
            } catch (IOException e)
            {
                LOG.log(Level.WARNING, "Failed to send IPC command.", e);
            }
        }

        return sent;
    }

    /**
     * Adds a listener to the list.
     *
     * @param l listener.
     */
    public void addListener(IIPCListener l)
    {
        if (!listeners.contains(l)) listeners.add(l);
    }

    /**
     * Removes a listener from the list.
     *
     * @param l listener.
     */
    public void removeListener(IIPCListener l)
    {
        listeners.remove(l);
    }

    /**
     * Fires subscribe to URL event.
     *
     * @param url URL.
     */
    public void fireSubscribe(URL url)
    {
        Iterator it = listeners.iterator();
        while (it.hasNext())
        {
            IIPCListener l = (IIPCListener)it.next();
            try
            {
                l.subscribe(url);
            } catch (Throwable e)
            {
                LOG.log(Level.SEVERE, "Unhandled exception", e);
            }
        }
    }

    /**
     * Invoked when new command string arrives.
     *
     * @param cmd command.
     */
    private void onCommand(String cmd)
    {
        // Collapse multi-spaces
        cmd = cmd.replaceAll("\\s+", " ").trim();

        // Break the command into pieces
        String[] chunks = cmd.split(" ");

        if (chunks.length > 0)
        {
            String op = chunks[0];

            if ("subscribe".equalsIgnoreCase(op))
            {
                if (chunks.length == 2)
                {
                    String arg = chunks[1];
                    
                    URL url = argToURL(arg);
                    if (url != null)
                    {
                        fireSubscribe(url);
                    } else
                    {
                        LOG.warning("Invalid URL '" + arg + "' for 'subscribe' command");
                    }
                } else
                {
                    LOG.warning(MessageFormat.format(
                        "Invalid number of arguments for ''subscribe'': {0} expected 1",
                        new Object[] { new Integer(chunks.length - 1) }));
                }
            }
        }
    }

    /**
     * Converts command-line argument into the URL if it's some valid URL or the path
     * to a file.
     *
     * @param arg argument.
     *
     * @return URL.
     */
    public static URL argToURL(String arg)
    {
        URL url = null;

        if (!StringUtils.isEmpty(arg))
        {
            arg = arg.trim();
            try
            {
                if (arg.matches("^(file|https?):.*"))
                {
                    url = new URL(arg);
                } else
                {
                    File f = new File(arg);
                    if (f.exists() && f.isFile()) url = f.toURI().toURL();
                }
            } catch (MalformedURLException e)
            {
                url = null;
            }
        }

        return url;
    }

    /**
     * Listener of socket commands.
     */
    private class SocketListener extends Thread
    {
        /** Time in ms to sleep between socket channel checks. */
        private static final int SLEEP_TIME = 1000;
        /** The size of a buffer for commands. */
        private static final int BUFFER_SIZE = 2048;

        /**
         * Initializes socket listener thread.
         */
        public SocketListener()
        {
            super("IPC Socket Listener");
            setDaemon(true);
        }

        public void run()
        {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.clear();
            int bytes;

            while (socketChannel != null && socketChannel.isOpen())
            {
                try
                {
                    bytes = socketChannel.read(buffer);
//                    socketChannel.truncate(0);

                    if (bytes != -1)
                    {
                        byte[] strb = new byte[bytes];
                        buffer.position(0);
                        buffer.get(strb, 0, bytes);
                        String str = new String(strb);
                        String[] cmds = str.split("\\n");

                        for (int i = 0; i < cmds.length; i++)
                        {
                            onCommand(cmds[i]);
                        }

                        buffer.clear();
                    } else Thread.sleep(SLEEP_TIME);

                } catch (IOException e)
                {
                    LOG.log(Level.SEVERE, "Failed to work with socket file.", e);
                } catch (InterruptedException e)
                {
                    LOG.warning("Interruption.");
                }
            }
        }
    }
}
