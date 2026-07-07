import { useEffect, useState } from "react";
import { FaWallet, FaWifi } from "react-icons/fa";
import api from "../services/api";
import "../styles/navbar.css";

function Navbar() {

    const [time, setTime] = useState(new Date());

    const [connected, setConnected] = useState(false);

    useEffect(() => {

        const timer = setInterval(() => {

            setTime(new Date());

        }, 1000);

        return () => clearInterval(timer);

    }, []);

    useEffect(() => {

        checkBackend();

        const interval = setInterval(() => {

            checkBackend();

        }, 10000);

        return () => clearInterval(interval);

    }, []);

    async function checkBackend() {

        try {

            await api.get("/accounts");

            setConnected(true);

        }

        catch {

            setConnected(false);

        }

    }

    return (

        <nav className="navbar">

            <div className="navbar-left">

                <FaWallet className="logo-icon" />

                <div>

                    <h1>Instant Offline UPI Dashboard</h1>

                    <p>Offline Payment Simulation</p>

                </div>

            </div>

            <div className="navbar-right">

                <div className="status">

                    <span
                        className={
                            connected
                                ? "status-dot online"
                                : "status-dot offline"
                        }
                    />

                    <FaWifi />

                    <span>

                        {connected
                            ? "Backend Connected"
                            : "Backend Offline"}

                    </span>

                </div>

                <div className="clock">

                    <div>

                        {time.toLocaleTimeString([], {

                            hour: "2-digit",
                            minute: "2-digit",
                            second: "2-digit"

                        })}

                    </div>

                    <small>

                        {time.toLocaleDateString()}

                    </small>

                </div>

            </div>

        </nav>

    );

}

export default Navbar;