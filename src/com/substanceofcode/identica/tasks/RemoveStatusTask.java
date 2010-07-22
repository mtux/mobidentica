/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.identica.tasks;

import com.substanceofcode.identica.IdenticaApi;
import com.substanceofcode.identica.IdenticaController;
import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.identica.model.Status;

/**
 *
 * @author mtux
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
