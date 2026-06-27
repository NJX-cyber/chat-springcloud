package com.chat.service.impl;

import com.chat.Redis.RedisComponent;
import com.chat.constants.Constants;
import com.chat.entity.config.AppConfig;
import com.chat.entity.dto.MessageSendDto;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.UserContact;
import com.chat.entity.po.UserInfo;
import com.chat.entity.po.UserInfoBeauty;
import com.chat.entity.query.SimplePage;
import com.chat.entity.query.UserContactQuery;
import com.chat.entity.query.UserInfoBeautyQuery;
import com.chat.entity.query.UserInfoQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.*;
import com.chat.exception.BusinessException;
import com.chat.mapper.UserContactMapper;
import com.chat.mapper.UserInfoBeautyMapper;
import com.chat.service.ChatSessionUserService;
import com.chat.service.UserContactService;
import com.chat.utils.StringUtils;
import com.chat.websocket.ChannelContextUtils;
import com.chat.websocket.MessageHandle;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import com.chat.service.UserInfoService;
import com.chat.mapper.UserInfoMapper;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description:用户信息表ServiceImpl
 * @author:normal
 * @Date:2026/01/03 20:04:12
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private MessageHandle messageHandle;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(query);
        PaginationResultVO<UserInfo> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(UserInfo bean) {
        return this.userInfoMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserId查询
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId更新
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email查询
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email更新
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * 注册
     */
    @Override
    public void register(String email, String password, String nickname) {
        UserInfo userInfoByEmail = getUserInfoByEmail(email);
        if (userInfoByEmail != null) {
            throw new BusinessException("邮箱已注册");
        }
        String userId = StringUtils.getUserId();

        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectByEmail(email);
        boolean beautyAccount = null != userInfoBeauty && userInfoBeauty.getStatus().equals(BeautyAccountStatusEnum.NO_USE.getStatus());

        if (beautyAccount) {
            userId = UserContactTypeEnum.USER.getPrefix() + userInfoBeauty.getUserId();
        }
        Date date = new Date();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setPassword(StringUtils.encodeMd5(password));
        userInfo.setNickname(nickname);
        userInfo.setCreateTime(date);
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setRecentLoginTime(date);
        userInfo.setRecentOfflineTime(date.getTime());
        userInfo.setConnectType(ConnectTypeEnum.NEED_VERIFY.getType());
        this.userInfoMapper.insert(userInfo);
        if (beautyAccount) {
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            this.userInfoBeautyMapper.updateByUserId(userInfoBeauty, userId);
        }

        this.userContactService.addContact4Robot(userId);
    }

    /**
     * 登录
     */
    @Override
    public TokenUserInfoDto login(String email, String password) {
        UserInfo userInfo = this.getUserInfoByEmail(email);
        if (userInfo == null || !userInfo.getPassword().equals(StringUtils.encodeMd5(password))) {
            throw new BusinessException("邮箱或密码输入有误");
        }

        if (userInfo.getStatus().equals(UserStatusEnum.DISABLE.getStatus())) {
            throw new BusinessException("账号已被封禁，请联系管理员");
        }

        Long userHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (userHeartBeat != null) {
            throw new BusinessException("此账号在别处登录，请退出后重试");
        }

        // 查询联系人
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setUserId(userInfo.getUserId());
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = this.userContactMapper.selectList(userContactQuery);
        List<String> contactIdList = userContactList.stream().map(item -> item.getContactId()).collect(Collectors.toList());
        this.redisComponent.cleanUserContactBatch(userInfo.getUserId());
        if (!contactIdList.isEmpty()) {
            this.redisComponent.saveUserContactBatch(userInfo.getUserId(), contactIdList);
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);
        String token = StringUtils.encodeMd5(tokenUserInfoDto.getUserId() + StringUtils.getRandomString(Constants.LENGTH_TWENTY));
        tokenUserInfoDto.setToken(token);
        // 保存登录信息
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        return tokenUserInfoDto;
    }

    @Override
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        if (avatarFile != null) {
            String baseFold = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File avatarFold = new File(baseFold + Constants.FILE_FOLDER_AVATAR);
            if (!avatarFold.exists()) {
                avatarFold.mkdirs();
            }
            String avatarFilePath = avatarFold.getPath() + "/" + userInfo.getUserId() + Constants.IMG_SUFFIX;
            String coverFilePath = avatarFold.getPath() + "/" + userInfo.getUserId() + Constants.COVER_IMG_SUFFIX;
            avatarFile.transferTo(new File(avatarFilePath));
            avatarCover.transferTo(new File(coverFilePath));
        }
        UserInfo dbUserInfo = this.userInfoMapper.selectByUserId(userInfo.getUserId());
        if (dbUserInfo == null || UserStatusEnum.DISABLE.getStatus().equals(dbUserInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
        String nicknameUpdate = null;
        if (dbUserInfo.getNickname().equals(userInfo.getNickname())) {
            nicknameUpdate = userInfo.getNickname();
        }

        if (nicknameUpdate == null) {
            return;
        }

        TokenUserInfoDto dto = this.redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        dto.setNickname(nicknameUpdate);
        this.redisComponent.saveTokenUserInfoDto(dto);

        this.chatSessionUserService.updateRedundancyInfo(userInfo.getNickname(), userInfo.getUserId());
    }

    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum statusEnum = UserStatusEnum.getByStatus(status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(statusEnum.getStatus());
        this.userInfoMapper.updateByUserId(userInfo, userId);
    }

    @Override
    public void forceOffLine(String userId) {
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setContactId(userId);
        messageSendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
        messageSendDto.setMessageContent(MessageTypeEnum.FORCE_OFF_LINE.getInitMessage());
        this.messageHandle.sendMessage(messageSendDto);
    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();

        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickname(userInfo.getNickname());

        String adminEmails = appConfig.getAdminEmails();
        tokenUserInfoDto.setAdmin(!StringUtils.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail()));
        return tokenUserInfoDto;
    }

    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        String userId = StringUtils.getUserId();
        while (!set.contains(userId)) {
            set.add(userId);
            userId = StringUtils.getUserId();
        }
        System.out.println(set.size());
    }
}