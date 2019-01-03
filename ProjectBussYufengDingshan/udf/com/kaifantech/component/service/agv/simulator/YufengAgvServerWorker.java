package com.kaifantech.component.service.agv.simulator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kaifantech.component.comm.worker.server.agv.IAgvServerWorker;
import com.kaifantech.util.socket.IConnect;
import com.kaifantech.util.socket.netty.server.yufeng.YufengAgvNettyServer;

@Service
public class YufengAgvServerWorker implements IAgvServerWorker {
	private Map<Integer, IConnect> map = new HashMap<>();

	public synchronized Map<Integer, IConnect> getMap() {
		return map;
	}

	public synchronized void init() {
		if (getMap() == null || getMap().size() == 0) {
			YufengAgvNettyServer simulator = YufengAgvNettyServer.create(8080);
			getMap().put(1, simulator);
			try {
				simulator.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
