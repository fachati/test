
package com.g2mobility.xbee;

import android.os.RemoteException;

import com.g2mobility.xbee.api.DiscoverResponse;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.api.G2BeeResponse;
import com.g2mobility.xbee.api.GetParameterResponse;
import com.g2mobility.xbee.api.GetReceiveTimeoutResponse;
import com.g2mobility.xbee.api.RemoteG2BeeDevice;
import com.g2mobility.xbee.api.listeners.IG2BeeDataReceiveListener;

import java.util.List;

public class G2BeeManager extends IG2BeeManager.Stub {

    private G2BeeService mService;

    @Override
    public List<G2BeeDevice> getLocalG2BeeDevices() throws RemoteException {
        return mService.getLocalXBeeDevices();
    }

    @Override
    public G2BeeDevice getLocalDeviceForRemoteDevice(
            byte[] xbee64BitAddress) throws RemoteException {
        return mService.getLocalXBeeDeviceForRemoteDevice(xbee64BitAddress);
    }

    @Override
    public List<RemoteG2BeeDevice> getRemoteG2BeeDevices() throws RemoteException {
        return mService.getRemoteXBeeDevices();
    }

    @Override
    public void addDataListener(IG2BeeDataReceiveListener listener) throws RemoteException {
        mService.addDataListener(listener);
    }

    @Override
    public void removeDataListener(IG2BeeDataReceiveListener listener) throws RemoteException {
        mService.removeDataListener(listener);
    }

    @Override
    public GetParameterResponse getParameter(byte[] xbee64BitAddress, String parameter)
            throws RemoteException {
        return mService.getParameter(xbee64BitAddress, parameter);
    }

    @Override
    public G2BeeResponse setParameter(byte[] xbee64BitAddress, String parameter,
            byte[] value) throws RemoteException {
        return mService.setParameter(xbee64BitAddress, parameter, value);
    }

    @Override
    public G2BeeResponse executeParameter(byte[] xbee64BitAddress,
            String parameter) throws RemoteException {
        return mService.executeParameter(xbee64BitAddress, parameter);
    }

    @Override
    public GetReceiveTimeoutResponse getReceiveTimeout(byte[] xbee64BitAddress)
            throws RemoteException {
        return mService.getReceiveTimeout(xbee64BitAddress);
    }

    @Override
    public G2BeeResponse setReceiveTimeout(byte[] xbee64BitAddress, int receiveTimeout)
            throws RemoteException {
        return mService.setReceiveTimeout(xbee64BitAddress, receiveTimeout);
    }

    @Override
    public DiscoverResponse discover(byte[] local64BitAddress) throws RemoteException {
        return mService.discover(local64BitAddress);
    }

    @Override
    public G2BeeResponse sendData(byte[] remote64BitAddress, byte[] data) throws RemoteException {
        return mService.sendData(remote64BitAddress, data);
    }

    @Override
    public G2BeeResponse sendDataAsync(byte[] remote64BitAddress, byte[] data)
            throws RemoteException {
        return mService.sendDataAsync(remote64BitAddress, data);
    }

    @Override
    public G2BeeResponse sendBroadcastData(byte[] data) throws RemoteException {
        return mService.sendBroadcastData(data);
    }

    public G2BeeManager(G2BeeService service) {
        if (service == null) {
            throw new NullPointerException("Missing G2BeeService");
        } else {
            mService = service;
        }
    }

}
