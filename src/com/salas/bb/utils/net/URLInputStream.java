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
// $Id: URLInputStream.java,v 1.22 2007/04/13 12:56:43 spyromus Exp $
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.auth.AuthCancelException;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Input stream which is created from URL's. It provides several missing features transparently:
 * <ul>
 *  <li>Bandwidth controlling</li>
 *  <li>Resuming</li>
 *  <li>Telling progress of operation</li>
 *  <li>Ensured connections</li>
 * </ul>
 *
 * <p>This input stream takes URL of resource as construction parameter and provides all of the
 * above mentioned services to the readers. The connection to a given URL is performed lazily,
 * meaning that there's no actual connection established before any of operations over the stream
 * required. However, the bandwidth limit and other settings can be changed at any moment before
 * or after initialization and will take effect instantly.</p>
 *
 * <h1>Bandwidth controlling</h1>
 *
 * <p>This functionality allows users of this class to limit the usage of available bandwidth in
 * relatively precise way (in average +/- 700 bytes/sec). The precision is a price for low overhead.
 * As it was said before the bandwidth limit value can be changed at any moment and will take effect
 * instantly. It's possible to set <i>unlimited</i> usage mode (default) by telling limit of &lt;=0.
 * This implementation utilizes <code>BandwidthInputStream</code> input stream implementation.</p>
 *
 * <h1>Resuming</h1>
 *
 * <p>Resuming support is what we all that needed for so long. If some error occurs during
 * connection or reading phases the engine provide all available info about currently failed attempt
 * to <code>IRetriesPolicy</code> (which is part of <i>Ensured connections</i> module) and receives
 * the value of time period to wait before the next attempt or order to terminate the attempts and
 * report the error.</p>
 *
 * <p>The resuming itself works closely with sources of resources to position of the pointer in the
 * stream after reconnection. For HTTP-protocol URL's it tries to use Range-query parameter of
 * HTTP servers defined in <a href="http://www.faqs.org/ftp/rfc/rfc2616.html">
 * RFC2616 "Hypertext Transfer Protocol - HTTP1/1"</a>. If the direct positioning isn't supported
 * (old HTTP servers, local resources or anything else) then the same effect is established with
 * skipping of first N bytes.</p>
 *
 * <p>The resuming functionality is <u>completely transparent</u> to the class using this enhanced
 * stream. There will be no time-out's reported during the emulated resuming and so on.</p>
 *
 * <h1>Telling progress of operation</h1>
 *
 * <p>This one is very simple, but can be used for making extremely user-friendly interfaces, which
 * are showing where we are with loading data at any given moment. Currently, the following list
 * of events is supported:</p>
 *
 * <ul>
 *  <li><b>Connecting</b> - indicates that the (re)connection to resource has started.</li>
 *  <li><b>Connected</b> - indicates that the connection to resource has been (re)established. Also
 *      the size of the resource is told.</li>
 *  <li><b>Read</b> - tells the number of bytes read/skipped (only delta). Note that bytes skipped
 *      when empulating the resuming aren't counted.</li>
 *  <li><b>Finished</b> - indicates that the resource has been fully fetched.</li>
 * </ul>
 *
 * <p>There's a method to attach only single listener. A tip: use Composite pattern to create a
 * complex listener. Also, note that the listeners may throw any run-time exception and it will
 * not affect the reading procedure in any way. The exceptions will be recorded in log with WARNING
 * level.</p>
 *
 * <h1>Ensured connections</h1>
 *
 * <p>This functionality is completely built on top of Retries Policy concept. You are free to
 * define your own policy which will be analyzing the history of failed attempts to decide whether
 * to make pause between the next attempt and what the length of the pause will be or simply order
 * to terminate any further attempts and abord reading.</p>
 *
 * <h1>Redirections handling</h1>
 *
 * <p>Standard HTTP(S) protocols implementations by SUN support seamless following redirections, but
 * it's isn't possible to learn when we are permanently redirected somewhere. Current implementation
 * addresses this issue by allowing client to register permanent redirection listener which will be
 * notified once the connection to resource is established and it's detected that we were redirected
 * somewhere during connection phase.</p>
 */
public class URLInputStream extends InputStream
{
    private static final Logger LOG = Logger.getLogger(URLInputStream.class.getName());

    private static final IRetriesPolicy DEFAULT_RETRIES_POLICY = new DirectRetriesPolicy();

    private static final String MSG_EXCEPTION_IN_THE_HANDLER = Strings.error("failed.to.handle");
    private static final String MSG_IO_ERROR = Strings.error("net.there.was.an.error.during.io");

    /** Unlimited bandwidth. */
    public static final int BANDWIDTH_UNLIMITED = 0;

    // URL we use to read data
    private URL                 sourceUrl;

    // Stream with controlled bandwidth
    private BandwidthInputStream bis;

    // Number of bytes which could be read per second (-1 means unlimited).
    private int                 bandwidth;

    // Bytes read
    private int                 read;

    // Length of the content
    private int                 contentLength;

    // Set to TRUE once the stream get closed
    private boolean             closed;

    // TRUE when the read bytes count reached the contentLength mark.
    private boolean             finished;

    private IRetriesPolicy      retriesPolicy;

    // Listener of the progress events (can be null)
    private List                listeners;

    // Date of last successful fetching of data from source URL.
    private long                lastFetchingTime;

    // Listener for redirection events.
    private IPermanentRedirectionListener redirectionListener;

    // TRUE to pause stream reading/skipping operation.
    private boolean             paused;

    // The code returned by source of stream data in response to connection attempt
    // It's (-1) if there was not connection attempt yet, valid HTTP response code
    // for HTTP-series connection or 200 for successful connection of other type.
    private int                 responseCode = -1;

    // A time of the last update taken from a response during a connection.
    private long                lastModifiedTime;

    // A time of a connection attempt (server time-zone).
    private long                serverTime;

    // A user agent to use for HTTP connections.
    private String              userAgent;

    // Basic HTTP Authentication info.
    private String              username;
    private String              password;

    /**
     * Creates stream out of URL.
     *
     * @param aSourceUrl        non-NULL URL.
     */
    public URLInputStream(URL aSourceUrl)
    {
        this(aSourceUrl, -1);
    }

    /**
     * Creates stream out of URL.
     *
     * @param aSourceUrl        non-NULL URL.
     * @param aUserAgent        user agent for HTTP connections.
     */
    public URLInputStream(URL aSourceUrl, String aUserAgent)
    {
        this(aSourceUrl, -1);
        userAgent = aUserAgent;
    }

    /**
     * Creates stream out of URL.
     *
     * @param aSourceUrl        non-NULL URL.
     * @param aLastFetchingTime when this URL was fetched for the last time (-1 if never).
     */
    public URLInputStream(URL aSourceUrl, long aLastFetchingTime)
    {
        this(aSourceUrl, 0, aLastFetchingTime);
    }

    /**
     * Creates stream out of URL.
     *
     * @param aSourceUrl        non-NULL URL.
     * @param aResumeFrom       position to resume from.
     * @param aLastFetchingTime when this URL was fetched for the last time (-1 if never).
     */
    public URLInputStream(URL aSourceUrl, int aResumeFrom, long aLastFetchingTime)
    {
        if (aSourceUrl == null) throw new NullPointerException(Strings.error("unspecified.url"));

        retriesPolicy = DEFAULT_RETRIES_POLICY;
        listeners = Collections.synchronizedList(new ArrayList());

        sourceUrl = aSourceUrl;
        lastFetchingTime = aLastFetchingTime;
        bis = null;

        closed = false;

        setBandwidth(BANDWIDTH_UNLIMITED);
        read = aResumeFrom;
        finished = false;
        paused = false;

        lastModifiedTime = -1;
        serverTime = -1;

        userAgent = null;
    }

    /**
     * Sets basic authentication info.
     *
     * @param username user name.
     * @param password password
     */
    public void setBasicAuthenticationInfo(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * Registers new redirection listener.
     *
     * @param listener new listener.
     */
    public void setRedirectionListener(IPermanentRedirectionListener listener)
    {
        redirectionListener = listener;
    }

    /**
     * Sets alternative retries policy.
     *
     * @param aRetriesPolicy policy.
     *
     * @see #BANDWIDTH_UNLIMITED
     */
    public void setRetriesPolicy(IRetriesPolicy aRetriesPolicy)
    {
        retriesPolicy = aRetriesPolicy;
    }

    /**
     * Adds new listener for progress events capturing.
     *
     * @param aListener listener.
     */
    public void addListener(IStreamProgressListener aListener)
    {
        listeners.add(aListener);
    }

    /**
     * Removes listener from list.
     *
     * @param aListener listener.
     */
    public void removeListener(IStreamProgressListener aListener)
    {
        listeners.remove(aListener);
    }

    /**
     * Returns current allowed bandwidth (bytes per second).
     *
     * @return bandwidth (byte/sec) (&lt;=0 means unlimited).
     */
    public int getBandwidth()
    {
        return bandwidth;
    }

    /**
     * Returns URL of the source.
     *
     * @return URL.
     */
    public URL getSourceURL()
    {
        return sourceUrl;
    }

    /**
     * Sets new allowed bandwidth (bytes per second).
     *
     * @param aBandwidth new bandwidth (bytes/sec) (&lt;=0 means unlimited).
     */
    public void setBandwidth(int aBandwidth)
    {
        bandwidth = aBandwidth;
        if (bis != null) bis.setBandwidth(aBandwidth);
    }

    /**
     * Increments number of read bytes.
     *
     * @param delta delta.
     */
    private synchronized void incRead(int delta)
    {
        if (!finished)
        {
            read += delta;
            fireRead(delta);

            if (contentLength != -1 && read >= contentLength) finished();
        }
    }

    /**
     * Sets the finish flag and fires the event.
     */
    private synchronized void finished()
    {
        if (!finished)
        {
            fireFinished();
            finished = true;
        }
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
     *
     * @throws IOException if an I/O error occurs.
     */
    public int read()
        throws IOException
    {
        int ch = -1;

        blockOnPause();

        boolean success = false;
        while (!success)
        {
            connect();
            try
            {
                ch = bis.read();
                success = true;
            } catch (IOException e)
            {
                // We need to reconnect
                LOG.log(Level.WARNING, MSG_IO_ERROR, e);
                bis = null;
            }
        }

        if (ch == -1) finished();

        return ch;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from this input stream without
     * blocking by the next caller of a method for this input stream.
     *
     * @return the number of bytes that can be read from this input stream without blocking.
     *
     * @throws IOException if an I/O error occurs.
     */
    public int available()
        throws IOException
    {
        int num = 0;

        boolean success = false;
        while (!success)
        {
            connect();
            try
            {
                num = bis.available();
                success = true;
            } catch (IOException e)
            {
                // We need to reconnect
                LOG.log(Level.WARNING, MSG_IO_ERROR, e);
                bis = null;
            }
        }

        return num;
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     * <p/>
     * <p> The <code>close</code> method of <code>InputStream</code> does nothing.
     *
     * @throws java.io.IOException if an I/O error occurs.
     */
    public void close()
        throws IOException
    {
        if (bis != null && !closed)
        {
            bis.close();
            bis = null;
            finished();
        }

        closed = true;
    }

    /**
     * Returns TRUE if stream is already closed.
     *
     * @return TRUE if stream is already closed.
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * Marks the current position in this input stream.
     *
     * @param readlimit the maximum limit of bytes that can be read before the mark position becomes
     *                  invalid.
     *
     * @see java.io.InputStream#reset()
     */
    public synchronized void mark(int readlimit)
    {
        try
        {
            connect();
            bis.mark(readlimit);
        } catch (IOException e)
        {
            LOG.log(Level.SEVERE, Strings.error("net.failed.to.establish.connection"), e);
        }
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and <code>reset</code> methods.
     *
     * @return <code>true</code> if this stream instance supports the mark and reset methods;
     *         <code>false</code> otherwise.
     *
     * @see java.io.InputStream#mark(int)
     * @see java.io.InputStream#reset()
     */
    public boolean markSupported()
    {
        boolean supported = false;

        try
        {
            connect();
            supported = bis.markSupported();
        } catch (IOException e)
        {
            LOG.log(Level.SEVERE, Strings.error("net.failed.to.establish.connection"), e);
        }

        return supported;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into an array of bytes.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in array <code>b</code> at which the data is written.
     * @param len the maximum number of bytes to read.
     *
     * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no
     *         more data because the end of the stream has been reached.
     *
     * @throws java.io.IOException  if an I/O error occurs.
     * @throws NullPointerException if <code>b</code> is <code>null</code>.
     * @see java.io.InputStream#read()
     */
    public int read(byte b[], int off, int len)
        throws IOException
    {
        int justRead = 0;

        blockOnPause();

        boolean success = false;
        while (!success)
        {
            connect();
            try
            {
                justRead = bis.read(b, off, len);

                if (justRead == -1) finished();

                success = true;
            } catch (IOException e)
            {
                // We need to reconnect
                LOG.log(Level.WARNING, MSG_IO_ERROR, e);
                bis = null;
            }
        }

        return justRead;
    }

    /**
     * Repositions this stream to the position at the time the <code>mark</code> method was last
     * called on this input stream.
     *
     * @throws java.io.IOException if this stream has not been marked or if the mark has been
     *                             invalidated.
     * @see java.io.InputStream#mark(int)
     * @see java.io.IOException
     */
    public synchronized void reset()
        throws IOException
    {
        boolean success = false;
        while (!success)
        {
            connect();
            try
            {
                bis.reset();
                success = true;
            } catch (IOException e)
            {
                // We need to reconnect
                LOG.log(Level.WARNING, MSG_IO_ERROR, e);
                bis = null;
            }
        }
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this input stream.
     *
     * @param n the number of bytes to be skipped.
     *
     * @return the actual number of bytes skipped.
     *
     * @throws java.io.IOException if an I/O error occurs.
     */
    public long skip(long n)
        throws IOException
    {
        long skipped = 0;

        blockOnPause();

        boolean success = false;
        while (!success)
        {
            connect();
            try
            {
                skipped = bis.skip(n);
                success = true;
            } catch (IOException e)
            {
                // We need to reconnect
                LOG.log(Level.WARNING, MSG_IO_ERROR, e);
                bis = null;
            }
        }

        return skipped;
    }

    /**
     * Checks for connection to exist. If there's no connection present then it will be
     * established.
     *
     * @throws IOException in case of any I/O exception.
     */
    public synchronized void connect()
        throws IOException
    {
        try
        {
            if (bis == null && !closed)
            {
                boolean connected = false;
                int attempt = 0;

                fireConnecting();

                while (!connected)
                {
                    IRetriesPolicy.Failure failure = null;
                    try
                    {
                        failure = connectionAttempt(attempt++);
                    } catch (RuntimeException e)
                    {
                        if (e.getCause() instanceof AuthCancelException)
                        {
                            throw new NotAuthenticatedException();
                        }

                        throw e;
                    }

                    // If there was a failure then ask the retries policy for how long we should
                    // wait and whether we should do it at all.
                    if (failure != null)
                    {
                        handleFailure(failure);
                    } else
                    {
                        connected = true;
                    }
                }

                fireConnected(contentLength);
            } else if (closed)
            {
                throw new IOException(Strings.error("net.stream.is.already.closed"));
            }
        } catch (IOException e)
        {
            fireErrored(e);
            throw e;
        }
    }

    /**
     * Makes a single attempt to connect to the source URL.
     *
     * @param attempt attempt sequence number.
     *
     * @return initialized failure object or NULL if successful.
     */
    IRetriesPolicy.Failure connectionAttempt(int attempt)
    {
        IRetriesPolicy.Failure failure = null;
        long start = System.currentTimeMillis();

        try
        {
            bis = new BandwidthInputStream(makeConnection(read));
            bis.setBandwidth(bandwidth);
        } catch (IOException e)
        {
            failure = new IRetriesPolicy.Failure(attempt, start,
                System.currentTimeMillis(), false, 0, e);
        }

        return failure;
    }

    /**
     * Handles connection failure. Asks the retries policy about the time to way before
     * retry and if policy says that retrying isn't necessary then throws original couse of
     * failue. Otherwise waits for told period.
     *
     * @param aFailure  failure description.
     *
     * @throws IOException in case of any I/O error.
     */
    void handleFailure(IRetriesPolicy.Failure aFailure)
        throws IOException
    {
        IOException cause = aFailure.getCause();
        if (cause instanceof FileNotFoundException ||
            cause instanceof CyclicRedirectionException)
        {
            throw cause;
        } else if (cause instanceof UISException)
        {
            int code = ((UISException)cause).getCode();

            if (code != HttpURLConnection.HTTP_CLIENT_TIMEOUT &&
                code != HttpURLConnection.HTTP_UNAUTHORIZED) throw cause;
        }

        // Ask retries policy what to do after this failed attempt
        long timeToWait = retriesPolicy.getTimeBeforeRetry(aFailure);
        if (timeToWait == -1)
        {
            throw cause;
        } else if (timeToWait > 0)
        {
            try
            {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e)
            {
                // Continue with another attempt right away
            }
        }
    }

    /**
     * Performs connection.
     *
     * @param read  number of bytes already read.
     *
     * @return input stream of resource we are connected to.
     *
     * @throws IOException in case of any I/O exception.
     */
    protected InputStream makeConnection(long read)
        throws IOException
    {
        URLConnectionHolder holder = ResumingSupport.resume(sourceUrl, read, lastFetchingTime, userAgent, username, password);
        URLConnection con = holder.getConnection();

        URL permRedirURL = holder.getPermanentRedirectionURL();
        if (permRedirURL != null)
        {
            sourceUrl = permRedirURL;

            firePermanentRedirection(sourceUrl);
        }

        contentLength = 0;
        responseCode = 200;
        boolean isCompressed = false;

        if (con instanceof HttpURLConnection)
        {
            HttpURLConnection httpCon = (HttpURLConnection)con;
            responseCode = httpCon.getResponseCode();
            isCompressed = "gzip".equalsIgnoreCase(httpCon.getContentEncoding());
        }

        if (responseCode != HttpURLConnection.HTTP_UNAUTHORIZED)
        {
            analyzeResponseCodes(con);
            contentLength = resolveContentLength(con);
            lastModifiedTime = resolveLastModifiedTime(con);
            serverTime = resolveServerTime(con);
        }

        InputStream is = new CountingFilterInputStream(con.getInputStream());
        if (isCompressed) is = new CorrectedGZIPInputStream(is);

        return is;
    }

    /**
     * Analyzes response codes after connecting to resource and converts them into exceptions.
     *
     * @param con connection.
     *
     * @throws IOException  in case if something goes wrong or we throw exceptions.
     */
    private void analyzeResponseCodes(URLConnection con)
        throws IOException
    {
        if (!(con instanceof HttpURLConnection)) return;
        HttpURLConnection hcon = (HttpURLConnection)con;

        int series = responseCode / 100;

        if (series == 5) throw new ServerErrorException(responseCode, hcon.getResponseMessage());
        if (series == 4 && responseCode != 401)
        {
            throw new ClientErrorException(responseCode, hcon.getResponseMessage());
        }
    }

    /**
     * Returns response code.
     *
     * @return response code.
     */
    public int getResponseCode()
    {
        return responseCode;
    }

    /**
     * Resolves content length for a given connection.
     *
     * @param aCon  connection.
     *
     * @return length of content.
     */
    protected int resolveContentLength(URLConnection aCon)
    {
        return aCon.getContentLength();
    }

    /**
     * Resolves a time of the last resource modification (server time-zone).
     *
     * @param aCon  connection.
     *
     * @return time.
     */
    protected long resolveLastModifiedTime(URLConnection aCon)
    {
        long time = aCon.getLastModified();
        return time == 0 ? -1 : time;
    }

    /**
     * Resolves a time of response generation (server time-zone).
     *
     * @param aCon  connection.
     *
     * @return time.
     */
    protected long resolveServerTime(URLConnection aCon)
    {
        long time = aCon.getDate();
        return time == 0 ? -1 : time;
    }

    /**
     * Returns wrapped stream.
     *
     * @return stream.
     */
    BandwidthInputStream getStream()
    {
        return bis;
    }

    /**
     * Returns list of registered listeners.
     *
     * @return listeners.
     */
    protected IStreamProgressListener[] getListeners()
    {
        IStreamProgressListener[] list;

        synchronized (listeners)
        {
            list = (IStreamProgressListener[])listeners.toArray(
                    new IStreamProgressListener[listeners.size()]);
        }

        return list;
    }

    /**
     * Fires event about connecting has started.
     */
    protected void fireConnecting()
    {
        IStreamProgressListener[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++)
        {
            IStreamProgressListener listener = listeners[i];
            try
            {
                listener.connecting(this);
            } catch (Exception e)
            {
                LOG.log(Level.WARNING, MSG_EXCEPTION_IN_THE_HANDLER, e);
            }
        }
    }

    /**
     * Fires event about connection established.
     *
     * @param size size of the stream.
     */
    protected void fireConnected(long size)
    {
        IStreamProgressListener[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++)
        {
            IStreamProgressListener listener = listeners[i];
            try
            {
                listener.connected(this, size);
            } catch (Exception e)
            {
                LOG.log(Level.WARNING, MSG_EXCEPTION_IN_THE_HANDLER, e);
            }
        }
    }

    /**
     * Fires event about another portion of bytes read.
     *
     * @param bytes bytes.
     */
    protected void fireRead(int bytes)
    {
        IStreamProgressListener[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++)
        {
            IStreamProgressListener listener = listeners[i];
            try
            {
                listener.read(this, bytes);
            } catch (Exception e)
            {
                LOG.log(Level.WARNING, MSG_EXCEPTION_IN_THE_HANDLER, e);
            }
        }
    }

    /**
     * Fires event about the end of stream reached.
     */
    protected void fireFinished()
    {
        IStreamProgressListener[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++)
        {
            IStreamProgressListener listener = listeners[i];
            try
            {
                listener.finished(this);
            } catch (Exception e)
            {
                LOG.log(Level.WARNING, MSG_EXCEPTION_IN_THE_HANDLER, e);
            }
        }
    }

    /**
     * Fires event about the error appeared.
     */
    protected void fireErrored(IOException ex)
    {
        IStreamProgressListener[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++)
        {
            IStreamProgressListener listener = listeners[i];
            try
            {
                listener.errored(this, ex);
            } catch (Exception e)
            {
                LOG.log(Level.WARNING, MSG_EXCEPTION_IN_THE_HANDLER, e);
            }
        }
    }

    /**
     * Notifies registered listener about permanent redirection.
     *
     * @param newURL new URL.
     */
    private void firePermanentRedirection(URL newURL)
    {
        try
        {
            if (redirectionListener != null) redirectionListener.redirectedTo(newURL);
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("failed.to.notify.a.listener"), e);
        }
    }

    /**
     * Pauses/unpauses the stream reading/skipping operations.
     *
     * @param aPaused TRUE to pause.
     */
    public synchronized void setPaused(boolean aPaused)
    {
        paused = aPaused;
        notifyAll();
    }

    /**
     * Returns TRUE if currently paused.
     *
     * @return TRUE if currently paused.
     */
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Blocks the execution until unpaused (if currently paused).
     */
    private synchronized void blockOnPause()
    {
        try
        {
            while (paused) wait();
        } catch (InterruptedException e)
        {
            LOG.log(Level.WARNING, Strings.error("interrupted"), e);
        }
    }

    /**
     * Returns a time of the last modification taken from a server response.
     *
     * @return time.
     */
    public long getLastModifiedTime()
    {
        return lastModifiedTime;
    }

    /**
     * Returns a time of the response generation taken from a server response.
     *
     * @return time.
     */
    public long getServerTime()
    {
        return serverTime;
    }

    /**
     * Filter input stream that increments read bytes count as new bytes are read or skipped.
     * We need it because the number of bytes read from GZIP stream and
     * the number of bytes read from the URLInputStream using GZIP'ed stream
     * are two different things.
     */
    private class CountingFilterInputStream extends FilterInputStream
    {
        /**
         * Creates a counting stream.
         *
         * @param in stream to wrap.
         */
        public CountingFilterInputStream(InputStream in)
        {
            super(in);
        }

        @Override
        public int read() throws IOException
        {
            int ch = super.read();
            if (ch != -1) incRead(1);
            return ch;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException
        {
            int read = super.read(b, off, len);
            if (read != -1) incRead(read);
            return read;
        }

        @Override
        public long skip(long n) throws IOException
        {
            long skipped = super.skip(n);
            incRead((int)skipped);
            return skipped;
        }
    }

}
