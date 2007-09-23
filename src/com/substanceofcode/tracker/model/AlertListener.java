/**
 * 
 */
package com.substanceofcode.tracker.model;

/**
 * Interface for classes which wish to be notified to events received by an
 * AlertManager
 * @author mch50
 */
public interface AlertListener {
    
    void notifyError();
}
