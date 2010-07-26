/*
 * RepeatStatusTask.java
 *
 * Copyright (C) 2010 Mehrdad Momeny
 * http://www.opatan.ir/
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
//import com.substanceofcode.utils.Log;

/**
 *
 * @author Mehrdad Momeny
 */
public class RepeatStatusTask extends AbstractTask {

    private IdenticaController controller;
    private IdenticaApi api;
    private String statusId;

    public RepeatStatusTask(IdenticaController controller, IdenticaApi api, String statusId) {
        this.controller = controller;
        this.api = api;
        this.statusId = statusId;
    }

    public void doTask() {
        try {
            Status updatedStatus = api.repeat(statusId);
            if(updatedStatus!=null) {
                controller.addStatus(updatedStatus);
            }
        } finally {
            controller.showRecentTimeline();
        }
    }

}
