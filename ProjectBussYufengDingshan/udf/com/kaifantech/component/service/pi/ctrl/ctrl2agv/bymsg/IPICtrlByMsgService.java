package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.agv.taskpath.DistanceStatus;
import com.ytgrading.util.AppTool;

@Component
public interface IPICtrlByMsgService {

	public default PreventImpactCommand dangerous(AGVMsgBean msgOne) {
		PreventImpactCommand command = new PreventImpactCommand();
		command.setDistanceStatus(DistanceStatus.DANGEROUS);
		if (!AppTool.isNull(msgOne)) {
			command.getDangerMsgs().add(msgOne);
		}
		return command;
	}

	public default PreventImpactCommand dangerous(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		PreventImpactCommand command = new PreventImpactCommand();
		command.setDistanceStatus(DistanceStatus.DANGEROUS);
		if (!AppTool.isNull(msgOne)) {
			command.getDangerMsgs().add(msgOne);
		}
		if (!AppTool.isNull(msgAnother)) {
			command.getDangerMsgs().add(msgAnother);
		}
		return command;
	}

	public default PreventImpactCommand safe(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		PreventImpactCommand command = new PreventImpactCommand();
		command.setDistanceStatus(DistanceStatus.SAFE);
		if (!AppTool.isNull(msgOne)) {
			command.getSafeMsgs().add(msgOne);
		}
		if (!AppTool.isNull(msgAnother)) {
			command.getSafeMsgs().add(msgAnother);
		}
		return command;
	}

	public default PreventImpactCommand warning(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		PreventImpactCommand command = new PreventImpactCommand();
		command.setDistanceStatus(DistanceStatus.WARNING);
		return command;
	}

}
