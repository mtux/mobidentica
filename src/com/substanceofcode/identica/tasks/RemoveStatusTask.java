/*
 * RemoveStatusTask.java
 *
 * Copyright (C) 2010 Mehrdad Momeny
 * http://momeny.wordpress.com
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
package com.substanceofcode.identica.tasks;

import com.substanceofcode.identica.IdenticaApi;
import com.substanceofcode.identica.IdenticaController;
import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.identica.model.Status;

/**
 *
 * @author Mehrdad Momeny
 */
public class RemoveStatusTask extends AbstractTask {

    private IdenticaController controller;
    private IdenticaApi api;
    private Status status;

    public RemoveStatusTask(IdenticaController controller, IdenticaApi api, Status status) {
        this.controller = controller;
        this.api = api;
        this.status = status;
    }

    public void doTask() {
        try {
            Status updatedStatus = api.removeFromServer(status.getId());
            if(updatedStatus!=null) {
                controller.removeStatus(status);
            }
        } finally {
            controller.showRecentTimeline();
        }
    }

}
