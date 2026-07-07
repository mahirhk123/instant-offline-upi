import "../styles/activity.css";

function ActivityLog({ logs = [] }) {

    return (

        <div className="card">

            <h2>Activity Log</h2>

            <div className="activity-box">

                {

                    logs.length === 0 ?

                        (

                            <div className="log-item">

                                Waiting for mesh events...

                            </div>

                        )

                        :

                        (

                            logs.map((log, index) => (

                                <div

                                    key={index}

                                    className="log-item"

                                >

                                    {log}

                                </div>

                            ))

                        )

                }

            </div>

        </div>

    );

}

export default ActivityLog;