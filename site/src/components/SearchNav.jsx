import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/index.css';

function SearchNav() {
    const handleLogOut = () => {
        const username = localStorage.getItem("username");
        localStorage.removeItem("username");
        console.log(username, " is logged out")
    };

    return (
        <header className="bg-customDark text-sm py-3 px-5">
            {/* Main Navigation */}
            <nav aria-label="main navigation" className="max-w-[85rem] w-full mx-auto flex flex-col md:flex-row items-center justify-between gap-y-4">

                {/* Left side links */}
                <ul className="flex flex-col md:flex-row md:space-x-8 items-center" aria-label="page sections">
                    <li>
                        <Link
                            to="/search"
                            aria-label="Go to Search Page"
                            className="block py-2 px-3 text-white rounded-sm hover:text-black text-2xl"
                        >
                            Search
                        </Link>
                    </li>
                    <li>
                        <Link
                            to="/favorites"
                            aria-label="Go to Favorites Page"
                            className="block py-2 px-3 text-white rounded-sm hover:text-black text-2xl"
                        >
                            Favorites
                        </Link>
                    </li>
                    <li>
                        <Link
                            to="/compare"
                            aria-label="Go to Compare Page"
                            className="block py-2 px-3 text-white rounded-sm hover:text-black text-2xl"
                        >
                            Compare
                        </Link>
                    </li>
                </ul>

                {/* Title between left and right */}
                <div className="text-white text-2xl mx-8" aria-label="Website Title">
                    Let's Get Lyrical
                </div>

                {/* Right side link */}
                <ul className="flex space-x-8" aria-label="User Actions">
                    <li>
                        <Link
                            to="/"
                            aria-label="Log Out"
                            className="block py-2 px-3 text-white rounded-sm hover:text-black text-2xl" onClick={handleLogOut}
                        >
                            Log Out
                        </Link>
                    </li>
                </ul>

            </nav>
        </header>
    );
}

export default SearchNav;
