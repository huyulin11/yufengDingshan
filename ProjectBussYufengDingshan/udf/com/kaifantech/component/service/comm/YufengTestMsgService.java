package com.kaifantech.component.service.comm;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kaifantech.component.dao.simulator.TestPathDao;
import com.ytgrading.util.AppTool;

@Service
public class YufengTestMsgService {
	@Autowired
	private TestPathDao testPathDao;

	private int flag = 0;
	private List<Map<String, Object>> list = null;

	public String getNextMsg() {
		if (AppTool.isNull(list) || list.size() <= 0 || flag >= list.size() - 1) {
			flag = 0;
			list = testPathDao.getYufengTestData();
		}
		Map<String, Object> obj;
		obj = list.get(flag++);
		if (AppTool.isNull(obj)) {
			return getNextMsg();
		}
		return obj.get("msg").toString();
	}
}
