package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.info.agv.AGVBeanWithLocation;
import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.component.dao.agv.info.AgvInfoDao;
import com.kaifantech.component.service.pi.ctrl.PIMsgService;
import com.kaifantech.init.sys.qualifier.SystemQualifier;
import com.kaifantech.util.agv.msg.MsgCompare;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.constant.pi.detail.CLASH_AREA_INFO;
import com.kaifantech.util.constant.pi.detail.ClashArea;
import com.kaifantech.util.log.AppFileLogger;
import com.ytgrading.util.AppTool;

@Component
public class PICtrlClashAreaService implements IPICtrlByMsgService {

	@Autowired
	private PIMsgService piMsgService;

	@Autowired
	@Qualifier(SystemQualifier.DEFAULT_AGV_INFO_DAO)
	private AgvInfoDao agvDao;

	@Autowired
	PICtrlSameTargetService piCtrlSameTargetService;

	AGVBeanWithLocation agvOne;
	AGVBeanWithLocation agvAnother;

	private ClashArea clashArea = new ClashArea(CLASH_AREA_INFO.CLASH_AREA_1);

	public ClashArea getClashArea() {
		return clashArea;
	}

	public void setClashArea(ClashArea clashArea) {
		this.clashArea = clashArea;
	}

	public PreventImpactCommand check(AGVMsgBean msgOne, AGVMsgBean msgAnother, MsgCompare<AGVMsgBean> compare) {
		PreventImpactCommand command = new PreventImpactCommand();
		if (msgOne.isInTheXArea(clashArea) && msgAnother.isInTheXArea(clashArea)) {

			agvOne = agvDao.getAGVBeanWithLocation(msgOne.getAGVId());
			agvAnother = agvDao.getAGVBeanWithLocation(msgAnother.getAGVId());

			boolean isOneCloseToArea = msgOne.isCloseToArea(clashArea);
			boolean isAnotherCloseToArea = msgAnother.isCloseToArea(clashArea);

			boolean isOneInTheArea = msgOne.isInTheArea(clashArea);
			boolean isAnotherInTheArea = msgAnother.isInTheArea(clashArea);

			if (isOneCloseToArea && agvOne.getNextXaxis() == agvAnother.getCurrentXaxis()
					&& agvAnother.getInCurrentXaxis() == 1
					&& agvOne.getNextLocation() == agvAnother.getCurrentLocation()) {
				command.dangerous(msgOne).safe(msgAnother);
			}
			if (isAnotherCloseToArea && agvAnother.getNextXaxis() == agvOne.getCurrentXaxis()
					&& agvOne.getInCurrentXaxis() == 1 && agvAnother.getNextLocation() == agvOne.getCurrentLocation()) {
				command.dangerous(msgAnother).safe(msgOne);
			}

			String info = "msgOne:" + msgOne.getAGVId() + "," + "msgAnother:" + msgAnother.getAGVId() + ","
					+ "isOneCloseToArea:" + isOneCloseToArea + "," + "isAnotherCloseToArea:" + isAnotherCloseToArea
					+ "," + "isOneInTheArea:" + isOneInTheArea + "," + "isAnotherInTheArea:" + isAnotherInTheArea;
			if ((isOneCloseToArea && isAnotherInTheArea) || (isAnotherCloseToArea && isOneInTheArea)) {
				AppFileLogger.piError("---危险---" + info);
				piMsgService.dangerInClashArea(msgOne, msgAnother,
						msgOne.isCloseToArea(clashArea) ? msgOne : msgAnother);
				return command.dangerous(msgOne.isCloseToArea(clashArea) ? msgOne : msgAnother)
						.safe(!msgOne.isCloseToArea(clashArea) ? msgOne : msgAnother);
			} else {
				if (command.getDangerMsgs().size() > 0) {
					piMsgService.dangerInClashAreaWhenSameTarger(
							!command.getDangerMsgs().contains(msgOne) ? msgOne : msgAnother,
							command.getDangerMsgs().contains(msgOne) ? msgOne : msgAnother);
					return command;
				}

				if (isOneCloseToArea && isAnotherCloseToArea) {
					double distanceOfOneToArea = msgOne.getDistanceToArea(clashArea);
					double distanceOfAnotherToArea = msgAnother.getDistanceToArea(clashArea);
					String s = "," + "distanceOfOneToArea:" + distanceOfOneToArea + "," + "distanceOfAnotherToArea:"
							+ distanceOfAnotherToArea;
					AppFileLogger.piError("---危险---" + info + s);
					piMsgService.dangerInClashArea(msgOne, msgAnother,
							distanceOfOneToArea > distanceOfAnotherToArea ? msgOne : msgAnother);
					return command.dangerous(distanceOfOneToArea > distanceOfAnotherToArea ? msgOne : msgAnother)
							.safe(!(distanceOfOneToArea > distanceOfAnotherToArea) ? msgOne : msgAnother);
				} else if (isOneInTheArea && isAnotherInTheArea) {
					AppFileLogger.piError("---同时处在易冲突区域，使用其它模式防止冲突---");
					return null;
				} else {
					AppFileLogger.piLogs("---安全---" + info);
					return null;
				}
			}
		}

		command = piCtrlSameTargetService.check(msgOne, msgAnother, compare);
		if (!AppTool.isNull(command)) {
			return command;
		}

		return null;
	}

}
