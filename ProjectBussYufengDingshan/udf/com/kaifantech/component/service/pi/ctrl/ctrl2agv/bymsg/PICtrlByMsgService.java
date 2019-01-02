package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.util.agv.msg.MsgCompare;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.log.AppFileLogger;
import com.ytgrading.util.DateFactory;
import com.ytgrading.util.AppTool;

@Component
public class PICtrlByMsgService implements IPICtrlByMsgService {
	@Autowired
	private PICtrlOtherService piCtrlOtherService;

	@Autowired
	PICtrlVericalService piCtrlVericalService;

	@Autowired
	PICtrlParallelService piCtrlParallelService;

	@Autowired
	PICtrlClashAreaService piCtrlClashAreaService;

	private String currentModel = "";

	// @Autowired
	// private DifferByMsg differByMsg;

	public PreventImpactCommand check2AgvsByMsg(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		// differByMsg.initPIParam();
		MsgCompare<AGVMsgBean> compare = new MsgCompare<AGVMsgBean>(msgOne, msgAnother);

		getTipsMsg(msgOne, msgAnother, compare);

		PreventImpactCommand command;

		command = piCtrlClashAreaService.check(msgOne, msgAnother, compare);
		if (!AppTool.isNull(command)) {
			printCurrentModel(msgOne, msgAnother, "易冲突区域控制");
			return command;
		}

		if (msgOne.isTaskfinished() || msgAnother.isTaskfinished()) {
			return this.warning(msgOne, msgAnother);
		}

		if (compare.isParallel()) {
			printCurrentModel(msgOne, msgAnother, "平行控制");
			return piCtrlParallelService.checkWhenParallel(msgOne, msgAnother, compare);
		} else if (compare.isVertical()) {
			printCurrentModel(msgOne, msgAnother, "垂直控制");
			return piCtrlVericalService.checkWhenV(msgOne, msgAnother, compare);
		} else {
			printCurrentModel(msgOne, msgAnother, "非正角控制2");
			return piCtrlOtherService.checkWhenOthers(msgOne, msgAnother, compare);
		}
	}

	@Async
	private void printCurrentModel(AGVMsgBean msgOne, AGVMsgBean msgAnother, String model) {
		if (!model.equals(currentModel)) {
			currentModel = model;
			AppFileLogger.piLogs(DateFactory.getCurrTime() + "--" + currentModel + "--");
		}
	}

	@Async
	private void getTipsMsg(AGVMsgBean msgOne, AGVMsgBean msgAnother, MsgCompare<AGVMsgBean> compare) {
		double distanceXAxis = 0;
		double distanceYAxis = 0;
		double distance = 0;
		String patten = msgOne + "-" + msgAnother;
		distanceYAxis = compare.getDistanceOfY();
		distanceXAxis = compare.getDistanceOfX();
		distance = compare.getDistance();

		if (compare.isParallel()) {
			patten += "平行模式";
		} else if (compare.isVertical()) {
			patten += "垂直模式";
		} else {
			patten += "非正角模式";
		}

		AppFileLogger
				.piLogs(patten + "，距离为：" + distance + "，X坐标距离：" + distanceXAxis + "，Y坐标距离：" + distanceYAxis);
	}
}
