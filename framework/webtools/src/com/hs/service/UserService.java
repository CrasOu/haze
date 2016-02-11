package com.hs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.hs.User;
import com.hs.dao.UserDao;
/**
 * 用户的业务层
 * @author ydq
 *
 */
@Service
public class UserService  {
	@Autowired
	UserDao userDao;
	
	/**
	 * 获取用户的分页对象
	 * @param pageSize
	 * @param nowPage
	 * @return
	 */
	public Page<User> getUserPage(Integer pageSize,Integer nowPage){
		Sort sort = new Sort(Direction.DESC, "id");
		Pageable pa =  new PageRequest(0, 10, sort); 
		return userDao.findAll(pa);
	} 
	
	public User saveUser(User user){
		return userDao.save(user);
	}
	
	public void deleteUser(User user){
		userDao.delete(user);
	}
	
	public void deleteUser(Integer id){
		userDao.delete(id);
	}
	
	public User findUserById(Long id){
		return userDao.findById(id);
	}
	
	public User updateUser(User newUser){
		return userDao.save(newUser);
	}
}
