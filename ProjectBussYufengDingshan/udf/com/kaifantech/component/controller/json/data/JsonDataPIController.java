package com.kaifantech.component.controller.json.data;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg.PICtrlClashAreaService;
import com.kaifantech.util.constant.pi.detail.ClashArea;

@Controller
@RequestMapping("/json/data/")
public class JsonDataPIController {

	@Autowired
	private PICtrlClashAreaService piCtrlClashAreaService;

	@RequestMapping("getClashArea")
	@ResponseBody
	public Object getClashArea() {
		Map<Integer, ClashArea> latestMsg = new HashMap<Integer, ClashArea>();
		latestMsg.put(-1, piCtrlClashAreaService.getClashArea());
		return JSONArray.toJSON(latestMsg);
	}
}
