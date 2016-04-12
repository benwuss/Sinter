package com.jl.hl.sinter.dao;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.Logger;

import com.jl.hl.sinter.model.SinterVO;
import com.jl.hl.sinter.util.LoggerUtil;

public class MySQLDAO{

	private final static Logger logger = LoggerUtil.getLogger("MySQL");

	private static SqlSessionFactory sessionFactory = null;

	private static String DB_CONF_FILE = "MySQL.xml";

	SqlSession session = null;

	public MySQLDAO() {
		try {
			if (sessionFactory == null) {
				InputStream is = Resources.getResourceAsStream(DB_CONF_FILE);
				sessionFactory = new SqlSessionFactoryBuilder().build(is);
			}
		} catch (FileNotFoundException e) {
			logger.error("Connect to MySQL failed! Check MySQL.xml in /src");
		} catch (Exception e) {
			logger.error("Connect to MySQL failed!", e);
		}
	}
	
	public void save(SinterVO svo){
		SqlSession session = sessionFactory.openSession();
		session.insert("addSinter", svo);
		session.commit();
		session.close();		
	}
	
	public SinterVO getOne(String selectId, String timeID){
		SqlSession session = sessionFactory.openSession();
		SinterVO vo = session.selectOne(selectId, timeID);
		session.close();
		return vo;		
	}
	
	public void insertOne(String insertId,Object obj){
		SqlSession session = sessionFactory.openSession();
		session.insert(insertId, obj);
		session.commit();
		session.close();
	}


	public SqlSession getSession() {
		return sessionFactory.openSession();
	}

	public static void main(String[] args) throws Exception {
		MySQLDAO dao = new MySQLDAO();
		SinterVO vo = dao.getOne("selectSinterOne", "20160313");
		System.out.println(vo);	
	}
}
