package com.kaifantech.component.timer.msg.agv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.kaifantech.component.business.msg.resolute.agv.IMsgResoluteModule;
import com.kaifantech.init.sys.qualifier.SystemQualifier;
import com.kaifantech.util.seq.ThreadID;

@Component
@Lazy(false)
public class AgvMsgResoluteTimer {
	private static boolean isRunning = false;
	private static String timerType = "AGV消息解析器";

	@Autowired
	@Qualifier(SystemQualifier.DEFAULT_MSGRESOLUTE_MODULE)
	private IMsgResoluteModule msgResoluteMgr;

	// @Scheduled(cron = "0/2 * * * * ?")
	public void resolute() {
		if (!isRunning) {
			Thread.currentThread().setName(timerType + (ThreadID.number++));
			isRunning = true;
			try {
				msgResoluteMgr.resoluteMsg();
			} catch (Exception e) {
				e.printStackTrace();
			}
			isRunning = false;
		} else {
			isRunning = false;
		}
	}
}
