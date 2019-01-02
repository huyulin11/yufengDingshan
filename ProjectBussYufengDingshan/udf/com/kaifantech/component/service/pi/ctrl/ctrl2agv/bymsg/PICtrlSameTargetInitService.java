package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.info.agv.AGVBeanWithLocation;
import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.component.dao.agv.info.AgvInfoDao;
import com.kaifantech.component.service.pi.path.distance.Differ;
import com.kaifantech.init.sys.qualifier.SystemQualifier;
import com.kaifantech.util.constant.pi.detail.BASIC_INFO;
import com.kaifantech.util.constant.pi.detail.CLASH_AREA_INFO;

@Component
public class PICtrlSameTargetInitService {
	private AGVMsgBean msgInCurrentXOne;
	private AGVMsgBean msgAnother;

	AGVBeanWithLocation agvInCurrentXOne;
	AGVBeanWithLocation agvAnother;

	private double distanceDangerXY = 0;
	@SuppressWarnings("unused")
	private double distanceDangerYY = 0;

	public static int CLASH_MODEL_ZERO = 0;
	public static int CLASH_MODEL_ONE_IN_CURRENT = 1;
	public static int CLASH_MODEL_BOTH_IN_CURRENT = 2;

	private int clashModel = CLASH_MODEL_ZERO;
	int xaxis = 0;

	int referMainRoadXaxis = 0;

	@Autowired
	private Differ differ;

	@Autowired
	@Qualifier(SystemQualifier.DEFAULT_AGV_INFO_DAO)
	private AgvInfoDao agvDao;

	public void init(AGVMsgBean msgOne, AGVMsgBean msgAnother) {
		this.msgInCurrentXOne = msgOne;
		this.msgAnother = msgAnother;

		agvInCurrentXOne = agvDao.getAGVBeanWithLocation(msgInCurrentXOne.getAGVId());
		agvAnother = agvDao.getAGVBeanWithLocation(msgAnother.getAGVId());

		distanceDangerYY = CLASH_AREA_INFO.DISTANCE_SAFE_WHEN_IN_SAME_ROAD_Y_ONE + BASIC_INFO.addedDistance(msgOne);
		distanceDangerXY = msgAnother.isOnTheXaxis() ? CLASH_AREA_INFO.DISTANCE_SAFE_WHEN_IN_SAME_ROAD_XY
				: CLASH_AREA_INFO.DISTANCE_SAFE_WHEN_IN_SAME_ROAD_YY + BASIC_INFO.addedDistance(msgAnother);

	}

	public int getClashModel() {
		return clashModel;
	}

	public AGVBeanWithLocation getAGVInCurrentXOne() {
		return agvInCurrentXOne;
	}

	public AGVBeanWithLocation getAGVAnother() {
		return agvAnother;
	}

	public AGVMsgBean getMsgInCurrentXOne() {
		return msgInCurrentXOne;
	}

	public AGVMsgBean getMsgAnother() {
		return msgAnother;
	}

	private void swap() {
		if (!(agvInCurrentXOne.getInCurrentXaxis() == 1)) {
			{
				AGVBeanWithLocation temp = agvInCurrentXOne;
				agvInCurrentXOne = agvAnother;
				agvAnother = temp;
			}
			{
				AGVMsgBean temp = msgInCurrentXOne;
				msgInCurrentXOne = msgAnother;
				msgAnother = temp;
			}

			distanceDangerYY = CLASH_AREA_INFO.DISTANCE_SAFE_WHEN_IN_SAME_ROAD_Y_ONE
					+ BASIC_INFO.addedDistance(msgInCurrentXOne);
			distanceDangerXY = msgAnother.isOnTheXaxis() ? CLASH_AREA_INFO.DISTANCE_SAFE_WHEN_IN_SAME_ROAD_XY
					: CLASH_AREA_INFO.DISTANCE_SAFE_WHEN_IN_SAME_ROAD_YY + BASIC_INFO.addedDistance(msgAnother);

			xaxis = (msgInCurrentXOne.xaxisOfTarget + msgAnother.xaxisOfTarget) / 2;

			referMainRoadXaxis = (xaxis > BASIC_INFO.COORDINATE_X_OF_LAP2) ? BASIC_INFO.COORDINATE_Y_MAIN_ROAD_NORTH
					: ((xaxis < BASIC_INFO.COORDINATE_X_OF_LAP2) ? BASIC_INFO.COORDINATE_Y_MAIN_ROAD_SOUTH : 0);
		}
	}

	public boolean isInTheWarningArea() {
		if (!(agvInCurrentXOne.getInCurrentXaxis() == 1) && !(agvAnother.getInCurrentXaxis() == 1)) {
			return false;
		}

		if ((agvInCurrentXOne.getInCurrentXaxis() == 1) && (agvAnother.getInCurrentXaxis() == 1)) {
			clashModel = CLASH_MODEL_BOTH_IN_CURRENT;
			msgAnother.xaxisOfTarget = agvAnother.getCurrentXaxis();
			msgInCurrentXOne.xaxisOfTarget = agvInCurrentXOne.getCurrentXaxis();
			return differ.diff(agvInCurrentXOne.getCurrentXaxis(),
					agvAnother.getCurrentXaxis()) < CLASH_AREA_INFO.DISTANCE_ALLOWED_IN_SAME_ROAD;
		}

		swap();

		if (differ.diffX(msgAnother, agvAnother.getNextXaxis()) < distanceDangerXY
				&& differ.diffX(msgAnother, agvInCurrentXOne.getCurrentXaxis()) < distanceDangerXY) {
			msgAnother.xaxisOfTarget = agvAnother.getNextXaxis();
			msgInCurrentXOne.xaxisOfTarget = agvInCurrentXOne.getCurrentXaxis();
			clashModel = CLASH_MODEL_ONE_IN_CURRENT;
			return differ.diff(agvAnother.getNextXaxis(),
					agvInCurrentXOne.getCurrentXaxis()) < CLASH_AREA_INFO.DISTANCE_ALLOWED_IN_SAME_ROAD;
		}

		return false;
	}

	public boolean isInTheClash() {
		if (!isInTheWarningArea()) {
			return false;
		}

		if ((agvInCurrentXOne.getInCurrentXaxis() == 1) && (agvAnother.getInCurrentXaxis() == 1)) {
			return differ.diff(agvInCurrentXOne.getCurrentXaxis(),
					agvAnother.getCurrentXaxis()) < CLASH_AREA_INFO.DISTANCE_ALLOWED_IN_SAME_ROAD;
		}

		if (differ.diffX(msgAnother, agvAnother.getNextXaxis()) < CLASH_AREA_INFO.DISTANCE_ALLOWED_IN_SAME_ROAD
				&& differ.diffX(msgAnother,
						agvInCurrentXOne.getCurrentXaxis()) < CLASH_AREA_INFO.DISTANCE_ALLOWED_IN_SAME_ROAD) {
			return true;
		}

		return false;
	}

}