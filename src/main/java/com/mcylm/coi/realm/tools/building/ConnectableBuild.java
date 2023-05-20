package com.mcylm.coi.realm.tools.building;

import com.mcylm.coi.realm.Entry;
import com.mcylm.coi.realm.tools.data.BuildData;
import com.mcylm.coi.realm.utils.LocationUtils;
import com.mcylm.coi.realm.utils.LoggerUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public abstract class ConnectableBuild extends COIBuilding {

    @Getter
    protected Set<ConnectableBuild> alreadyConnected = new HashSet<>();

    @Override
    public void buildSuccess(Location location, Player player) {

        setLocation(location);
        LoggerUtils.debug("self loc " + location);

        for (COIBuilding building : LocationUtils.getNearbyBuildings(location, getMaxLength())) {
            LoggerUtils.debug("find " + building.getLocation());
            if (building instanceof ConnectableBuild connectableBuild) {

                if (!connectConditionsCheck(connectableBuild)) {
                    continue;
                }
                Location[] points = getNearestConnectPoints(connectableBuild);

                if (alreadyConnected.size() < getMaxConnectBuild() && Math.abs((points[0].getY() - points[1].getY())) < 5) {
                    LoggerUtils.debug("try connect");
                    if (alreadyConnected.contains(connectableBuild)) {
                        continue;
                    }
                    if (connectableBuild.getAlreadyConnected().size() >= connectableBuild.getMaxConnectBuild()) {
                        continue;
                    }

                    if (!connect(points[0], points[1])) return;
                    alreadyConnected.add(connectableBuild);
                    connectableBuild.getAlreadyConnected().add(this);

                    LoggerUtils.debug("connected" + points[0] + " to " + points[1]);
                }
            }
        }
    }

    public int getMaxLength() {
        return 20;
    };

    public boolean connect(Location start, Location end) {
        List<Location> locations = LocationUtils.line(start, end, getLineRate());

        if (!connectLineCheck(locations)) {
            return false;
        }
        for (Location location : locations) {
            buildPoint(location, end.clone().subtract(start).toVector());
        }
        return true;
    }

    public boolean connectLineCheck(List<Location> line) {
        for (Location point : line) {
            if (BuildData.getBuildingByBlock(point.getBlock()) != null) {
                return false;
            }
        }
        return true;
    }

        public boolean connectConditionsCheck(ConnectableBuild to) {

        Vector vectorAB = to.getLocation().clone().subtract(getLocation()).toVector();

        for (ConnectableBuild build : getAlreadyConnected()) {
            Vector vectorAC = build.getLocation().clone().subtract(getLocation()).toVector();
            if (vectorAC.angle(vectorAB) < Math.toRadians(15)) {
                return false;
            }
        }

            return to.getTeam() == getTeam();
        }

    public abstract void buildPoint(Location point, Vector line);

    public List<Location> getConnectPoints() {
        return List.of(getLocation());
    };

    public Location[] getNearestConnectPoints(ConnectableBuild build) {
        Location[] points = new Location[2];
        double min = 99999999;
        for (Location point1 : build.getConnectPoints()) {
            for (Location point2 : getConnectPoints()) {
                double distance = point1.distance(point2);
                if (distance < min) {
                    points[0] = point1;
                    points[1] = point2;
                    min = distance;
                }
            }
        }
        return points;
    }

    @Override
    public void destroy(boolean effect) {
        super.destroy(effect);
        getAlreadyConnected().forEach(connectableBuild -> connectableBuild.getAlreadyConnected().remove(this));
        getAlreadyConnected().clear();
    }

    public abstract int getMaxConnectBuild();

    public double getLineRate() {
        return 1;
    }
}
