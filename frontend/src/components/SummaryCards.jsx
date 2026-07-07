import { useEffect, useState } from "react";
import {
    FaUsers,
    FaExchangeAlt,
    FaBluetooth,
    FaWifi
} from "react-icons/fa";

import api from "../services/api";
import "../styles/summary.css";

function SummaryCards({ refresh }) {

    const [summary, setSummary] = useState({
        accounts: 0,
        transactions: 0,
        meshDevices: 0,
        bridgeOnline: false
    });

    useEffect(() => {

        loadSummary();

    }, [refresh]);

    async function loadSummary() {

        try {

            const [
                accountsResponse,
                transactionCountResponse,
                meshResponse
            ] = await Promise.all([
                api.get("/accounts"),
                api.get("/transactions/count"),
                api.get("/mesh/state")
            ]);

            const devices = meshResponse.data.devices;

            const bridge = devices.find(
                device => device.deviceId === "phone-bridge"
            );

            setSummary({
                accounts: accountsResponse.data.length,
                transactions: transactionCountResponse.data.count,
                meshDevices: devices.length,
                bridgeOnline: bridge ? bridge.hasInternet : false
            });

        } catch (error) {

            console.error("Failed to load dashboard summary:", error);

        }

    }

    return (

        <div className="summary-grid">

            <div className="summary-card">

                <FaUsers className="summary-icon" />

                <h3>Accounts</h3>

                <h2>{summary.accounts}</h2>

            </div>

            <div className="summary-card">

                <FaExchangeAlt className="summary-icon" />

                <h3>Transactions</h3>

                <h2>{summary.transactions}</h2>

            </div>

            <div className="summary-card">

                <FaBluetooth className="summary-icon" />

                <h3>Mesh Devices</h3>

                <h2>{summary.meshDevices}</h2>

            </div>

            <div className="summary-card">

                <FaWifi className="summary-icon" />

                <h3>Bridge</h3>

                <h2
                    style={{
                        color: summary.bridgeOnline ? "#16a34a" : "#dc2626"
                    }}
                >
                    {summary.bridgeOnline ? "Online" : "Offline"}
                </h2>

            </div>

        </div>

    );

}

export default SummaryCards;