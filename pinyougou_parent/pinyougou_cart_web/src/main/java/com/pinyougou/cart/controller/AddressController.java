package com.pinyougou.cart.controller;
import java.util.List;

import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.ResultInfo;
/**
 * 请求处理器
 * @author Steven
 *
 */
@RestController
@RequestMapping("/address")
public class AddressController {

	@Reference
	private AddressService addressService;

	/**
	 * 跟据登陆的用户名查找收货地址
	 *
	 * @return
	 */
	@RequestMapping("/findListByUserId")
	public List<TbAddress> findListByUserId() {
		//获取登录名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return addressService.findListByUserId(username);
	}

	/**
	 * 返回全部列表
	 *
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbAddress> findAll() {
		return addressService.findAll();
	}


	/**
	 * 分页查询数据
	 *
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int pageNo, int pageSize, @RequestBody TbAddress address) {
		return addressService.findPage(pageNo, pageSize, address);
	}

	/**
	 * 增加
	 *
	 * @param address
	 * @return
	 */
	@RequestMapping("/add")
	public ResultInfo add(@RequestBody TbAddress address) {
		try {
			//获取登入用户名
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			address.setUserId(userId);
			addressService.add(address);
			return new ResultInfo(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "增加失败");
		}
	}

	/**
	 * 修改
	 *
	 * @param address
	 * @return
	 */
	@RequestMapping("/update")
	public ResultInfo update(@RequestBody TbAddress address) {
		try {
			addressService.update(address);
			return new ResultInfo(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping("/getById")
	public TbAddress getById(Long id) {
		return addressService.getById(id);
	}

	/**
	 * 批量删除
	 *
	 * @param
	 * @return
	 * @RequestMapping("/delete") public ResultInfo delete(Long [] ids){
	 * try {
	 * addressService.delete(ids);
	 * return new ResultInfo(true, "删除成功");
	 * } catch (Exception e) {
	 * e.printStackTrace();
	 * return new ResultInfo(false, "删除失败");
	 * }
	 * }
	 */

	@RequestMapping("/delete")
	public ResultInfo delete(Long id) {
		try {
			addressService.delete(id);
			return new ResultInfo(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultInfo(false, "删除失败");
		}

	}
}
