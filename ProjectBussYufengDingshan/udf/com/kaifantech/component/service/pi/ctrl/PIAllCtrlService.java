package com.kaifantech.component.service.pi.ctrl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.info.agv.AgvBean;
import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.component.business.msg.info.agv.IAgvMsgInfoModule;
import com.kaifantech.component.service.pi.ctrl.ctrl2agv.byangle.PICtrlService;
import com.kaifantech.component.service.pi.path.info.TaskPathInfoService;
import com.kaifantech.component.service.taskexe.info.TaskexeInfoService;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.ytgrading.util.AppTool;

@Component
public class PIAllCtrlService {

	@Autowired
	private PICtrlService piCtrlService;

	@Autowired
	private IAgvMsgInfoModule msgFromAGVService;

	@Autowired
	private PIMsgService piMsgService;

	@Autowired
	private TaskexeInfoService taskexeInfoService;

	@Autowired
	private TaskPathInfoService taskPathInfoService;

	public PreventImpactCommand check2Agvs(AgvBean agvBeanOne, AgvBean agvBeanAnother) {
		AGVMsgBean msgOne = getAGVMsgBean(agvBeanOne.getId());
		AGVMsgBean msgAnother = getAGVMsgBean(agvBeanAnother.getId());
		if (AppTool.isNull(msgOne) || AppTool.isNull(msgAnother) || AppTool.isNull(msgOne.getAGVId())
				|| AppTool.isNull(msgAnother.getAGVId())) {
			return null;
		}
		TaskexeBean taskexeBeanOne = taskexeInfoService.getNotOverOneF(agvBeanOne.getId());
		TaskexeBean taskexeBeanAnother = taskexeInfoService.getNotOverOneF(agvBeanAnother.getId());
		List<TaskPathInfoPointBean> pathOne = null;
		List<TaskPathInfoPointBean> pathAnother = null;
		if (!AppTool.isNull(taskexeBeanOne)) {
			pathOne = taskPathInfoService.findPathInMap(taskexeBeanOne);
		}
		if (!AppTool.isNull(taskexeBeanAnother)) {
			pathAnother = taskPathInfoService.findPathInMap(taskexeBeanAnother);
		}

		if (!AppTool.isNull(taskexeBeanOne) || !AppTool.isNull(taskexeBeanAnother)) {
			piMsgService.printMsg(msgOne, msgAnother, !AppTool.isNull(pathOne) || !AppTool.isNull(pathAnother),
					!AppTool.isNull(pathOne), !AppTool.isNull(pathAnother));
		}

		return piCtrlService.check2Agvs(pathOne, pathAnother, msgOne, msgAnother, taskexeBeanOne, taskexeBeanAnother);
	}

	private AGVMsgBean getAGVMsgBean(Integer agvId) {
		return msgFromAGVService.getLatestMsgBean(agvId);
	}

}
