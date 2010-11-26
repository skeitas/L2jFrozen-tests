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
package interlude.gameserver.model.zone.type;

import interlude.gameserver.model.L2Character;
import interlude.gameserver.model.actor.instance.L2PcInstance;
import interlude.gameserver.model.base.Race;
import interlude.gameserver.model.zone.L2ZoneType;
import interlude.gameserver.network.SystemMessageId;
import interlude.gameserver.network.serverpackets.SystemMessage;

/**
 * A mother-trees zone
 *
 * @author durgus
 */
public class L2MotherTreeZone extends L2ZoneType
{
	public L2MotherTreeZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			if (player.isInParty())
			{
				for (L2PcInstance member : player.getParty().getPartyMembers()) {
					if (member.getRace() != Race.elf) {
						return;
					}
				}
			}
			player.setInsideZone(L2Character.ZONE_MOTHERTREE, true);
			player.sendPacket(new SystemMessage(SystemMessageId.ENTER_SHADOW_MOTHER_TREE));
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance && character.isInsideZone(L2Character.ZONE_MOTHERTREE))
		{
			character.setInsideZone(L2Character.ZONE_MOTHERTREE, false);
			((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.EXIT_SHADOW_MOTHER_TREE));
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}