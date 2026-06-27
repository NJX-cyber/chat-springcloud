package com.chat.utils;

import com.chat.constants.Constants;
import com.chat.enums.UserContactTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * function:
 * author:聂佳鑫
 * date:2025/10/31 11:02
 */
public class StringUtils {
    public static String upperCaseFirstLetter(String s) {
        if (s == null || s.equals("")) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String lowerCaseFirstLetter(String s) {
        if (s == null || s.equals("")) return "";
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static Boolean isEmpty(String str) {
        if (str == null || str.equals("") || str.equals("null") || str.equals("\u0000") || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static String getUserId() {
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(Constants.LENGTH_ELEVEN);
    }

    public static String getGroupId() {
        return UserContactTypeEnum.GROUP.getPrefix() + getRandomNumber(Constants.LENGTH_ELEVEN);
    }

    public static String getRandomNumber(Integer num) {
        return RandomStringUtils.random(11, false, true);
    }

    public static String getRandomString(Integer num) {
        return RandomStringUtils.random(11, true, true);
    }

    public static final String encodeMd5(String str) {
        return isEmpty(str) ? null : DigestUtils.md5Hex(str);
    }

    public static String cleanHtmlTag(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace("\r\n", "<br>");
        content = content.replace("\n", "<br>");

        return content;
    }

    public static final String getChatSessionId4User(String[] userIds) {
        Arrays.sort(userIds);
        String res = Arrays.stream(userIds).collect(Collectors.joining());
        return encodeMd5(res);
    }

    public static void main(String[] args) {
        System.out.println(getChatSessionId4User(new String[]{"123", "234"}));
        System.out.println(getChatSessionId4User(new String[]{"234", "123"}));
    }

    public static String getChatSessionId4Group(String groupId) {
        return encodeMd5(groupId);
    }

    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static Boolean isNumber(String s) {
        String checkPattern = "^[0-9]+$";
        if (null == s) {
            return false;
        }
        if (!s.matches(checkPattern)) {
            return false;
        }
        return true;
    }

    public static String normalizeSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return "";
        }
        segment = segment.replaceAll("^[\\\\/]+", "");

        segment = segment.replaceAll("[\\\\/]+$", "");
        return segment;
    }

}