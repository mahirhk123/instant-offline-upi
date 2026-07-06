package com.instantupi.offline.service;

import com.instantupi.offline.dto.MeshPacket; // DTO (not DB entity)

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class simulates a Bluetooth mesh network.
 *
 * Think of each VirtualDevice as a mobile phone.
 * Packets move from one phone to another like gossip.
 */
@Service
public class MeshSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(MeshSimulatorService.class);

    // Stores all devices (phones) in the network
    private final Map<String, VirtualDevice> devices = new ConcurrentHashMap<>();

    // Constructor → automatically runs when app starts
    public MeshSimulatorService() {
        seedDefaultDevices(); // create initial phones
    }

    /**
     * Create default devices:
     * - 4 offline phones (inside basement)
     * - 1 bridge phone (has internet)
     */
    private void seedDefaultDevices() {
        devices.put("phone-alice",   new VirtualDevice("phone-alice",   false));
        devices.put("phone-stranger1", new VirtualDevice("phone-stranger1", false));
        devices.put("phone-stranger2", new VirtualDevice("phone-stranger2", false));
        devices.put("phone-stranger3", new VirtualDevice("phone-stranger3", false));
        devices.put("phone-bridge",  new VirtualDevice("phone-bridge",  true));
    }

    // Get all devices
    public Collection<VirtualDevice> getDevices() {
        return devices.values();
    }

    // Get one device by ID
    public VirtualDevice getDevice(String id) {
        return devices.get(id);
    }

    /**
     * Inject packet into network (starting point)
     * Example: Alice sends payment → goes into her phone
     */
    public void inject(String senderDeviceId, MeshPacket packet) {

        VirtualDevice sender = devices.get(senderDeviceId);

        if (sender == null)
            throw new IllegalArgumentException("Unknown device: " + senderDeviceId);

        // Store packet inside sender device
        sender.hold(packet);

        log.info("Packet {} injected at {} (TTL={})",
                packet.getPacketId().substring(0, 8),
                senderDeviceId,
                packet.getTtl());
    }

    /**
     * One round of gossip:
     * Every device shares its packets with others
     */
    public GossipResult gossipOnce() {

        int transfers = 0;

        // Convert devices to list for iteration
        List<VirtualDevice> deviceList = new ArrayList<>(devices.values());

        // Snapshot = copy of current packets (important!)
        Map<String, List<MeshPacket>> snapshot = new HashMap<>();

        for (VirtualDevice d : deviceList) {
            snapshot.put(d.getDeviceId(), new ArrayList<>(d.getHeldPackets()));
        }

        // Start gossip
        for (VirtualDevice src : deviceList) {

            for (MeshPacket pkt : snapshot.get(src.getDeviceId())) {

                // Stop if TTL = 0 (no more forwarding)
                if (pkt.getTtl() <= 0) continue;

                for (VirtualDevice dst : deviceList) {

                    // Don't send to itself
                    if (dst == src) continue;

                    // Don't send duplicate packet
                    if (dst.holds(pkt.getPacketId())) continue;

                    // Create a COPY of packet
                    MeshPacket copy = new MeshPacket();
                    copy.setPacketId(pkt.getPacketId());
                    copy.setTtl(pkt.getTtl() - 1); // reduce TTL
                    copy.setCreatedAt(pkt.getCreatedAt());
                    copy.setCiphertext(pkt.getCiphertext());

                    // Store packet in destination device
                    dst.hold(copy);

                    transfers++;
                }
            }
        }

        log.info("Gossip round complete: {} packet transfers", transfers);

        return new GossipResult(transfers, snapshotMap());
    }

    /**
     * Returns number of packets each device holds
     */
    public Map<String, Integer> snapshotMap() {
        Map<String, Integer> m = new LinkedHashMap<>();

        for (VirtualDevice d : devices.values()) {
            m.put(d.getDeviceId(), d.packetCount());
        }

        return m;
    }

    /**
     * Collect packets from devices that have internet
     * (bridge nodes)
     */
    public List<BridgeUpload> collectBridgeUploads() {

        List<BridgeUpload> out = new ArrayList<>();

        for (VirtualDevice d : devices.values()) {

            // Only consider devices with internet
            if (!d.hasInternet()) continue;

            for (MeshPacket pkt : d.getHeldPackets()) {
                out.add(new BridgeUpload(d.getDeviceId(), pkt));
            }
        }

        return out;
    }

    // Clear all packets (reset simulation)
    public void resetMesh() {
        devices.values().forEach(VirtualDevice::clear);
    }

    // Result of gossip
    public record GossipResult(int transfers, Map<String, Integer> deviceCounts) {}

    // Packet ready to upload to backend
    public record BridgeUpload(String bridgeNodeId, MeshPacket packet) {}
}