package io.github.alshain01.flags.api.area;

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.Flag;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
@SuppressWarnings("unused")
public interface Area extends Comparable<Area> {
    public enum AreaRelationship {
        EQUAL, PARENT, SUBDIVISION, SISTER_SUBDIVISION, UNRELATED;
    }

    /**
     * Returns the system type that this object belongs to.
     *
     * @return The Cuboid System that created this object
     */
    public AreaPlugin getCuboidPlugin();

    /**
     * Checks if the underlying object from the cuboid system is null.
     *
     * @return true if the area is not null.
     */
    public boolean isArea();

    /**
     * Gets the cuboid system's ID for this area.
     *
     * @return the area's ID in the format provided by the cuboid system.
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     */
    public String getId();

    /**
     * Gets the world for the area.
     *
     * @return the world associated with the area.
     * @throws io.github.alshain01.flags.api.exception.InvalidAreaException
     */
    public World getWorld();

    /**
     * Gets the state of the flag for this area following the inheritance path.
     * Equivalent to getState(flag, false);
     *
     * @param flag
     *            The flag to retrieve the state for.
     * @return The state of the flag or the inherited state of the flag from
     *         defaults if not defined.
     */
    public boolean getState(Flag flag);

    /**
     * Gets the state of the flag for this area.
     *
     * @param flag
     *            The flag to retrieve the state for.
     * @param absolute
     *            True if you want a null state if the flag is not defined.
     *            False if you want the inherited default (ensures not null).
     * @return The state of the flag or the inherited state of the flag from
     *         defaults if not defined.
     */
    public Boolean getState(Flag flag, boolean absolute);

    /**
     * Sets the state of the flag for this area.
     *
     * @param flag
     *            The flag to set the state for.
     * @param state
     *            The state to set, null to remove.
     * @param sender
     *            The command sender for event call and economy, may be null if
     *            no associated player or console.
     * @return False if the event was canceled.
     */
    public boolean setState(Flag flag, Boolean state, CommandSender sender);

    /**
     * Gets the message associated with a player flag. Translates the color
     * codes and populates instances of {AreaType} and {Owner}
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @return The message associated with the flag.
     */
    public String getMessage(Flag flag);

    /**
     * Gets the message associated with a player flag and parses {AreaType}, {AreaName},
     * {Owner}, {World}, and {Player}
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @param playerName
     *            The player who's name will be inserted into the message.
     * @return The message associated with the flag.
     */
    public String getMessage(Flag flag, String playerName);

    /**
     * Gets the message associated with a player flag.
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @param parse
     *            True if you wish to populate instances of {AreaType}, {Owner}, {AreaName},
     *            and {World} and translate color codes
     * @return The message associated with the flag.
     */
    public String getMessage(Flag flag, boolean parse);

    /**
     * Sets or removes the message associated with a player flag.
     *
     * @param flag
     *            The flag to set the message for.
     * @param message
     *            The message to set, null to remove.
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful
     */
    public boolean setMessage(Flag flag, String message, CommandSender sender);

    /**
     * Gets a list of trusted players
     *
     * @param flag
     *            The flag to retrieve the trust list for.
     * @return The list of players
     */
    public Collection<OfflinePlayer> getPlayerTrust(Flag flag);

    /**
     * Gets a list of trusted permissions
     *
     * @param flag
     *            The flag to retrieve the trust list for.
     * @return The list of permissions
     */
    public Collection<Permission> getPermissionTrust(Flag flag);

    /**
     * Adds player to a the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param trustee
     *            The player being trusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public boolean setTrust(Flag flag, OfflinePlayer trustee, CommandSender sender);

    /**
     * Adds permission to a the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being trusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public boolean setTrust(Flag flag, Permission permission, CommandSender sender);

    /**
     * Removes a player from the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param trustee
     *            The player being distrusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public boolean removeTrust(Flag flag, OfflinePlayer trustee, CommandSender sender);

    /**
     * Removes a permission from the trust list.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being distrusted
     * @param sender
     *            CommandSender for event, may be null if no associated player
     *            or console.
     * @return True if successful.
     */
    public boolean removeTrust(Flag flag, Permission permission, CommandSender sender);

    /**
     * Returns true if the provided player is the area owner, has explicit trust, or permission trust.
     *
     * @param flag
     *            The flag to check the trust list for.
     * @param player
     *            The player to check trust for.
     * @return The list of permissions
     */
    public boolean hasTrust(Flag flag, Player player);

    /**
     * Checks the players permission to set flags at this location.
     *
     * @param p
     *            The player to check.
     * @return true if the player has permissions.
     */
    public boolean hasFlagPermission(Permissible p);

    /**
     * Checks the players permission to set bundles at this location
     *
     * @param p
     *            The player to check.
     * @return true if the player has permissions.
     */
    public boolean hasBundlePermission(Permissible p);

    /**
     * Returns the relationship of the provided area to the existing area
     * This is true such that this is a (PARENT, SUBDIVISION, ETC.) of the provided area.
     *
     * @param area the area to check the relationship.
     * @return the relationship of the provided area to this one.
     */
    public AreaRelationship getRelationship(Area area);

    /**
     * Returns an alphabetic comparison based on the area name
     * or ID if name is unsupported.
     *
     * @return The value of the comparison.
     */
    @Override
    public int compareTo(@Nonnull Area a);
}
