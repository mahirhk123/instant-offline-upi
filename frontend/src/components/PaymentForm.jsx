import { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/paymentForm.css";

function PaymentForm({ onSuccess }) {

    const [accounts, setAccounts] = useState([]);

    const [form, setForm] = useState({
        senderVpa: "",
        receiverVpa: "",
        amount: "",
        pin: "",
        ttl: 5,
        startDevice: "phone-alice"
    });

    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadAccounts();
    }, []);

    async function loadAccounts() {

        try {

            const response = await api.get("/accounts");

            setAccounts(response.data);

            if (response.data.length >= 2) {

                setForm(prev => ({
                    ...prev,
                    senderVpa: response.data[0].vpa,
                    receiverVpa: response.data[1].vpa
                }));

            }

        } catch (error) {

            console.error("Failed to load accounts:", error);

            setMessage("❌ Failed to load accounts.");

        }

    }

    function handleChange(event) {

        const { name, value } = event.target;

        setForm(prev => ({
            ...prev,
            [name]: value
        }));

    }

    async function handleSubmit(event) {

        event.preventDefault();

        if (form.senderVpa === form.receiverVpa) {

            setMessage("❌ Sender and Receiver cannot be the same.");

            return;

        }

        if (Number(form.amount) <= 0) {

            setMessage("❌ Amount must be greater than zero.");

            return;

        }

        setLoading(true);

        setMessage("");

        try {

            const response = await api.post("/demo/send", form);

            setMessage(
`✅ Packet Injected Successfully

Packet ID : ${response.data.packetId}

Injected At : ${response.data.injectedAt}

TTL : ${response.data.ttl}`
            );

            // Tell Dashboard to refresh Mesh Devices
            if (onSuccess) {
                onSuccess();
            }

            console.log(response.data);

            setForm(prev => ({
                ...prev,
                amount: "",
                pin: ""
            }));

        }
        catch (error) {

            console.error(error);

            if (error.response?.data?.message) {

                setMessage(`❌ ${error.response.data.message}`);

            } else {

                setMessage("❌ Failed to inject packet.");

            }

        }
        finally {

            setLoading(false);

        }

    }

    return (

        <div className="card">

            <h2>Send Payment</h2>

            <form onSubmit={handleSubmit}>

                <label>Sender</label>

                <select
                    name="senderVpa"
                    value={form.senderVpa}
                    onChange={handleChange}
                >

                    {accounts.map(account => (

                        <option
                            key={account.vpa}
                            value={account.vpa}
                        >
                            {account.vpa}
                        </option>

                    ))}

                </select>

                <label>Receiver</label>

                <select
                    name="receiverVpa"
                    value={form.receiverVpa}
                    onChange={handleChange}
                >

                    {accounts.map(account => (

                        <option
                            key={account.vpa}
                            value={account.vpa}
                        >
                            {account.vpa}
                        </option>

                    ))}

                </select>

                <label>Amount</label>

                <input
                    type="number"
                    name="amount"
                    min="1"
                    step="0.01"
                    value={form.amount}
                    onChange={handleChange}
                    placeholder="Enter amount"
                    required
                />

                <label>PIN</label>

                <input
                    type="password"
                    name="pin"
                    value={form.pin}
                    onChange={handleChange}
                    placeholder="Enter PIN"
                    required
                />

                <label>Starting Device</label>

                <select
                    name="startDevice"
                    value={form.startDevice}
                    onChange={handleChange}
                >

                    <option value="phone-alice">phone-alice</option>
                    <option value="phone-stranger1">phone-stranger1</option>
                    <option value="phone-stranger2">phone-stranger2</option>
                    <option value="phone-stranger3">phone-stranger3</option>
                    <option value="phone-bridge">phone-bridge</option>

                </select>

                <label>TTL</label>

                <input
                    type="number"
                    name="ttl"
                    min="1"
                    max="10"
                    value={form.ttl}
                    onChange={handleChange}
                    required
                />

                <button
                    type="submit"
                    disabled={loading}
                >

                    {loading ? "Injecting..." : "Inject Into Mesh"}

                </button>

            </form>

            {message && (

                <div className="message">

                    <pre>{message}</pre>

                </div>

            )}

        </div>

    );

}

export default PaymentForm;