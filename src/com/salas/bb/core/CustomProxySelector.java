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
// $Id: CustomProxySelector.java,v 1.4 2007/04/10 14:04:04 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom proxy selector.
 */
public class CustomProxySelector extends ProxySelector
{
    public static final CustomProxySelector INSTANCE = new CustomProxySelector();

    /** <code>TRUE</code> to use proxy. */
    private boolean proxyEnabled;

    /** Proxy host and port values. */
    private String  proxyHost = "";
    private int     proxyPort;

    /** The list of hosts not to use proxies for. */
    private String  proxyExclusions = "";
    private List<String> exclusions = new ArrayList<String>();

    /** The list of proxies. */
    private List<Proxy> proxies = new ArrayList<Proxy>();
    private List<Proxy> noProxyList = new ArrayList<Proxy>();

    /**
     * Creates the selector.
     */
    private CustomProxySelector()
    {
        noProxyList.add(Proxy.NO_PROXY);
    }

    // ----------------------------------------------------------------------------------
    // Logic methods
    // ----------------------------------------------------------------------------------

    /** Updates the list of proxies to use for non-excluded domain. */
    private void updateProxyList()
    {
        proxies.clear();

        // If proxy is enabled, create it and add to the list
        if (proxyEnabled && StringUtils.isNotEmpty(proxyHost))
        {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            proxies.add(proxy);
        }

        // Add default proxy as the default resort
        proxies.add(Proxy.NO_PROXY);
    }

    /** Updates the list of proxy exclusions. */
    private void updateProxyExclusions()
    {
        exclusions.clear();

        if (StringUtils.isNotEmpty(proxyExclusions))
        {
            String[] domains = StringUtils.split(proxyExclusions, ",");
            for (String domain : domains) exclusions.add(domain.trim().toLowerCase());
        }
    }

    // ----------------------------------------------------------------------------------
    // Bean properties
    // ----------------------------------------------------------------------------------

    /**
     * Returns <code>TRUE</code> if proxy is enabled.
     *
     * @return <code>TRUE</code> if proxy is enabled.
     */
    public boolean isProxyEnabled()
    {
        return proxyEnabled;
    }

    /**
     * Set <code>TRUE</code> if proxy is enabled.
     *
     * @param enabled <code>TRUE</code> if proxy is enabled.
     */
    public void setProxyEnabled(boolean enabled)
    {
        proxyEnabled = enabled;
        updateProxyList();
    }

    /**
     * Returns proxy host name.
     * 
     * @return proxy host name.
     */
    public String getProxyHost()
    {
        return proxyHost;
    }

    /**
     * Sets new proxy host name.
     *
     * @param host host name.
     */
    public void setProxyHost(String host)
    {
        this.proxyHost = host;
        updateProxyList();
    }

    /**
     * Returns proxy port number.
     *
     * @return proxy port.
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * Sets new proxy port.
     *
     * @param port new port.
     */
    public void setProxyPort(int port)
    {
        this.proxyPort = port;
        updateProxyList();
    }

    /**
     * Returns the list of proxy exclusions.
     *
     * @return proxy exclusions.
     */
    public String getProxyExclusions()
    {
        return proxyExclusions;
    }

    /**
     * Sets new list of proxy exclusions.
     *
     * @param exclusions proxy exclusions.
     */
    public void setProxyExclusions(String exclusions)
    {
        this.proxyExclusions = exclusions;
        updateProxyExclusions();
    }

    // ----------------------------------------------------------------------------------
    // Proxy Selector implementation
    // ----------------------------------------------------------------------------------

    /**
     * Selects all the applicable proxies based on the protocol to
     * access the resource with and a destination address to access
     * the resource at.
     *
     * @param uri URI that a connection is required to.
     *
     * @return a list of proxies. Each element in the the List is of type
     *          {@link java.net.Proxy Proxy}; when no proxy is available,
     *          the list will contain one element of type {@link java.net.Proxy Proxy}
     *          that represents a direct connection.
     *
     * @throws IllegalArgumentException if either argument is null.
     */
    public List<Proxy> select(URI uri)
    {
        return proxyEnabled && !isExcluded(uri.getHost()) ? proxies : noProxyList;
    }

    /**
     * Check if the host is excluded.
     *
     * @param host host name.
     *
     * @return <code>TRUE</code> if it is excluded.
     */
    private boolean isExcluded(String host)
    {
        if (host == null) return true;
        
        host = host.trim().toLowerCase();

        for (String exc : exclusions)
        {
            if (host.equals(exc) || host.endsWith(exc)) return true;
        }

        return false;
    }

    /**
     * Called to indicate that a connection could not be established
     * to a proxy/socks server. An implementation of this method can
     * temporarily remove the proxies or reorder the sequence of
     * proxies returned by select(String, String), using the address
     * and they kind of IOException given.
     *
     * @param uri   The URI that the proxy at sa failed to serve.
     * @param sa    The socket address of the proxy/SOCKS server.
     * @param ioe   The I/O exception thrown when the connect failed.
     *
     * @throws IllegalArgumentException if either argument is null
     */
    public synchronized void connectFailed(URI uri, SocketAddress sa, IOException ioe)
    {
        if (proxyEnabled)
        {
            int res = JOptionPane.showConfirmDialog(GlobalController.SINGLETON.getMainFrame(),
                Strings.message("proxy.inaccessible"),
                "BlogBridge",
                JOptionPane.YES_NO_OPTION);

            if (res == JOptionPane.YES_OPTION) setProxyEnabled(false);
        }
    }
}
