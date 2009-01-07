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
// $Id: ServerService.java,v 1.52 2007/07/04 09:38:30 spyromus Exp $
//

package com.salas.bb.service;

import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.CommunityFields;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import org.apache.xmlrpc.XmlRpcClientSimple;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BlogBridge server service. Communicates with server and exchanges information
 * with it.
 */
public final class ServerService
{
    private static final Logger LOG = Logger.getLogger(ServerService.class.getName());

    private static final String MSG_COM_PROBLEM = "Communication problem.";
    private static final String MSG_PROCESSING_ERROR = "Processing error.";
    private static final String MSG_UNABLE_TO_STORE_DATA = "Unable to store data on the server.";

    // getSessionId error codes
    private static final int ERROR_ACCOUNT_NOT_ACTIVATED    = -1;
    private static final int ERROR_PASSWORD_INCORRECT       = -2;
    private static final int ERROR_ACCOUNT_NOT_REGISTERED   = -3;

    private static final URL SERVICE_URL;

    private static ServerService instance;
    private static final int OPML_VERSION = 1;

    private final static Object opmlURLsLock = new Object();
    private static Map opmlURLs = null;


    static
    {
        String serviceURL = System.getProperty(ResourceID.URL_SERVICE);
        if (serviceURL == null) serviceURL = ResourceUtils.getString(ResourceID.URL_SERVICE);

        LOG.config("Server service URL: " + serviceURL);

        URL url = null;
        try
        {
            url = new URL(serviceURL);
        } catch (MalformedURLException e)
        {
            LOG.severe(MessageFormat.format("Failed to initialize service with bad URL: {0}",
                serviceURL));
        } finally
        {
            SERVICE_URL = url;
        }
    }

    /**
     * Private singleton constructor.
     */
    private ServerService()
    {
    }

    /**
     * Returns instances of service.
     *
     * @return instance.
     */
    public static synchronized ServerService getInstance()
    {
        if (instance == null) instance = new ServerService();

        return instance;
    }

    /**
     * Returns server XML-RPC client.
     *
     * @return client or null in case of error.
     */
    public static XmlRpcHandler getClient()
    {
        return new XmlRpcClientSimple(SERVICE_URL);
    }

    /**
     * Registers user on the server.
     *
     * @param fullName        full user name.
     * @param email           user's email.
     * @param password        user's password.
     * @param locale          user's locale.
     * @param notifyOnUpdates if user allowed to use his email for updates notifications.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public void registerUser(String fullName, String email, String password, String locale,
                             boolean notifyOnUpdates)
            throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(5);
        params.add(fullName);
        params.add(email);
        params.add(password);
        params.add(locale);
        params.add(notifyOnUpdates);

        String errMessage;
        try
        {
            errMessage = (String)cl.execute("accounts.registerAccount", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError(Strings.error("service.unable.to.process.registration.on.server"), e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        // If not-empty error message, then throw error.
        if (errMessage != null && errMessage.length() > 0)
        {
            throw new ServerServiceException(errMessage);
        }
    }

    /**
     * Simple ping showing whether the service is there or no.
     *
     * @return <code>TRUE</code> if service is accessible.
     */
    public static boolean ping()
    {
        boolean online = false;

        XmlRpcHandler cl = getClient();
        Vector params = new Vector(0);

        try
        {
            String response = (String)cl.execute("ping.ping", params);
            online = "pong".equals(response);
        } catch (XmlRpcException e)
        {
            // Service is offline
        } catch (Exception e)
        {
            logError(Level.WARNING, "Invalid response.", e);
        }

        return online;
    }

    /**
     * Sends ping with information to the server.
     *
     * @param installationId ID of application instance.
     * @param appVersion     version of application.
     * @param runs           number of runs.
     * @param os             OS name and version.
     * @param javaVersion    JRE version.
     * @param email          user's account email (nullable).
     * @param password       user's account password (nullable).
     *
     * @throws ServerServiceException in case of communication problem.
     */
    public void ping(long installationId, String appVersion, int runs,
                     String os, String javaVersion, String email, String password)
        throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(7);
        params.add(Long.toString(installationId));
        params.add(appVersion);
        params.add(runs);
        params.add(os);
        params.add(javaVersion);

        if (!StringUtils.isEmpty(email) && !StringUtils.isEmpty(password))
        {
            try
            {
                params.add(email);
                params.add(StringUtils.digestMD5(email, password));
            } catch (NoSuchAlgorithmException e)
            {
                LOG.log(Level.SEVERE, "There's no necessary digesting algorithm implemetned.", e);
            }
        }

        try
        {
            cl.execute("ping.ping1", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Can't ping the server.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }
    }

    /**
     * Stores data on server.
     *
     * @param email    email of account.
     * @param password password of account.
     * @param opml     opml with all necessary information.
     *
     * @return returns the ID of the user by the session ID.
     * 
     * @throws ServerServiceException in case of any errors.
     */
    public static int syncStore(String email, String password, String opml)
            throws ServerServiceException
    {
        int userId;
        byte[] opmlBytes = StringUtils.toUTF8(opml);

        Integer sessionId = getSessionId(email, password);

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(sessionId);
        params.add(opmlBytes);

        String errMessage;
        try
        {
            errMessage = (String)cl.execute("sync.storeInUtf8", params);
            userId = getUserId(sessionId);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError(MSG_UNABLE_TO_STORE_DATA, e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        if (errMessage != null && errMessage.length() > 0)
        {
            throw new ServerServiceException(errMessage);
        }

        return userId;
    }

    /**
     * Gets current user ID using the session ID and stores it in the cache.
     *
     * @param sessionId session ID.
     *
     * @return id.
     */
    private static int getUserId(Integer sessionId)
    {
        // Small trick here. We would need to ask the service if only we didn't know
        // that sessionId is our user id. :)
        return sessionId == null ? -1 : sessionId;
    }

    /**
     * Stores preferences on the server.
     *
     * @param email     email of account.
     * @param password  password of account.
     * @param prefs     preferences to store.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public static void syncStorePrefs(String email, String password, Map prefs)
        throws ServerServiceException
    {
        Integer sessionId = getSessionId(email, password);

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(sessionId);
        params.add(prefs);

        try
        {
            cl.execute("sync.storePrefs", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError(MSG_UNABLE_TO_STORE_DATA, e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }
    }

    /**
     * Restores information stored on the server.
     *
     * @param email    user's account email.
     * @param password user's account password.
     *
     * @return OPML with set of guides for replacement.
     *
     * @throws ServerServiceException in case of errors with communication or account.
     */
    public static String syncRestore(String email, String password)
            throws ServerServiceException
    {
        Integer sessionId = getSessionId(email, password);

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(sessionId);
        params.add(OPML_VERSION);

        byte[] opmlBytes;
        try
        {
            opmlBytes = (byte[])cl.execute("sync.restoreInUtf8", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Unable to restore data from the server.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        // Convert result to string
        return StringUtils.fromUTF8(opmlBytes);
    }

    /**
     * Restores preferences previously stored on the server.
     *
     * @param email    user's account email.
     * @param password user's account password.
     *
     * @return map of stored preferences.
     *
     * @throws ServerServiceException in case of errors with communication or account.
     */
    public static Map syncRestorePrefs(String email, String password)
        throws ServerServiceException
    {
        Integer sessionId = getSessionId(email, password);

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(1);
        params.add(sessionId);

        Map prefs;
        try
        {
            prefs = (Map)cl.execute("sync.restorePrefs", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Unable to restore preferences from the server.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        return prefs;
    }

    /**
     * Takes session id using account information.
     *
     * @param email    user's account email.
     * @param password user's account password.
     *
     * @return id of account.
     *
     * @throws ServerServiceException in case of errors with communication or account.
     */
    private static Integer getSessionId(String email, String password)
            throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(email);
        params.add(password);

        Integer idInt;
        try
        {
            idInt = (Integer)cl.execute("accounts.getSessionId", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Unable to get session id.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        int sessionId = idInt;

        switch (sessionId)
        {
            case ERROR_ACCOUNT_NOT_ACTIVATED:
                throw new ServerServiceException(Strings.error("service.you.need.active.account"));
            case ERROR_PASSWORD_INCORRECT:
                throw new ServerServiceException(Strings.error("service.account.password.is.incorrect"));
            case ERROR_ACCOUNT_NOT_REGISTERED:
                throw new ServerServiceException(Strings.error("service.account.is.not.registered"));
            default:
        }

        return idInt;
    }

    /**
     * Fetches the list of shared tags for a given URL.
     *
     * @param email     user's email.
     * @param password  user's password.
     * @param aLink     link to get tags for.
     *
     * @return list of tags.
     *
     * @throws ServerServiceException in case of errors with communication or account.
     */
    public static List tagsFetch(String email, String password, URL aLink)
        throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(getSessionId(email, password));
        params.add(StringUtils.toUTF8(aLink.toString()));

        Vector tags;
        try
        {
            tags = (Vector)cl.execute("tags.getTags", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        return tags;
    }

    /**
     * Stores user's tags for a given URL.
     *
     * @param email         user's email.
     * @param password      user's password.
     * @param aLink         link to assign tags to.
     * @param aFeed         <code>TRUE</code> if link corresponds to feed.
     * @param aUserTags     list of tags to assign.
     * @param aDescription  link description.
     * @param aExtended     extended description.
     *
     * @throws ServerServiceException in case of errors with communication or account.
     */
    public static void tagsStore(String email, String password, URL aLink, boolean aFeed,
                                 String[] aUserTags, String aDescription, String aExtended) throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(6);
        params.add(getSessionId(email, password));
        params.add(StringUtils.toUTF8(aLink.toString()));
        params.add(aFeed);
        params.add(StringUtils.toUTF8(StringUtils.arrayToQuotedKeywords(aUserTags)));
        params.add(StringUtils.toUTF8(aDescription));
        params.add(StringUtils.toUTF8(aExtended));

        String error;
        try
        {
            error = (String)cl.execute("tags.tag", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError(MessageFormat.format("Could not save user tags for: {0}", aLink), e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        if (error != null && error.length() > 0)
        {
            throw new ServerServiceException(error);
        }
    }

    /**
     * Sends request to the server to resend password on a given email.
     *
     * @param email email address of registered account.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public void requestPasswordResending(String email)
        throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(1);
        params.add(email);

        String msg;
        try
        {
            msg = (String)cl.execute("accounts.requestPasswordResending", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Could not process password resending request.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        if (msg.length() > 0) throw new ServerServiceException(msg);
    }

    /**
     * Performs discovery of feed by specified <code>ref</code>.
     *
     * @param ref   reference string (URL, word or whatever).
     *
     * @return result of resolution.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public Map discover(String ref)
        throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(1);
        params.add(ref);

        Map result;
        try
        {
            result = (Map)cl.execute("meta.getBlogByUrlInUtf8", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Could not process the discovery request.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        // Convert community fields into client-side format
        Map serviceFields = (Map)result.get("communityFields");
        if (serviceFields != null)
        {
            result.put("communityFields", convertToClientFields(serviceFields));
        }

        return result;
    }

    /**
     * Suggests the Feed URL associated with the reference. If this reference still has no
     * association in service database or associated with BadBlog record then it will be
     * (re)associated to a newly discovered blog (if it will be discovered, of course).
     *
     * @param reference         original reference.
     * @param suggestedFeedUrl  suggested Feed Url.
     */
    public static void metaSuggestFeedUrl(String reference, String suggestedFeedUrl)
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Suggesting Feed URL: Ref=" + reference + ", Feed URL=" + suggestedFeedUrl);
        }

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(reference);
        params.add(suggestedFeedUrl);

        try
        {
            cl.execute("meta.suggestFeedUrl", params);
        } catch (XmlRpcException e)
        {
            // No feedback here or we can get in dead cycle
        } catch (Exception e)
        {
            // No feedback here or we can get in dead cycle
            logError(Level.WARNING, "Could not suggest feed URL.", e);
        }
    }

    /**
     * Loads community fields for the given blog.
     *
     * @param dataUrl   data URL of the blog.
     *
     * @return fields map (Name:Value) where Name is String and Value is String or String[].
     *
     * @throws ServerServiceException in case of any errors.
     */
    public static CommunityFields metaGetCommunityFields(String dataUrl)
        throws ServerServiceException
    {
        if (dataUrl == null) return null;

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(1);
        params.add(dataUrl);

        Map fields;
        try
        {
            fields = (Map)cl.execute("meta.getCommunityFields", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Could not load community fields.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        return new CommunityFields(convertToClientFields(fields));
    }

    /**
     * Saves community fields.
     *
     * @param email     user account email.
     * @param password  user account password.
     * @param dataUrl   feed URL of the blog.
     * @param fields    fields to save (Name:Vector(Value)). Name should be String.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public static void metaSetCommunityFields(String email, String password, String dataUrl,
                                              CommunityFields fields)
        throws ServerServiceException
    {
        if (fields == null || fields.size() == 0) return;

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(3);
        params.add(getSessionId(email, password));
        params.add(dataUrl);
        params.add(convertToServiceFields(fields));

        String message;
        try
        {
            message = (String)cl.execute("meta.setCommunityFields", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Could not save community fields.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        if (message.length() > 0)
        {
            throw new ServerServiceException(message);
        }
    }

    /**
     * Converts the values assigned to keys into valid values understood by client.
     *
     * @param serviceFields   service fields map.
     *
     * @return result.
     */
    static Map convertToClientFields(Map serviceFields)
    {
        if (serviceFields == null) return null;

        Map<String, Object> clientFields = new HashMap<String, Object>(serviceFields.size());

        for (Object o : serviceFields.entrySet())
        {
            Map.Entry entry = (Map.Entry)o;

            String name = (String)entry.getKey();
            Object value = convertFieldToClientValue(entry.getValue());

            if (value != null) clientFields.put(name, value);
        }

        return clientFields;
    }

    /**
     * Converts byte[] to String and Vector(byte[]) to String[].
     *
     * @param serviceValue  service value to convert.
     *
     * @return client value.
     */
    static Object convertFieldToClientValue(Object serviceValue)
    {
        if (serviceValue == null) return null;

        Object clientValue = null;

        if (serviceValue instanceof byte[])
        {
            clientValue = StringUtils.fromUTF8((byte[])serviceValue);
        } else if (serviceValue instanceof Vector)
        {
            Vector vector = (Vector)serviceValue;
            String[] values = new String[vector.size()];

            for (int i = 0; i < vector.size(); i++)
            {
                byte[] value = (byte[])vector.get(i);
                values[i] = StringUtils.fromUTF8(value);
            }

            clientValue = values;
        }

        return clientValue;
    }

    /**
     * Converts the values assigned to keys into valid values understood by service.
     *
     * @param clientFields   source fields map.
     *
     * @return result.
     */
    static Map convertToServiceFields(Map clientFields)
    {
        if (clientFields == null) return null;

        Map<String, Object> serviceFields = new HashMap<String, Object>(clientFields.size());

        for (Object o : clientFields.entrySet())
        {
            Map.Entry entry = (Map.Entry)o;

            String name = entry.getKey().toString();
            Object value = convertFieldToServiceValue(entry.getValue());

            if (value != null) serviceFields.put(name, value);
        }

        return serviceFields;
    }

    /**
     * Converts String to byte[], arrayof(Object) to Vector(byte[]).
     *
     * @param aValue    String, Vector or array.
     *
     * @return value or NULL if conversion is not possible.
     */
    static Object convertFieldToServiceValue(Object aValue)
    {
        if (aValue == null) return null;

        Object value;
        boolean isList = false;

        if (aValue instanceof byte[])
        {
            value = aValue;
        } else if (aValue instanceof Object[] || (isList = aValue instanceof List))
        {
            Object[] array = !isList ? (Object[])aValue : ((List)aValue).toArray();

            Vector<byte[]> vector = new Vector<byte[]>(array.length);
            for (Object o : array) vector.add(StringUtils.toUTF8(o.toString()));

            value = vector;
        } else
        {
            value = StringUtils.toUTF8(aValue.toString());
        }

        return value;
    }

    /**
     * Sends client error report to the service.
     *
     * @param message error message.
     * @param details error details.
     */
    public static void reportClientError(final String message, final String details)
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(3);
        params.add(message);
        params.add(details);
        params.add(Application.getDescription().getVersion());

        try
        {
            cl.execute("reports.clientError", params);
        } catch (Exception e)
        {
            // No feedback here or we can get in dead cycle
        }
    }

    /**
     * Sends feedback to the service.
     *
     * @param message feedback message (UTF-8 is supported).
     *
     * @return TRUE if successfully sent.
     */
    public static boolean reportFeedback(String message)
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(1);
        params.add(StringUtils.toUTF8(message));

        boolean sent = false;
        try
        {
            cl.execute("reports.feedbackMessage", params);
            sent = true;
        } catch (XmlRpcException e)
        {
            // No feedback here or we can get in dead cycle
        } catch (Exception e)
        {
            // No feedback here or we can get in dead cycle
            logError(Level.WARNING, "Could not send feedback message.", e);
        }

        return sent;
    }

    /**
     * Reports exception.
     *
     * @param message       message.
     * @param description   description of exception.
     * @param exception     exception to report.
     */
    public static void reportClientError(String message, String description, Throwable exception)
    {
        StringBuffer details = new StringBuffer();

        if (description != null) details.append(description).append("\n\n");

        if (exception != null)
        {
            dumpThrowable(exception, details);
        } else details.append("No stack dump.");

        reportClientError(message, details.toString());
    }

    /**
     * Recursively dumps throwable.
     *
     * @param exception exception.
     * @param details   details buffer.
     */
    static void dumpThrowable(Throwable exception, StringBuffer details)
    {
        details.append(exception.toString());
        for (int i = 0; i < exception.getStackTrace().length; i++)
        {
            StackTraceElement element = exception.getStackTrace()[i];
            details.append("\n\t").append(element);
        }

        Throwable cause = exception.getCause();
        if (cause != null)
        {
            details.append("\nCaused by:\n");
            dumpThrowable(cause, details);
        }
    }

    /**
     * Asks service for available updates.
     *
     * @param aCurrentVersion current version.
     *
     * @return service XML response or empty packet if no updates available.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public static String checkForUpdates(String aCurrentVersion)
        throws ServerServiceException
    {
        // We use this property to test new release detection
        // When set the service provides the list of all updates since
        // current version as if the latest version were final.
        boolean productionOnly = System.getProperty("service.checkupdates.allversions") == null;

        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(2);
        params.add(aCurrentVersion);
        params.add(productionOnly);

        String response;
        try
        {
            response = (String)cl.execute("updates.checkForUpdates", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Could not check for updates.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        return response;
    }

    /**
     * Questions the service for the list of available forums.
     *
     * @return forums list.
     *
     * @throws ServerServiceException in case of any errors.
     */
    public static Map forumGetForums()
        throws ServerServiceException
    {
        XmlRpcHandler cl = getClient();
        Vector params = new Vector(0);

        Map response;
        try
        {
            response = (Map)cl.execute("forum.getForums", params);
        } catch (XmlRpcException e)
        {
            throw new ServerServiceException(MSG_COM_PROBLEM, e);
        } catch (Exception e)
        {
            logError("Could not fetch forums list.", e);
            throw new ServerServiceException(MSG_PROCESSING_ERROR, e);
        }

        return response;
    }

    /**
     * Posts message to the forum.
     *
     * @param aName     name of the author.
     * @param aEmail    email address of the author.
     * @param aForumId  ID of selected forum.
     * @param aSubject  subject of the message.
     * @param aMessage  message text.
     * 
     * @return <code>TRUE</code> if the message has been delivered.
     */
    public static boolean forumPost(String aName, String aEmail, int aForumId, String aSubject,
                                    String aMessage)
    {
        XmlRpcHandler cl = getClient();
        Vector<Object> params = new Vector<Object>(5);
        params.add(StringUtils.toUTF8(aName));
        params.add(StringUtils.toUTF8(aEmail));
        params.add(aForumId);
        params.add(StringUtils.toUTF8(aSubject));
        params.add(StringUtils.toUTF8(aMessage));

        boolean sent = false;
        try
        {
            cl.execute("forum.post", params);
            sent = true;
        } catch (XmlRpcException e)
        {
            // No feedback here or we can get in dead cycle
        } catch (Exception e)
        {
            // No feedback here or we can get in dead cycle
            logError(Level.WARNING, "Could not send forum message.", e);
        }

        return sent;
    }

    /**
     * Returns starting points OPML URL.
     *
     * @return starting points OPML URL.
     */
    public static URL getStartingPointsURL()
    {
        return getOPMLURLbyKey("opml.starting.points.url");
    }

    /**
     * Returns experts OPML URL.
     *
     * @return experts OPML URL.
     */
    public static URL getExpertsURL()
    {
        return getOPMLURLbyKey("opml.experts.url");
    }

    /**
     * Loads OPML URLs list if necessary from the service.
     *
     * @param urlKey key to get URL.
     *
     * @return URL or NULL if opml URLs aren't loaded or the key is missing.
     */
    private static URL getOPMLURLbyKey(String urlKey)
    {
        URL url = getSystemURLProperty(urlKey);

        if (url == null)
        {
            synchronized (opmlURLsLock)
            {
                loadOPMLURLs();

                // There can be a problem with the service not responding.
                if (opmlURLs != null)
                {
                    String urlS = StringUtils.fromUTF8((byte[])opmlURLs.get(urlKey));
                    try
                    {
                        if (urlS == null) url = null; else url = new URL(urlS);
                    } catch (MalformedURLException e)
                    {
                        url = null;
                    }
                }
            }
        }

        return url;
    }

    /**
     * Checks if there's a system property for the given key.
     *
     * @param key key.
     *
     * @return URL.
     */
    private static URL getSystemURLProperty(String key)
    {
        URL url = null;
        String val = System.getProperty(key);

        if (val != null)
        {
            try
            {
                url = new URL(val);
            } catch (MalformedURLException e)
            {
                LOG.log(Level.WARNING, MessageFormat.format( "Invalid overriden URL for key {0}", key), e);
            }
        }

        return url;
    }

    /**
     * Loads the list of OPML URLs from the service.
     */
    private static void loadOPMLURLs()
    {
        if (opmlURLs == null)
        {
            XmlRpcHandler cl = getClient();
            Vector params = new Vector(0);

            try
            {
                opmlURLs = (Map)cl.execute("meta.getOPMLURLs", params);
            } catch (Exception e)
            {
                logError("Could not retrieve OPML URLs.", e);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Plan and Feature related calls to the service
    // ------------------------------------------------------------------------

    /**
     * Returns the plan hash to compare with the current and decided if it's time
     * to update the features list or no.
     *
     * @param email     user email.
     * @param password  user password.
     *
     * @return the hash.
     */
    public static String getPlanHash(String email, String password)
    {
        XmlRpcHandler cl = getClient();

        Vector<String> params = new Vector<String>(2);
        params.add(email);
        params.add(password);

        String hash = null;
        try
        {
            hash = (String)cl.execute("plans.getHash", params);
        } catch (Exception e)
        {
            // No feedback here or we can get in dead cycle
            LOG.log(Level.WARNING, "Failed to get plan hash.", e);
        }

        return hash;
    }

    public static Map<String, String> getPlanFeatures(String email, String password)
    {
        XmlRpcHandler cl = getClient();

        Vector<String> params = new Vector<String>(2);
        params.add(email);
        params.add(password);

        Map<String, String> features = null;
        try
        {
            features = (Map<String, String>)cl.execute("plans.getFeatures", params);
        } catch (Exception e)
        {
            // No feedback here or we can get in dead cycle
            logError(Level.WARNING, "Failed to get plan features.", e);
        }

        return features;
    }

    private static void logError(String msg, Exception ex)
    {
        logError(Level.SEVERE, msg, ex);
    }

    private static void logError(Level level, String msg, Exception ex)
    {
        level = ex instanceof IOException ? Level.FINE : level;
        LOG.log(level, msg, ex);
    }
}
