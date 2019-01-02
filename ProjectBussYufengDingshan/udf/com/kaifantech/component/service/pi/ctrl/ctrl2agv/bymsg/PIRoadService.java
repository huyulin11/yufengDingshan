package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.util.agv.msg.MsgCompare;
import com.kaifantech.util.agv.msg.PreventImpactCommand;

@Component
public class PIRoadService implements IPICtrlByMsgService {
	@Autowired
	private PICtrlOtherService piCtrlOtherService;

	@Autowired
	PICtrlVericalService piCtrlVericalService;

	@Autowired
	PICtrlParallelService piCtrlParallelService;

	public PreventImpactCommand check2AgvsByMsg(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		MsgCompare<AGVMsgBean> compare = new MsgCompare<AGVMsgBean>(msgOne, msgAnother);

		if (msgOne.isTaskfinished() || msgAnother.isTaskfinished()) {
			return this.warning(msgOne, msgAnother);
		}

		if (compare.isParallel()) {
			return piCtrlParallelService.checkWhenParallel(msgOne, msgAnother, compare);
		} else if (compare.isVertical()) {
			return piCtrlVericalService.checkWhenV(msgOne, msgAnother, compare);
		} else {
			return piCtrlOtherService.checkWhenOthers(msgOne, msgAnother, compare);
		}
	}

}
