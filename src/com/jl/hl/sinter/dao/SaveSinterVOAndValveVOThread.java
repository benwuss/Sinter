package com.jl.hl.sinter.dao;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.jl.hl.sinter.model.SinterVO;
import com.jl.hl.sinter.model.ValveVO;

public class SaveSinterVOAndValveVOThread extends Thread {

	private MySQLDAO dao = new MySQLDAO();

	private SinterVO sinterVO;

	private ArrayList<ValveVO> valveList = new ArrayList<ValveVO>();

	public SaveSinterVOAndValveVOThread() {

	}

	public void setDataToSave(SinterVO sinterVO, ArrayList<ValveVO> valveList) {
		this.sinterVO = sinterVO;
		this.valveList = valveList;
	}
	
	public void save(){
		SqlSession session = dao.getSession();
		if (sinterVO != null && sinterVO.getTempNorth().intValue() > 0) {
			session.insert("addSinter", sinterVO);
		}

		if (valveList != null && valveList.size() > 0) {
			for (ValveVO vo : valveList) {
				session.insert("addValve", vo);
			}
		}
		session.commit();
		session.close();
	}

	public void run() {
		save();
	}
}
