/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.identica.tasks;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.identica.IdenticaApi;
import com.substanceofcode.identica.IdenticaController;
import com.substanceofcode.identica.model.Status;
//import com.substanceofcode.utils.Log;

/**
 *
 * @author mtux
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
