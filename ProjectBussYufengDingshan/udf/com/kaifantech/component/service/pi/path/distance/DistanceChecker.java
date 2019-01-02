package com.kaifantech.component.service.pi.path.distance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.IXYBean;
import com.kaifantech.util.constant.pi.detail.BASIC_INFO;
import com.ytgrading.util.AppTool;

@Component
public class DistanceChecker {
	@Autowired
	private Differ differNonPath;

	@Autowired
	private Differ differWithPath;

	public Differ getDiffer(boolean withPath) {
		if (AppTool.isNull(differNonPath.isWithPath()) || AppTool.isNull(differWithPath.isWithPath())) {
			differNonPath.setWithPath(false);
			differWithPath.setWithPath(true);
		}
		return withPath ? differWithPath : differNonPath;
	}

	/** 单边有路径，另外一边无路径-即时位置比对-是否需要做停止控制 */
	public boolean isDangerous(List<IXYBean> path1, IXYBean msg1, IXYBean msg2, boolean withPath) {
		return path1.stream().anyMatch((point) -> getDiffer(withPath).diffPowToUnsafe(point, msg2) <= 0);
	}

	public boolean isDangerous(IXYBean msg1, IXYBean msg2, boolean withPath) {
		return getDiffer(withPath).diffPowToUnsafe(msg1, msg2) <= 0;
	}

	public boolean isRepeat(IXYBean msg1, IXYBean msg2) {
		if (Math.abs(msg1.getAngle() - msg2.getAngle()) > BASIC_INFO.ANGLE_ALLOWED_DEVIATION) {
			return false;
		}
		return getDiffer(false).diffPowToRepeat(msg1, msg2) <= 0;
	}

	/** 比较双方均无路径记录-即时位置比对-是否需要做启动控制 */
	public boolean isTotallySafe(IXYBean msg1, IXYBean msg2, boolean withPath) {
		return getDiffer(withPath).diffPowToTotalSafe(msg1, msg2) > 0;
	}

	/** 单边有路径，另外一边无路径-即时位置比对-是否需要做启动控制 */
	public boolean isTotallySafe(List<IXYBean> path1, IXYBean msg1, IXYBean msg2, boolean withPath) {
		return path1.stream().allMatch((point) -> getDiffer(withPath).diffPowToTotalSafe(point, msg2) > 0);
	}

}
