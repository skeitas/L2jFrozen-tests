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
package interlude.gameserver.network.clientpackets;

import java.util.logging.Logger;

import interlude.gameserver.datatables.ClanTable;
import interlude.gameserver.model.L2Clan;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.ActionFailed;
import interlude.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	private static final String _C__51_REQUESTSURRENDERPLEDGEWAR = "[C] 51 RequestSurrenderPledgeWar";
	private static Logger _log = Logger.getLogger(RequestSurrenderPledgeWar.class.getName());
	private String _pledgeName;
	private L2Clan _clan;
	private L2PcInstance _activeChar;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null) {
			return;
		}
		_clan = _activeChar.getClan();
		if (_clan == null) {
			return;
		}
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
		{
			_activeChar.sendMessage("No such clan.");
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		_log.info("RequestSurrenderPledgeWar by " + getClient().getActiveChar().getClan().getName() + " with " + _pledgeName);
		if (!_clan.isAtWarWith(clan.getClanId()))
		{
			_activeChar.sendMessage("You aren't at war with this clan.");
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(_pledgeName);
		_activeChar.sendPacket(msg);
		msg = null;
		_activeChar.deathPenalty(false);
		ClanTable.getInstance().deleteclanswars(_clan.getClanId(), clan.getClanId());
		/*
		 * L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName()); if(leader != null && leader.isOnline() == 0) { player.sendMessage("Clan leader isn't online."); player.sendPacket(new ActionFailed()); return; } if (leader.isTransactionInProgress()) { SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER); sm.addString(leader.getName());
		 * player.sendPacket(sm); return; } leader.setTransactionRequester(player); player.setTransactionRequester(leader); leader.sendPacket(new SurrenderPledgeWar(_clan.getName(),player.getName()));
		 */
	}

	@Override
	public String getType()
	{
		return _C__51_REQUESTSURRENDERPLEDGEWAR;
	}
}