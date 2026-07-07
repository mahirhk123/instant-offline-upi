import { useState } from "react";

import Navbar from "../components/Navbar";
import SummaryCards from "../components/SummaryCards";
import NetworkStats from "../components/NetworkStats";
import PaymentForm from "../components/PaymentForm";
import MeshDevices from "../components/MeshDevices";
import AccountsTable from "../components/AccountsTable";
import TransactionsTable from "../components/TransactionsTable";
import ActivityLog from "../components/ActivityLog";

import "../styles/dashboard.css";

function Dashboard() {

    const [meshRefresh, setMeshRefresh] = useState(0);
    const [accountRefresh, setAccountRefresh] = useState(0);
    const [transactionRefresh, setTransactionRefresh] = useState(0);

    const [logs, setLogs] = useState([
        "System Started",
        "Waiting for mesh events..."
    ]);

    function addLog(message) {

        const time = new Date().toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        });

        setLogs(prev => [
            `[${time}] ${message}`,
            ...prev
        ]);

    }

    function refreshMesh() {

        setMeshRefresh(prev => prev + 1);

    }

    function refreshEverything() {

        setMeshRefresh(prev => prev + 1);
        setAccountRefresh(prev => prev + 1);
        setTransactionRefresh(prev => prev + 1);

    }

    return (

        <div className="dashboard">

            <Navbar />

            <SummaryCards
                refresh={
                    meshRefresh +
                    accountRefresh +
                    transactionRefresh
                }
            />

            <NetworkStats
                refresh={meshRefresh}
            />

            <div className="top-section">

                <PaymentForm

                    onSuccess={() => {

                        refreshMesh();

                        addLog("Payment packet injected into mesh.");

                    }}

                />

                <MeshDevices

                    refresh={meshRefresh}

                    onGossip={() => {

                        refreshMesh();

                        addLog("Mesh gossip completed.");

                    }}

                    onFlush={() => {

                        refreshEverything();

                        addLog("Bridge uploaded packets to backend.");

                    }}

                    onReset={() => {

                        refreshEverything();

                        addLog("Mesh network reset.");

                    }}

                />

            </div>

            <div className="middle-section">

                <AccountsTable

                    refresh={accountRefresh}

                />

            </div>

            <div className="bottom-section">

                <TransactionsTable

                    refresh={transactionRefresh}

                />

                <ActivityLog

                    logs={logs}

                />

            </div>

        </div>

    );

}

export default Dashboard;