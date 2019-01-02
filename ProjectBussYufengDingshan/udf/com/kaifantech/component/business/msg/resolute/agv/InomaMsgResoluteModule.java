package com.kaifantech.component.business.msg.resolute.agv;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.kaifantech.bean.iot.client.IotClientBean;
import com.kaifantech.bean.singletask.SingletaskBean;
import com.kaifantech.bean.singletask.SingletaskGroupBean;
import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.bean.wms.alloc.AllocItemInfoBean;
import com.kaifantech.component.business.msg.info.agv.IAgvMsgInfoModule;
import com.kaifantech.component.dao.agv.info.AgvInfoDao;
import com.kaifantech.component.dao.singletask.SingletaskDao;
import com.kaifantech.component.dao.taskexe.op.TaskexeOpDao;
import com.kaifantech.component.log.AgvStatusDBLogger;
import com.kaifantech.component.service.alloc.info.IAllocInfoService;
import com.kaifantech.component.service.alloc.status.IAllocStatusMgrService;
import com.kaifantech.component.service.iot.client.IotClientService;
import com.kaifantech.component.service.lap.LapInfoService;
import com.kaifantech.component.service.singletask.group.SingletaskGroupService;
import com.kaifantech.component.service.singletask.info.SingleTaskInfoService;
import com.kaifantech.component.service.taskexe.info.TaskexeInfoService;
import com.kaifantech.component.service.taskexe.oper.ITaskexeAddService;
import com.kaifantech.component.service.taskexe.status.ITaskexeStatusService;
import com.kaifantech.init.sys.qualifier.InomaSystemQualifier;
import com.kaifantech.init.sys.qualifier.SystemQualifier;
import com.kaifantech.util.constant.taskexe.TaskexeOpFlag;
import com.kaifantech.util.constant.taskexe.ctrl.AgvCtrlType.AgvMoveStatus;
import com.kaifantech.util.constant.taskexe.ctrl.AgvTaskType;
import com.kaifantech.util.thread.ThreadTool;
import com.ytgrading.util.AppTool;
import com.ytgrading.util.msg.AppMsg;

@Service(InomaSystemQualifier.AGV_MSG_RESOLUTE_MODULE)
public class InomaMsgResoluteModule implements IMsgResoluteModule {

	@Autowired
	private TaskexeOpDao taskexeTaskDao;

	@Autowired
	private TaskexeInfoService taskInfoService;

	@Autowired
	private IAllocInfoService allocInfoService;

	@Autowired
	private IAllocStatusMgrService allocService;

	@Autowired
	@Qualifier(SystemQualifier.DEFAULT_TASKEXE_ADD_SERVICE)
	private ITaskexeAddService taskexeService;

	@Autowired
	private ITaskexeStatusService taskexeStatusService;

	@Autowired
	private IAgvMsgInfoModule msgService;

	@Autowired
	private SingletaskDao singletaskDao;

	@Autowired
	private SingletaskGroupService singletaskGroupService;

	@Autowired
	private AgvStatusDBLogger kaifantechDBLogger;

	@Autowired
	private SingleTaskInfoService singleTaskInfoService;

	@Autowired
	private LapInfoService lapInfoService;

	@Autowired
	@Qualifier(SystemQualifier.DEFAULT_AGV_INFO_DAO)
	private AgvInfoDao agvInfoDao;

	@Autowired
	private IotClientService iotClientService;

	@Transactional(propagation = Propagation.REQUIRED)
	public void resoluteMsg() {
		for (IotClientBean agvBean : iotClientService.getAgvCacheList()) {
			TaskexeBean latestTaskexe = taskInfoService.getNotOverOneF(agvBean.getId());
			AppMsg msg = resoluteTaskexe(latestTaskexe);
			if (latestTaskexe != null) {
				if (!AppTool.isNull(msg)) {
					if (!AppTool.isNullStr(msg.getMsg())) {
						kaifantechDBLogger.info(msg.getMsg(), latestTaskexe.getAgvId());
					}
				}
			}
			ThreadTool.sleep(500);
		}
	}

	public AppMsg resoluteTaskexe(TaskexeBean latestTaskexe) {
		if (latestTaskexe != null) {
			if (AgvMoveStatus.CONTINUE.equals(agvInfoDao.getMoveStatus(latestTaskexe.getAgvId()))
					&& !TaskexeOpFlag.OVER.equals(latestTaskexe.getOpflag())) {
				SingletaskBean singletaskBean = singleTaskInfoService.getSingletask(latestTaskexe.getTaskid());
				if (!AgvTaskType.ZUHE_RENWU.equals(singletaskBean.getAllocOpType())) {
					AppMsg msg = resoluteGroupTask(latestTaskexe);
					if (msg.getCode() < 0) {
						return msg;
					}
				} else {
					AppMsg msg = resoluteTask(latestTaskexe, singletaskBean);
					if (msg.getCode() < 0) {
						return msg;
					}
				}
				return new AppMsg(0, "当前AGV任务执行结束！");
			}
		}
		return new AppMsg(0, "");
	}

	private AppMsg resoluteTask(TaskexeBean latestTaskexe, Object obj) {
		SingletaskBean singletaskBean = (SingletaskBean) obj;
		if (isSendToAgv(singletaskBean)) {
			if (!TaskexeOpFlag.SEND.equals(latestTaskexe.getOpflag())) {
				return new AppMsg(-1, "任务尚未发送，不能解析！");
			}
			if (!msgService.getLatestMsgBean(latestTaskexe.getAgvId()).isSuccessDone(latestTaskexe)) {
				return new AppMsg(-1, "任务：" + singletaskBean.getTaskText() + "，尚未执行结束！");
			}
		}

		AllocItemInfoBean allocItem = allocInfoService.getByTaskid(latestTaskexe.getTaskid());
		AppMsg msg = AgvTaskType.RECEIPT.equals(singletaskBean.getAllocOpType())
				? allocService.transferUpDone(allocItem) : allocService.transferDownDone(allocItem);
		if (msg.getCode() >= 0) {
			if (lapInfoService.getLapInUsed(latestTaskexe.getLapId())) {
				lapInfoService.setLapInUsed(latestTaskexe.getLapId(), false);
			}
			if (isSendToAgv(singletaskBean)) {
				taskexeStatusService.changeStatusWhenOver(singletaskBean.getId());
				taskexeTaskDao.overASendTask(latestTaskexe.getUuid());
			} else {
				taskexeTaskDao.overANormalTask(latestTaskexe.getUuid());
			}
			kaifantechDBLogger.warning(latestTaskexe.getAgvId() + "号AGV任务：" + singletaskBean.getTaskText() + "执行完毕！ ",
					latestTaskexe.getAgvId(), AgvStatusDBLogger.MSG_LEVEL_WARNING);
		} else {
			return msg;
		}
		return new AppMsg(0, "可以继续解析！");
	}

	private AppMsg resoluteGroupTask(TaskexeBean latestTaskexe) {
		List<TaskexeBean> latestTaskexeList = taskInfoService.getNewList();

		List<SingletaskBean> singletaskBeanList = null;
		String parentTaskid = "";
		for (TaskexeBean tmpTaskBean : latestTaskexeList) {
			List<SingletaskGroupBean> groupList = singletaskGroupService
					.getSingletaskGroupListByTask(tmpTaskBean.getTaskid());
			if (groupList != null && groupList.size() == 1) {
				parentTaskid = groupList.get(0).getParentTaskid();
				singletaskBeanList = singletaskDao.getSingletaskBeanListByGroup(parentTaskid);
			}
		}

		if (latestTaskexeList != null && latestTaskexeList.size() > 0) {
			if (singletaskBeanList != null && singletaskBeanList.size() > 0) {
				boolean flag = true;
				StringBuffer msgStr = new StringBuffer();
				msgStr.append("等待执行下列任务：");
				for (SingletaskBean tmpBean : singletaskBeanList) {
					if (!(latestTaskexeList.stream().filter((bean) -> bean.getTaskid().equals(tmpBean.getId()))
							.count() == 1)) {
						flag = false;
						msgStr.append(tmpBean.getTaskText() + ",");
					}
				}
				if (!flag) {
					return new AppMsg(-1, msgStr.append("执行完成后下达到AGV！").toString());
				}
				for (TaskexeBean tmpTaskBean : latestTaskexeList) {
					taskexeTaskDao.overANormalTask(tmpTaskBean.getUuid());
				}
				taskexeService.addTask(new TaskexeBean(parentTaskid, latestTaskexe.getAgvId(), 1));
			}
		}
		return new AppMsg(0, "可以继续解析！");
	}

	private boolean isSendToAgv(SingletaskBean singletaskBean) {
		if (AppTool.isNull(singletaskBean)) {
			return false;
		} else {
			return singletaskBean.getIsSendToAgv() == 1;
		}

	}
}
