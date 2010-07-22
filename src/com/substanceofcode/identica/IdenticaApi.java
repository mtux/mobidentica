/*
 * IdenticaApi.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.substanceofcode.identica;

import com.substanceofcode.identica.model.Status;
import com.substanceofcode.utils.HttpUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.URLUTF8Encoder;
import java.io.IOException;
import java.util.Vector;

/**
 * Identi.ca API
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class IdenticaApi {

    private String username;
    private String password;
    private String serviceUrl;
    private static final String PUBLIC_TIMELINE_URL = "/api/statuses/public_timeline.xml";
    private static final String FRIENDS_TIMELINE_URL = "/api/statuses/home_timeline.xml";
    private static final String USER_TIMELINE_URL = "/api/statuses/user_timeline.xml";
    private static final String RESPONSES_TIMELINE_URL = "/api/statuses/replies.xml";
    private static final String STATUS_UPDATE_URL = "/api/statuses/update.xml";
    private static final String DIRECT_TIMELINE_URL = "/api/direct_messages.xml";
    private static final String FRIENDS_URL = "/api/statuses/friends.xml";

    /** Creates a new instance of identi.ca API */
    public IdenticaApi() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Request direct messages from identi.ca API
     * @return Vector containing direct messages.
     */
    public Vector requestDirectTimeline() {
        return requestTimeline( DIRECT_TIMELINE_URL );
    }


    /**
     * Request public timeline from identi.ca API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestFriendsTimeline() {
        return requestTimeline( FRIENDS_TIMELINE_URL );
    }    
    
    /**
     * Request public timeline from identi.ca API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestUserTimeline() {
        return requestTimeline(USER_TIMELINE_URL);
    }

    /**
     * Request public timeline from identi.ca API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestPublicTimeline() {
        return requestTimeline(PUBLIC_TIMELINE_URL);
    }

    /**
     * Request responses timeline from identi.ca API.{
     * @return Vector containing StatusEntry items.
     */
    public Vector requestResponsesTimeline() {
        return requestTimeline(RESPONSES_TIMELINE_URL);
    }
    
    public Status updateStatus(String status, String inReplyTo) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = serviceUrl + STATUS_UPDATE_URL +
                    "?status=" + URLUTF8Encoder.encode(status);
            if( !inReplyTo.equals("") )
                url += "&in_reply_to_status_id=" + inReplyTo;
            url += "&source=Mobidentica";
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false && status.startsWith("d ")==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            Log.error("Error while updating status: " + ex.getMessage());
        }
        return null;
    }

    public Status setAsFavorite(String statusId) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = serviceUrl + "/api/favorites/create/" + statusId + ".xml";
            HttpUtil.doPost(url, parser);
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            Log.error("Error while setting status as favorite: " + ex.getMessage());
        }
        return null;
    }

    public Status repeat(String statusId) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = serviceUrl + "/api/statuses/retweet/" + statusId + ".xml";
            HttpUtil.doPost(url, parser);
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            Log.error("Error while repeating status: " + ex.getMessage());
        }
        return null;
    }

    public Status removeFromServer(String statusId) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = serviceUrl + "/api/statuses/destroy/" + statusId + ".xml";
            //TODO: detect direct messages and destroy them through this: "/api/direct_messages/destroy/"
            HttpUtil.doPost(url, parser);
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            Log.error("Error while removeing status: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Request friends from identi.ca API.
     * @return Vector containing friends.
     */
    public Vector requestFriends() throws IOException, Exception{
        Vector entries = new Vector();
        try {
            HttpUtil.setBasicAuthentication(username, password);
            UsersParser parser = new UsersParser();
            HttpUtil.doGet(serviceUrl + FRIENDS_URL, parser);
            entries = parser.getUsers();
        } catch (IOException ex) {
            throw new IOException("Error in IdenticaApi.requestFriends: "
                    + ex.getMessage());
        } catch (Exception ex) {
            throw new Exception("Error in IdenticaApi.requestFriends: "
                    + ex.getMessage());
        }
        return entries;
    }

    String getUrl() {
        return serviceUrl;
    }

    void setUrl(String url) {
        serviceUrl = url;
        if(serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl.substring(0, serviceUrl.length()-1);
        }
    }
    
    private Vector requestTimeline(String timelineUrl) {
        Vector entries = new Vector();
        if(serviceUrl==null || serviceUrl.length()==0) {
            IdenticaController controller = IdenticaController.getInstance();
            controller.showError("Service URL is not defined. "
                    + "Please define it in login form.");
            return null;
        }
        
        try {
            HttpUtil.setBasicAuthentication(username, password);
            StatusFeedParser parser = new StatusFeedParser();
            HttpUtil.doGet(serviceUrl + timelineUrl, parser);
            entries = parser.getStatuses();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;
    }
    
    
    
}
