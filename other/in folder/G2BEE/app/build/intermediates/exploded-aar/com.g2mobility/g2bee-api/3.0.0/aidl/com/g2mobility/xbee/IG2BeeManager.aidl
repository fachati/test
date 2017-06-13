package com.g2mobility.xbee;

import com.g2mobility.xbee.api.DiscoverResponse;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.api.G2BeeResponse;
import com.g2mobility.xbee.api.GetParameterResponse;
import com.g2mobility.xbee.api.GetReceiveTimeoutResponse;
import com.g2mobility.xbee.api.RemoteG2BeeDevice;

import com.g2mobility.xbee.api.listeners.IG2BeeDataReceiveListener;

/**
 * Interface that allow sending XBee requests and registering a callback listener. The XBee
 * responses will be processed by calling back the listener.
 * 
 * @author Hanyu Li
 */
interface IG2BeeManager {

    List<G2BeeDevice> getLocalG2BeeDevices();

    G2BeeDevice getLocalDeviceForRemoteDevice(in byte[] xbee64BitAddress);

    List<RemoteG2BeeDevice> getRemoteG2BeeDevices();

    void addDataListener(in IG2BeeDataReceiveListener listener);

    void removeDataListener(in IG2BeeDataReceiveListener listener);

    GetParameterResponse getParameter(in byte[] xbee64BitAddress, String parameter);

    G2BeeResponse setParameter(in byte[] xbee64BitAddress, String parameter, in byte[] receiveTimeout);

    G2BeeResponse executeParameter(in byte[] xbee64BitAddress, String parameter);

    GetReceiveTimeoutResponse getReceiveTimeout(in byte[] xbee64BitAddress);

    G2BeeResponse setReceiveTimeout(in byte[] xbee64BitAddress, int receiveTimeout);

    DiscoverResponse discover(in byte[] local64BitAddress);

    G2BeeResponse sendData(in byte[] remote64BitAddress, in byte[] data);

    G2BeeResponse sendDataAsync(in byte[] remote64BitAddress, in byte[] data);

    G2BeeResponse sendBroadcastData(in byte[] data);

}
