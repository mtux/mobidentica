/*
 * Task.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
 * http://www.substanceofcode.com
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

package com.opatan.tasks;

import com.opatan.identica.views.WaitCanvas;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public abstract class AbstractTask implements Runnable {
    
    private Thread executionThread;
    protected WaitCanvas waitCanvas;
//    private boolean isAborted = false;

    public void execute() {
        executionThread = new Thread(this);
        executionThread.start();
    }

    public void run() {
        doTask();
    }
    
    public abstract void doTask();

    public void setWaitCanvas(WaitCanvas canvas) {
        waitCanvas = canvas;
    }
//    public void abort() {
//        isAborted = true;
//    }
        
}
