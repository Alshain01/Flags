package io.github.alshain01.flags.api.area;

import io.github.alshain01.flags.api.AreaPlugin;
import io.github.alshain01.flags.api.Flag;
import io.github.alshain01.flags.api.exception.InvalidAreaException;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings("unused")
/**
 * Defines a location in the world comprised of multiple blocks to serve a specific purpose.
 */
public interface Area extends Comparable<Area> {
    public enum AreaRelationship {
        EQUAL, PARENT, CHILD, SIBLING, UNRELATED
    }

    /**
     * Gets the plugin that defines this area.
     *
     * @return the plugin that defines this area.
     */
    public AreaPlugin getAreaPlugin();

    /**
     * Validates the underlying object from the area plugin is not null.
     *
     * @return true if the underlying area object is not null.
     */
    public boolean isArea();

    /**
     * Gets the area plugin's ID for this area.
     *
     * @return the area's ID in the format provided by the area plugin.
     * @throws InvalidAreaException
     */
    public String getId();

    /**
     * Returns the name of the area defined in the area plugin.
     *
     * @return The name of the area
     * @throws InvalidAreaException
     */
    public String getName();

    /**
     * Gets the world in which area resides.
     *
     * @return the world in which area resides.
     * @throws InvalidAreaException
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
     * @see #getState(Flag, boolean)
     */
    public boolean getState(@Nonnull Flag flag);

    /**
     * Gets the state of the flag for this area.
     *
     * @param flag
     *            The flag to retrieve the state for.
     * @param absolute
     *            Whether to return the exact value of the flag or inherited flag state if none exists.
     *            True may return null, False will always return a boolean value.
     * @return the state of the flag or the inherited state of the flag from defaults if not defined.
     */
    public Boolean getState(@Nonnull Flag flag, boolean absolute);

    /**
     * Sets the state of the flag for this area.
     *
     * @param flag
     *            The flag to set the state for.
     * @param state
     *            The state to set or null to remove.
     * @param sender
     *            The command sender for event call and economy transactions.
     *            May be null if no associated player or console.
     * @return false if the event was canceled.
     */
    public boolean setState(@Nonnull Flag flag, @Nullable Boolean state, @Nullable CommandSender sender);

    /**
     * Gets the message associated with a player flag. Translates the color
     * codes and populates instances of {AreaType}, {Owner}, {AreaName}, and {World}
     * Equivalent to getMessage(flag, true);
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @return the message associated with the flag.
     * @see #getMessage(Flag, boolean)
     */
    public String getMessage(@Nonnull Flag flag);

    /**
     * Gets the message associated with a player flag and parses {AreaType}, {AreaName},
     * {Owner}, {World}, and {Player}.
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @param playerName
     *            The player who's name will be inserted into the message.
     * @return The message associated with the flag.
     */
    public String getMessage(@Nonnull Flag flag, @Nonnull String playerName);

    /**
     * Gets the message associated with a player flag.
     *
     * @param flag
     *            The flag to retrieve the message for.
     * @param parse
     *            True to populate instances of {AreaType}, {Owner}, {AreaName},
     *            and {World} and translate color codes
     * @return The message associated with the flag.
     */
    public String getMessage(@Nonnull Flag flag, boolean parse);

    /**
     * Sets or removes the message associated with a player flag.
     *
     * @param flag
     *            The flag to set the message for.
     * @param message
     *            The message to set, null to remove.
     * @param sender
     *            The command sender for event call and economy transactions.
     *            May be null if no associated player or console.
     * @return true if successful
     */
    public boolean setMessage(@Nonnull Flag flag, @Nullable String message, @Nullable CommandSender sender);

    /**
     * Gets a list of trusted players
     *
     * @param flag
     *            The flag to retrieve the trust list for.
     * @return The list of players
     */
    public Collection<OfflinePlayer> getPlayerTrust(@Nonnull Flag flag);

    /**
     * Gets a collection of trusted permissions
     *
     * @param flag
     *            The flag to retrieve the trust for.
     * @return the collection of trusted permissions
     */
    public Collection<Permission> getPermissionTrust(@Nonnull Flag flag);

    /**
     * Adds player to a the trust.
     *
     * @param flag
     *            The flag to change trust for.
     * @param trustee
     *            The player being trusted
     * @param sender
     *            The command sender for event call and economy transactions.
     *            May be null if no associated player or console.
     * @return true if successful.
     */
    public boolean setTrust(@Nonnull Flag flag, @Nonnull OfflinePlayer trustee, @Nullable CommandSender sender);

    /**
     * Adds permission to a the trust.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being trusted
     * @param sender
     *            The command sender for event call and economy transactions.
     *            May be null if no associated player or console.
     * @return true if successful.
     */
    public boolean setTrust(@Nonnull Flag flag, @Nonnull Permission permission, @Nullable CommandSender sender);

    /**
     * Removes a player from the trust.
     *
     * @param flag
     *            The flag to change trust for.
     * @param trustee
     *            The player being distrusted
     * @param sender
     *            The command sender for event call and economy transactions.
     *            May be null if no associated player or console.
     * @return true if successful.
     */
    public boolean removeTrust(@Nonnull Flag flag, @Nonnull OfflinePlayer trustee, @Nullable CommandSender sender);

    /**
     * Removes a permission from the trust.
     *
     * @param flag
     *            The flag to change trust for.
     * @param permission
     *            The permission being distrusted
     * @param sender
     *            The command sender for event call and economy transactions.
     *            May be null if no associated player or console.
     * @return true if successful.
     */
    public boolean removeTrust(@Nonnull Flag flag, @Nonnull Permission permission, @Nullable CommandSender sender);

    /**
     * Gets if the provided player is the area owner, has explicit trust, or has permission trust.
     *
     * @param flag
     *            The flag to check the trust for.
     * @param player
     *            The player to check trust for.
     * @return true if the player is trusted.
     */
    public boolean hasTrust(@Nonnull Flag flag, @Nonnull Player player);

    /**
     * Checks the players permission to set flags at this location.
     *
     * @param permission
     *            The player to check.
     * @return true if the player has permissions.
     */
    public boolean hasFlagPermission(@Nonnull Permissible permission);

    /**
     * Checks the players permission to set bundles at this location
     *
     * @param permission
     *            The player to check.
     * @return true if the player has permissions.
     */
    public boolean hasBundlePermission(@Nonnull Permissible permission);

    /**
     * Returns the relationship of the provided area to the existing area
     * This is true such that this is a (PARENT, CHILD, ETC.) of the provided area.
     *
     * @param area the area to check the relationship.
     * @return the relationship of the provided area to this one.
     */
    public AreaRelationship getRelationship(@Nonnull Area area);

    /**
     * Returns an alphabetic comparison based on the area name or ID if name is unsupported.
     *
     * @return The value of the comparison.
     */
    @Override
    public int compareTo(@Nonnull Area a);
}
