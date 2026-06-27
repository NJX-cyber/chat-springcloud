package com.example.messagemanager.controller;

import com.example.messagemanager.service.ChatMessageService;
import com.example.model.annotation.GlobalInterceptor;
import com.example.model.annotation.RateLimit;
import com.example.model.config.AppConfig;
import com.example.model.constants.Constants;
import com.example.model.entity.dto.MessageSendDto;
import com.example.model.entity.dto.TokenUserInfoDto;
import com.example.model.entity.po.ChatMessage;
import com.example.model.entity.vo.ResponseVO;
import com.example.model.enums.ResponseCodeEnum;
import com.example.model.enums.UserContactTypeEnum;
import com.example.model.controller.BaseController;
import com.example.model.exception.BusinessException;
import com.example.model.utils.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.example.model.utils.StringUtils.normalizeSegment;

/**
 * author:normal
 * date:2026/2/4 15:03
 * description:
 */
@RestController("chatController")
@RequestMapping("/chat")
@Validated
public class ChatController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private AppConfig appConfig;

    @PostMapping("/sendMessage")
    @RateLimit
    @GlobalInterceptor
    public ResponseVO sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Size(max = 500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserinfo(request);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setMessageType(messageType);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        MessageSendDto messageSendDto = this.chatMessageService.saveMessage(chatMessage, tokenUserInfoDto);

        return getSuccessResponseVO(messageSendDto);
    }

    @DeleteMapping("/deleteMessage")
    @RateLimit
    @GlobalInterceptor
    public ResponseVO deleteMessageById(HttpServletRequest request,Long messageId) {
        String userId = getTokenUserinfo(request).getUserId();
        this.chatMessageService.deleteChatMessageByMessageId(userId, messageId);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/uploadFile")
    @GlobalInterceptor
    public ResponseVO uploadFile(HttpServletRequest request,
                                 @NotNull Long messageId,
                                 @NotNull MultipartFile file,
                                 MultipartFile cover) {
        TokenUserInfoDto userInfoDto = getTokenUserinfo(request);
        this.chatMessageService.saveMessageFile(userInfoDto.getUserId(), messageId, file, cover);
        return getSuccessResponseVO(null);
    }

    @GetMapping("/downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request,
                             HttpServletResponse response,
                             @NotEmpty String fileId,
                             @NotNull Boolean showCover) {
        TokenUserInfoDto userInfoDto = getTokenUserinfo(request);
        FileInputStream in = null;
        ServletOutputStream out = null;

        try {
            File file = null;
            if (!StringUtils.isNumber(fileId) && ArrayUtils.contains(new String[]{UserContactTypeEnum.USER.getPrefix(), UserContactTypeEnum.GROUP.getPrefix()}, (fileId.charAt(0) + "")
            )) {

                String avatarFileName;
                if (showCover) {
                    avatarFileName = fileId + Constants.COVER_IMG_SUFFIX;
                } else {
                    avatarFileName = fileId + Constants.IMG_SUFFIX;
                }

                Path avatarPath = Paths.get(appConfig.getProjectFolder())
                        .resolve(normalizeSegment(Constants.FILE_FOLDER_FILE))
                        .resolve(normalizeSegment(Constants.FILE_FOLDER_AVATAR))
                        .resolve(normalizeSegment(avatarFileName))
                        .normalize();

                file = avatarPath.toFile();

                if (!file.exists() || !file.isFile() || file.length() == 0) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }

            } else {
                file = this.chatMessageService.downloadFile(userInfoDto, Long.parseLong(fileId), showCover);
            }
            response.setContentType("application/x-msdownload;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment");
            response.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            out = response.getOutputStream();
            int len;
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            logger.error("文件下载失败，{}", e.toString());
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("IO异常，e", e.toString());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO移出，{}", e.toString());
                }
            }

        }
    }
}