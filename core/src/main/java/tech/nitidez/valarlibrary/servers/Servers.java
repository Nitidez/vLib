package tech.nitidez.valarlibrary.servers;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.ME1312.SubServers.Client.Bukkit.SubAPI;
import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.plugin.config.ValarConfig;
import tech.nitidez.valarlibrary.servers.ServerHandshake.ServerInfo;

public class Servers {
    private static List<ServerGroup> GROUPS = new ArrayList<>();
    private static ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    public static class ServerGroup {
        private List<ValarServer> servers;
        private List<String> subservers;
        private String name;

        public ServerGroup(String name, List<ValarServer> servers, List<String> subservers) {
            this.name = name;
            this.servers = servers;
            this.subservers = subservers;
            GROUPS.add(this);
        }

        public String getName() {
            return this.name;
        }

        public List<ValarServer> getServers() {
            List<ValarServer> serverList = new ArrayList<>();
            serverList.addAll(this.servers);
            if (vLib.SUBSERVERS) {
                for (String sgroup : this.subservers) {
                    SubAPI api = SubAPI.getInstance();
                    api.getGroup(sgroup, group -> {
                        if (group != null) {
                            group.value().forEach(server -> {
                                InetSocketAddress address = server.getAddress();
                                serverList.add(new ValarServer(address.getHostName(), address.getPort(), server.getName()));
                            });
                        }
                    });
                }
            }
            return serverList;
        }

        public ValarServer getServer(String serverName) {
            return this.getServers().stream().filter(s -> s.getName().equals(serverName)).findFirst().orElse(null);
        }

        public ValarServer getServer(String serverIP, int serverPort) {
            return this.getServers().stream().filter(s -> s.getIP().equals(serverIP) && s.getPort() == serverPort).findFirst().orElse(null);
        }

        public int getOnlinePlayers() {
            return this.getServers().stream().filter(s -> s.isOnline()).mapToInt(ValarServer::getOnlinePlayers).sum();
        }
    }

    public static class ValarServer {
        private String ip;
        private int port;
        private String name;
        private ServerInfo handshake;

        public ValarServer(String ip, int port, String name) {
            this.ip = ip;
            this.port = port;
            this.name = name;
            updateHandshake();
        }

        public void updateHandshake() {
            this.handshake = ServerHandshake.getServerInfo(ip, port);
        }

        public String getIP() {return this.ip;}
        public int getPort() {return this.port;}
        public String getName() {return this.name;}
        public boolean isOnline() {return this.handshake.online;}
        public int getOnlinePlayers() {return this.handshake.onlinePlayers;}
        public int getMaxPlayers() {return this.handshake.maxPlayers;}
        public String getMOTD() {return this.handshake.motd;}
        public String getVersion() {return this.handshake.version;}
        public ServerInfo getHandshake() {return this.handshake;}
        public List<ServerGroup> getGroups() {
            return GROUPS.stream().filter(g -> g.servers.contains(this)).collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return "ValarServer{ip="+getIP()+", port="+getPort()+", name="+getName()
            +", online="+isOnline()+", onlineplayers="+getOnlinePlayers()+", maxplayers="+getMaxPlayers()
            +", motd="+getMOTD()+", version="+getVersion()+", groups=["+
            String.join(", ", getGroups().stream().map(g -> g.getName()).collect(Collectors.toList()))+
            "]}";
        }
    }

    public static List<ServerGroup> getServerGroups() {
        return GROUPS;
    }

    public static List<ValarServer> getServers() {
        Set<Object> seen = new HashSet<>();
        return GROUPS.stream().map(ServerGroup::getServers).flatMap(List::stream).filter(s -> seen.add(s.getIP()) && seen.add(s.getPort()) && seen.add(s.getName())).collect(Collectors.toList());
    }

    public static ServerGroup getServerGroup(String groupName) {
        return GROUPS.stream().filter(g -> g.getName().equals(groupName)).findFirst().orElse(null);
    }

    public static ValarServer getServer(String serverName) {
        return GROUPS.stream().map(g -> g.getServer(serverName)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static ValarServer getServer(String serverIP, int serverPort) {
        return GROUPS.stream().map(g -> g.getServer(serverIP, serverPort)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static void setupServers() {
        ValarConfig cfg = vLib.getInstance().getConfig("servers");
        for (String key : cfg.getSection("servers.servergroups").getKeys(false)) {
            List<String> keyList = cfg.getStringList("servers.servergroups."+key);
            List<ValarServer> groupServers = new ArrayList<>();
            List<String> groupSubservers = new ArrayList<>();
            for (String serverString : keyList) {
                if (serverString.split("; ")[0].equals("subservers")) {
                    groupSubservers.add(serverString.split("; ")[1]);
                } else {
                    String serverName = serverString.split("; ")[1];
                    String serverIP = serverString.split(":")[0];
                    int serverPort = Integer.parseInt(serverString.split("; ")[0].split(":")[1]);
                    ValarServer server = new ValarServer(serverIP, serverPort, serverName);
                    groupServers.add(server);
                }
            }
            new ServerGroup(key, groupServers, groupSubservers);
        }

        long updateTime = cfg.getInt("servers.update");
        SCHEDULER.scheduleAtFixedRate(() -> {
            getServers().forEach(s -> s.updateHandshake());
            if (vLib.SUBSERVERS) {
                List<String> sgroups = new ArrayList<>(GROUPS.stream().map(g -> g.subservers).flatMap(List::stream).collect(Collectors.toSet()));
                for (String sgroup : sgroups) {
                    SubAPI api = SubAPI.getInstance();
                    api.getGroup(sgroup, group -> {
                        if (group != null) {
                            group.value().forEach(server -> {
                                server.refresh();
                            });
                        }
                    });
                }
            }
        }, updateTime, updateTime, TimeUnit.SECONDS);
    }

    public static ScheduledExecutorService getScheduler() {
        return SCHEDULER;
    }
}