package com.kaifantech.component.service.pi.ctrl;

import java.text.DecimalFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.component.log.AgvStatusDBLogger;
import com.kaifantech.component.service.pi.path.distance.Differ;
import com.kaifantech.component.service.pi.path.distance.DistanceChecker;
import com.kaifantech.init.sys.SystemInitiator;
import com.kaifantech.util.agv.msg.Location;
import com.kaifantech.util.constant.pi.PICtrlConstant;
import com.kaifantech.util.constant.pi.TaskPathCtrlConstant;
import com.kaifantech.util.log.AppFileLogger;
import com.ytgrading.util.AppTool;

@Component
public class PIMsgService {

	@Autowired
	private DistanceChecker distanceChecker;

	@Autowired
	private AgvStatusDBLogger kaifantechDBLogger;

	@Autowired
	private Differ differ;

	private int safeTimes = 0;

	@Async
	public void printMsg(AGVMsgBean msgOne, AGVMsgBean msgAnother, boolean withPath, boolean withPathOne,
			boolean withPathAnother) {
		if (!SystemInitiator.alwaysTrue) {
			double distance = Math.sqrt(distanceChecker.getDiffer(withPath).diffPow(msgOne, msgAnother));
			String distanceStr = new DecimalFormat("#.##").format(distance);
			String patternOne = TaskPathCtrlConstant.getValue(withPathOne);
			String patternAnother = TaskPathCtrlConstant.getValue(withPathAnother);
			String msg = "控制：" + patternOne + msgOne.getAGVId() + "车与" + patternAnother + msgAnother.getAGVId() + "车"
					+ "距离：" + distanceStr;
			AppFileLogger.piLogs(msg);
			if (distance < distanceChecker.getDiffer(withPath).getDistanceUnsafe()) {
				safeTimes = 0;
			} else {
				if (safeTimes++ > 5) {
					safeTimes = 0;
					kaifantechDBLogger.warning("系统中agv正常运转，无需要进行安全控制车辆", 0, AgvStatusDBLogger.MSG_LEVEL_WARNING);
				}
			}
		}
	}

	@Async
	public void danger(AGVMsgBean msgOne, AGVMsgBean msgAnother, int occurredPathCtrlModel, int dangerPathCtrlModel,
			String tips) {
		safeTimes = 0;
		boolean withPath = TaskPathCtrlConstant.withPath(dangerPathCtrlModel);
		double distance = Math.sqrt(distanceChecker.getDiffer(withPath).diffPow(msgOne, msgAnother) / 100);
		String distanceStr = new DecimalFormat("#.##").format(distance);
		String msg = (occurredPathCtrlModel == dangerPathCtrlModel
				? PICtrlConstant.getValue(occurredPathCtrlModel) + "中"
				: PICtrlConstant.getValue(occurredPathCtrlModel) + "中发生"
						+ PICtrlConstant.getValue(dangerPathCtrlModel));

		if (dangerPathCtrlModel == TaskPathCtrlConstant.NEITHER_WITH_PATH) {
			msg = msg + "：" + msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "距离过近：" + distanceStr
					+ "米，系统将暂停两台车的运行，直到手动将冲突解决";
		} else if (dangerPathCtrlModel == TaskPathCtrlConstant.ONE_WITH_PATH) {
			int idWithPath, idNonPath;
			if (msgOne.isWithPath()) {
				idWithPath = msgOne.getAGVId();
				idNonPath = msgAnother.getAGVId();
			} else {
				idNonPath = msgOne.getAGVId();
				idWithPath = msgAnother.getAGVId();
			}
			msg = msg + "：" + idWithPath + "车路径中，部分点与" + idNonPath + "车" + "当前位置距离过近，系统将暂停" + idNonPath + "车的运行，" + "等待"
					+ idWithPath + "车任务执行结束，系统将自动开始" + idNonPath + "车的任务";
		} else if (dangerPathCtrlModel == TaskPathCtrlConstant.BOTH_WITH_PATH) {
			msg = msg + "：" + msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "靠近路线冲突区域，系统将停止其中一台车进行防撞控制。"
					+ "等待任务结束后，系统将自动开始另一台车的任务" + "（注意：如果两台车同时进入冲突区域，将会造成两台车同时停止工作）";
		}

		msg = msgOne.getAGVId() + "-" + msgAnother.getAGVId() + "冲突-" + msg + (AppTool.isNull(tips) ? "" : "-注：" + tips);
		String msg2 = "" + msgOne + msgAnother;
		kaifantechDBLogger.warning(msg, msg2, 0, AgvStatusDBLogger.MSG_LEVEL_WARNING);
	}

	@Async
	public void danger(AGVMsgBean msgOne, AGVMsgBean msgAnother, int occurredPathCtrlModel, int dangerPathCtrlModel) {
		this.danger(msgOne, msgAnother, occurredPathCtrlModel, dangerPathCtrlModel, null);
	}

	@Async
	public void danger(AGVMsgBean msgOne, AGVMsgBean msgAnother, int occurredPathCtrlModel) {
		danger(msgOne, msgAnother, occurredPathCtrlModel, occurredPathCtrlModel);
	}

	@Async
	public void danger(AGVMsgBean msgOne, AGVMsgBean msgAnother, int occurredPathCtrlModel, String tips) {
		danger(msgOne, msgAnother, occurredPathCtrlModel, occurredPathCtrlModel, tips);
	}

	@Async
	public void dangerInSameTarget(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		danger(msgOne, msgAnother, PICtrlConstant.MAINCTRL,
				msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "道路重叠，" + "（距离："
						+ differ.diffX(msgAnother, msgOne.xaxisOfTarget) + "）" + "，" + "需等待" + msgOne.getAGVId()
						+ "车驶出后方能正常通行" + "（msgOne.xaxisOfRoad:" + msgOne.xaxisOfTarget + ",msgAnother.xaxisOfRoad:"
						+ msgAnother.xaxisOfTarget + "）");
	}

	@Async
	public void dangerInSameTargetDangerTwo(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		danger(msgOne, msgAnother, PICtrlConstant.MAINCTRL,
				msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "道路重叠，且均处于中间冲突地带，停双车，需手工解决冲突！"
						+ "（msgOne.xaxisOfRoad:" + msgOne.xaxisOfTarget + ",msgAnother.xaxisOfRoad:"
						+ msgAnother.xaxisOfTarget + "）");
	}

	@Async
	public void dangerInSameTargetDangerNotMiddle(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		danger(msgOne, msgAnother, PICtrlConstant.MAINCTRL,
				msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "道路重叠，" + "停车（远离公共区域）"
						+ (!(msgOne.getCurrentLoacation() == Location.OTHERS) ? msgOne : msgAnother).getAGVId() + "！"
						+ "（msgOne.xaxisOfRoad:" + msgOne.xaxisOfTarget + ",msgAnother.xaxisOfRoad:"
						+ msgAnother.xaxisOfTarget + "）");
	}

	@Async
	public void dangerInSameTargetDangerFarawayOne(AGVMsgBean msgOne, AGVMsgBean msgAnother, int roadYaxis) {
		danger(msgOne, msgAnother, PICtrlConstant.MAINCTRL,
				msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "道路重叠，" + "且均处于中间冲突地带，停远离回归道路的车辆"
						+ (differ.diffY(msgOne, roadYaxis) > differ.diffY(msgAnother, roadYaxis) ? msgOne : msgAnother)
								.getAGVId()
						+ "！" + "（msgOne.xaxisOfRoad:" + msgOne.xaxisOfTarget + ",msgAnother.xaxisOfRoad:"
						+ msgAnother.xaxisOfTarget + "）");
	}

	@Async
	public void dangerInSameTarget(AGVMsgBean msgOne, AGVMsgBean msgAnother, AGVMsgBean stopOne) {
		danger(msgOne, msgAnother, PICtrlConstant.MAINCTRL,
				msgOne.getAGVId() + "车与" + msgAnother.getAGVId() + "车" + "，" + "停" + stopOne.getAGVId() + "车"
						+ "（msgOne.xaxisOfRoad:" + msgOne.xaxisOfTarget + ",msgAnother.xaxisOfRoad:"
						+ msgAnother.xaxisOfTarget + "）");
	}

	@Async
	public void dangerInClashArea(AGVMsgBean msgOne, AGVMsgBean msgAnother, AGVMsgBean stopOne) {
		danger(msgOne, msgAnother, PICtrlConstant.CLASHAREA, "停远离区域或非在区域内车：" + stopOne.getAGVId() + "车");
	}

	@Async
	public void dangerInClashAreaWhenSameTarger(AGVMsgBean msgOne, AGVMsgBean stopOne) {
		String tips = stopOne.getAGVId() + "车将进入冲突区域，" + "但其目的地被" + msgOne.getAGVId() + "车占用，" + "停"
				+ stopOne.getAGVId() + "车";
		danger(msgOne, stopOne, PICtrlConstant.CLASHAREA, tips);
	}

}
