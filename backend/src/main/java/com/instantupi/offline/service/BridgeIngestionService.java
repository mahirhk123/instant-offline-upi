package com.instantupi.offline.service;

import com.instantupi.offline.crypto.HybridCryptoService;
import com.instantupi.offline.dto.MeshPacket;
import com.instantupi.offline.dto.PaymentInstruction;
import com.instantupi.offline.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles packets received from a bridge device.
 *
 * Flow:
 * 1. Generate packet hash
 * 2. Check duplicate packet
 * 3. Decrypt packet
 * 4. Validate packet freshness
 * 5. Perform transaction settlement
 */
@Service
public class BridgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(BridgeIngestionService.class);

    // Handles encryption, decryption and hashing
    @Autowired
    private HybridCryptoService crypto;

    // Prevents processing the same packet multiple times
    @Autowired
    private IdempotencyService idempotency;

    // Handles actual money transfer
    @Autowired
    private SettlementService settlement;

    // Maximum allowed packet age (configured in application.properties)
    @Value("${upi.mesh.packet-max-age-seconds:86400}")
    private long maxAgeSeconds;

    /**
     * Process one incoming mesh packet.
     */
    public IngestResult ingest(MeshPacket packet, String bridgeNodeId, int hopCount) {

        try {

            // Generate SHA-256 hash of encrypted packet
            String packetHash = crypto.hashCiphertext(packet.getCiphertext());

            // -------------------------------------------------
            // Duplicate Check
            // -------------------------------------------------
            if (!idempotency.claim(packetHash)) {

                log.info("DUPLICATE packet {} from bridge {} — dropped",
                        packetHash.substring(0, 12) + "...",
                        bridgeNodeId);

                return IngestResult.duplicate(packetHash);
            }

            // -------------------------------------------------
            // Decrypt Packet
            // -------------------------------------------------
            PaymentInstruction instruction;

            try {

                instruction = crypto.decrypt(packet.getCiphertext());

            } catch (Exception e) {

                log.warn("Decryption failed for packet {} : {}",
                        packetHash.substring(0, 12) + "...",
                        e.getMessage());

                return IngestResult.invalid(packetHash, "decryption_failed");
            }

            // -------------------------------------------------
            // Replay Protection (Freshness Check)
            // -------------------------------------------------

            long ageSeconds =
                    (Instant.now().toEpochMilli() - instruction.getSignedAt()) / 1000;

            // Reject packets that are too old
            if (ageSeconds > maxAgeSeconds) {

                log.warn("Packet {} too old ({}s), rejected",
                        packetHash.substring(0, 12) + "...",
                        ageSeconds);

                return IngestResult.invalid(packetHash, "stale_packet");
            }

            // Reject packets coming from the future
            // (Allows 5 minutes clock difference)
            if (ageSeconds < -300) {

                return IngestResult.invalid(packetHash, "future_dated");
            }

            // -------------------------------------------------
            // Settlement
            // -------------------------------------------------

            Transaction tx = settlement.settle(
                    instruction,
                    packetHash,
                    bridgeNodeId,
                    hopCount
            );

            return IngestResult.settled(packetHash, tx);

        } catch (Exception e) {

            log.error("Ingestion error : {}", e.getMessage(), e);

            return IngestResult.invalid(
                    "?",
                    "internal_error : " + e.getMessage()
            );
        }
    }

    /**
     * Result returned after processing a packet.
     */
    public record IngestResult(
            String outcome,
            String packetHash,
            String reason,
            Long transactionId
    ) {

        // Transaction completed successfully
        public static IngestResult settled(String hash, Transaction tx) {

            return new IngestResult(
                    "SETTLED",
                    hash,
                    null,
                    tx.getId()
            );
        }

        // Duplicate packet received
        public static IngestResult duplicate(String hash) {

            return new IngestResult(
                    "DUPLICATE_DROPPED",
                    hash,
                    null,
                    null
            );
        }

        // Packet rejected
        public static IngestResult invalid(String hash, String reason) {

            return new IngestResult(
                    "INVALID",
                    hash,
                    reason,
                    null
            );
        }
    }
}