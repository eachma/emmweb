package org.em.test;


import org.em.pojos.*;
import org.em.utils.*;
import org.apache.ibatis.session.SqlSession;



public class ETest {

	public static void main(String[] args) {		
	
		SqlSession session = DBSession.getSession(false);
		try{
			User one=new User("testor","dsf","tt@qq.com","hello");
			
			
			session.insert("newUser", one);
			session.commit();
			System.out.println(one.getId());

		}
		catch(Exception e)
		{
			e.printStackTrace();
			session.rollback();
		}
		finally
		{
			session.close();
		}

	}

}
