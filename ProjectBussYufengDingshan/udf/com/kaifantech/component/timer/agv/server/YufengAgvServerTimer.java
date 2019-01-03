package com.kaifantech.component.timer.agv.server;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kaifantech.component.comm.worker.server.agv.IAgvServerWorker;
import com.kaifantech.component.dao.AgvMsgDao;
import com.kaifantech.component.service.comm.YufengAgvManager;
import com.kaifantech.init.sys.ProjectClient;
import com.kaifantech.init.sys.SystemInfo;
import com.kaifantech.init.sys.params.SystemParameters;
import com.kaifantech.util.seq.ThreadID;
import com.kaifantech.util.thread.ThreadTool;
import com.ytgrading.util.AppTool;

@Component
@Lazy(false)
public class YufengAgvServerTimer {
	private static boolean isRunning = false;
	private static String timerType = "IOT模拟器";
	private final Logger logger = Logger.getLogger(YufengAgvServerTimer.class);

	@Autowired
	private IAgvServerWorker agvServerWorker;

	@Autowired
	private AgvMsgDao msgDao;

	public YufengAgvServerTimer() {
		logger.info(timerType + "开始启动！");
	}

	@Scheduled(cron = "0/1 * * * * ?")
	public void resolute() {
		if (!SystemInfo.CURRENT_CLIENT.equals(ProjectClient.YUFENG)) {
			return;
		}
		if (!SystemParameters.isConnectIotServer()) {
			return;
		}
		if (!isRunning) {
			Thread.currentThread().setName(timerType + (ThreadID.number++));
			isRunning = true;
			agvSimulate();
		}
		isRunning = false;
	}

	private void agvSimulate() {
		ThreadTool.run(() -> {
			agvServerWorker.startConnect();
		});
	}

	@Scheduled(initialDelay = 5000, fixedDelay = 1000)
	public void getMsgFromAgv() {
		if (!SystemInfo.CURRENT_CLIENT.equals(ProjectClient.YUFENG)) {
			return;
		}
		String msg = agvServerWorker.get(1).getMsg();
		if (AppTool.isNull(msg)) {
			return;
		}
		YufengAgvManager.nextMsg = msg;
		msgDao.addAMsg(msg, "getMsgFromAgv", 1);
	}
}
