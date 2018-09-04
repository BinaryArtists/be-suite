package com.open.es;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class EsTransportClient {
	private Client client;

	@SuppressWarnings("resource")
	public EsTransportClient(String clusterName, String ipAddr) throws UnknownHostException {
		
		Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName)
				.put("client.transport.sniff", true).build();
		List<InetSocketTransportAddress> addList = new ArrayList<InetSocketTransportAddress>();
		String[] ipArr = ipAddr.split(",");
		for (String ip : ipArr) {
			if (ip != null && !ip.trim().isEmpty()) {
				InetSocketTransportAddress addr = new InetSocketTransportAddress(InetAddress.getByName(ip.trim()) , 9300);
				addList.add(addr);
			}
		}
		this.client = new TransportClient.Builder().settings(settings).build()
				.addTransportAddresses(addList.toArray(new InetSocketTransportAddress[] {}));
	}

	@SuppressWarnings("resource")
	public EsTransportClient(String clusterName, String ipAddr, Integer port) throws UnknownHostException {
		
		Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName)
				.put("client.transport.sniff", true).build();
		List<InetSocketTransportAddress> addList = new ArrayList<InetSocketTransportAddress>();
		String[] ipArr = ipAddr.split(",");
		for (String ip : ipArr) {
			if (ip != null && !ip.trim().isEmpty()) {
				InetSocketTransportAddress addr = new InetSocketTransportAddress(InetAddress.getByName(ip.trim()), port);
				addList.add(addr);
			}
		}
		this.client = new TransportClient.Builder().settings(settings).build()
				.addTransportAddresses(addList.toArray(new InetSocketTransportAddress[] {}));
	}

	public void closeClient() {
		this.client.close();
	}

	public Client getClient() {
		return client;
	}
}
