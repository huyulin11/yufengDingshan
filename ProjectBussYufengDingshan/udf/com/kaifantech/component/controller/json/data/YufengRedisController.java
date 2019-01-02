package com.kaifantech.component.controller.json.data;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.yufeng.YufengAGVMsgBean;
import com.kaifantech.bean.msg.agv.yufeng.YufengAgvMsgBeanTransfer;
import com.kaifantech.cache.manager.ISingleCacheWorkerGetter;
import com.kaifantech.component.service.agv.simulator.YufengAgvServerWorker;
import com.ytgrading.util.AppTool;

@Controller
@RequestMapping("/json/open/yufeng/")
public class YufengRedisController implements ISingleCacheWorkerGetter {

	@Autowired
	private YufengAgvServerWorker agvServerMgr;

	@ResponseBody
	public synchronized Object getLatestMsg() {
		String msg = null;
		Map<Integer, AGVMsgBean> latestMsg = new HashMap<Integer, AGVMsgBean>();
		msg = agvServerMgr.getMap().get(1).getMsgReceived();
		if (!AppTool.isNull(msg)) {
			msg = msg.trim();
			System.out.println(msg);
			if (!(msg.startsWith("#IGVMSG;"))) {
				return null;
			}
			YufengAGVMsgBean yufengAgvMsgBean = new YufengAGVMsgBean();
			yufengAgvMsgBean = YufengAgvMsgBeanTransfer.transToBean(1, msg, yufengAgvMsgBean);
			AGVMsgBean sendMsgBean = yufengAgvMsgBean.toAGVMsgBean();
			latestMsg.put(1, sendMsgBean);
		}
		return JSONArray.toJSON(latestMsg);
	}
}
