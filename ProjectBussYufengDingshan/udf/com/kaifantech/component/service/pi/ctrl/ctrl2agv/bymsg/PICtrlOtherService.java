package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.component.service.pi.ctrl.PIMsgService;
import com.kaifantech.component.service.pi.path.distance.Differ;
import com.kaifantech.util.agv.msg.MsgCompare;
import com.kaifantech.util.agv.msg.Point;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.constant.pi.PICtrlConstant;
import com.kaifantech.util.constant.pi.detail.OTHERS_1_POSITIVE_CLOSE_1_OTHERS;
import com.kaifantech.util.constant.pi.detail.OTHERS_1_POSITIVE_FARAWAY_1_OTHERS;
import com.kaifantech.util.constant.pi.detail.OTHERS_2_OTHERS;
import com.ytgrading.util.AppTool;

@Component
public class PICtrlOtherService implements IPICtrlByMsgService {
	@Autowired
	private Differ differ;

	@Autowired
	private PIMsgService piMsgService;

	private PreventImpactCommand command;

	public PreventImpactCommand checkWhenOthers(AGVMsgBean msgOne, AGVMsgBean msgAnother,
			MsgCompare<AGVMsgBean> compare) {
		boolean isOnePositiveAngle = msgOne.isPositiveAngle();
		boolean isAnotherPositiveAngle = msgAnother.isPositiveAngle();
		double distance = differ.diff(msgOne, msgAnother);
		/** --------------------------------------一正角，另一非正角-------------------------------------- **/
		if (isOnePositiveAngle == !isAnotherPositiveAngle) {
			AGVMsgBean positiveAngleBean = isOnePositiveAngle ? msgOne : msgAnother;
			AGVMsgBean unPositiveAngleBean = !isOnePositiveAngle ? msgOne : msgAnother;
			Point croosPoint = compare.getCrossPoint();
			if (AppTool.isNull(croosPoint)) {
				return command;
			}
			double distanceOfPositiveToPoint = differ.diff(croosPoint, positiveAngleBean);
			double distanceOfUnPositiveToPoint = differ.diff(croosPoint, unPositiveAngleBean);
			boolean isPositiveAngleBeanCloseToAnother = positiveAngleBean.isCloseTo(unPositiveAngleBean);
			/** ------------------正角靠近------------------ **/
			if (isPositiveAngleBeanCloseToAnother) {
				if (distanceOfPositiveToPoint < OTHERS_1_POSITIVE_CLOSE_1_OTHERS.DISTANCE_DANGEROUS
						&& distanceOfUnPositiveToPoint < OTHERS_1_POSITIVE_CLOSE_1_OTHERS.DISTANCE_DANGEROUS_OTHERS) {
					piMsgService.danger(msgOne, msgAnother, PICtrlConstant.OTHERS,
							"非正角行驶，其中" + positiveAngleBean.getAGVId() + "车正角，"
									+ unPositiveAngleBean.getAGVId() + "车非正角，距离为" + distance + "，在"
									+ OTHERS_1_POSITIVE_CLOSE_1_OTHERS.DISTANCE_DANGEROUS
									+ "，正角车远离非正角车时停非正角车，正角车靠近非正角车时停正角车：" + positiveAngleBean.getAGVId());
					command = this.dangerous(positiveAngleBean, null);
				} else {
					command = this.safe(msgOne, msgAnother);
				}
			}
			/** ------------------正角远离------------------ **/
			else {
				if (distance <= OTHERS_1_POSITIVE_FARAWAY_1_OTHERS.DISTANCE_DANGEROUS) {
					piMsgService.danger(msgOne, msgAnother, PICtrlConstant.OTHERS,
							"非正角行驶，其中" + positiveAngleBean.getAGVId() + "车正角，"
									+ unPositiveAngleBean.getAGVId() + "车非正角，距离为" + distance + "，小于"
									+ OTHERS_1_POSITIVE_FARAWAY_1_OTHERS.DISTANCE_DANGEROUS
									+ "，正角车远离非正角车时停非正角车，正角车靠近非正角车时停正角车：" + unPositiveAngleBean.getAGVId());
					command = this.dangerous(unPositiveAngleBean, null);
				} else {
					command = this.safe(msgOne, msgAnother);
				}
			}
		}
		/** --------------------------------------两个都非正角-------------------------------------- **/
		else {
			if (distance < OTHERS_2_OTHERS.DISTANCE_DANGEROUS) {
				piMsgService.danger(msgOne, msgAnother, PICtrlConstant.OTHERS,
						"非正角行驶，两车均非正角，距离为" + distance + "，小于" + OTHERS_2_OTHERS.DISTANCE_DANGEROUS + "，两车同时停止");
				command = this.dangerous(msgOne, msgAnother);
			} else if (distance < OTHERS_2_OTHERS.DISTANCE_DANGEROUS_ONE) {
				piMsgService.danger(msgOne, msgAnother, PICtrlConstant.OTHERS,
						"非正角行驶，两车均非正角，距离为" + distance + "，小于" + OTHERS_2_OTHERS.DISTANCE_DANGEROUS_ONE + "，随机停止任一agv");
				command = this.dangerous(msgOne, null);
			} else {
				command = this.safe(msgOne, msgAnother);
			}
		}
		return command;
	}

}
