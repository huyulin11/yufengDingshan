package com.kaifantech.component.service.pi.path.op;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.component.dao.ControlInfoDao;
import com.kaifantech.component.dao.taskexe.TaskPathInfoDao;
import com.kaifantech.component.dao.taskexe.TaskPathMemoryDao;
import com.kaifantech.component.service.pi.path.distance.DistanceChecker;

@Component
public class TaskPathInfoOpService implements ITaskPathInfoOpService {

	@Autowired
	private TaskPathMemoryDao taskPathMemoryDao;

	@Autowired
	private DistanceChecker distanceChecker;

	@Autowired
	private TaskPathInfoDao taskPathInfoDao;

	@Autowired
	private ControlInfoDao controlInfoDao;

	@Override
	public synchronized void transToInfo(Integer agvId, String taskid) {
		List<TaskPathInfoPointBean> memPointList = taskPathMemoryDao.selectPath(agvId, taskid);
		List<TaskPathInfoPointBean> infoPointList = taskPathInfoDao.selectPath(agvId, taskid);

		if (memPointList == null || memPointList.size() == 0) {
			return;
		}

		if (infoPointList.size() < controlInfoDao.getMaxPointNumInPathInfo()) {
			List<TaskPathInfoPointBean> delPointList = new ArrayList<>();
			for (int i = 0; i < memPointList.size(); i++) {
				TaskPathInfoPointBean bean = memPointList.get(i);
				if (delPointList.contains(bean)) {
					continue;
				}
				for (int j = i + 1; j < memPointList.size(); j++) {
					TaskPathInfoPointBean bean2 = memPointList.get(j);
					if (distanceChecker.isRepeat(bean, bean2)) {
						if (!delPointList.contains(bean2)) {
							delPointList.add(bean2);
						}
					}
				}
			}

			memPointList.removeAll(delPointList);
			delPointList.clear();

			for (TaskPathInfoPointBean bean : infoPointList) {
				for (TaskPathInfoPointBean bean2 : memPointList) {
					if (distanceChecker.isRepeat(bean, bean2)) {
						if (!delPointList.contains(bean2)) {
							delPointList.add(bean2);
						}
					}
				}
			}
			memPointList.removeAll(delPointList);
			delPointList.clear();

			for (TaskPathInfoPointBean bean2 : memPointList) {
				taskPathInfoDao.addAPoint(bean2, bean2.getSecondToStart());
			}
		}

		taskPathMemoryDao.clearMemory(agvId, taskid);
	}
}
