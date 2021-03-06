package com.ssword.imserver.server.listener;

import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.ImSessionContext;
import org.jim.common.packets.*;
import org.jim.server.listener.ImGroupListener;
import org.tio.core.ChannelContext;

/**
 * @author WChao 
 * 2017年5月13日 下午10:38:36
 */
public class ImDemoGroupListener extends ImGroupListener{
	/** 
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author: WChao
	 */
	@Override
	public void onAfterUnbind(ChannelContext channelContext, String group) throws Exception {
		//发退出房间通知  COMMAND_EXIT_GROUP_NOTIFY_RESP
		ImSessionContext imSessionContext = (ImSessionContext)channelContext.getAttribute();
		ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
		exitGroupNotifyRespBody.setGroup(group);
		Client client = imSessionContext.getClient();
		if(client == null){
			return;
		}
		User clientUser = client.getUser();
		if(clientUser == null) {
			return;
		}
		User notifyUser = new User(clientUser.getId(),clientUser.getNick());
		exitGroupNotifyRespBody.setUser(notifyUser);
		
		RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP,exitGroupNotifyRespBody);
		ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
		ImAio.sendToGroup(group, imPacket);
		
	}
}
