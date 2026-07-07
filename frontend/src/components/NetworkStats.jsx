import { useEffect, useState } from "react";
import {
    FaBluetooth,
    FaWifi,
    FaBoxes,
    FaServer
} from "react-icons/fa";

import api from "../services/api";
import "../styles/networkStats.css";

function NetworkStats({ refresh }) {

    const [stats, setStats] = useState({
        totalDevices: 0,
        bridgeDevices: 0,
        totalPackets: 0,
        offlineDevices: 0
    });

    useEffect(() => {
        loadStats();
    }, [refresh]);

    async function loadStats() {

        try {

            const response = await api.get("/mesh/state");

            const devices = response.data.devices;

            const totalPackets =
                devices.reduce(
                    (sum, d) => sum + d.packetCount,
                    0
                );

            const bridgeDevices =
                devices.filter(d => d.hasInternet).length;

            const offlineDevices =
                devices.filter(d => !d.hasInternet).length;

            setStats({

                totalDevices: devices.length,

                bridgeDevices,

                totalPackets,

                offlineDevices

            });

        } catch (error) {

            console.error(error);

        }

    }

    return (

        <div className="network-card">

            <h2>Network Statistics</h2>

            <div className="network-grid">

                <div className="network-item">

                    <FaBluetooth />

                    <span>Total Devices</span>

                    <h3>{stats.totalDevices}</h3>

                </div>

                <div className="network-item">

                    <FaWifi />

                    <span>Bridge Devices</span>

                    <h3>{stats.bridgeDevices}</h3>

                </div>

                <div className="network-item">

                    <FaBoxes />

                    <span>Total Packets</span>

                    <h3>{stats.totalPackets}</h3>

                </div>

                <div className="network-item">

                    <FaServer />

                    <span>Offline Devices</span>

                    <h3>{stats.offlineDevices}</h3>

                </div>

            </div>

        </div>

    );

}

export default NetworkStats;