package com.kaifantech.component.business.agv.msg.road;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kaifantech.bean.info.agv.AgvBean;
import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.component.business.msg.info.agv.IAgvMsgInfoModule;
import com.kaifantech.component.dao.agv.info.AgvAxisDao;
import com.kaifantech.component.dao.agv.info.AgvInfoDao;
import com.kaifantech.component.service.pi.path.distance.Differ;
import com.kaifantech.init.sys.qualifier.SystemQualifier;
import com.kaifantech.util.agv.msg.Direction;
import com.kaifantech.util.constant.pi.detail.CLASH_AREA_INFO;

@Service
public class RoadsRecordService {

	@Autowired
	@Qualifier(SystemQualifier.DEFAULT_AGV_INFO_DAO)
	private AgvInfoDao agvDao;

	@Autowired
	private AgvAxisDao agvOpDao;

	@Autowired
	private IAgvMsgInfoModule msgFromAGVService;

	@Autowired
	private Differ differ;

	@Autowired
	private VerticalRoadService verticalRoadService;

	@Async
	public void changeTarget(TaskexeBean latestTaskexe) {
		AGVMsgBean agvMsgBean = msgFromAGVService.getLatestMsgBean(latestTaskexe.getAgvId());

		AgvBean agvBean = agvDao.get(latestTaskexe.getAgvId());

		List<VerticalRoad> roads = verticalRoadService.getRoadsMayAppear(latestTaskexe);
		for (int i = 0; i < roads.size(); i++) {
			VerticalRoad currentRoad = roads.get(i);
			VerticalRoad nextRoad = i + 1 >= roads.size() ? roads.get(0) : roads.get(i + 1);
			boolean isNeedInDeep = false;
			if (currentRoad.isNeedInDeep()) {
				isNeedInDeep = true;
			}
			if (agvMsgBean.isOnTheYaxisStrict()) {
				if (differ.diffX(agvMsgBean,
						currentRoad.getXaxis()) < CLASH_AREA_INFO.DISTANCE_IN_TARGET_ALLOWED_DEVIATION
						&& (currentRoad.getLocation() == agvMsgBean.getCurrentLoacation() || !isNeedInDeep)) {
					doChange(agvBean, agvMsgBean, currentRoad, nextRoad);
				}
			}
		}

		doLeaveTarget(agvBean, agvMsgBean);
	}

	private void doChange(AgvBean agvBean, AGVMsgBean agvMsgBean, VerticalRoad currentRoad, VerticalRoad nextRoad) {
		agvOpDao.changeNextXaxis(agvBean.getId(), nextRoad.getXaxis(), nextRoad.getLocation());
		agvOpDao.changeCurrentXaxis(agvBean.getId(), currentRoad.getXaxis(), currentRoad.getLocation());
	}

	private void doLeaveTarget(AgvBean agvBean, AGVMsgBean agvMsgBean) {
		if (!(Direction.Y_NEG.equals(agvMsgBean.getDirection()) || Direction.Y_POS.equals(agvMsgBean.getDirection()))) {
			agvOpDao.leaveCurrentXaxis(agvBean.getId());
		}
	}

}
