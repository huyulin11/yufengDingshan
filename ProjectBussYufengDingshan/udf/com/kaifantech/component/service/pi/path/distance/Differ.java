package com.kaifantech.component.service.pi.path.distance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.IXYBean;
import com.kaifantech.component.dao.DistanceInfoDao;
import com.kaifantech.util.agv.msg.Point;
import com.ytgrading.util.AppTool;

@Component
@Scope("prototype")
public class Differ {

	private static Integer distanceUnsafe;
	private static Integer distanceWarning;
	private static Integer distanceRepeatPoint;
	private static Integer distanceUnsafeNonPath;
	private static Integer distanceWarningNonPath;

	private static Integer distanceForComputeNonRightAngleArea;
	private static Integer distanceForComputeRightAngleArea;

	private Boolean withPath = null;

	@Autowired
	private DistanceInfoDao distanceInfoDao;

	public Integer getDistanceUnsafe() {
		init();
		return withPath ? distanceUnsafe : distanceUnsafeNonPath;
	}

	public Integer getDistanceWarning() {
		init();
		return withPath ? distanceWarning : distanceWarningNonPath;
	}

	public Integer getDistanceRepeatPoint() {
		init();
		return distanceRepeatPoint;
	}

	public Integer getDistanceForComputeNonRightAngleArea() {
		init();
		return distanceForComputeNonRightAngleArea;
	}

	public Integer getDistanceForComputeRightAngleArea() {
		init();
		return distanceForComputeRightAngleArea;
	}

	public Integer getDistanceUnsafeNonPath() {
		init();
		return distanceUnsafeNonPath;
	}

	public Integer getDistanceWarningNonPath() {
		init();
		return distanceWarningNonPath;
	}

	public void init() {
		if (AppTool.isNull(distanceUnsafe) || AppTool.isNull(distanceWarning) || AppTool.isNull(distanceRepeatPoint)) {
			distanceUnsafe = distanceInfoDao.getDistanceUnsafe();
			distanceWarning = distanceInfoDao.getDistanceWarning();
			distanceRepeatPoint = distanceInfoDao.getDistanceRepeat();
		}

		if (AppTool.isNull(distanceForComputeNonRightAngleArea) || AppTool.isNull(distanceForComputeRightAngleArea)
				|| AppTool.isNull(distanceUnsafeNonPath) || AppTool.isNull(distanceWarningNonPath)) {
			distanceForComputeNonRightAngleArea = distanceInfoDao.getDistanceForComputeNonRightAngleArea();
			distanceForComputeRightAngleArea = distanceInfoDao.getDistanceForComputeRightAngleArea();
			distanceUnsafeNonPath = distanceInfoDao.getDistanceUnsafeNonPath();
			distanceWarningNonPath = distanceInfoDao.getDistanceWarningNonPath();
		}
	}

	public double diffPow(IXYBean msg1, IXYBean msg2) {
		return Math.pow((msg1.getX() - msg2.getX()), 2) + Math.pow((msg1.getY() - msg2.getY()), 2);
	}

	public double diff(IXYBean msg1, IXYBean msg2) {
		return Math.sqrt(Math.pow((msg1.getX() - msg2.getX()), 2) + Math.pow((msg1.getY() - msg2.getY()), 2));
	}

	public double diff(Point msg1, IXYBean msg2) {
		return Math.sqrt(Math.pow((msg1.getX() - msg2.getX()), 2) + Math.pow((msg1.getY() - msg2.getY()), 2));
	}

	public double diffX(IXYBean msg1, Integer x) {
		return Math.sqrt(Math.pow((msg1.getX() - x), 2));
	}

	public double diff(Integer a1, Integer a2) {
		return Math.abs(a1 - a2);
	}

	public double diffY(IXYBean msg1, Integer y) {
		return Math.sqrt(Math.pow((msg1.getY() - y), 2));
	}

	public double diffPowX(IXYBean msg1, IXYBean msg2) {
		return Math.pow((msg1.getX() - msg2.getX()), 2);
	}

	public double diffPowY(IXYBean msg1, IXYBean msg2) {
		return Math.pow((msg1.getY() - msg2.getY()), 2);
	}

	public double diffPowToUnsafe(IXYBean msg1, IXYBean msg2) {
		return diffPow(msg1, msg2) - Math.pow(getDistanceUnsafe(), 2);
	}

	public double diffPowToRepeat(IXYBean msg1, IXYBean msg2) {
		return diffPow(msg1, msg2) - Math.pow(getDistanceRepeatPoint(), 2);
	}

	public double diffPowToTotalSafe(IXYBean msg1, IXYBean msg2) {
		return diffPow(msg1, msg2) - Math.pow(getDistanceWarning(), 2);
	}

	public Boolean isWithPath() {
		return withPath;
	}

	public void setWithPath(Boolean withPath) {
		this.withPath = withPath;
	}
}
