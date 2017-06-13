package com.g2mobility.xbee;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeePacket;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;

/**
 * Interface that allow sending XBee requests and registering a callback listener.
 * The XBee responses will be processed by calling back the listener. 
 * 
 * @author Hanyu Li
 */
interface IXBeeManager {
	
	/**
     * Register a callback listener if there are new messages arrived.
     * @param listener a {@link PacketListener} implemented the 
     * {@link PacketListener#processResponse(XBeeResponse)} method.
     */
	void addPacketListener(PacketListener packetListener);
	
	/**
     * Unregister the listener. Should be called when unbinding to the service.
     * @param listener The {@link PacketListener} registered.
     */
	void removePacketListener(PacketListener packetListener);
	
	void sendRequest(in XBeeRequest request);
	
	/** 
	 * It's possible for packets to get interspersed if multiple threads send simultaneously.  
	 * 
	 * This method is thread-safe.
	 *  
	 * @param packet
	 */
	void sendXBeePacket(in XBeePacket packet);
	
    /**
	 * This exists solely for the XMPP project.  Use sendRequest instead
	 * 
	 * This method is thread-safe.
	 * 
	 * @param packet
	 */
	void sendPacket(in int[] packet);
	
	/**
	 * Sends an XBeeRequest though the XBee interface in an asynchronous manner, such that
	 * it will return immediately, without waiting for a response.
	 * Refer to the getResponse method for obtaining a response
	 * 
	 * This method is thread-safe.
	 * 
	 * @param request
	 */
	void sendAsynchronous(in XBeeRequest request);
	
	/**
	 * Uses sendSynchronous timeout defined in XBeeConfiguration (default is 5000ms)
	 */
	XBeeResponse sendSynchronous(in XBeeRequest request);
	
	/**
	 * Synchronous method for sending an XBeeRequest and obtaining the 
	 * corresponding response (response that has same frame id).
	 * <p/>
	 * This method returns the first response object with a matching frame id, within the timeout
	 * period, so it is important to use a unique frame id (relative to previous subsequent requests).
	 * <p/>
	 * This method must only be called with requests that receive a response of
	 * type XBeeFrameIdResponse.  All other request types will timeout.
	 * <p/>
	 * Keep in mind responses received here will also be available through the getResponse method
	 * and the packet listener.
	 * <p/>
	 * It is recommended to use a timeout of at least 5 seconds, since some responses can take a few seconds or more
	 * (e.g. if remote radio is not powered on).
	 * <p/>
	 * This method is thread-safe 
	 * 
	 * @param xbeeRequest
	 * 
	 * @return
	 */
	XBeeResponse sendSynchronousTimeOut(in XBeeRequest xbeeRequest, int timeout);
			
	/**
	 * Same as getResponse(int) but does not timeout.
	 * It's highly recommend that you always use a timeout because
	 * if the serial connection dies under certain conditions, you will end up waiting forever!
	 * <p/>
	 * Consider using the PacketListener for asynchronous (non-blocking) behavior
	 * 
	 * @return
	 */
	XBeeResponse getResponse();
	
	/**
	 * This method returns an XBeeResponse from the queue, if available, or
	 * waits up to "timeout" milliseconds for a response.
	 * <p/>
	 * There are three possible outcomes:
	 * <p/>
	 * 1.  A packet is returned within "timeout" milliseconds <br/>
	 * 2.  A RemoteException is thrown (i.e. queue was empty for duration of timeout) <br/>
	 * 3.  Null is returned if timeout is 0 and queue is empty. <br/>
	 * <p/>
	 * @param timeout milliseconds to wait for a response.  A value of zero disables the timeout
	 * @return
	 */
	XBeeResponse getResponseTimeout(int timeout);
	
	int getCurrentFrameId();
	
	/**
	 * This is useful for obtaining a frame id when composing your XBeeRequest.
	 * It will return frame ids in a sequential manner until the maximum is reached (0xff)
	 * and it flips to 1 and starts over.
	 * 
	 * This method is thread-safe.
	 * 
	 * @return
	 */
	int getNextFrameId();
	
	/**
	 * Updates the frame id.  Any value between 1 and ff is valid
	 * 
	 * @param val
	 * Jan 24, 2009
	 */
	void updateFrameId(int val);
	
	/**
	 * Removes all packets off of the response queue
	 */
	void clearResponseQueue();
	
	/**
     * Returns the address MAC of the local XBee module.
     * @return the address MAC of 64 bits
     */
	XBeeAddress getLocalAddress();
	
	/**
	 * Shuts down RXTX and packet parser thread
	 */
	void close();
}