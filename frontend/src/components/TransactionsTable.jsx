import { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/transaction.css";

function TransactionsTable({ refresh }) {

    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadTransactions();
    }, [refresh]);

    async function loadTransactions() {

        try {

            setLoading(true);

            const response = await api.get("/transactions");

            setTransactions(response.data);

        } catch (error) {

            console.error("Failed to load transactions:", error);

        } finally {

            setLoading(false);

        }

    }

    return (

        <div className="card">

            <h2>Transaction Ledger</h2>

            {

                loading ?

                    <p>Loading transactions...</p>

                    :

                    <table className="transaction-table">

                        <thead>

                            <tr>

                                <th>ID</th>
                                <th>Sender</th>
                                <th>Receiver</th>
                                <th>Amount</th>
                                <th>Status</th>

                            </tr>

                        </thead>

                        <tbody>

                            {

                                transactions.length === 0 ?

                                    <tr>

                                        <td colSpan="5">

                                            No Transactions Yet

                                        </td>

                                    </tr>

                                    :

                                    transactions.map(transaction => (

                                        <tr key={transaction.id}>

                                            <td>

                                                {transaction.id}

                                            </td>

                                            <td>

                                                {transaction.senderVpa}

                                            </td>

                                            <td>

                                                {transaction.receiverVpa}

                                            </td>

                                            <td>

                                                ₹ {Number(transaction.amount).toFixed(2)}

                                            </td>

                                            <td>

                                                <span
                                                    className={
                                                        transaction.status === "SUCCESS"
                                                            ? "status-success"
                                                            : "status-failed"
                                                    }
                                                >

                                                    {transaction.status}

                                                </span>

                                            </td>

                                        </tr>

                                    ))

                            }

                        </tbody>

                    </table>

            }

        </div>

    );

}

export default TransactionsTable;