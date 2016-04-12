package com.jl.hl.sinter.dao;

import org.apache.ibatis.session.SqlSession;

import com.jl.hl.sinter.model.RunningVO;

public class RecordRunningTimeThread extends Thread {

	RunningVO runningVO = null;
	
	String startTime = "";
	
	String endTime = "";

	public void setData(long lastTime, long mins, int mode) {
		runningVO = new RunningVO(TimeUtil.getTimeFormat(lastTime),
				TimeUtil.genTimeID(), mins, mode);
		startTime = TimeUtil.getTimeFormat3(lastTime);
	    endTime = TimeUtil.getTimeFormat3(System.currentTimeMillis());
	}

	public void run() {
		MySQLDAO db = new MySQLDAO();
		SqlSession session = db.getSession();
		session.insert("addRunning", runningVO);
		AnalyzeThis analyze = new AnalyzeThis();
		analyze.setSqlSession(session);
		analyze.analyzeResult(startTime, endTime);
		session.commit();
		session.close();
	}
}
