package com.jl.hl.sinter;

import com.jl.hl.sinter.dao.MySQLDAO;
import com.jl.hl.sinter.model.SinterVO;
import com.jl.hl.sinter.plc.s7.S7Service;

public class GetFromPLCAndSave {

	private MySQLDAO dao;

	private S7Service s7400;

	public GetFromPLCAndSave() {
		dao = new MySQLDAO();
		s7400 = new S7Service();
	}

	public void getAndSave() {
		byte[] buffer = s7400.readS7400();
		SinterVO svo = new SinterVO();
		svo.loadFromPLC(buffer);
		dao.save(svo);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GetFromPLCAndSave getSave = new GetFromPLCAndSave();

		while (true) {

			try {
				getSave.getAndSave();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
	}
}
