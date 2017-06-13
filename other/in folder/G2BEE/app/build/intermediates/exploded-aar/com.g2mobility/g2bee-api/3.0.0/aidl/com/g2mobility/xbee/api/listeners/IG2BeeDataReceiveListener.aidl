package com.g2mobility.xbee.api.listeners;

import com.g2mobility.xbee.api.models.G2BeeMessage;

interface IG2BeeDataReceiveListener {

	void dataReceived(in G2BeeMessage xbeeMessage);

}
