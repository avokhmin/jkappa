package com.plectix.simulator.gui.lib;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.event.InvocationEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

/**
 * Event queue implementation that uses a timer thread to show a watch cursor if the
 * processing of any event takes longer than the specified time. To use this class,
 * call Toolkit.getDefaultToolkit().getSystemEventQueue().push(new WaitingEventQueue());
 * 
 * Based on code from http://www.javaworld.com/javaworld/javatips/jw-javatip87.html
 * 
 * @author ecemis
 */
public class WaitingEventQueue extends EventQueue {
	private static final String TIMEOUT_PROPERTY_STRING = "timeout.waitcursor.millis"; 
	
	private Timer timer = new Timer();
	private int delay;
	private Component rootComponent;
	private Object lock = new Object();

	
	public WaitingEventQueue(Component rootComponent) {
		this.rootComponent = rootComponent;
		this.delay = UIProperties.getInt(TIMEOUT_PROPERTY_STRING);
	}

	public void disable() {
		timer.cancel();
		timer = null;
	}

	@Override
	protected void dispatchEvent(AWTEvent event) {

		// Skip some event types that may cause re-entrancy problems, or that we
		// just don't care about
		String classname = event.getClass().getName();
		if (timer == null || // we've been disabled
				classname.equals("java.awt.SequencedEvent") ||
				classname.equals("java.awt.SentEvent") ||
				event instanceof WindowEvent ||
				event instanceof InvocationEvent || // These seem to be generated by repaint() among other things
				(event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_MOVED)) {
			super.dispatchEvent(event);
			return;
		}

		final AWTEvent timedEvent = event;

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				Component c = findRootComponent(rootComponent, timedEvent);
				// log.debug("Showing wait cursor on " + c + " for " + timedEvent);
				c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		};

		synchronized (lock) {
			timer.schedule(timerTask, delay);
			try {
				super.dispatchEvent(event);
			} finally {
				timerTask.cancel();
				Component c = findRootComponent(rootComponent, timedEvent);
				// log.debug("Clearing wait cursor on " + c + " for " + timedEvent);
				if (c != null)
					c.setCursor(null);
			}
		}
	}

	/**
	 * This logic seems to be necessary to use the right root component for 
	 * when modal dialogs are being displayed.  If we can get a root component
	 * from the event (either the dialog or the main window), use it.  For some
	 * events (for whatever reason) we can't find a valid root (or the event's
	 * root is no longer visible), so use the global root (the main window).
	 */
	private Component findRootComponent(Component rootComponent, AWTEvent event) {
		
		if (event == null)
			return rootComponent;

		Object eventSource = event.getSource();

		if (eventSource instanceof Component) {
			Component c = SwingUtilities.getRoot((Component) eventSource);
			if (c != null && c.isVisible())
				return c;
			else
				return rootComponent;
		}

		if (eventSource instanceof MenuComponent) {
			MenuContainer menuParent = ((MenuComponent) eventSource).getParent();
			if (menuParent instanceof Component) {
				Component c = SwingUtilities.getRoot((Component) menuParent);
				if (c != null && c.isVisible())
					return c;
				else
					return rootComponent;
			}
		}

		return null;
	}
}