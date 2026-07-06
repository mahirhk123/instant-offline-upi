package com.instantupi.offline.controller;

import com.instantupi.offline.crypto.ServerKeyHolder;
import com.instantupi.offline.dto.MeshPacket;
import com.instantupi.offline.entity.Account;
import com.instantupi.offline.entity.Transaction;
import com.instantupi.offline.repository.AccountRepository;
import com.instantupi.offline.repository.TransactionRepository;
import com.instantupi.offline.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired private ServerKeyHolder serverKey;   // provides public key
    @Autowired private DemoService demo;            // creates packets
    @Autowired private MeshSimulatorService mesh;   // simulates mesh network
    @Autowired private BridgeIngestionService bridge; // main processing logic
    @Autowired private AccountRepository accountRepo; // DB access (accounts)
    @Autowired private TransactionRepository txRepo;  // DB access (transactions)
    @Autowired private IdempotencyService idempotency; // duplicate protection

    // -------------------- server key

    // Returns server public key (used by sender to encrypt data)
    @GetMapping("/server-key")
    public Map<String, String> getServerPublicKey() {
        return Map.of(
                "publicKey", serverKey.getPublicKeyBase64(),
                "algorithm", "RSA-2048 / OAEP-SHA256",
                "hybridScheme", "RSA-OAEP encrypts AES-256-GCM key"
        );
    }

    // -------------------- demo send

    // Creates a payment packet and injects it into mesh
    @PostMapping("/demo/send")
    public ResponseEntity<?> demoSend(@RequestBody DemoSendRequest req) throws Exception {

        // create encrypted packet
        MeshPacket packet = demo.createPacket(
                req.senderVpa,
                req.receiverVpa,
                req.amount,
                req.pin,
                req.ttl == null ? 5 : req.ttl
        );

        // choose starting device
        String startDevice = req.startDevice == null ? "phone-alice" : req.startDevice;

        // inject into mesh
        mesh.inject(startDevice, packet);

        // return basic info
        return ResponseEntity.ok(Map.of(
                "packetId", packet.getPacketId(),
                "ciphertextPreview", packet.getCiphertext().substring(0, 64) + "...",
                "ttl", packet.getTtl(),
                "injectedAt", startDevice
        ));
    }

    // request body structure for demo send
    public static class DemoSendRequest {
        public String senderVpa;
        public String receiverVpa;
        public BigDecimal amount;
        public String pin;
        public Integer ttl;
        public String startDevice;
    }

    // -------------------- mesh state

    // Shows current state of all devices in mesh
    @GetMapping("/mesh/state")
    public Map<String, Object> meshState() {

        List<Map<String, Object>> deviceData = new ArrayList<>();

        for (VirtualDevice d : mesh.getDevices()) {
            deviceData.add(Map.of(
                    "deviceId", d.getDeviceId(),
                    "hasInternet", d.hasInternet(),
                    "packetCount", d.packetCount(),
                    "packetIds", d.getHeldPackets().stream()
                            .map(p -> p.getPacketId().substring(0, 8))
                            .toList()
            ));
        }

        return Map.of(
                "devices", deviceData,
                "idempotencyCacheSize", idempotency.size() // number of processed packets
        );
    }

    // -------------------- gossip

    // One round of packet spreading
    @PostMapping("/mesh/gossip")
    public Map<String, Object> meshGossip() {

        MeshSimulatorService.GossipResult r = mesh.gossipOnce();

        return Map.of(
                "transfers", r.transfers(),
                "deviceCounts", r.deviceCounts()
        );
    }

    // -------------------- flush

    // Bridge devices upload packets to backend
    @PostMapping("/mesh/flush")
    public Map<String, Object> meshFlush() {

        List<MeshSimulatorService.BridgeUpload> uploads = mesh.collectBridgeUploads();

        List<Map<String, Object>> results = new ArrayList<>();

        // process uploads in parallel (simulate real network)
        uploads.parallelStream().forEach(up -> {

            BridgeIngestionService.IngestResult r =
                    bridge.ingest(up.packet(), up.bridgeNodeId(), 5 - up.packet().getTtl());

            synchronized (results) {
                results.add(Map.of(
                        "bridgeNode", up.bridgeNodeId(),
                        "packetId", up.packet().getPacketId().substring(0, 8),
                        "outcome", r.outcome(),
                        "reason", r.reason() == null ? "" : r.reason(),
                        "transactionId", r.transactionId() == null ? -1 : r.transactionId()
                ));
            }
        });

        return Map.of(
                "uploadsAttempted", uploads.size(),
                "results", results
        );
    }

    // -------------------- reset

    // Clears mesh and idempotency cache
    @PostMapping("/mesh/reset")
    public Map<String, Object> meshReset() {
        mesh.resetMesh();
        idempotency.clear();
        return Map.of("status", "mesh and idempotency cache cleared");
    }

    // -------------------- bridge ingest

    // Main endpoint where real bridge sends packets
    @PostMapping("/bridge/ingest")
    public ResponseEntity<?> ingest(
            @RequestBody MeshPacket packet,
            @RequestHeader(value = "X-Bridge-Node-Id", defaultValue = "unknown") String bridgeNodeId,
            @RequestHeader(value = "X-Hop-Count", defaultValue = "0") int hopCount) {

        BridgeIngestionService.IngestResult r = bridge.ingest(packet, bridgeNodeId, hopCount);

        return ResponseEntity.ok(r);
    }

    // -------------------- accounts

    // Get all accounts
    @GetMapping("/accounts")
    public List<Account> listAccounts() {
        return accountRepo.findAll();
    }

    // Get latest transactions
    @GetMapping("/transactions")
    public List<Transaction> listTransactions() {
        return txRepo.findTop20ByOrderByIdDesc();
    }
}