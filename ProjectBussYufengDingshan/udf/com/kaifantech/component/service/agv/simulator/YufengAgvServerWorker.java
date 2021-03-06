package com.kaifantech.component.service.agv.simulator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kaifantech.bean.iot.client.IotClientBean;
import com.kaifantech.component.comm.worker.server.IServerWorker;
import com.kaifantech.util.socket.IConnect;
import com.kaifantech.util.socket.netty.server.yufeng.YufengAgvNettyServer;

@Service
public class YufengAgvServerWorker implements IServerWorker {
	private Map<Integer, IConnect> map = new HashMap<>();

	public synchronized Map<Integer, IConnect> getMap() {
		return map;
	}

	public synchronized void init() {
		if (getMap() == null || getMap().size() == 0) {
			YufengAgvNettyServer simulator = YufengAgvNettyServer.create(new IotClientBean(null, "" + 8080));
			getMap().put(1, simulator);
			try {
				simulator.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
