/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.odinms.handling.login;

import net.sf.odinms.handling.MapleServerHandler;
import net.sf.odinms.handling.mina.MapleCodecFactory;
import net.sf.odinms.server.config.Configuration;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import net.sf.odinms.server.Configuration;
import net.sf.odinms.tools.Triple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LoginServer {

    public static int PORT = 8484;
    private static InetSocketAddress InetSocketadd;
    private static IoAcceptor acceptor;
    private static Map<Integer, Integer> load = new HashMap<Integer, Integer>();
    private static String serverName, eventMessage;
    private static byte flag;
    private static int maxCharacters, userLimit, usersOn = 0;
    private static boolean isClosed = true;
    private static boolean adminOnly = false;
    private static final HashMap<Integer, Triple<String, String, Integer>> loginAuth = new HashMap();
    private static final HashSet<String> loginIPAuth = new HashSet();
    private static LoginServer instance = new LoginServer();

    public static LoginServer getInstance() {
        return instance;
    }

    public static void putLoginAuth(int chrid, String ip, String tempIp, int channel) {
        loginAuth.put(Integer.valueOf(chrid), new Triple(ip, tempIp, Integer.valueOf(channel)));
        loginIPAuth.add(ip);
    }

    public static Triple<String, String, Integer> getLoginAuth(int chrid) {
        return (Triple) loginAuth.remove(Integer.valueOf(chrid));
    }

    public static boolean containsIPAuth(String ip) {
        return loginIPAuth.contains(ip);
    }

    public static void removeIPAuth(String ip) {
        loginIPAuth.remove(ip);
    }

    public static void addIPAuth(String ip) {
        loginIPAuth.add(ip);
    }

    public static final void addChannel(final int channel) {
        load.put(channel, 0);
    }

    public static final void removeChannel(final int channel) {
        load.remove(channel);
    }

    public static final void run_startup_configurations() {
        userLimit = Integer.MAX_VALUE;
        isClosed = false;
        serverName = Configuration.getProperty("ServerName");
        eventMessage = Configuration.getProperty("EventMessage");
        flag = Byte.parseByte(Configuration.getProperty("Flag"));
        PORT = Integer.parseInt(Configuration.getProperty("LPort"));
        adminOnly = Boolean.parseBoolean(Configuration.getProperty("Admin", "false"));
        maxCharacters = Integer.parseInt(Configuration.getProperty("MaxCharacters"));

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));

        acceptor.setHandler(new MapleServerHandler(-1, false));
        //acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);

        try {
            acceptor.bind(new InetSocketAddress(PORT));
            System.out.println("登录器服务器绑定端口：" + PORT);
        } catch (IOException e) {
            System.err.println("Binding to port " + PORT + " failed" + e);
        }
    }

    public static final void shutdown() {
        if (isClosed) {
            return;
        }
        System.out.println("正在关闭登录伺服器...");
 //       acceptor.setCloseOnDeactivation(true);
//        for (IoSession ss : acceptor.getManagedSessions().values()) {
//            ss.close(true);
//        }
        //acceptor.unbind();
        isClosed = true; //nothing. lol
    }

    public static final String getServerName() {
        return serverName;
    }

    public static final String getEventMessage() {
        return eventMessage;
    }

    public static final byte getFlag() {
        return flag;
    }

    public static final int getMaxCharacters() {
        return maxCharacters;
    }

    public static final Map<Integer, Integer> getLoad() {
        return load;
    }

    public static void setLoad(final Map<Integer, Integer> load_, final int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    public static final void setEventMessage(final String newMessage) {
        eventMessage = newMessage;
    }

    public static final void setFlag(final byte newflag) {
        flag = newflag;
    }

    public static final int getUserLimit() {
        return userLimit;
        //  return Integer.parseInt(Configuration.getProperty("UserLimit"));
    }

    public static final int getUsersOn() {
        return usersOn;
    }

    public static final void setUserLimit(final int newLimit) {
        userLimit = newLimit;
    }

    public static final int getNumberOfSessions() {
        return acceptor.getManagedSessions().size();
    }

    public static final boolean isAdminOnly() {
        return adminOnly;
    }

    public static final boolean isClosed() {
        return isClosed;
    }

}
