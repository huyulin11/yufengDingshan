package com.kaifantech.component.service.pi.ctrl.ctrl2agv.byangle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.component.service.pi.path.intersection.IntersectionService;
import com.kaifantech.util.agv.msg.PreventImpactCommand;

@Component
public class PICtrlBothWithPathService {

	@Autowired
	private IntersectionService intersectionService;

	public PreventImpactCommand check2Agvs(List<TaskPathInfoPointBean> pathOne, List<TaskPathInfoPointBean> pathAnother,
			AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		return intersectionService.check2Agvs(pathOne, pathAnother, msgOne, msgAnother);
	}

}
