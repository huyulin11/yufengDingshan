package com.kaifantech.component.business.agv.msg.road;

import com.kaifantech.bean.taskexe.TaskexeBean;
import com.kaifantech.util.agv.msg.Location;
import com.kaifantech.util.constant.pi.detail.BASIC_INFO;
import com.ytgrading.util.AppTool;

public class VerticalRoad {

	private int initXaxis;
	private int initLocation;
	private boolean isNeedInDeep = false;

	public VerticalRoad(int initXaxis2, int initLocation2) {
		this.setInitXaxis(initXaxis2);
		this.setInitLocation(initLocation2);
	}

	public static VerticalRoad getRoadOfLap(int lapId) {
		int initXaxis = 0;
		int initLocation = Location.Y_NEG;
		if (lapId == 1) {
			initXaxis = BASIC_INFO.COORDINATE_X_OF_LAP1;
		}
		if (lapId == 2) {
			initXaxis = BASIC_INFO.COORDINATE_X_OF_LAP2;
		}
		if (lapId == 3) {
			initXaxis = BASIC_INFO.COORDINATE_X_OF_LAP3;
		}
		return new VerticalRoad(initXaxis, initLocation);
	}

	public static VerticalRoad getRoadOfInitPlace(TaskexeBean latestTaskexe) {
		VerticalRoad init;
		if (latestTaskexe.getAgvId().equals(4)) {
			init = new VerticalRoad(Location.Y_POS, BASIC_INFO.COORDINATE_X_OF_FORKLIFT4);
		} else {
			init = getRoadOfLap(latestTaskexe.getLapId());
		}
		init.setNeedInDeep(true);
		return init;
	}

	public int getXaxis() {
		return initXaxis;
	}

	private void setInitXaxis(int initXaxis) {
		this.initXaxis = initXaxis;
	}

	public int getLocation() {
		return initLocation;
	}

	private void setInitLocation(int initLocation) {
		this.initLocation = initLocation;
	}

	public boolean isNeedInDeep() {
		return isNeedInDeep;
	}

	public void setNeedInDeep(boolean isNeedInDeep) {
		this.isNeedInDeep = isNeedInDeep;
	}

	public String toString() {
		return "initXaxis:" + initXaxis + ",initLocation:" + initLocation;
	}

	public boolean equals(Object anotherOne) {
		if (!(anotherOne instanceof VerticalRoad)) {
			return false;
		}
		VerticalRoad bean = ((VerticalRoad) anotherOne);
		if (AppTool.isNullObj(bean)) {
			return false;
		}
		if (initXaxis == bean.initXaxis && initLocation == bean.initLocation && isNeedInDeep == bean.isNeedInDeep) {
			return true;
		}
		return false;
	}
}
