package com.kaifantech.component.business.agv.msg.road;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.bean.wms.alloc.AllocationAreaInfoBean;
import com.kaifantech.bean.wms.alloc.AllocColumnInfoBean;
import com.kaifantech.component.service.alloc.area.AllocAreaService;
import com.kaifantech.component.service.alloc.column.AllocColumnService;

@Service
public class VerticalRoadService {

	@Autowired
	private AllocColumnService allocColumnService;

	@Autowired
	private AllocAreaService allocAreaService;

	public VerticalRoad getRoadNextOutFromInit(TaskexeBean latestTaskexe) {
		VerticalRoad init;
		if (latestTaskexe.getAgvId().equals(4)) {
			init = VerticalRoad.getRoadOfLap(latestTaskexe.getLapId());
			init.setNeedInDeep(true);
		} else {
			init = getRoadOfAlloc(latestTaskexe);
		}
		return init;
	}

	public VerticalRoad getRoadOfAlloc(TaskexeBean latestTaskexe) {
		AllocColumnInfoBean column = allocColumnService
				.getBeanByTaskid(latestTaskexe.getTaskid());
		AllocationAreaInfoBean area = allocAreaService.getAllocationAreaInfoBeanByAreaId(column.getAreaId());
		VerticalRoad init = new VerticalRoad(column.getXaxis(), area.getLocation());
		init.setNeedInDeep(false);
		return init;
	}

	public List<VerticalRoad> getRoadsMayAppear(TaskexeBean latestTaskexe) {
		List<VerticalRoad> roads = new ArrayList<>();
		roads.add(VerticalRoad.getRoadOfInitPlace(latestTaskexe));
		VerticalRoad road;
		road = getRoadNextOutFromInit(latestTaskexe);
		if (!roads.contains(road)) {
			roads.add(road);
		}
		road = getRoadOfAlloc(latestTaskexe);
		if (!roads.contains(road)) {
			roads.add(road);
		}
		return roads;
	}
}
