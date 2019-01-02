package com.kaifantech.component.service.pi.path.intersection;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.bean.msg.agv.IXYBean;
import com.kaifantech.bean.msg.agv.TaskPathInfoPointBean;
import com.kaifantech.component.service.pi.ctrl.PIMsgService;
import com.kaifantech.component.service.pi.path.distance.DistanceChecker;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.agv.taskpath.DistanceStatus;
import com.kaifantech.util.agv.taskpath.Intersection;
import com.kaifantech.util.agv.taskpath.TaskPathInfoMap;
import com.kaifantech.util.constant.pi.TaskPathCtrlConstant;
import com.ytgrading.util.AppTool;

@Component
public class IntersectionService {

	@Autowired
	private DistanceChecker distanceChecker;

	private List<Intersection> intersectionList = new ArrayList<>();

	@Autowired
	private PIMsgService piMsgService;

	public void init() {
		intersectionList.clear();
	}

	private Intersection getIntersectionFromCC(IXYBean msgOne, IXYBean msgAnother) {
		Intersection intersection = new Intersection();
		List<Intersection> tmpIntersectionList = new ArrayList<>();
		intersectionList.stream().filter((i) -> i.same(msgOne, msgAnother)).forEach(tmpIntersectionList::add);
		intersection = (tmpIntersectionList != null && tmpIntersectionList.size() == 1) ? tmpIntersectionList.get(0)
				: null;

		if (AppTool.isNull(intersection)) {
			return null;
		}

		return intersection;
	}

	private Intersection getIntersection(TaskPathInfoMap bean1, TaskPathInfoMap bean2) {
		Intersection intersection = new Intersection();
		List<TaskPathInfoPointBean> tmpTaskPathInfoPointBeanList1 = new ArrayList<TaskPathInfoPointBean>();
		List<TaskPathInfoPointBean> tmpTaskPathInfoPointBeanList2 = new ArrayList<TaskPathInfoPointBean>();
		bean1.getPathList().stream().filter((point) -> isDangerousPointTo(bean2, point))
				.forEach(tmpTaskPathInfoPointBeanList1::add);
		bean2.getPathList().stream().filter((point) -> isDangerousPointTo(bean1, point))
				.forEach(tmpTaskPathInfoPointBeanList2::add);

		if (tmpTaskPathInfoPointBeanList1.size() > 0 && tmpTaskPathInfoPointBeanList2.size() > 0) {
			TaskPathInfoMap mapA = new TaskPathInfoMap();
			mapA.setTaskid(bean1.getTaskid());
			mapA.setAGVId(bean1.getAGVId());
			mapA.setPathList(tmpTaskPathInfoPointBeanList1);
			intersection.setMapA(mapA);
			TaskPathInfoMap mapB = new TaskPathInfoMap();

			mapB.setTaskid(bean2.getTaskid());
			mapB.setAGVId(bean2.getAGVId());
			mapB.setPathList(tmpTaskPathInfoPointBeanList2);
			intersection.setMapB(mapB);
			intersectionList.add(intersection);
		}
		return intersection;
	}

	public Intersection getIntersection(List<TaskPathInfoPointBean> pathOne, List<TaskPathInfoPointBean> pathAnother,
			IXYBean msgOne, IXYBean msgAnother) {
		if (AppTool.isNull(pathOne) || AppTool.isNull(pathAnother)) {
			return null;
		}

		Intersection intersection = getIntersectionFromCC(msgOne, msgAnother);

		if (AppTool.isNull(intersection)) {
			intersection = getIntersection(new TaskPathInfoMap(pathOne), new TaskPathInfoMap(pathAnother));
		}

		return intersection;
	}

	public boolean isDangerousPointTo(TaskPathInfoMap bean, TaskPathInfoPointBean point) {
		return bean.getPathList().stream().anyMatch((s) -> distanceChecker.isDangerous(s, point, true));
	}

	/** 比较双方均有路径记录-即时位置比对-是否需要做停止控制 */
	public PreventImpactCommand check2Agvs(List<TaskPathInfoPointBean> pathOne, List<TaskPathInfoPointBean> pathAnother,
			AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		PreventImpactCommand command = new PreventImpactCommand();

		Intersection intersection = getIntersection(pathOne, pathAnother, msgOne, msgAnother);

		if (AppTool.isNull(intersection)) {
			command.setDistanceStatus(DistanceStatus.SAFE);
			command.getSafeMsgs().add(msgOne);
			command.getSafeMsgs().add(msgAnother);
			return command;
		}

		boolean dangerFlagOne = intersection.getMapA().getPathList().stream()
				.anyMatch((point) -> distanceChecker.getDiffer(true).diffPowToUnsafe(point, msgOne) <= 0);
		boolean dangerFlagAnother = intersection.getMapB().getPathList().stream()
				.anyMatch((point) -> distanceChecker.getDiffer(true).diffPowToUnsafe(point, msgAnother) <= 0);

		if (dangerFlagOne || dangerFlagAnother) {
			command.setDistanceStatus(DistanceStatus.DANGEROUS);
			piMsgService.danger(msgOne, msgAnother, TaskPathCtrlConstant.BOTH_WITH_PATH);
			if (dangerFlagOne) {
				command.getDangerMsgs().add(msgOne);
			}
			if (dangerFlagAnother) {
				command.getDangerMsgs().add(msgAnother);
			}
			return command;
		}

		boolean safeFlagOne = pathOne.stream()
				.allMatch((point) -> distanceChecker.getDiffer(true).diffPowToTotalSafe(msgOne, point) > 0);
		boolean safeFlagAnother = pathAnother.stream()
				.allMatch((point) -> distanceChecker.getDiffer(true).diffPowToTotalSafe(point, msgAnother) > 0);
		if (safeFlagOne || safeFlagAnother) {
			command.setDistanceStatus(DistanceStatus.SAFE);
			if (safeFlagOne) {
				command.getSafeMsgs().add(msgOne);
			}
			if (safeFlagAnother) {
				command.getSafeMsgs().add(msgAnother);
			}
			return command;
		} else {
			command.setDistanceStatus(DistanceStatus.WARNING);
			return command;
		}
	}

}
