import React, {useState, useRef, useEffect} from 'react';
import Nav from '../components/Nav';
import '../styles/index.css';
import PopUp from "../components/PopUp";

function Register() {
    const [showPopup, setShowPopup] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [isError, setIsError] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [showPasswordRequirements, setShowPasswordRequirements] = useState(false);
    const formRef = useRef(null);
    const [pageTimer, setPageTimer] = useState(null);
    const [firstTimeOnPage, setFirstTimeOnPage] = useState(false);

    //Redirect to Login page if no user logged in
    useEffect(() => {
        //Once window.location.href switches to this page (changes to /search), set timer for user activity
        // if (firstTimeOnPage === false) {
        // setFirstTimeOnPage(true);
        //Set timer for user activity
        setPageTimer(setTimeout(() => {
            handleLogOutAndRedirectToLogin();
            }, 60000));
        // }
    }, []);

    const handleLogOutAndRedirectToLogin = () => {
        //Log user out and redirect them to the login page
        const username = localStorage.getItem("username");
        localStorage.removeItem("username");
        window.location.href = "/";
    }

    //Reset timer if activity on page
    const resetTimerWithActivity = () => {
        //Reset inactivity timer since activity occured
        if (pageTimer){
            clearTimeout(pageTimer);

            //Set timer again for user activity
            setPageTimer(setTimeout(() => {
                handleLogOutAndRedirectToLogin();
            }, 60000));
        }
    }

    const handleCancel = () => {
        if (window.confirm("Are you sure you want to cancel?")) {
            window.location.href = "/login";
        }
    };

    async function handleSubmit(event) {
        event.preventDefault();
        const userName = event.target.elements.username.value;
        const passWord = event.target.elements.password.value;
        const confirmPassword = event.target.elements.confirmPassword.value;

        const hasUpperCase = /[A-Z]/.test(passWord);
        const hasLowerCase = /[a-z]/.test(passWord);
        const hasNumber = /\d/.test(passWord);

        setShowPopup(false);
        setIsError(false);
        setShowPasswordRequirements(false);
        setErrorMessage(null); // Reset error message before checks

        if (!hasUpperCase || !hasLowerCase || !hasNumber) {
            setErrorMessage("Password needs uppercase letter, lowercase letter, number.");
            setIsError(true);
            setShowPasswordRequirements(true);
            setShowPopup(true);
            return;
        }

        if (passWord !== confirmPassword) {
            setErrorMessage("Passwords do not match.");
            setIsError(true);
            setShowPopup(true);
            return;
        }

        const requestData = { username: userName, password: passWord };

        try {
            const response = await fetch('/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                const errorMsg = await response.text();
                throw new Error(errorMsg);
            }

            setErrorMessage("Registration successful! You can now log in.");
            setShowPopup(true);

            setTimeout(() => {
                window.alert("Registration successful! Redirecting to login page...");
                window.location.href = "/login";
            }, 500);
        } catch (error) {
            console.error('Registration failed:', error.message);
            setErrorMessage(error.message || "An unexpected error occurred.");
            setIsError(true);
            setShowPopup(true);
        }
    }

    return (
        <div className="w-full h-screen bg-customBlue relative" onClick={resetTimerWithActivity} onMouseMove={resetTimerWithActivity} onKeyDown={resetTimerWithActivity}>
            <Nav/>
            <PopUp showPopup={showPopup} errorMessage={errorMessage} setShowPopup={setShowPopup} isError={isError}/>
            <div className="flex items-center justify-center min-h-full px-4 sm:px-6 md:px-12">
                <div className="w-full max-w-sm sm:max-w-md md:max-w-lg lg:max-w-xl bg-customDark shadow-md rounded-xl p-6 sm:p-8 md:p-10 mt-8">
                    <h3 className="mb-4 text-2xl sm:text-3xl md:text-4xl font-sans leading-none tracking-tight text-center text-white">
                        SIGN UP
                    </h3>
                    <form ref={formRef} onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label className="block text-white text-sm font-bold mb-2">Username</label>
                            <input tabIndex="0" aria-label="Input username" id="username" type="text"
                                   placeholder={"Username"}
                                   required
                                   className="shadow border rounded w-full py-2 px-3 text-gray-700 focus:outline-none focus:shadow-outline"
                                   onChange={resetTimerWithActivity}/>
                        </div>
                        <div className="mb-4">
                            <label className="block text-white text-sm font-bold mb-2">Password</label>
                            <div className="relative">
                                <input tabIndex="0" aria-label="Input password" id="password"
                                       type={showPassword ? "text" : "password"} placeholder={"Password"} required
                                       className="py-2.5 sm:py-3 ps-4 pe-10 block w-full border-gray-200 rounded-lg" onChange={resetTimerWithActivity}/>
                                <button tabIndex="0" aria-label="Toggle password visibility" type="button"
                                        onClick={() => setShowPassword(!showPassword)} data-testid={"toggle-password"}
                                        className="absolute inset-y-0 end-0 flex items-center pe-3">
                                    👁
                                </button>
                            </div>
                        </div>
                        <div className="mb-4">
                            <label className="block text-white text-sm font-bold mb-2">Confirm Password</label>
                            <div className="relative">
                                <input tabIndex="0" aria-label="Input confirm password" id="confirmPassword"
                                       type={showConfirmPassword ? "text" : "password"} placeholder={"Confirm Password"}
                                       data-testid={"confirm-password-id"} required
                                       className="py-2.5 sm:py-3 ps-4 pe-10 block w-full border-gray-200 rounded-lg"
                                       onChange={resetTimerWithActivity}/>
                                <button tabIndex="0" aria-label="Toggle confirm password visibility" type="button"
                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                        data-testid={"toggle-confirm-password"}
                                        className="absolute inset-y-0 end-0 flex items-center pe-3">
                                    👁
                                </button>
                            </div>
                        </div>
                        <div className="mb-4">
                            <button tabIndex="0" aria-label="Submit sign up form" type="submit"
                                    className="bg-buttonDark w-full hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
                                Submit
                            </button>
                        </div>
                        <div className="mb-4">
                            <button tabIndex="0" aria-label="Cancel sign up form" type="button" onClick={handleCancel}
                                    className="bg-red-500 w-full hover:bg-red-700 text-white font-bold py-2 px-4 rounded">
                                Cancel
                            </button>
                        </div>
                        {showPasswordRequirements && (
                            <ul className="list-disc text-white">
                                <strong>Password Must Contain: </strong>
                                <li className="ml-4">1 Capital Letter</li>
                                <li className="ml-4">1 Lowercase Letter</li>
                                <li className="ml-4">1 Number</li>
                            </ul>
                        )}
                    </form>
                </div>
            </div>
            <p className="mb-4 text-4xl font-sans leading-none tracking-tight text-center text-black">
                Team 9
            </p>
        </div>
    );
}

export default Register;
