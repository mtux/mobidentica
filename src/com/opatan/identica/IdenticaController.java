/*
 * TwitterController.java
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

package com.opatan.identica;

import com.opatan.identica.model.Status;
import com.opatan.identica.model.User;
import com.opatan.identica.tasks.RemoveStatusTask;
import com.opatan.identica.tasks.RepeatStatusTask;
import com.opatan.identica.tasks.RequestFriendsTask;
import com.opatan.identica.tasks.RequestTimelineTask;
import com.opatan.identica.tasks.UpdateStatusTask;
import com.opatan.identica.tasks.SetAsFavoriteTask;
import com.opatan.identica.views.AboutCanvas;
import com.opatan.identica.views.SettingsForm;
import com.opatan.identica.views.SplashCanvas;
import com.opatan.identica.views.TimelineCanvas;
import com.opatan.identica.views.UpdateStatusTextBox;
import com.opatan.identica.views.WaitCanvas;
import com.opatan.utils.Log;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

/**
 * TwitterController controls the application flow.
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class IdenticaController {

    IdenticaMidlet midlet;
    Display display;
    IdenticaApi api;
    Settings settings;
    TimelineCanvas timeline;

    Vector publicTimeline;
    Vector recentTimeline;
    Vector archiveTimeline;
    Vector responsesTimeline;
    Vector directTimeline;
    Vector friendsStatuses;

    static IdenticaController instance;

    /**
     * Get controller instance.
     * @return
     */
    public static IdenticaController getInstance() {
        return instance;
    }

    /**
     * Get new instance of controller.
     * @param midlet
     * @return
     */
    public static IdenticaController getInstance(IdenticaMidlet midlet) {
        if(instance==null) {
            instance = new IdenticaController(midlet);
        }
        return instance;
    }

    /** 
     * Creates a new instance of TwitterController
     * @param midlet Application midlet.
     */
    private IdenticaController(IdenticaMidlet midlet) {

        publicTimeline = new Vector();
        recentTimeline = new Vector();
        archiveTimeline = new Vector();
        responsesTimeline = new Vector();
        directTimeline = new Vector();
        friendsStatuses = new Vector();
        try {
            this.midlet = midlet;
            this.display = Display.getDisplay(midlet);
            this.api = new IdenticaApi();
            this.timeline = new TimelineCanvas(this);
            settings = Settings.getInstance(midlet);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }

    public void about() {
        AboutCanvas canvas = new AboutCanvas(this);
        display.setCurrent(canvas);
    }

    public void addStatus(Status status) {
        if(recentTimeline!=null) {
            recentTimeline.insertElementAt(status, 0);
        }
        if(archiveTimeline!=null) {
            archiveTimeline.insertElementAt(status, 0);
        }
    }

    public boolean removeStatus(Status status) {

        if(recentTimeline!=null) {
            return recentTimeline.removeElement(status);
        }
        if(archiveTimeline!=null) {
            return archiveTimeline.removeElement(status);
        }
        return false;
    }

    public void clearTimelines() {
        setRecentTimeline(null);
        setPublicTimeline(null);
        setResponsesTimeline(null);
        setUserTimeline(null);
        setDirectTimeline(null);
        setFriendsStatuses(null);
    }

    public MIDlet getMIDlet() {
        return midlet;
    }
    
    public Settings getSettings() {
        return settings;
    }

    public void exit() {
        try {
            midlet.destroyApp(true);
            midlet.notifyDestroyed();
        } catch(Exception ex) {
            Log.error("Exit: " + ex.getMessage());
        }
    }

    public Displayable getCurrentDisplay() {
        return display.getCurrent();
    }

    /** 
     * Login to twitter.
     * @param username Username for Twitter
     * @param password Password for Twitter
     */
    public void login(String username, String password, String serviceUrl) {
        api.setUsername(username);
        api.setPassword(password);
        api.setUrl(serviceUrl);
        api.setCount(settings.getStringProperty(Settings.NUM_OF_DENTS, "20") );
        showRecentTimeline();
    }

    public void setPublicTimeline(Vector publicTimeline) {
        this.publicTimeline = publicTimeline;
    }

    public void setResponsesTimeline(Vector responsesTimeline) {
        this.responsesTimeline = responsesTimeline;
    }

    public void setServiceUrl(String url) {
        api.setUrl( url );
    }

    public String getServiceUrl() {
        return api.getUrl();
    }

    public void setUserTimeline(Vector archiveTimeline) {
        this.archiveTimeline = archiveTimeline;
    }

    public void setDirectTimeline(Vector directTimeline) {
        this.directTimeline = directTimeline;
    }

    public void setFriendsStatuses(Vector friendStatuses) {
        this.friendsStatuses = friendStatuses;
    }

    public void showError(String string) {
        Alert alert = new Alert("Error");
        alert.setString(string);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert, timeline);
    }

    /** Show friends */
    public void showFriends() {
        if(friendsStatuses==null) {
            updateFriends();
        } else {
            timeline.setTimeline(friendsStatuses);
            display.setCurrent(timeline);
        }
    }

    public void updateFriends() {
        RequestFriendsTask task = new RequestFriendsTask(this, api);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading friends");
        display.setCurrent(wait);
    }

    /** Show friends */
    public void showFriends(Vector friends) {
        String state = "";
        int nullUserCount = 0; // Only for debugging purposes
        try {
            if(friends==null) {
                showError("Friends vector is null");
                return;
            }
            state = "initializing vector";
            friendsStatuses = new Vector();
            state = "creating enumeration";
            Enumeration friendEnum = friends.elements();
            state = "starting the loop friends";
            while(friendEnum.hasMoreElements()) {
                state = "getting user from element";
                User user = (User) friendEnum.nextElement();
                if(user==null) {
                    // why?
                    nullUserCount++;
                }
                state = "getting user's last status";
                if(user.getLastStatus()!=null) {
                    state = "adding last status to vector";
                    friendsStatuses.addElement(user.getLastStatus());
                }
            }
            state = "setting friends timeline";
            timeline.setTimeline(friendsStatuses);
            state = "showing timeline";
            display.setCurrent(timeline);
        } catch(Exception ex) {
            this.showError("Error while " + state + ": " + ex.getMessage()
                    + "\nNull users: " + nullUserCount
                    + "\nFriends: " + friends.capacity());
        }
    }

    public void showPublicTimeline() {
        if(publicTimeline==null) {
            updatePublicTimeline();
        } else {
            timeline.setTimeline(publicTimeline);
            display.setCurrent(timeline);
        }
    }

    public void updatePublicTimeline() {
        RequestTimelineTask task = new RequestTimelineTask(
            this, api, RequestTimelineTask.FEED_PUBLIC);
        WaitCanvas wait = new WaitCanvas(this, task);
        display.setCurrent(wait);
    }

    public void showResponsesTimeline() {
        if(responsesTimeline==null) {
            updateResponsesTimeline();
        } else {
            timeline.setTimeline(responsesTimeline);
            display.setCurrent(timeline);
        }
    }

    public void updateResponsesTimeline() {
        RequestTimelineTask task = new RequestTimelineTask(
            this, api, RequestTimelineTask.FEED_RESPONSES);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading responses...");
        display.setCurrent(wait);
    }

    public void showDirectMessages() {
        if(directTimeline==null) {
            updateDirectMessages();
        } else {
            timeline.setTimeline(directTimeline);
            display.setCurrent(timeline);
        }
    }

    public void updateDirectMessages() {
        RequestTimelineTask task = new RequestTimelineTask(
            this, api, RequestTimelineTask.FEED_DIRECT);
        WaitCanvas wait = new WaitCanvas(this, task);
        display.setCurrent(wait);
    }

    public void showArchiveTimeline() {
        if(archiveTimeline==null) {
            updateArchiveTimeline();
        } else {
            timeline.setTimeline(archiveTimeline);
            display.setCurrent(timeline);
        }
    }

    public void updateArchiveTimeline() {
        RequestTimelineTask task = new RequestTimelineTask(
            this, api, RequestTimelineTask.FEED_ARCHIVE);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading...");
        display.setCurrent(wait);
    }

    public void showRecentTimeline() {
        if( recentTimeline==null) {
            updateRecentTimeline();
        } else {
            timeline.setTimeline( recentTimeline );
            display.setCurrent( timeline );
        }
    }

    public void updateRecentTimeline() {
        RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_FRIENDS);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading your timeline...");
        display.setCurrent(wait);
    }


    /** Show status updating view. */
    public void showStatusView(String prefix) {
        UpdateStatusTextBox statusView = new UpdateStatusTextBox(this, prefix, "");
        display.setCurrent(statusView);
    }

    /** Show status updating view. */
    public void showStatusView(String prefix, String inReplyTo) {
        UpdateStatusTextBox statusView = new UpdateStatusTextBox(this, prefix, inReplyTo);
        display.setCurrent(statusView);
    }

    /** 
     * Update Twitter status.
     * @param status    New status
     */
    public void updateStatus(String status, String inReplyTo) {
        UpdateStatusTask task = new UpdateStatusTask( this, api, status, inReplyTo );
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Updating status...");
        display.setCurrent(wait);
    }
    
    public void setAsFavorite(String statusId) {
        SetAsFavoriteTask task = new SetAsFavoriteTask(this, api, statusId);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Setting the status as favorite...");
        display.setCurrent(wait);
    }

    public void repeat(String statusId) {
        RepeatStatusTask task = new RepeatStatusTask(this, api, statusId);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Repeating the status...");
        display.setCurrent(wait);
    }

    public void removeFromServer(Status status) {
        RemoveStatusTask task = new RemoveStatusTask(this, api, status);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Deleting the status...");
        display.setCurrent(wait);
    }

    public void useArchiveTimeline() {
        timeline.setTimeline(archiveTimeline);
    }
    
    public void useResponsesTimeline() {
        timeline.setTimeline(responsesTimeline);
    }
    
    /** 
     * Set friends time line entries.
     * @param friendsTimeline 
     */
    public void setRecentTimeline(Vector friendsTimeline) {
        this.recentTimeline = friendsTimeline;
    }
    
    /** Show login form */
    public void showLoginForm() {
        SettingsForm loginForm = new SettingsForm( this );
        display.setCurrent( loginForm );
    }
    
    public void showTimeline(Vector timelineFeed ) {        
        timeline.setTimeline( timelineFeed );
        display.setCurrent( timeline );
    }

    /** Show splash screen */
    void showSplash() {
        SplashCanvas splash = new SplashCanvas(this);
        display.setCurrent(splash);
    }

}
