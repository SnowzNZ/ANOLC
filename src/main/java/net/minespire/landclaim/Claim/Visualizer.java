package net.minespire.landclaim.Claim;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Visualizer {


    public static Map<String, Queue<BlockVector3>> playerParticleCoords = new HashMap<>(50);
    public static Map<String, Integer> seeNearbyBukkitTask = new HashMap<>();
    public static Map<String, ScheduledExecutorService> seeNearbyAsyncService = new HashMap<>();
    public static Map<String, AtomicInteger> timer = new HashMap<>();

    public static void seeNearbyRegions(final org.bukkit.entity.Player player) {
        if (playerParticleCoords.containsKey(player.getName())) playerParticleCoords.get(player.getName()).clear();
        final Location playerLoc = player.getLocation();
        final org.bukkit.World bukkitWorld = player.getWorld();
        final World world = BukkitAdapter.adapt(bukkitWorld);
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final int playerX;
        int playerY;
        final int playerZ;
        playerX = playerLoc.getBlockX();
        playerY = playerLoc.getBlockY();
        playerZ = playerLoc.getBlockZ();
        final int[] playerPoint = new int[]{ playerX, playerY, playerZ };
        final BlockVector3 point1 = BlockVector3.at(playerX - 50, playerY, playerZ - 50);
        final BlockVector3 point2 = BlockVector3.at(playerX + 50, playerY, playerZ + 50);
        final ProtectedCuboidRegion regionToCheck = new ProtectedCuboidRegion("_dummy342513", point1, point2);
        regionToCheck.getIntersectingRegions(rgManager.getRegions().values()).forEach(rg -> {
            //int[][] regionBoundaries = getRegionPerimeters(rg.getMaximumPoint(), rg.getMinimumPoint());
            final int[][] regionBoundaries = getRegionSlice(rg.getMaximumPoint(), rg.getMinimumPoint());
            final Queue<BlockVector3> particleCoords;
            if (!playerParticleCoords.containsKey(player.getName())) {
                particleCoords = new LinkedList<>();
                playerParticleCoords.put(player.getName(), particleCoords);
            } else particleCoords = playerParticleCoords.get(player.getName());

            final int countBlocksNear = 0;
            final int playerRadius = 50;
            final Queue<BlockVector3> regionPerimeterCoords = new LinkedList<>();
            for (final int[] coord : regionBoundaries) {
                if (getDistance(
                    coord,
                    playerPoint
                ) < playerRadius /*&& (playerPoint[1] > coord[1] - 2 && playerPoint[1] < coord[1] + 3)*/) {
                    if (!particleCoords.contains(BlockVector3.at(coord[0], playerPoint[1], coord[1])))
                        regionPerimeterCoords.add(BlockVector3.at(coord[0], playerPoint[1], coord[1]));
                    //countBlocksNear++;
                    //if(bukkitWorld.getBlockAt(coord[0],coord[1],coord[2]).getType().equals(Material.AIR))
                }
            }
            particleCoords.addAll(regionPerimeterCoords);
        });


    }

    public static double getDistance(final int[] point1, final int[] point2) {
        final double distance = Math.sqrt((squareInts(point1[0] - point2[0]) + squareInts(point1[1] - point2[2])));
        return distance;
    }

    public static int squareInts(final int toSquare) {
        return toSquare * toSquare;
    }

    public static int[][] getRegionSlice(final BlockVector3 maxPoint, final BlockVector3 minPoint) {
        final int x1;
        int x2;
        int z1;
        int z2;
        int length;
        int width;
        final int surfaceArea;
        x1 = maxPoint.x();
        z1 = maxPoint.z();
        x2 = minPoint.x();
        z2 = minPoint.z();
        length = x1 - x2;
        width = z1 - z2;
        surfaceArea = length * width;
        final int[][] coordinateArray = new int[surfaceArea][2];
        int arrayIndex = 0;
        for (int i = x2; i < x1; i++) {
            for (int j = z2; j < z1; j++) {
                coordinateArray[arrayIndex++] = new int[]{ i, j };
            }
        }
        return coordinateArray;
    }

    public static int[][] getRegionPerimeters(final BlockVector3 maxPoint, final BlockVector3 minPoint) {
        final int x1;
        int x2;
        int y1;
        int y2;
        int z1;
        int z2;
        int length;
        int width;
        int height;
        final int surfaceArea;
        x1 = maxPoint.x();
        y1 = maxPoint.y();
        z1 = maxPoint.z();
        x2 = minPoint.x();
        y2 = minPoint.y();
        z2 = minPoint.z();
        length = x1 - x2;
        width = z1 - z2;
        height = y1 - y2;
        surfaceArea = (2 * length * width) + (2 * length * height) + (2 * height * width);
        final int[][] coordinateArray = new int[surfaceArea][3];
        int arrayIndex = 0;
        int tempX = x2;
        int tempY = y2;
        int tempZ = z2;
        for (int half = 1; half < 3; half++) {
            for (int i = x2; i < x1; i++) {
                for (int j = y2; j < y1; j++) {
                    coordinateArray[arrayIndex++] = new int[]{ i, j, tempZ };
                }
            }
            for (int i = z2; i < z1; i++) {
                for (int j = y2; j < y1; j++) {
                    coordinateArray[arrayIndex++] = new int[]{ tempX, j, i };
                }
            }
            for (int i = x1; i < x2; i++) {
                for (int j = z2; j < z1; j++) {
                    coordinateArray[arrayIndex++] = new int[]{ i, tempY, j };
                }
            }
            tempX = x1;
            tempY = y1;
            tempZ = z1;
        }

        return coordinateArray;
    }

    public static Location getBestSpawnLocation(final org.bukkit.World world, final int x, final int y, final int z) {
        Block block;
        if (world.getBlockAt(x, y, z).getType().equals(Material.AIR)) {
            if (world.getBlockAt(x, y - 1, z).getType().equals(Material.AIR)) {
                if (world.getBlockAt(x, y - 2, z).getType().equals(Material.AIR)) {
                    return world.getBlockAt(x, y - 2, z).getLocation();
                } else return world.getBlockAt(x, y - 1, z).getLocation();
            } else return world.getBlockAt(x, y, z).getLocation();
        }

        if (world.getBlockAt(x, y + 1, z).getType().equals(Material.AIR)) return world.getBlockAt(
            x,
            y + 1,
            z
        ).getLocation();
        if (world.getBlockAt(x, y + 2, z).getType().equals(Material.AIR)) return world.getBlockAt(
            x,
            y + 1,
            z
        ).getLocation();
        if (world.getBlockAt(x, y + 3, z).getType().equals(Material.AIR)) return world.getBlockAt(
            x,
            y + 1,
            z
        ).getLocation();
        else return null;
    }
}
