import { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/accounts.css";

function AccountsTable({ refresh }) {

    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadAccounts();
    }, [refresh]);

    async function loadAccounts() {

        try {

            setLoading(true);

            const response = await api.get("/accounts");

            setAccounts(response.data);

        } catch (error) {

            console.error(error);

        } finally {

            setLoading(false);

        }

    }

    return (

        <div className="card">

            <h2>Account Balances</h2>

            {

                loading ?

                    <p>Loading accounts...</p>

                    :

                    <table className="accounts-table">

                        <thead>

                            <tr>

                                <th>Account Holder</th>
                                <th>VPA</th>
                                <th>Balance</th>

                            </tr>

                        </thead>

                        <tbody>

                            {

                                accounts.map(account => (

                                    <tr key={account.vpa}>

                                        <td>

                                            {account.holderName}

                                        </td>

                                        <td>

                                            {account.vpa}

                                        </td>

                                        <td>

                                            ₹ {Number(account.balance).toFixed(2)}

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

export default AccountsTable;