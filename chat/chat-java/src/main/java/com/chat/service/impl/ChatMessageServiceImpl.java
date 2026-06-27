package com.chat.service.impl;

import com.chat.Redis.RedisComponent;
import com.chat.constants.Constants;
import com.chat.entity.config.AppConfig;
import com.chat.entity.dto.MessageSendDto;
import com.chat.entity.dto.SystemSettingsDto;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.entity.po.ChatMessage;
import com.chat.entity.po.ChatSession;
import com.chat.entity.po.UserContact;
import com.chat.entity.query.ChatSessionQuery;
import com.chat.entity.query.SimplePage;
import com.chat.entity.query.ChatMessageQuery;
import com.chat.entity.query.UserContactQuery;
import com.chat.entity.vo.PaginationResultVO;
import com.chat.enums.*;
import com.chat.exception.BusinessException;
import com.chat.mapper.ChatSessionMapper;
import com.chat.mapper.UserContactMapper;
import com.chat.utils.CopyUtils;
import com.chat.utils.DateUtils;
import com.chat.utils.StringUtils;
import com.chat.websocket.MessageHandle;
import jodd.util.ArraysUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.chat.service.ChatMessageService;
import com.chat.mapper.ChatMessageMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Description:ServiceImpl
 * @author:normal
 * @Date:2026/01/26 14:34:11
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandle messageHandle;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery query) {
        return this.chatMessageMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(ChatMessageQuery query) {
        return this.chatMessageMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNum(), count, pageSize);
        query.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(query);
        PaginationResultVO<ChatMessage> paginationResultVO = new PaginationResultVO<>(count, page.getPageSize(), page.getPageNum(), page.getPageTotal(), list);
        return paginationResultVO;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 新增或修改
     */
    @Override
    public Integer addOrUpdate(ChatMessage bean) {
        return this.chatMessageMapper.insertOrUpdate(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据MessageId查询
     */
    @Override
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId更新
     */
    @Override
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteChatMessageByMessageId(String userId, Long messageId) {
        ChatMessage chatMessage = this.chatMessageMapper.selectByMessageId(messageId);
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        // 不是机器人回复，判断好友状态
        String contactId = chatMessage.getContactId();
        String sessionId = null;
        String sendUserId = tokenUserInfoDto.getUserId();
        if (!Constants.ROBOT_UID.equals(contactId)) {
            List<String> contactList = redisComponent.getUserContactList(sendUserId);
            if (!contactList.contains(contactId) && !sendUserId.equals(Constants.ROBOT_UID)) {
                UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
                if (typeEnum == null) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600);
                }
                if (typeEnum.equals(UserContactTypeEnum.USER)) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (null == contactTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (UserContactTypeEnum.USER.equals(contactTypeEnum)) {
            sessionId = StringUtils.getChatSessionId4User(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringUtils.getChatSessionId4Group(contactId);
        }

        long currentTimeMillis = System.currentTimeMillis();
        chatMessage.setSendTime(currentTimeMillis);

        MessageTypeEnum typeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (null == typeEnum || !ArraysUtil.contains(new Integer[]{MessageTypeEnum.CHAT.getType(),
                MessageTypeEnum.MEDIA_CHAT.getType()}, chatMessage.getMessageType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        MessageStatusEnum statusEnum = typeEnum.equals(MessageTypeEnum.MEDIA_CHAT) ? MessageStatusEnum.SENDING : MessageStatusEnum.SENT;
        chatMessage.setStatus(statusEnum.getStatus());

        String content = StringUtils.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(content);

        // 更新会话
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(content);
        chatSession.setLastReceiveTime(currentTimeMillis);
        this.chatSessionMapper.updateBySessionId(chatSession, sessionId);

        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickname());
        chatMessage.setContactType(contactTypeEnum.getType());

        this.chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = CopyUtils.copy(chatMessage, MessageSendDto.class);

        if (Constants.ROBOT_UID.equals(contactId)) {
            SystemSettingsDto systemSettingsDto = redisComponent.getSystemSettings();
            TokenUserInfoDto robotDto = new TokenUserInfoDto();
            robotDto.setUserId(systemSettingsDto.getRobotUid());
            robotDto.setNickname(systemSettingsDto.getRobotNickName());
            ChatMessage robotMessage = new ChatMessage();
            robotMessage.setContactId(sendUserId);
            robotMessage.setMessageContent("你的消息无法识别!");
            robotMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            saveMessage(robotMessage, robotDto);
        } else {
            this.messageHandle.sendMessage(messageSendDto);
        }
        return messageSendDto;
    }

    private void sendMessageError(Long messageId, String contactId) {
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMessageType(MessageTypeEnum.MESSAGE_ERROR.getType());
        messageSendDto.setContactId(contactId);
        this.messageHandle.sendMessage(messageSendDto);
    }

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = this.chatMessageMapper.selectByMessageId(messageId);
        if (null == chatMessage) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SystemSettingsDto systemSettings = this.redisComponent.getSystemSettings();
        String filename = file.getOriginalFilename();
        String fileSuffix = StringUtils.getFileSuffix(filename);
        if (!StringUtils.isEmpty(fileSuffix)
                && ArraysUtil.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > systemSettings.getMaxImageSize() * Constants.FILE_SIZE_MB) {
            sendMessageError(messageId, chatMessage.getContactId());
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        } else if (!StringUtils.isEmpty(fileSuffix)
                && ArraysUtil.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > systemSettings.getMaxVideoSize() * Constants.FILE_SIZE_MB) {
            sendMessageError(messageId, chatMessage.getContactId());
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        } else if (!StringUtils.isEmpty(fileSuffix)
                && !ArraysUtil.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && !ArraysUtil.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > systemSettings.getMaxFileSize() * Constants.FILE_SIZE_MB) {
            sendMessageError(messageId, chatMessage.getContactId());
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        }

        String fileFinalName = messageId + fileSuffix;
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYY_MM.getPattern());
        File fold = new File(appConfig.getProjectFolder(), Constants.FILE_FOLDER_FILE + month);
        if (!fold.exists()) {
            fold.mkdirs();
        }

        String pathname = fold.toPath().toString();
        try {
            file.transferTo(new File(pathname, fileFinalName));
            if (cover != null) cover.transferTo(new File(pathname, messageId + Constants.COVER_IMG_SUFFIX));
        } catch (IOException e) {
            logger.error("文件保存失败，{}", e.toString());
            sendMessageError(messageId, chatMessage.getContactId());
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        }

        ChatMessage uploadMessage = new ChatMessage();
        uploadMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        ChatMessageQuery chatMessageQuery = new ChatMessageQuery();
        chatMessageQuery.setMessageId(messageId);
        chatMessageQuery.setStatus(MessageStatusEnum.SENDING.getStatus());
        this.chatMessageMapper.updateByParam(uploadMessage, chatMessageQuery);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setStatus(MessageStatusEnum.SENT.getStatus());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setContactId(chatMessage.getContactId());
        this.messageHandle.sendMessage(messageSendDto);
        messageSendDto.setContactId(userId);
        this.messageHandle.sendMessage(messageSendDto);
    }

    @Override
    public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover) {
        ChatMessage chatMessage = this.chatMessageMapper.selectByMessageId(messageId);
        String contactId = chatMessage.getContactId();
        String userId = tokenUserInfoDto.getUserId();
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.USER.equals(typeEnum) && !userId.equals(chatMessage.getContactId())
                && !userId.equals(chatMessage.getSendUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (UserContactTypeEnum.GROUP.equals(typeEnum)) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setUserId(userId);
            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectCount(userContactQuery);
            if (count == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYY_MM.getPattern());
        File folder = new File(appConfig.getProjectFolder(), Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = messageId + StringUtils.getFileSuffix(chatMessage.getFileName());
        if (showCover != null && showCover) {
            fileName = messageId + Constants.COVER_IMG_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileName);
        if (!file.exists()) {
            logger.info("文件不存在，{}", messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        return file;

    }
}