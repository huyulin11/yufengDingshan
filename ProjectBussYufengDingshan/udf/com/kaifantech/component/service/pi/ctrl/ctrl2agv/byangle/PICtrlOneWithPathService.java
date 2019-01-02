package com.kaifantech.component.service.pi.ctrl.ctrl2agv.byangle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.IXYBean;
import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.component.service.pi.ctrl.PIMsgService;
import com.kaifantech.component.service.pi.path.distance.DistanceChecker;
import com.kaifantech.component.service.pi.path.info.TaskPathInfoService;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.agv.taskpath.DistanceStatus;
import com.kaifantech.util.constant.pi.TaskPathCtrlConstant;

@Component
public class PICtrlOneWithPathService {

	@Autowired
	private TaskPathInfoService taskPathInfoService;

	@Autowired
	private PICtrlNeitherWithPathService neitherWithPathService;

	@Autowired
	private DistanceChecker distanceChecker;

	@Autowired
	private PIMsgService piMsgService;

	public PreventImpactCommand check2Agvs(List<TaskPathInfoPointBean> pathOne, AGVMsgBean msgOne,
			AGVMsgBean msgAnother) {

		PreventImpactCommand commandByMsg = neitherWithPathService.check2AgvsByMsg(msgOne, msgAnother);
		if (commandByMsg.getDistanceStatus() == DistanceStatus.DANGEROUS) {
			piMsgService.danger(msgOne, msgAnother, TaskPathCtrlConstant.ONE_WITH_PATH,
					TaskPathCtrlConstant.NEITHER_WITH_PATH);
			return commandByMsg;
		}

		PreventImpactCommand command = new PreventImpactCommand();

		if (isDangerousOneWithPath(pathOne, msgOne, msgAnother)) {
			msgOne.setWithPath(true);
			piMsgService.danger(msgOne, msgAnother, TaskPathCtrlConstant.ONE_WITH_PATH);
			command.setDistanceStatus(DistanceStatus.DANGEROUS);
			command.getDangerMsgs().add(msgAnother);
			return command;
		} else if (isTotallySafeOneWithPath(pathOne, msgOne, msgAnother)) {
			command.setDistanceStatus(DistanceStatus.SAFE);
			command.getSafeMsgs().add(msgAnother);
			return command;
		} else {
			command.setDistanceStatus(DistanceStatus.WARNING);
			return command;
		}
	}

	/** 比较一方有路径记录，另一无记录-即时位置比对-是否需要做停止控制 */
	public <T extends IXYBean> boolean isDangerousOneWithPath(List<T> pathOne, IXYBean msgOne, IXYBean msgAnother) {
		return taskPathInfoService.findPathInMap(msgOne.getAGVId(), msgOne.getTaskid()).stream()
				.anyMatch((point) -> distanceChecker.isDangerous(point, msgAnother, true));
	}

	/** 比较一方有路径记录，另一无记录-即时位置比对-是否需要做启动控制 */
	public <T extends IXYBean> boolean isTotallySafeOneWithPath(List<T> pathOne, IXYBean msgOne, IXYBean msgAnother) {
		return pathOne.stream().allMatch((point) -> distanceChecker.isTotallySafe(point, msgAnother, true));
	}

}
