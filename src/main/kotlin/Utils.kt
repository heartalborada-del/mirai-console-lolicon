/**
 * Copyright (c) 2020-2021 Samarium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.github.samarium150.mirai.plugin

import com.github.samarium150.mirai.plugin.Lolicon.set
import net.mamoe.mirai.contact.*
import org.jetbrains.annotations.Nullable
import java.net.Proxy

/**
 * Object for utility functions
 * <br>
 * 实用函数
 *
 * @constructor Create a Utility instance <br> 实例化Utils
 */
object Utils {

    /**
     * Check whether [user] is the bot owner
     * <br>
     * 检查用户是否是Bot所有者
     *
     * @param user target user <br> 目标用户
     * @return checking result <br> 检查结果
     */
    fun checkMaster(@Nullable user: User?): Boolean {
        return user == null || user.id == PluginConfig.master
    }

    /**
     * Check whether [user] is trusted
     * <br>
     * 检查用户是否受信任
     *
     * @param user target user <br> 目标用户
     * @return checking result <br> 检查结果
     */
    fun checkUserPerm(@Nullable user: User?): Boolean {
        return user == null || PluginData.trustedUsers.contains(user.id)
    }

    /**
     * Check whether [user] is a group owner or administrator
     * <br>
     * 检查用户在群里的权限
     *
     * @param user target user <br> 目标用户
     * @return checking result <br> 检查结果
     */
    fun checkMemberPerm(@Nullable user: User?): Boolean {
        return (user as Member).permission != MemberPermission.MEMBER
    }

    /**
     * Convert [value] into a valid number
     * for setting the corresponding property
     * <br>
     * 将字符串转为整数值
     *
     * @param value input string value <br> 输入的字符串
     * @param type input property <br> 需要转化的类别
     * @return integer value <br> 转换后的值
     * @throws NumberFormatException if [value] is invalid <br> 数值非法时抛出
     * @see Lolicon.set
     */
    @Throws(NumberFormatException::class)
    fun convertValue(value: String, type: String): Int {
        val setting = value.toInt()
        when (type) {
            "r18" -> if (setting != 0 && setting != 1 && setting != 2) throw NumberFormatException(value)
            "recall" -> if (setting < 0 || setting >= 120) throw NumberFormatException(value)
            "cooldown" -> if (setting < 0) throw NumberFormatException(value)
        }
        return setting
    }

    /**
     * Get proxy type from the given string
     * <br>
     * 根据输入值返回代理类型
     *
     * @param value input string value <br> 输入的字符串
     * @return [Proxy.Type] <br> 代理的类型
     * @throws IllegalArgumentException if [value] is not [Proxy.Type] <br> 数值非法时抛出
     * @see Proxy
     */
    @Throws(IllegalArgumentException::class)
    fun getProxyType(value: String): Proxy.Type {
        return when (value) {
            "DIRECT" -> Proxy.Type.DIRECT
            "HTTP" -> Proxy.Type.HTTP
            "SOCKS" -> Proxy.Type.SOCKS
            else -> throw IllegalArgumentException(value)
        }
    }

    /**
     * Process tags
     *
     * @param str
     * @return
     */
    fun processTags(str: String): List<List<String>> {
        val result: MutableList<List<String>> = listOf<List<String>>().toMutableList()
        val and = str.split("&")
        for (s in and) result.add(s.split("|"))
        return result.toList()
    }

    /**
     * Is the given [tag] allowed
     *
     * @param tag
     * @return
     */
    private fun isTagAllowed(tag: String): Boolean {
        return when (PluginConfig.tagFilterMode) {
            "none" -> true
            "whitelist" -> {
                for (filter in PluginConfig.tagFilter) {
                    if (filter.toRegex(setOf(RegexOption.IGNORE_CASE)).matches(tag)) return true
                }
                false
            }
            "blacklist" -> {
                for (filter in PluginConfig.tagFilter) {
                    if (filter.toRegex(setOf(RegexOption.IGNORE_CASE)).matches(tag)) return false
                }
                true
            }
            else -> true
        }
    }

    /**
     * Are these given [tags] allowed
     *
     * @param tags
     * @return
     */
    fun areTagsAllowed(tags: List<String>): Boolean {
        return when (PluginConfig.tagFilterMode) {
            "none" -> true
            "whitelist" -> {
                var flag = false
                for (tag in tags) {
                    if (isTagAllowed(tag)) {
                        flag = true
                        break
                    }
                }
                flag
            }
            "blacklist" -> {
                var flag = true
                for (tag in tags) {
                    if (!isTagAllowed(tag)) {
                        flag = false
                        break
                    }
                }
                flag
            }
            else -> true
        }
    }

    private val sizeMap: Map<String, Int> = mapOf(
        "original" to 0,
        "regular" to 1,
        "small" to 2,
        "thumb" to 3,
        "mini" to 4
    )

    fun getUrl(urls: Map<String, String>): String? {
        return urls[urls.keys.sortedBy { sizeMap[it] }[0]]
    }

    /**
     * Is the subject/user permitted to use the bot
     * <br>
     * 是否能执行命令
     *
     * @param subject
     * @param user
     * @return
     */
    fun isPermitted(subject: Contact?, user: User?): Boolean {
        return when (PluginConfig.mode) {
            "whitelist" -> {
                when {
                    subject == null -> true
                    subject is User && PluginData.userSet.contains(subject.id) -> true
                    subject is Group &&
                        PluginData.groupSet.contains(subject.id) && PluginData.userSet.contains(user?.id) -> true
                    else -> false
                }
            }
            "blacklist" -> {
                when {
                    subject == null -> true
                    subject is User && !PluginData.userSet.contains(subject.id) -> true
                    subject is Group &&
                        !PluginData.groupSet.contains(subject.id) && !PluginData.userSet.contains(user?.id) -> true
                    else -> false
                }
            }
            else -> true
        }
    }
}
