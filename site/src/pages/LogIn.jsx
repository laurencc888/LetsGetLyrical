import React, { useState, useEffect } from 'react';
import '../styles/index.css';
import Nav from '../components/Nav';
import PopUp from "../components/PopUp";

function LogIn() {
    const [submissionStatus, setSubmissionStatus] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [isError, setIsError] = useState(false);
    const [attempts, setAttempts] = useState(0);
    const [isLocked, setIsLocked] = useState(false);
    const [lockoutTimer, setLockoutTimer] = useState(null);
    const [firstAttemptTime, setFirstAttemptTime] = useState(null);

    async function handleSubmit(event)
    {
        event.preventDefault();

        const userName = event.target.elements.username.value;
        const passWord = event.target.elements.password.value;

        setIsError(false);

        if (!userName || !passWord) {
            setErrorMessage("Please enter both username and password.");
            setShowPopup(true);
            setIsError(true);
            return;
        }


        const requestData = {
            username: userName,
            password: passWord,
        };

        try {
            // Send the POST request to the /login endpoint
            const response = await fetch('/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData),
            });

            // Check if the response is ok (status in the range 200-299)
            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg);
            }

            // Handle successful response
            const result = await response.text();
            console.log('Login successful:', result);
            await new Promise((resolve) => setTimeout(resolve, 300));
            window.alert("Login successful! Redirecting to search...");
            localStorage.setItem("username", userName);
            window.location.href = "/search";
            // You can add further actions here, like redirecting the user or updating the UI.
        } catch (error) {
            // Handle errors here
            console.error('Login failed:', error.message);

            const currentTime = Date.now();
            if (attempts === 0 || (firstAttemptTime && currentTime - firstAttemptTime > 60000)) {
                setFirstAttemptTime(currentTime);
                setAttempts(1);
                setErrorMessage("Login incorrect. 1 failed attempt. Try again.");
            } else {
                setAttempts((prevAttempts) => {
                    const newAttempts = prevAttempts + 1;
                    if (newAttempts >= 3 && currentTime - firstAttemptTime <= 60000) {
                        setIsLocked(true);
                        setErrorMessage("3 wrong tries in 1 minute. No login for 30 seconds.");
                        setLockoutTimer(setTimeout(() => {
                            setIsLocked(false);
                            setAttempts(0);
                            setFirstAttemptTime(null);
                        }, 30000));
                    } else {
                        setErrorMessage(`Login incorrect. ${newAttempts} failed attempts. Try again.`);
                    }
                    return newAttempts;
                });
            }

            setIsError(true);
            setShowPopup(true);
            setSubmissionStatus('failure');
        }

    }



    useEffect(() => {
        return () => {
            if (lockoutTimer) clearTimeout(lockoutTimer);
        };
    }, [lockoutTimer]);

    return (
        <div className="w-full min-h-screen bg-customBlue">
            <Nav/>
            <PopUp showPopup={showPopup} errorMessage={errorMessage} setShowPopup={setShowPopup} isError={isError}/>
            <div className="flex items-center justify-center min-h-screen px-4">
                <div className="container shadow-md rounded w-full min-h-10 max-w-md p-8 rounded-lg bg-customDark mt-8">
                    <h3 className="mb-4 text-4xl font-sans leading-none tracking-tight text-center text-white dark:text-white">
                        LOGIN
                    </h3>

                    <form onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Username
                            </label>
                            <input
                                tabIndex="0" aria-label="Input username" className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                id="username"
                                type="text"
                                placeholder="Username"
                                required
                            />
                        </div>

                        <div className="mb-4">
                            <label className="block text-gray-700 text-sm font-bold mb-2">
                                Password
                            </label>
                            <input
                                tabIndex="0" aria-label="Input password" className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                id="password"
                                type="password"
                                placeholder="Password"
                                required
                            />
                        </div>

                        <div className="mb-4">
                            <button
                                tabIndex="0" aria-label="Submit the login form" type="submit"
                                className="bg-buttonDark w-full hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                                disabled={isLocked}
                            >
                                Submit
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            <p className="mb-4 text-4xl font-sans leading-none tracking-tight text-center text-black">
                Team 9
            </p>
        </div>
    );
}

export default LogIn;