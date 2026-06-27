package com.chat.controller;

import com.chat.entity.po.UserContactApply;
import com.chat.entity.query.UserContactApplyQuery;
import com.chat.entity.vo.ResponseVO;
import com.chat.service.UserContactApplyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:申请联系人Controller
 * @author:normal
 * @Date:2026/01/06 14:59:46
 */
@RestController("userContactApplyController")
@RequestMapping("/userContactApply")
public class UserContactApplyController extends BaseController {

	@Resource
	private UserContactApplyService userContactApplyService;

	/**
	 * 根据条件查询列表
	 */
	@GetMapping("/loadDataList")
	public ResponseVO loadDataList(UserContactApplyQuery query){
		return getSuccessResponseVO(this.userContactApplyService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@PostMapping("/add")
	public ResponseVO add(@RequestBody UserContactApply bean) {
		this.userContactApplyService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/addOrUpdate")
	public ResponseVO addOrUpdate(@RequestBody UserContactApply bean) {
		this.userContactApplyService.addOrUpdate(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@PostMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserContactApply> listBean) {
		this.userContactApplyService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增或修改
	 */
	@PostMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserContactApply> listBean) {
		this.userContactApplyService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyId查询
	 */
	@GetMapping("/getUserContactApplyByApplyId")
	public ResponseVO getUserContactApplyByApplyId(Integer applyId) {
		return getSuccessResponseVO(this.userContactApplyService.getUserContactApplyByApplyId(applyId));
	}

	/**
	 * 根据ApplyId更新
	 */
	@PutMapping("/updateUserContactApplyByApplyId")
	public ResponseVO updateUserContactApplyByApplyId(@RequestBody UserContactApply bean, Integer applyId) {
		this.userContactApplyService.updateUserContactApplyByApplyId(bean, applyId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyId删除
	 */
	@DeleteMapping("/deleteUserContactApplyByApplyId")
	public ResponseVO deleteUserContactApplyByApplyId(Integer applyId) {
		this.userContactApplyService.deleteUserContactApplyByApplyId(applyId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId查询
	 */
	@GetMapping("/getUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId")
	public ResponseVO getUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(String applyUserId, String receiverUserId, String contactId) {
		return getSuccessResponseVO(this.userContactApplyService.getUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(applyUserId, receiverUserId, contactId));
	}

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId更新
	 */
	@PutMapping("/updateUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId")
	public ResponseVO updateUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(@RequestBody UserContactApply bean, String applyUserId, String receiverUserId, String contactId) {
		this.userContactApplyService.updateUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(bean, applyUserId, receiverUserId, contactId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据ApplyUserIdAndReceiverUserIdAndContactId删除
	 */
	@DeleteMapping("/deleteUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId")
	public ResponseVO deleteUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(String applyUserId, String receiverUserId, String contactId) {
		this.userContactApplyService.deleteUserContactApplyByApplyUserIdAndReceiverUserIdAndContactId(applyUserId, receiverUserId, contactId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据LastApplyTime查询
	 */
	@GetMapping("/getUserContactApplyByLastApplyTime")
	public ResponseVO getUserContactApplyByLastApplyTime(Long lastApplyTime) {
		return getSuccessResponseVO(this.userContactApplyService.getUserContactApplyByLastApplyTime(lastApplyTime));
	}

	/**
	 * 根据LastApplyTime更新
	 */
	@PutMapping("/updateUserContactApplyByLastApplyTime")
	public ResponseVO updateUserContactApplyByLastApplyTime(@RequestBody UserContactApply bean, Long lastApplyTime) {
		this.userContactApplyService.updateUserContactApplyByLastApplyTime(bean, lastApplyTime);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据LastApplyTime删除
	 */
	@DeleteMapping("/deleteUserContactApplyByLastApplyTime")
	public ResponseVO deleteUserContactApplyByLastApplyTime(Long lastApplyTime) {
		this.userContactApplyService.deleteUserContactApplyByLastApplyTime(lastApplyTime);
		return getSuccessResponseVO(null);
	}
}