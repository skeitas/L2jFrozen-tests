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
package interlude.gameserver.skills.effects;

import interlude.gameserver.model.L2Effect;
import interlude.gameserver.model.actor.instance.L2PlayableInstance;
import interlude.gameserver.skills.Env;

/**
 * @author WhiteMonk Implementation of skill Soul of The Phoenix
 */
final class EffectSoulOfThePhoenix extends L2Effect
{
	public EffectSoulOfThePhoenix(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.SOUL_OF_THE_PHOENIX;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		if (getEffected() instanceof L2PlayableInstance) {
			((L2PlayableInstance) getEffected()).startSoulOfThePhoenix();
		}
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PlayableInstance) {
			((L2PlayableInstance) getEffected()).stopSoulOfThePhoenix(this);
		}
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
