package com.kaifantech.component.business.comm.socketdev;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kaifantech.bean.iot.client.IotClientBean;
import com.kaifantech.component.dao.IotClientConnectMsgDao;
import com.kaifantech.component.log.AgvStatusDBLogger;
import com.kaifantech.component.service.iot.client.IotClientService;
import com.kaifantech.component.service.iot.client.msg.IotClientMsgService;
import com.kaifantech.component.service.lap.LapInfoService;
import com.kaifantech.component.service.taskexe.auto.ITaskexeAutoService;
import com.kaifantech.init.sys.ProjectName;
import com.kaifantech.init.sys.SystemInfo;
import com.kaifantech.init.sys.params.SystemParameters;
import com.kaifantech.util.constant.taskexe.ctrl.AgvCtrlType.IotDevType;
import com.kaifantech.util.socket.client.AbstractSocketClient;
import com.kaifantech.util.socket.netty.client.NettyClientFactory;
import com.kaifantech.util.thread.ThreadTool;
import com.ytgrading.util.AppTool;

@Service
public class RoboticArmWorker {
	private Map<Integer, AbstractSocketClient> clientMap = new HashMap<Integer, AbstractSocketClient>();

	@Autowired
	private IotClientService socketdevService;

	@Autowired
	private IotClientMsgService msgService;

	@Autowired
	private IotClientConnectMsgDao connectMsgDao;

	@Autowired
	private LapInfoService lapInfoService;

	@Autowired
	private ITaskexeAutoService taskexeAutoService;

	private int tipsTime = 0;

	@Autowired
	private AgvStatusDBLogger kaifantechDBLogger;

	public AbstractSocketClient getClient(Integer keyId) {
		return getClientMap().get(keyId);
	}

	public Map<Integer, AbstractSocketClient> getClientMap() {
		if (clientMap == null || clientMap.size() <= 0) {
			for (IotClientBean bean : socketdevService.getList()) {
				if (ProjectName.KF_CSY_DAJ.equals(SystemInfo.CURRENT_PROJECT)
						&& bean.getDevtype().equals(IotDevType.ROBOT_GOODS_FROM)) {
					try {
						AbstractSocketClient client;
						client = NettyClientFactory.create(bean);
						clientMap.put(bean.getId(), client);
						client.init();
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		return clientMap;
	}

	public void startConnect() {
		Iterator<Entry<Integer, AbstractSocketClient>> iterator = getClientMap().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, AbstractSocketClient> entry = iterator.next();
			doReceive(entry.getKey(), entry.getValue());
		}
	}

	@Async
	private void doReceive(Integer keyId, AbstractSocketClient client) {
		if (!SystemParameters.isAutoTask()) {
			if (tipsTime++ > 20) {
				tipsTime = 0;
				kaifantechDBLogger.warning("系统自动任务功能关闭中，请注意观察机械手实际生产情况！！！", 0, AgvStatusDBLogger.MSG_LEVEL_INFO);
			}
			return;
		}
		List<Integer> lapIds = client.getLatestMsgList();
		for (Integer lapId : lapIds) {
			if (!AppTool.isNull(lapInfoService.getLap(lapId))) {
				if (!lapInfoService.getLapInUsed(lapId)) {
					taskexeAutoService.addTask(lapId, 1);
					ThreadTool.sleep(1000);
				}
			}
		}
		String msg = client.getMsg();
		if (!AppTool.isNullStr(msg)) {
			ThreadTool.getThreadPool().execute(new Runnable() {
				public void run() {
					connectMsgDao.addAReceiveMsg(msg, keyId);
					msgService.setLatestMsg();
				}
			});
		}
	}
}
