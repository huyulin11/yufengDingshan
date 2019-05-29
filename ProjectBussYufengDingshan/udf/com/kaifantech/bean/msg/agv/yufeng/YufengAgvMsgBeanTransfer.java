package com.kaifantech.bean.msg.agv.yufeng;

import com.calculatedfun.util.AppTool;

public class YufengAgvMsgBeanTransfer {
	public static YufengAGVMsgBean transToBean(Integer agvId, String sFromAGV, YufengAGVMsgBean agvMsgBean) {
		try {
			if (AppTool.isNull(agvId)) {
				return null;
			}

			if (AppTool.isNull(sFromAGV)) {
				return new YufengAGVMsgBean();
			}
			sFromAGV = sFromAGV.trim();

			String latitude = "";
			String longitude = "";
			String heading = "";
			String quantityofelectric = "";
			String speed = "";
			String battery = "";
			String vehiclestatu = "";
			String workpattern = "";
			String filename = "";
			String taskstage = "";
			String taskresponse = "";
			String task = "";
			String taskIsfinished = "";
			String error = "";

			if (sFromAGV.toString().trim().indexOf("error=") >= 0) {
				int start = sFromAGV.indexOf("error=") + "error=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				error = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("task_isfinished=") >= 0) {
				int start = sFromAGV.indexOf("task_isfinished=") + "task_isfinished=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				taskIsfinished = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("latitude=") >= 0) {
				int start = sFromAGV.indexOf("latitude=") + "latitude=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				latitude = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("task=") >= 0) {
				int start = sFromAGV.indexOf("task=") + "task=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				task = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("longitude=") >= 0) {
				int start = sFromAGV.indexOf("longitude=") + "longitude=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				longitude = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("heading=") >= 0) {
				int start = sFromAGV.indexOf("heading=") + "heading=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				heading = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("quantityofelectric=") >= 0) {
				int start = sFromAGV.indexOf("quantityofelectric=") + "quantityofelectric=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				quantityofelectric = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("speed=") >= 0) {
				int start = sFromAGV.indexOf("speed=") + "speed=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				speed = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("battery=") >= 0) {
				int start = sFromAGV.indexOf("battery=") + "battery=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				battery = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("vehiclestatu=") >= 0) {
				int start = sFromAGV.indexOf("vehiclestatu=") + "vehiclestatu=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				vehiclestatu = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("workpattern=") >= 0) {
				int start = sFromAGV.indexOf("workpattern=") + "workpattern=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				workpattern = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("filename=") >= 0) {
				int start = sFromAGV.indexOf("filename=") + "filename=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				filename = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("taskstage=") >= 0) {
				int start = sFromAGV.indexOf("taskstage=") + "taskstage=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				taskstage = sFromAGV.substring(start, start + end);
			}
			if (sFromAGV.toString().trim().indexOf("taskresponse=") >= 0) {
				int start = sFromAGV.indexOf("taskresponse=") + "taskresponse=".length();
				int end = sFromAGV.substring(start).indexOf(";");
				taskresponse = sFromAGV.substring(start, start + end);
			}

			agvMsgBean.setTask(task);
			agvMsgBean.setError(error);
			agvMsgBean.setTaskIsfinished(taskIsfinished);
			agvMsgBean.setLatitude(latitude);
			agvMsgBean.setLongitude(longitude);
			agvMsgBean.setHeading(heading);
			agvMsgBean.setQuantityofelectric(quantityofelectric);
			agvMsgBean.setSpeed(speed);
			agvMsgBean.setBattery(battery);
			agvMsgBean.setVehiclestatu(vehiclestatu);
			agvMsgBean.setWorkpattern(workpattern);
			agvMsgBean.setFilename(filename);
			agvMsgBean.setTaskstage(taskstage);
			agvMsgBean.setTaskresponse(taskresponse);

			return agvMsgBean;
		} catch (Exception e) {
			return null;
		}
	}
}
