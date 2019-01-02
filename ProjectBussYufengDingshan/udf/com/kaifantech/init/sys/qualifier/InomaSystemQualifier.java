package com.kaifantech.init.sys.qualifier;

import com.kaifantech.init.sys.ServicePrefix;

public class InomaSystemQualifier {
	public static final String AGV_CMD_TASK_MODULE = ServicePrefix.INOMA + "AgvCmdTask" + "Module";
	public static final String AGV_CMD_CTRL_MODULE = ServicePrefix.INOMA + "AgvCmdCtrl" + "Module";
	public static final String AGV_MSG_RESOLUTE_MODULE = ServicePrefix.INOMA + "MsgResolute" + "Module";
	public static final String AGV_MSG_INFO_MODULE = ServicePrefix.INOMA + "AgvMsgInfo" + "Module";

	public static final String AGV_CLIENT_WORKER = ServicePrefix.INOMA + "AgvClientWorker";

	public static final String AGV_SIMULATOR_MGR = ServicePrefix.INOMA + "AgvSimulatorMgr";

	public static final String TASKEXE_ADD_SERVICE = ServicePrefix.INOMA + "TaskexeAddService";

	public static final String ALLOC_CHECK_SERVICE = ServicePrefix.WMS + "AllocCheckService";
	public static final String ALLOC_SERVICE = ServicePrefix.WMS + "AllocService";

	public static final String TASKEXE_CHECK_SERVICE = ServicePrefix.WMS + "TaskexeCheckService";

}
