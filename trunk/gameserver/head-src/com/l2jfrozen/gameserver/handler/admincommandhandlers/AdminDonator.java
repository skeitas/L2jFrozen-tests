/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class AdminDonator implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_setdonator"
	};

	protected static final Logger _log = Logger.getLogger(AdminDonator.class.getName());
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){
			return false;
		}
		
		if(Config.GMAUDIT)
		{
			Logger _logAudit = Logger.getLogger("gmaudit");
			LogRecord record = new LogRecord(Level.INFO, command);
			record.setParameters(new Object[]
			{
					"GM: " + activeChar.getName(), " to target [" + activeChar.getTarget() + "] "
			});
			_logAudit.log(record);
		}
		*/

		if(activeChar == null)
			return false;

		if(command.startsWith("admin_setdonator"))
		{
			L2Object target = activeChar.getTarget();

			if(target instanceof L2PcInstance)
			{
				L2PcInstance targetPlayer = (L2PcInstance) target;
				boolean newDonator = !targetPlayer.isDonator();

				if(newDonator)
				{
					targetPlayer.setDonator(true);
					targetPlayer.updateNameTitleColor();
					updateDatabase(targetPlayer, true);
					sendMessages(true, targetPlayer, activeChar, false, true);
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
					targetPlayer.broadcastUserInfo();
				}
				else
				{
					targetPlayer.setDonator(false);
					targetPlayer.updateNameTitleColor();
					updateDatabase(targetPlayer, false);
					sendMessages(false, targetPlayer, activeChar, false, true);
					targetPlayer.broadcastUserInfo();
				}

				targetPlayer = null;
			}
			else
			{
				activeChar.sendMessage("Impossible to set a non Player Target as Donator.");
				_log.info("GM: " + activeChar.getName() + " is trying to set a non Player Target as Donator.");

				return false;
			}

			target = null;
		}
		return true;
	}

	private void sendMessages(boolean forNewDonator, L2PcInstance player, L2PcInstance gm, boolean announce, boolean notifyGmList)
	{
		if(forNewDonator)
		{
			player.sendMessage(gm.getName() + " has granted Donator Status for you!");
			gm.sendMessage("You've granted Donator Status for " + player.getName());

			if(announce)
			{
				Announcements.getInstance().announceToAll(player.getName() + " has received Donator Status!");
			}

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has set " + player.getName() + " as Donator !");
			}
		}
		else
		{
			player.sendMessage(gm.getName() + " has revoked Donator Status from you!");
			gm.sendMessage("You've revoked Donator Status from " + player.getName());

			if(announce)
			{
				Announcements.getInstance().announceToAll(player.getName() + " has lost Donator Status!");
			}

			if(notifyGmList)
			{
				GmListTable.broadcastMessageToGMs("Warn: " + gm.getName() + " has removed Donator Status of player" + player.getName());
			}
		}
	}

	/**
	 * @param activeChar
	 * @param newDonator
	 */
	private void updateDatabase(L2PcInstance player, boolean newDonator)
	{
		Connection con = null;
		try
		{
			// prevents any NPE.
			// ----------------
			if(player == null)
				return;

			// Database Connection
			//--------------------------------
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(newDonator ? INSERT_DATA : DEL_DATA);

			// if it is a new donator insert proper data
			// --------------------------------------------
			if(newDonator)
			{
				stmt.setInt(1, player.getObjectId());
				stmt.setString(2, player.getName());
				stmt.setInt(3, player.isHero() ? 1 : 0);
				stmt.setInt(4, player.isNoble() ? 1 : 0);
				stmt.setInt(5, 1);
				stmt.execute();
				stmt.close();
				stmt = null;
			}
			else
			// deletes from database
			{
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
				stmt.close();
				stmt = null;
			}
		}
		catch(Exception e)
		{
			if(Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			_log.log(Level.SEVERE, "Error: could not update database: ", e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) { 
				if(Config.ENABLE_ALL_EXCEPTIONS)
					e.printStackTrace();
			}
			con = null;
		}
	}

	// Updates That Will be Executed by MySQL
	// ----------------------------------------
	String INSERT_DATA = "REPLACE INTO characters_custom_data (obj_Id, char_name, hero, noble, donator) VALUES (?,?,?,?,?)";
	String DEL_DATA = "UPDATE characters_custom_data SET donator = 0 WHERE obj_Id=?";

	/**
	 * @return
	 */
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
