/*
 * TimelineCanvas.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
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

package com.opatan.identica.views;

import com.opatan.infrastructure.Device;
import com.opatan.identica.IdenticaController;
import com.opatan.identica.Settings;
import com.opatan.identica.model.Status;
import com.opatan.utils.ImageUtil;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
//import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * TimelineCanvas
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TimelineCanvas extends Canvas {

    private IdenticaController controller;
    private Vector statuses;
    private StatusList statusList;
    private TabBar menuBar;
    private Menu menu;
    private Menu statusMenu;
    private int verticalScroll;
    private Image logoImage;
    
    /** 
     * Creates a new instance of TimelineCanvas
     * @param controller Application controller
     */
    public TimelineCanvas(IdenticaController controller) {
        this.controller = controller;
        setFullScreenMode(true);
        
        /** Menu bar tabs */
        String[] labels = {"Archive", "Replies", "Recent", "Direct", "Friends", "Public"};
        menuBar = new TabBar(2, labels, getWidth());
        
        /** Menu */
        String[] menuLabels = {"Update status", "Reload items", "Settings", "About", "Exit", "Cancel"};
        menu = new Menu(menuLabels, getWidth(), getHeight());

        /** Status menu */
        String[] statusMenuLabels = {"Open in browser", "Open link in browser", "Reply", "Repeat this notice",
                                     "Favor this notice", "Delete", "Send direct message", "Cancel"};
        statusMenu = new Menu(statusMenuLabels, getWidth(), getHeight());

        /** Status list control */
        statusList = new StatusList(getWidth(), getHeight());        
        
        verticalScroll = 0;

        logoImage = ImageUtil.loadImage("/images/logo.png");
    }

    public void setTimeline(Vector friendsTimeline) {
        this.statuses = friendsTimeline;
    }

    protected void paint(Graphics g) {
        g.setColor(Theme.BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        if(menu.isActive()==false && statusMenu.isActive()==false) {
            if(statuses.isEmpty()) {
                drawBackground(g);
            } else {
            boolean drawSelectionBox = menuBar.isSelectedActive();
            statusList.draw(
                    g, statuses,
                    menuBar.getHeight() + verticalScroll + TalkBalloon.textFont.getHeight()/2,
                    drawSelectionBox);
            }
            menuBar.draw(g, 0);
        } else if(menu.isActive()) {
            menu.draw(g);
        } else if(statusMenu.isActive()) {
            statusMenu.draw(g);
        }
    }

    public void showCurrentTimeline() {
        handleTabChange();
    }

    private void handleTabChange() {

        menuBar.activateSelectedTab();

        verticalScroll = 0;
        int tabIndex = menuBar.getSelectedTabIndex();
        switch(tabIndex){
            case 0:
                controller.showArchiveTimeline();
                break;
            case 1:
                controller.showResponsesTimeline();
                break;
            case 2:
                controller.showRecentTimeline();
                break;
            case 3:
                controller.showDirectMessages();
                break;
            case 4:
                controller.showFriends();
                break;
            case 5:
                controller.showPublicTimeline();
                break;
        }
        repaint();

    }

    private void updateCurrentTimeline() {
        verticalScroll = 0;
        int tabIndex = menuBar.getSelectedTabIndex();
        switch(tabIndex){
            case 0:
                controller.updateArchiveTimeline();
                break;
            case 1:
                controller.updateResponsesTimeline();
                break;
            case 2:
                controller.updateRecentTimeline();
                break;
            case 3:
                controller.updateDirectMessages();
                break;
            case 4:
                controller.updateFriends();
                break;
            case 5:
                controller.updatePublicTimeline();
                break;
        }
    }
    
    /** Handle repeated key presses. */
    protected void keyRepeated(int keyCode) {
        handleUpAndDownKeys(keyCode);
        repaint();
    }

    private void handleUpAndDownKeys(int keyCode) {
        int gameAction = this.getGameAction(keyCode);
        if(gameAction == Canvas.UP) {
            menuBar.resetSelectedTab();
            if(menu.isActive()) {
                menu.selectPrevious();
            } else if(statusMenu.isActive()) {
                statusMenu.selectPrevious();
            } else {
                verticalScroll += getHeight()/6;
                if(verticalScroll>0) {
                    verticalScroll = 0;
                }
            }
        } else if(gameAction == Canvas.DOWN) {
            menuBar.resetSelectedTab();
            if(menu.isActive()) {
                menu.selectNext();
            } else if(statusMenu.isActive()) {
                statusMenu.selectNext();
            } else {
                if(statusList.getSelected() != null)
                    verticalScroll -= getHeight()/6;
            }
        }        
    }
    
    public void activateMenuItem() {
        int selectedIndex = menu.getSelectedIndex();
        if(selectedIndex==0) {
            controller.showStatusView("");
        } else if(selectedIndex==1) {
            updateCurrentTimeline();
        } else if(selectedIndex==2) {
            controller.showLoginForm();
        } else if(selectedIndex==3) {
            controller.about();
        } else if(selectedIndex==4) {
            controller.exit();
        } else if(selectedIndex==5) {
            /** Cancel = Do nothing */
        }
    }

    public void activateStatusMenuItem() {
        int selectedIndex = statusMenu.getSelectedIndex();
        Status selectedStatus = statusList.getSelected();
        if(selectedIndex==0) {
            /** Open post in browser */
            if(selectedStatus!=null) {
                selectedStatus.openInBrowser(
                        controller.getMIDlet(),
                        controller.getServiceUrl());
                return;
            }
        } else if(selectedIndex==1) {
            /** Open post link in browser */
            if(selectedStatus!=null) {
                selectedStatus.openIncludedLink(
                        controller.getMIDlet(),
                        controller.getServiceUrl());
                return;
            }
        } else if(selectedIndex==2) {
            /** Reply to post */
            if(selectedStatus!=null) {
                controller.showStatusView("@" + selectedStatus.getScreenName() + " ", selectedStatus.getId());
            }
        } else if(selectedIndex==3) {
            /** Repeat an status */
            if(selectedStatus!=null) {
                controller.repeat(selectedStatus.getId());
            }
        } else if(selectedIndex==4) {
            /** Set selectedStatus as favorite */
            if(selectedStatus!=null) {
                controller.setAsFavorite(selectedStatus.getId());
            }
        } else if(selectedIndex==5) {
            /** Set selectedStatus as favorite */
            if(selectedStatus!=null && selectedStatus.getScreenName().equals(controller.getSettings().getStringProperty(Settings.USERNAME, ""))) {
                controller.removeFromServer(selectedStatus);
            }
        } else if(selectedIndex==6) {
            /** Send direct message */
            if(selectedStatus!=null) {
                controller.showStatusView("d " + selectedStatus.getScreenName() + " ");
            }
        }else if(selectedIndex==7) {
            /** Cancel = Do nothing */
        }
    }
    
    public void keyPressed(int keyCode) {
        int gameAction = this.getGameAction(keyCode);
        String keyName = this.getKeyName(keyCode);
        if(gameAction == Canvas.LEFT) {
            menuBar.selectPreviousTab();
            handleTabChange();
            repaint();
            return;
        } else if(gameAction == Canvas.RIGHT) {
            menuBar.selectNextTab();
            handleTabChange();
            repaint();
            return;
        } else if(gameAction == Canvas.FIRE) {
            
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();
            } else if(statusMenu.isActive()) {
                statusMenu.deactivate();
                activateStatusMenuItem();
            } else if(statusList.getSelected()!=null){
                statusMenu.activate();
            }
                
        } else if( keyName.indexOf("SOFT")>=0 && keyName.indexOf("1")>0 ||
            (Device.isNokia() && keyCode==-6) ) {
            /** Left soft key pressed */
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();                
            } else {
                menu.activate();
            }
        } else if( ((keyName.indexOf("SOFT")>=0 && keyName.indexOf("2")>0) ||
            (Device.isNokia() && keyCode==-7) ) ) {
            /** Right soft key pressed */
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();                
            } else {
                menu.activate();
            }
        } else if (keyCode == TimelineCanvas.KEY_POUND){
            controller.showStatusView("");
        } else if (keyCode == TimelineCanvas.KEY_STAR) {
            updateCurrentTimeline();
        }
        handleUpAndDownKeys(keyCode);
        repaint();
    }
    
    public void drawBackground(Graphics g) {
        g.drawImage(logoImage, getWidth()/2, getHeight()/2, Graphics.HCENTER|Graphics.VCENTER);
        g.setColor(0xBBBBBB);
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL );
        g.setFont( font );
        int fontHeight = font.getHeight();
        String copyright = "© 2010 Mehrdad Momeny";
        int copyWidth = font.stringWidth(copyright);
        g.drawString(copyright, getWidth()/2 - copyWidth/2, getHeight()-fontHeight*2, Graphics.LEFT|Graphics.BOTTOM);
        String urlLink = "http://opatan.ir/opidentica/";
        int urlWidth = font.stringWidth(urlLink);
        g.drawString(urlLink, getWidth()/2 - urlWidth/2, getHeight()-fontHeight, Graphics.LEFT|Graphics.BOTTOM);
    }
    
}
