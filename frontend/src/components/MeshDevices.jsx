import { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/mesh.css";

function MeshDevices({
    refresh,
    onGossip,
    onFlush,
    onReset
}) {

    const [devices, setDevices] = useState([]);
    const [cacheSize, setCacheSize] = useState(0);

    const [loadingGossip, setLoadingGossip] = useState(false);
    const [loadingFlush, setLoadingFlush] = useState(false);
    const [loadingReset, setLoadingReset] = useState(false);

    useEffect(() => {
        loadMeshState();
    }, [refresh]);

    async function loadMeshState() {

        try {

            const response = await api.get("/mesh/state");

            setDevices(response.data.devices);
            setCacheSize(response.data.idempotencyCacheSize);

        }
        catch (error) {

            console.error(error);

        }

    }

    async function runGossip() {

        try {

            setLoadingGossip(true);

            await api.post("/mesh/gossip");

            await loadMeshState();

            if (onGossip) {
                onGossip();
            }

        }
        catch (error) {

            console.error(error);

        }
        finally {

            setLoadingGossip(false);

        }

    }

    async function flushBridge() {

        try {

            setLoadingFlush(true);

            await api.post("/mesh/flush");

            await loadMeshState();

            if (onFlush) {
                onFlush();
            }

        }
        catch (error) {

            console.error(error);

        }
        finally {

            setLoadingFlush(false);

        }

    }

    async function resetMesh() {

        try {

            setLoadingReset(true);

            await api.post("/mesh/reset");

            await loadMeshState();

            if (onReset) {
                onReset();
            }

        }
        catch (error) {

            console.error(error);

        }
        finally {

            setLoadingReset(false);

        }

    }

    return (

        <div className="card">

            <h2>Mesh Devices</h2>

            <table className="mesh-table">

                <thead>

                    <tr>

                        <th>Device</th>
                        <th>Internet</th>
                        <th>Packets</th>

                    </tr>

                </thead>

                <tbody>

                    {

                        devices.map(device => (

                            <tr key={device.deviceId}>

                                <td>

                                    {device.deviceId}

                                </td>

                                <td>

                                    {

                                        device.hasInternet ?

                                            <span className="online">

                                                🟢 Online

                                            </span>

                                            :

                                            <span className="offline">

                                                🔴 Offline

                                            </span>

                                    }

                                </td>

                                <td>

                                    {device.packetCount}

                                </td>

                            </tr>

                        ))

                    }

                </tbody>

            </table>

            <p>

                <strong>

                    Idempotency Cache :

                </strong>

                {" "}

                {cacheSize}

            </p>

            <div className="mesh-buttons">

                <button

                    onClick={runGossip}

                    disabled={loadingGossip}

                >

                    {

                        loadingGossip ?

                            "Running..."

                            :

                            "Run Gossip"

                    }

                </button>

                <button

                    onClick={flushBridge}

                    disabled={loadingFlush}

                >

                    {

                        loadingFlush ?

                            "Flushing..."

                            :

                            "Flush Bridge"

                    }

                </button>

                <button

                    onClick={resetMesh}

                    disabled={loadingReset}

                >

                    {

                        loadingReset ?

                            "Resetting..."

                            :

                            "Reset Mesh"

                    }

                </button>

            </div>

        </div>

    );

}

export default MeshDevices;