package com.kaifantech.bean.msg.agv.yufeng;

import com.kaifantech.bean.msg.agv.AGVMsgBean;

public class YufengAGVMsgBean implements Cloneable {
	private String latitude;
	private String longitude;
	private String heading;
	private String quantityofelectric;
	private String speed;
	private String vehiclestatu;
	private String workpattern;
	private String filename;
	private String taskstage;
	private String taskresponse;
	private String battery;
	private String task;
	private String taskIsfinished;
	private String error;

	public YufengAGVMsgBean() {
	}

	public YufengAGVMsgBean(String msg) {
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getQuantityofelectric() {
		return quantityofelectric;
	}

	public void setQuantityofelectric(String quantityofelectric) {
		this.quantityofelectric = quantityofelectric;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getVehiclestatu() {
		return vehiclestatu;
	}

	public void setVehiclestatu(String vehiclestatu) {
		this.vehiclestatu = vehiclestatu;
	}

	public String getWorkpattern() {
		return workpattern;
	}

	public void setWorkpattern(String workpattern) {
		this.workpattern = workpattern;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTaskstage() {
		return taskstage;
	}

	public void setTaskstage(String taskstage) {
		this.taskstage = taskstage;
	}

	public String getTaskresponse() {
		return taskresponse;
	}

	public void setTaskresponse(String taskresponse) {
		this.taskresponse = taskresponse;
	}

	public AGVMsgBean toAGVMsgBean() {
		AGVMsgBean tmpBean = new AGVMsgBean();
		tmpBean.setX(Double.parseDouble(longitude) * 1000000);
		tmpBean.setY(Double.parseDouble(latitude) * 1000000);
		tmpBean.setSpeed(Double.parseDouble(speed));
		tmpBean.setError(error);
		tmpBean.setTask(task);
		tmpBean.setTaskIsfinished(taskIsfinished);
		tmpBean.setBattery(battery + "%");
		return tmpBean;
	}

	public String getBattery() {
		return battery;
	}

	public void setBattery(String battery) {
		this.battery = battery;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getTaskIsfinished() {
		return taskIsfinished;
	}

	public void setTaskIsfinished(String taskIsfinished) {
		this.taskIsfinished = taskIsfinished;
	}
}
