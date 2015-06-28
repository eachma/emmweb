package org.em.utils;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


public class DBSession {
	private static final String cfgxml = "cfg/batis.xml";
	private static SqlSessionFactory sqlMapper;
	private static Reader reader = null;
	static {
		
			try {
				reader = Resources.getResourceAsReader(cfgxml);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			 sqlMapper = new SqlSessionFactoryBuilder().build(reader);
		
	}
	public static SqlSession getSession(boolean autocommit){
		return DBSession.sqlMapper.openSession(autocommit);//cease auto commit
		
	}
	

}
