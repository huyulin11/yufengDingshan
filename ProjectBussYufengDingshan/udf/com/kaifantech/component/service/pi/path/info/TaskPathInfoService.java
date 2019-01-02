package com.kaifantech.component.service.pi.path.info;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.component.dao.taskexe.TaskPathInfoDao;
import com.kaifantech.util.agv.taskpath.TaskPathInfoMap;
import com.ytgrading.util.AppTool;

@Component
public class TaskPathInfoService {
	@Autowired
	private TaskPathInfoDao taskPathInfoDao;

	private List<TaskPathInfoMap> taskPathInfoMapList = new ArrayList<TaskPathInfoMap>();

	public List<TaskPathInfoPointBean> getPathFromDB(TaskexeBean bean) {
		return getPathFromDB(bean.getAgvId(), bean.getTaskid());
	}

	public List<TaskPathInfoPointBean> getPathFromDB(Integer agvId, String taskid) {
		TaskPathInfoMap pathMap = getPathMap(agvId, taskid);
		return AppTool.isNull(pathMap) ? null : pathMap.getPathList();
	}

	public TaskPathInfoMap getPathMap(Integer agvId, String taskid) {
		List<TaskPathInfoPointBean> msg = taskPathInfoDao.selectPath(agvId, taskid);
		if (msg != null && msg.size() > 0) {
			TaskPathInfoMap taskPathInfoMap = new TaskPathInfoMap();
			taskPathInfoMap.setAGVId(agvId);
			taskPathInfoMap.setTaskid(taskid);
			taskPathInfoMap.setPathList(msg);
			taskPathInfoMapList.add(taskPathInfoMap);
			return taskPathInfoMap;
		}
		return null;
	}

	public List<TaskPathInfoPointBean> findPathInMap(Integer agvId, String taskid) {
		List<TaskPathInfoMap> tmpTaskPathInfoMapList = new ArrayList<TaskPathInfoMap>();
		taskPathInfoMapList.stream().filter((map) -> map.getAGVId().equals(agvId) && map.getTaskid().equals(taskid))
				.forEach(tmpTaskPathInfoMapList::add);
		return (tmpTaskPathInfoMapList != null && tmpTaskPathInfoMapList.size() == 1)
				? tmpTaskPathInfoMapList.get(0).getPathList() : null;
	}

	public List<TaskPathInfoPointBean> findPathInMap(TaskexeBean taskexeBean) {
		List<TaskPathInfoPointBean> tmpList = findPathInMap(taskexeBean.getAgvId(), taskexeBean.getTaskid());
		return tmpList == null ? getPathFromDB(taskexeBean) : tmpList;
	}

}
