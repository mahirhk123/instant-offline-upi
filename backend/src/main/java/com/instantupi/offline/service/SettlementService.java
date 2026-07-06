package com.instantupi.offline.service;

import com.instantupi.offline.entity.Account;
import com.instantupi.offline.entity.Transaction;
import com.instantupi.offline.dto.PaymentInstruction;
import com.instantupi.offline.repository.AccountRepository;
import com.instantupi.offline.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * This service handles the actual money transfer (debit + credit).
 *
 * @Transactional ensures:
 * 👉 Either BOTH debit and credit happen
 * 👉 OR nothing happens (if error occurs)
 */
@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    // Repository to access account table
    @Autowired private AccountRepository accounts;

    // Repository to store transaction history
    @Autowired private TransactionRepository transactions;

    /**
     * Main method to process a payment
     */
    @Transactional
    public Transaction settle(PaymentInstruction instruction, String packetHash,
                              String bridgeNodeId, int hopCount) {

        // 🔹 Fetch sender account from DB
        Account sender = accounts.findById(instruction.getSenderVpa())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown sender VPA: " + instruction.getSenderVpa()));

        // 🔹 Fetch receiver account from DB
        Account receiver = accounts.findById(instruction.getReceiverVpa())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown receiver VPA: " + instruction.getReceiverVpa()));

        // 🔹 Get amount from instruction
        BigDecimal amount = instruction.getAmount();

        // 🔹 Validate amount (must be > 0)
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // 🔹 Check if sender has enough balance
        if (sender.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance: {} has ₹{}, tried to send ₹{}",
                    sender.getVpa(), sender.getBalance(), amount);

            // ❌ If not enough balance → reject transaction
            return recordRejected(instruction, packetHash, bridgeNodeId, hopCount);
        }

        // 🔹 Debit sender
        sender.setBalance(sender.getBalance().subtract(amount));

        // 🔹 Credit receiver
        receiver.setBalance(receiver.getBalance().add(amount));

        // 🔹 Save updated balances
        accounts.save(sender);
        accounts.save(receiver);

        // 🔹 Create transaction record
        Transaction tx = new Transaction();
        tx.setPacketHash(packetHash); // unique id for idempotency
        tx.setSenderVpa(instruction.getSenderVpa());
        tx.setReceiverVpa(instruction.getReceiverVpa());
        tx.setAmount(amount);
        tx.setSignedAt(Instant.ofEpochMilli(instruction.getSignedAt())); // original time
        tx.setSettledAt(Instant.now()); // current time
        tx.setBridgeNodeId(bridgeNodeId);
        tx.setHopCount(hopCount);
        tx.setStatus(Transaction.Status.SETTLED);
        tx.setFailureReason(null);

        // 🔹 Save transaction in DB
        transactions.save(tx);

        // 🔹 Log success
        log.info("SETTLED ₹{} from {} to {} (packetHash={}, bridge={}, hops={})",
                amount, sender.getVpa(), receiver.getVpa(),
                packetHash.substring(0, 12) + "...", bridgeNodeId, hopCount);

        return tx;
    }

    /**
     * Called when transaction fails (like insufficient balance)
     */
    private Transaction recordRejected(PaymentInstruction instruction, String packetHash,
                                       String bridgeNodeId, int hopCount) {

        Transaction tx = new Transaction();

        tx.setPacketHash(packetHash);
        tx.setSenderVpa(instruction.getSenderVpa());
        tx.setReceiverVpa(instruction.getReceiverVpa());
        tx.setAmount(instruction.getAmount());
        tx.setSignedAt(Instant.ofEpochMilli(instruction.getSignedAt()));
        tx.setSettledAt(Instant.now());
        tx.setBridgeNodeId(bridgeNodeId);
        tx.setHopCount(hopCount);

        // ❌ Mark as rejected
        tx.setStatus(Transaction.Status.REJECTED);
        tx.setFailureReason("INSUFFICIENT_BALANCE");

        // 🔹 Save rejected transaction
        return transactions.save(tx);
    }
}