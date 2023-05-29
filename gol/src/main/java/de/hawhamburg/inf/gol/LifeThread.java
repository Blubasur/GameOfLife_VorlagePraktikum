package de.hawhamburg.inf.gol;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interruptible Thread that receives its next tasks from a LifeThreadPool.
 *
 * @author Christian Lins
 */
public class LifeThread extends Thread {

    private final LifeThreadPool pool;
    private boolean isInterrupted = false;

    public LifeThread(LifeThreadPool pool) {
        this.pool = pool;
    }

    /**
     * Sets the interrupted flag, so that this thread stops after finishing the
     * current task.
     */
    @Override
    public void interrupt() {
        isInterrupted = true;
    }

    @Override
    public void run() {
        while (!isInterrupted) {
            try {
                pool.nextTask();
            } catch (InterruptedException e) {
                pool.interrupt();
                break;
            }
        }
    }
}
