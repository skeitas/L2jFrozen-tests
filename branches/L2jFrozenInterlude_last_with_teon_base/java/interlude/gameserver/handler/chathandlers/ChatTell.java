/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package interlude.gameserver.handler.chathandlers;

import interlude.Config;
import interlude.gameserver.handler.IChatHandler;
import interlude.gameserver.model.BlockList;
import interlude.gameserver.model.L2World;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.CreatureSay;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * A chat handler
 * 
 * @author durgus
 */
public class ChatTell implements IChatHandler {
	private static final int[] COMMAND_IDS = { 2 };

	/**
	 * Handle chat type 'tell'
	 * 
	 * @see interlude.gameserver.handler.IChatHandler#handleChat(int,
	 *      interlude.gameserver.model.actor.instance.L2PcInstance,
	 *      java.lang.String)
	 */
	public void handleChat(int type, L2PcInstance activeChar, String target,
			String text) {
		// Return if no target is set
		if (target == null) {
			return;
		}
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type,
				activeChar.getName(), text);
		L2PcInstance receiver = null;
		receiver = L2World.getInstance().getPlayer(target);
		if (receiver != null && !BlockList.isBlocked(receiver, activeChar)) {
			if (Config.JAIL_DISABLE_CHAT && receiver.isInJail()) {
				activeChar.sendMessage("Player is in jail.");
				return;
			}
			if (receiver.isChatBanned()) {
				activeChar.sendMessage("Player is chat banned.");
				return;
			}
			if (!receiver.getMessageRefusal()) {
				receiver.sendPacket(cs);
				activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(),
						type, "->" + receiver.getName(), text));
			} else {
				activeChar.sendPacket(new SystemMessage(
						SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
			}
		} else {
			SystemMessage sm = new SystemMessage(
					SystemMessageId.S1_IS_NOT_ONLINE);
			sm.addString(target);
			activeChar.sendPacket(sm);
			sm = null;
		}
	}

	/**
	 * Returns the chat types registered to this handler
	 * 
	 * @see interlude.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	public int[] getChatTypeList() {
		return COMMAND_IDS;
	}
}