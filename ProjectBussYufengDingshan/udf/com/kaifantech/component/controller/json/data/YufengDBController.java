package com.kaifantech.component.controller.json.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kaifantech.cache.manager.ISingleCacheWorkerGetter;
import com.kaifantech.component.service.comm.YufengAgvManager;
import com.ytgrading.util.msg.AppMsg;

@Controller
@RequestMapping("/json/open/yufeng/")
public class YufengDBController implements ISingleCacheWorkerGetter {
	@Autowired
	private YufengAgvManager agvManager;

	@RequestMapping("getLatestMsg")
	@ResponseBody
	public synchronized Object getLatestMsg() {
		return agvManager.getLatestMsg();
	}

	@RequestMapping("pause")
	@ResponseBody
	public String pause(HttpServletRequest request, HttpServletResponse response) {
		try {
			AppMsg appMsg = agvManager.pause(null);
			return appMsg.isSuccess() ? "成功:" + appMsg.getMsg() : "失败:" + appMsg.getMsg();
		} catch (Exception e) {
			return "失败" + e.getMessage();
		}
	}

	@RequestMapping("startup")
	@ResponseBody
	public String startup(HttpServletRequest request, HttpServletResponse response) {
		try {
			AppMsg appMsg = agvManager.startup(null);
			return appMsg.isSuccess() ? "成功:" + appMsg.getMsg() : "失败:" + appMsg.getMsg();
		} catch (Exception e) {
			return "失败" + e.getMessage();
		}
	}
}
