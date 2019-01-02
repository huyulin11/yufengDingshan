package com.kaifantech.component.service.pi.ctrl.ctrl2agv.byangle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg.PICtrlByMsgService;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.agv.taskpath.DistanceStatus;
import com.ytgrading.util.AppTool;

@Component
public class PICtrlService {

	@Autowired
	private PICtrlNeitherWithPathService neitherWithPathService;

	@Autowired
	private PICtrlOneWithPathService oneWithPathService;

	@Autowired
	private PICtrlBothWithPathService bothWithPathService;

	@Autowired
	private PICtrlByMsgService ctrlService;

	public PreventImpactCommand check2Agvs(List<TaskPathInfoPointBean> pathOne, List<TaskPathInfoPointBean> pathAnother,
			AGVMsgBean msgOne, AGVMsgBean msgAnother, TaskexeBean taskexeBeanOne,
			TaskexeBean taskexeBeanAnother) {
		/** --------------------------------------两台AGV都没有任务-------------------------------------- **/
		/** 如果两台AGV均无任务在执行，直接返回安全 */
		if (AppTool.isNull(taskexeBeanOne) && AppTool.isNull(taskexeBeanAnother)) {
			PreventImpactCommand command = new PreventImpactCommand();
			command.setDistanceStatus(DistanceStatus.SAFE);
			command.getSafeMsgs().add(msgOne);
			command.getSafeMsgs().add(msgAnother);
			return command;
		}
		return ctrlService.check2AgvsByMsg(msgOne, msgAnother);
		// return checkOnePatten(pathOne, pathAnother, msgOne, msgAnother,
		// taskexeBeanOne, taskexeBeanAnother);
	}

	@SuppressWarnings("unused")
	private PreventImpactCommand checkOnePatten(List<TaskPathInfoPointBean> pathOne,
			List<TaskPathInfoPointBean> pathAnother, AGVMsgBean msgOne, AGVMsgBean msgAnother,
			TaskexeBean taskexeBeanOne, TaskexeBean taskexeBeanAnother) {
		/** ----------------------------------------一台AGV有任务---------------------------------------- **/
		/** 如果至少有一个不处在任务执行状态，仅用AGV的消息作为控制条件 */
		if (AppTool.isNull(taskexeBeanOne) == !AppTool.isNull(taskexeBeanAnother)) {
			return neitherWithPathService.check2AgvsByMsg(msgOne, msgAnother);
		}

		/** ---------------------------------------两台AGV都有任务--------------------------------------- **/
		/** 如果两台AGV执行的任务均无路线记录，仅用AGV的消息作为控制条件 */
		if (AppTool.isNull(pathOne) && AppTool.isNull(pathAnother)) {
			return neitherWithPathService.check2AgvsByMsg(msgOne, msgAnother);
		} else
		/** 如果两台AGV仅有一台有路线记录，。。。 */
		if (AppTool.isNull(pathOne) == !AppTool.isNull(pathAnother)) {
			if (AppTool.isNull(pathOne)) {
				return oneWithPathService.check2Agvs(pathAnother, msgAnother, msgOne);
			} else {
				return oneWithPathService.check2Agvs(pathOne, msgOne, msgAnother);
			}
		} else
		/** 如果两台AGV均有路线记录，。。。 */
		{
			return bothWithPathService.check2Agvs(pathOne, pathAnother, msgOne, msgAnother);
		}
	}
}
