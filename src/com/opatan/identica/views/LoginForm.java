/*
 * LoginForm.java
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

import com.opatan.identica.Settings;
import com.opatan.identica.IdenticaController;
import com.opatan.utils.Log;
import java.io.IOException;
//import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;

/**
 * LoginForm for laconi.ca based services.
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class LoginForm extends Form implements CommandListener {

    private IdenticaController controller;
    private Command loginCommand;
    private Command backCommand;
    private TextField usernameField;
    private TextField passwordField;
    private TextField serviceUrlField;
//    private ChoiceGroup rememberValuesChoice;
    private TextField countField;

    /**
     * Creates a new instance of LoginForm
     * @param controller    Application controller.
     */
    public LoginForm(IdenticaController controller) {
        super("Login");
        this.controller = controller;

        Settings settings = controller.getSettings();

        String username = settings.getStringProperty(Settings.USERNAME, "");
        usernameField = new TextField("Username", username, 32, TextField.ANY);
        append(usernameField);

        String password = settings.getStringProperty(Settings.PASSWORD, "");
        passwordField = new TextField("Password", password, 32, TextField.PASSWORD);
        append(passwordField);

        String serviceUrl = settings.getStringProperty(Settings.SERVICE_URL, "http://identi.ca");
        serviceUrlField = new TextField("Service URL", serviceUrl, 64, TextField.URL);
        append(serviceUrlField);

        String count = settings.getStringProperty(Settings.NUM_OF_DENTS, "20");
        countField = new TextField("Max no of dents per list", count, 3, TextField.NUMERIC);
        append(countField);

        loginCommand = new Command("Login", Command.ITEM, 1);
        this.addCommand(loginCommand);

        backCommand = new Command("Back", Command.BACK, 2);
        this.addCommand(backCommand);

        this.setCommandListener(this);
    }

    /**
     * Handle commands (Login/Logout)
     * @param cmd   Activated command.
     * @param disp  Displayable item.
     */
    public void commandAction(Command cmd, Displayable disp) {
        if (cmd == loginCommand) {
            String username = usernameField.getString();
            String password = passwordField.getString();
            String count = countField.getString();
            String url = serviceUrlField.getString();
            Settings settings = controller.getSettings();
            /** Store username and password */
            Log.add("LoginForm: "+ count);
            settings.setStringProperty(Settings.USERNAME, username);
            settings.setStringProperty(Settings.PASSWORD, password);
            settings.setStringProperty(Settings.SERVICE_URL, url);
            settings.setStringProperty(Settings.NUM_OF_DENTS, count);
            try {
                settings.save(true);
            } catch (IOException ex) {
                Log.error(ex.getMessage());
            } catch (RecordStoreException ex) {
                Log.error(ex.getMessage());
            }
            controller.setServiceUrl(url);
            controller.login(username, password, url);
        } else if (cmd == backCommand) {
            controller.showRecentTimeline();
        }
    }
}