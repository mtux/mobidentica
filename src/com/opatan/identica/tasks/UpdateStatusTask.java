/*
 * UpdateStatusTask.java
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

package com.opatan.identica.tasks;

import com.opatan.tasks.AbstractTask;
import com.opatan.identica.IdenticaApi;
import com.opatan.identica.IdenticaController;
import com.opatan.identica.model.Status;

/**
 * Task to update Twitter status.
 * 
 * @author Tommi Laukkanen
 */
public class UpdateStatusTask extends AbstractTask {

    private IdenticaController controller;
    private IdenticaApi api;
    private String status;
    private String inReplyTo;

    /** 
     * Create new instance of UpdateStatusTask.
     * @param controller    Application controller
     * @param api           Twitter API wrapper
     * @param status        Your current status text
     * @param inReplyTo     in reply to Id
     */
    public UpdateStatusTask(
            IdenticaController controller,
            IdenticaApi api,
            String status,
            String inReplyTo) {
        this.controller = controller;
        this.api = api;
        this.status = status;
        this.inReplyTo = inReplyTo;
    }

    /**
     * Create new instance of UpdateStatusTask.
     * @param controller    Application controller
     * @param api           Twitter API wrapper
     * @param status        Your current status text
     */
    public UpdateStatusTask(
            IdenticaController controller,
            IdenticaApi api,
            String status) {
        this.controller = controller;
        this.api = api;
        this.status = status;
    }

    /** Execute task that updates your Twitter status. */
    public void doTask() {
        try {
            Status updatedStatus = api.updateStatus(status, inReplyTo);
            if(updatedStatus!=null) {
                controller.addStatus(updatedStatus);
            }
        } finally {
            controller.showRecentTimeline();
        }
    }
    
}