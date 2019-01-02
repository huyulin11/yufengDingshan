package com.kaifantech.component.service.pi.ctrl.ctrl2agv.bymsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaifantech.bean.msg.agv.AGVMsgBean;
import com.kaifantech.component.service.pi.ctrl.PIMsgService;
import com.kaifantech.util.agv.msg.Direction;
import com.kaifantech.util.agv.msg.MsgCompare;
import com.kaifantech.util.agv.msg.PreventImpactCommand;
import com.kaifantech.util.constant.pi.PICtrlConstant;
import com.kaifantech.util.constant.pi.detail.BASIC_INFO;
import com.kaifantech.util.constant.pi.detail.PARALLEL_CONVERSE_IN_LINE;
import com.kaifantech.util.constant.pi.detail.PARALLEL_SYNTROPY_IN_LINE;

@Component
public class PICtrlParallelService implements IPICtrlByMsgService {

	@Autowired
	private PIMsgService piMsgService;

	public PreventImpactCommand checkWhenParallel(AGVMsgBean msgOne, AGVMsgBean msgAnother,
			MsgCompare<AGVMsgBean> compare) {
		double distanceInParallel = 0;
		double distanceInOtherAxis = 0;
		if (msgOne.isOnTheXaxis()) {
			distanceInParallel = compare.getDistanceOfY();
			distanceInOtherAxis = compare.getDistanceOfX();
		} else {
			distanceInParallel = compare.getDistanceOfX();
			distanceInOtherAxis = compare.getDistanceOfY();
		}

		/** --------------------------------------逆向行驶-------------------------------------- **/
		if (compare.isConverse()) {
			/** ------------------平行线距离较大------------------ **/
			if (distanceInParallel > BASIC_INFO.DISTANCE_IN_LINE_DANGEROUS) {
				return this.safe(msgOne, msgAnother);
			}

			/** ------------------平行线距离小，视为一条直线------------------ **/

			if (distanceInOtherAxis > PARALLEL_CONVERSE_IN_LINE.DISTANCE_DANGEROUS_ONE) {
				return this.safe(msgOne, msgAnother);
			}

			if (!msgOne.isCloseTo(msgAnother) && !msgAnother.isCloseTo(msgOne)) {
				return this.safe(msgOne, msgAnother);
			}

			if (distanceInOtherAxis <= PARALLEL_CONVERSE_IN_LINE.DISTANCE_DANGEROUS_ONE
					&& distanceInOtherAxis > PARALLEL_CONVERSE_IN_LINE.DISTANCE_DANGEROUS_TWO) {
				AGVMsgBean stopOne = null;
				if (Math.abs(msgOne.getY() - BASIC_INFO.COORDINATE_Y_MAIN_ROAD_SOUTH) > Math
						.abs(msgAnother.getY() - BASIC_INFO.COORDINATE_Y_MAIN_ROAD_SOUTH)) {
					stopOne = msgOne;
				} else {
					stopOne = msgAnother;
				}
				piMsgService.danger(msgOne, msgAnother, PICtrlConstant.PARALLEL,
						"相逆行驶，平行距离为" + distanceInParallel + "，垂直距离为" + distanceInOtherAxis + "，小于"
								+ PARALLEL_CONVERSE_IN_LINE.DISTANCE_DANGEROUS_ONE + ",停止远离回归干道车辆:"
								+ stopOne.getAGVId());
				return this.dangerous(stopOne, null).safe(stopOne.equals(msgOne) ? msgAnother : msgOne);
			}

			if (distanceInOtherAxis <= PARALLEL_CONVERSE_IN_LINE.DISTANCE_DANGEROUS_TWO) {
				piMsgService.danger(msgOne, msgAnother, PICtrlConstant.PARALLEL,
						"相逆行驶，平行距离为" + distanceInParallel + "，垂直距离为" + distanceInOtherAxis + "，小于"
								+ PARALLEL_CONVERSE_IN_LINE.DISTANCE_DANGEROUS_TWO + ",两车同时停止");
				return this.dangerous(msgOne, msgAnother);
			}
		}
		/** --------------------------------------同向行驶-------------------------------------- **/
		else {
			/** ------------------平行线距离相当大------------------ **/
			if (distanceInParallel > BASIC_INFO.DISTANCE_IN_LINE_SAFE) {
				return this.safe(msgOne, msgAnother);
			}
			/** ------------------平行线距离较大，但是同时转弯可能存在问题------------------ **/
			else if (distanceInParallel > BASIC_INFO.DISTANCE_IN_LINE_DANGEROUS) {
				if (distanceInOtherAxis < BASIC_INFO.DISTANCE_IN_LINE_SAFE
						&& msgOne.getDirection().equals(Direction.Y_POS)) {
					piMsgService.danger(msgOne, msgAnother, PICtrlConstant.PARALLEL,
							"同向行驶，非平行线距离小于" + BASIC_INFO.DISTANCE_IN_LINE_SAFE + "，垂直距离为" + distanceInOtherAxis
									+ "，停相对位置靠后车辆：" + compare.getBehindOne().getAGVId());
					return this.dangerous(compare.getBehindOne(), null)
							.safe(msgOne.equals(compare.getBehindOne()) ? msgAnother : msgOne);
				} else {
					return this.safe(msgOne, msgAnother);
				}
			}
			/** ------------------平行线距离相当小，视为在一条线------------------ **/
			else {
				if (distanceInOtherAxis > PARALLEL_SYNTROPY_IN_LINE.DISTANCE_DANGEROUS
						+ BASIC_INFO.addedDistance((!msgOne.equals(compare.getBehindOne()) ? msgAnother : msgOne))) {
					return this.safe(msgOne, msgAnother);
				} else {
					piMsgService.danger(msgOne, msgAnother, PICtrlConstant.PARALLEL,
							"同向行驶，平行距离小于" + PARALLEL_SYNTROPY_IN_LINE.DISTANCE_DANGEROUS + "，垂直距离为"
									+ distanceInOtherAxis + "，停相对位置靠后车辆：" + compare.getBehindOne().getAGVId());
					return this.dangerous(compare.getBehindOne(), null)
							.safe(msgOne.equals(compare.getBehindOne()) ? msgAnother : msgOne);
				}
			}

		}
		return null;
	}

}
