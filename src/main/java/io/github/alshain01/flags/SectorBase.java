package io.github.alshain01.flags;

import io.github.alshain01.flags.api.FlagsAPI;
import io.github.alshain01.flags.api.event.SectorChangeOwnerEvent;
import io.github.alshain01.flags.api.sector.Sector;
import io.github.alshain01.flags.api.sector.SectorLocation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Defines a Sector cuboid.
 */
@SuppressWarnings("unchecked")
final class SectorBase implements Sector {
    private final UUID id;
    private final UUID parent;
    private UUID owner;
    private String name;
    private final SectorLocation greater, lesser;
    private int depth;

    SectorBase(UUID id, Location corner1, Location corner2, int depth, OfflinePlayer owner) {
        this(id, corner1, corner2, depth, null, owner);
    }

    private SectorBase(Location corner1, Location corner2, int depth) {
        this.id = UUID.randomUUID();
        parent = null;
        this.depth = depth;
        this.owner = UUID.randomUUID();

        //Find the lesser/greater corners
        greater = getGreaterCorner(corner1, corner2);
        lesser = getLesserCorner(corner1, corner2);
    }

    SectorBase(UUID id, Location corner1, Location corner2, int depth, UUID parentID, OfflinePlayer owner) {
        this.id = id;
        parent = parentID;
        this.depth = depth;
        this.owner = owner.getUniqueId();

        //Find the lesser/greater corners
        greater = getGreaterCorner(corner1, corner2);
        lesser = getLesserCorner(corner1, corner2);
    }

    public SectorBase(UUID id, Map<String, Object> sector) {
        this.id = id;
        greater = new SectorLocationBase((Map<String, Object>)sector.get("GreaterCorner"));
        lesser = new SectorLocationBase((Map<String, Object>)sector.get("LesserCorner"));
        parent = String.valueOf(sector.get("Parent")).equals("null") ? null : UUID.fromString((String)sector.get("Parent"));
        depth = (Integer)sector.get("Depth");
        name = (String)sector.get("Name");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> sector = new HashMap<String, Object>();
        sector.put("Parent", parent == null ? "null" : parent.toString());
        sector.put("Name", name);
        sector.put("GreaterCorner", greater.serialize());
        sector.put("LesserCorner", lesser.serialize());
        sector.put("Depth", depth);
        return sector;
    }

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public String getName() {
        return name != null ? name : "unnamed sector";
    }

    @Override
    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(owner);
    }

    @Override
    public void setOwner(OfflinePlayer player) {
        if(parent == null) {
            this.owner = player.getUniqueId();
        } else {
            final SectorChangeOwnerEvent event = new SectorChangeOwnerEvent(this, player);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.getParent().setOwner(player);
            }
        }
    }

    @Override
    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    public SectorLocation getGreaterCorner() {
        return greater;
    }

    @Override
    public SectorLocation getGreaterXCorner() {
        return new SectorLocationBase(greater.getWorld().getUID(), greater.getX(), greater.getY(), lesser.getZ());
    }

    @Override
    public SectorLocation getGreaterZCorner() {
        return new SectorLocationBase(greater.getWorld().getUID(), lesser.getX(), greater.getY(), greater.getZ());
    }

    public SectorLocation getLesserCorner() {
        return lesser;
    }

    public World getWorld() {
        return greater.getLocation().getWorld();
    }

    public int getDepth() { return depth; }

    public void setDepth(int depth) { this.depth = depth; }

    public Sector getParent() {
        return FlagsAPI.getSectorManager().get(parent);
    }

    public boolean contains(@Nonnull Location location) {
        int x = location.getBlockX(), z = location.getBlockZ();

        // Greater will always have a higher X and Y due to constructor
        if((x >= lesser.getX() && x <= greater.getX()) && (z >= lesser.getZ() && z <= greater.getZ())) {
            // Check the depth below both points
            if(getWorld().getHighestBlockYAt(greater.getLocation()) - depth < location.getBlockY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether the sector fully contains a provided cuboid.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return True if the cuboid lies completely within this sector
     */
    public boolean contains(@Nonnull Location corner1, @Nonnull Location corner2) {
        return !(!corner1.getWorld().equals(this.getWorld()) || !corner2.getWorld().equals(this.getWorld()))
                && isLesser(getGreaterCorner(corner1, corner2), greater) && isGreater (getLesserCorner(corner1, corner2), lesser);
    }

    /**
     * Gets whether the sector overlaps the provided cuboid in any way.
     * Includes partial overlapping or fully contained.
     *
     * @param corner1 One corner of the cuboid
     * @param corner2 The diagonal opposite of corner1
     * @return True if the cuboid overlaps this sector
     */
    public boolean overlaps(@Nonnull Location corner1, @Nonnull Location corner2) {
        //Find the lesser/greater corners
        Sector testSector = new SectorBase(corner1, corner2, 25);

        return testSector.contains(getGreaterCorner().getLocation()) || testSector.contains(getLesserCorner().getLocation())
                || testSector.contains(getGreaterXCorner().getLocation()) || testSector.contains(getGreaterZCorner().getLocation())
                || contains(testSector.getGreaterCorner().getLocation()) || contains(testSector.getLesserCorner().getLocation())
                || contains(testSector.getGreaterXCorner().getLocation()) || contains(testSector.getGreaterZCorner().getLocation());
    }

    @Override
    public int compareTo(@Nonnull Sector s) {
        return id.compareTo(s.getID());
    }

    /*
     * Returns true if loc1 <= loc2 on both x & z points
     */
    private boolean isLesser(SectorLocation loc1, SectorLocation loc2) {
        return (loc1.getX() <= loc2.getX() && loc1.getZ() <= loc2.getZ());
    }

    /*
     * Returns true if loc1 >= loc2 on both x & z points
     */
    private boolean isGreater(SectorLocation loc1, SectorLocation loc2) {
        return (loc1.getX() >= loc2.getX() && loc1.getZ() >= loc2.getZ());
    }

    private int getLesserPoint(int x1, int x2) {
        return x1 < x2 ? x1 : x2;
    }

    private int getGreaterPoint(int x1, int x2) {
        return x1 > x2 ? x1 : x2;
    }

    private SectorLocation getLesserCorner(Location loc1, Location loc2) {
        UUID world = loc1.getWorld().getUID();
        int y = getLesserPoint(loc1.getBlockY(), loc2.getBlockY());

        int x = getLesserPoint(loc1.getBlockX(), loc2.getBlockX());
        int z = getLesserPoint(loc1.getBlockZ(), loc2.getBlockZ());

        return new SectorLocationBase(world, x, y, z);
    }

    private SectorLocation getGreaterCorner(Location loc1, Location loc2) {
        UUID world = loc1.getWorld().getUID();
        int y = getLesserPoint(loc1.getBlockY(), loc2.getBlockY());

        int x = getGreaterPoint(loc1.getBlockX(), loc2.getBlockX());
        int z = getGreaterPoint(loc1.getBlockZ(), loc2.getBlockZ());

        return new SectorLocationBase(world, x, y, z);
    }
}
