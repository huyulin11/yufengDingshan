package com.kaifantech.component.service.pi.path.distance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.IXYBean;
import com.ytgrading.util.AppTool;

@Component
public class DistanceCheckerByMsg {
	@Autowired
	private Differ differNonPath;

	public Differ getDiffer() {
		if (AppTool.isNull(differNonPath.isWithPath())) {
			differNonPath.setWithPath(false);
		}
		return differNonPath;
	}

	public boolean isDangerous(IXYBean msg1, IXYBean msg2) {
		return getDiffer().diffPowToUnsafe(msg1, msg2) <= 0;
	}

	public boolean isRepeat(IXYBean msg1, IXYBean msg2) {
		return getDiffer().diffPowToRepeat(msg1, msg2) <= 0;
	}

	public boolean isTotallySafe(IXYBean msg1, IXYBean msg2) {
		return getDiffer().diffPowToTotalSafe(msg1, msg2) > 0;
	}

}
