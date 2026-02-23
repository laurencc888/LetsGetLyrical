import { React } from 'react';
import { Link } from "react-router-dom";
import '../styles/index.css';

function Nav() {
    return (
        <header className="flex items-center justify-between bg-customDark text-sm py-3 px-5">
            <nav className="max-w-[85rem] w-full mx-auto flex items-center justify-between">
                <h1 className="font-serif drop-shadow-lg text-5xl mx-auto flex-1 text-white text-center">
                    Let's Get Lyrical
                </h1>
                <ul className="font-medium flex space-x-8 md:ml-auto border border-gray-100 rounded-lg
               bg-gray-50 md:bg-customDark dark:bg-gray-800 md:dark:bg-gray-900">
                    <li>
                        <Link to="/" aria-label="Login link" className="block py-2 px-3 text-white rounded-sm hover:text-black text-2xl">
                            Login
                        </Link>
                    </li>
                    <li>
                        <Link to="/register" aria-label="Sign up link" className="block py-2 px-3 text-white rounded-sm hover:text-black text-2xl">
                            Sign Up
                        </Link>
                    </li>
                </ul>
            </nav>
        </header>

    );
}

export default Nav;
