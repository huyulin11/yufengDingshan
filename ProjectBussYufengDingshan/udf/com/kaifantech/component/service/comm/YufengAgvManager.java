package com.kaifantech.component.service.comm;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.yufeng.YufengAGVMsgBean;
import com.kaifantech.bean.msg.agv.yufeng.YufengAgvMsgBeanTransfer;
import com.kaifantech.component.service.agv.simulator.YufengAgvServerWorker;
import com.kaifantech.init.sys.params.SystemParameters;
import com.ytgrading.util.AppTool;
import com.ytgrading.util.msg.AppMsg;

@Service
public class YufengAgvManager {
	@Autowired
	private YufengTestMsgService yufengTestMsgService;

	@Autowired
	private YufengAgvServerWorker agvServerWorker;

	public static String nextMsg = "";

	public synchronized Object getLatestMsg() {
		String msg = null;
		Map<Integer, AGVMsgBean> latestMsg = new HashMap<Integer, AGVMsgBean>();
		if (SystemParameters.isLocalTest()) {
			msg = yufengTestMsgService.getNextMsg();
		} else {
			msg = nextMsg;
		}
		if (AppTool.isNull(msg)) {
			return null;
		}
		if (!(msg.startsWith("#IGVMSG;") || msg.startsWith("cmd=position;"))) {
			return null;
		}

		YufengAGVMsgBean agvMsgBean = new YufengAGVMsgBean();
		agvMsgBean = YufengAgvMsgBeanTransfer.transToBean(1, msg, agvMsgBean);
		AGVMsgBean sendMsgBean = agvMsgBean.toAGVMsgBean();
		latestMsg.put(1, sendMsgBean);
		return JSONArray.toJSON(latestMsg);
	}

	public AppMsg pause(Integer agvId) {
		agvServerWorker.get(1).setMsgTobeSend("cmd=pause;pauseStat=1");
		return AppMsg.success();
	}

	public AppMsg startup(Integer agvId) {
		agvServerWorker.get(1).setMsgTobeSend("cmd=pause;pauseStat=0");
		return AppMsg.success();
	}

}
