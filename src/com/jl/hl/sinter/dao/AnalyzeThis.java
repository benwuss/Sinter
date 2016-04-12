package com.jl.hl.sinter.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.Logger;

import com.jl.hl.sinter.model.ResultVO;
import com.jl.hl.sinter.util.LoggerUtil;

public class AnalyzeThis {

	private final static Logger logger = LoggerUtil.getLogger("MYSQL");

	private SqlSession session = null;

	public AnalyzeThis() {

	}

	public void setSqlSession(SqlSession session) {
		this.session = session;
	}

	public void analyzeResult(String startTime, String endTime) {
		ArrayList<ResultVO> rlist = new ArrayList<ResultVO>();

		Map<String, BigDecimal> resultMap = getResultMap(startTime + "%", endTime + "%");
		for(Map.Entry<String, BigDecimal> entry : resultMap.entrySet()){
			ResultVO rvo = new ResultVO(TimeUtil.change2Format2(startTime), TimeUtil.change2Format2(endTime));
			rvo.setCriteria(entry.getKey());
			Object obj = entry.getValue();
            rvo.setRatio(new BigDecimal(obj.toString()));		
            rlist.add(rvo);
		}		
		insertResult(rlist);
		logger.exit();
	}

	public Map<String, BigDecimal> getResultMap(String startTime, String endTime) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);

		Map<String, BigDecimal> resultMap = session.selectOne("selectResult", map);

		Map<String, BigDecimal> sortMap = new TreeMap<String, BigDecimal>(
				new MapKeyComparator());

		sortMap.putAll(resultMap);
		
		return sortMap;
	}

	public void insertResult(ArrayList<ResultVO> rlist) {
		for (ResultVO resultVO : rlist) {
			session.insert("addResult", resultVO);
		}
	}
	
	class MapKeyComparator implements Comparator<String>{

		@Override
		public int compare(String str1, String str2) {			
			return str1.compareTo(str2);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String startTime = "20160312124729.735";
		String endTime = "20160321135232.399";

		MySQLDAO db = new MySQLDAO();
		SqlSession session = db.getSession();
		AnalyzeThis analyze = new AnalyzeThis();
		analyze.setSqlSession(session);
		analyze.analyzeResult(startTime, endTime);
		session.commit();
		session.close();
	}

}
